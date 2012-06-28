/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.misc.tupleconnector.TupleConnectorFactory;
import de.hu_berlin.german.korpling.saltnpepper.misc.tupleconnector.TupleReader;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.exception.ConllConversionInputFileException;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.exception.ConllConversionMandatoryValueMissingException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SPOSAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SaltSemanticsFactory;

/**
 * This class maps input data from CoNLL format to Salt format
 * @author hildebax
 *
 */
public class Conll2SaltMapper{

	//property defaults
	private final boolean defaultUseSPOSAnnoation    = true;
	private final boolean defaultUseSLemmaAnnoation  = true;
	
	//some values and keys used in this class
	public final static String PROPERTYKEY_PROJECTIVITY    = "conll.considerProjectivity";
	public final static String PROPERTYKEY_PROJECTIVEMODE  = "conll.projectiveMode";
	public final static String PROPERTYKEY_SPOS            = "conll.SPOS";
	public final static String PROPERTYKEY_SLEMMA          = "conll.SLEMMA";

	public final static String PROPERTYKEY_FIELD6_POSTAG_  = "conll.field6.POSTAG.";  //the dot at the end is correct
	public final static String PROPERTYKEY_FIELD6_CPOSTAG_ = "conll.field6.CPOSTAG."; //the dot at the end is correct	
	public final static String PROPERTYKEY_FIELD6_DEFAULT  = "conll.field6.default";
	public final static String PROPERTYKEY_SPLIT_FEATURES  = "conll.splitFeatures";	
	
	private final String PROPERTYVAL_NONE            = "NONE";
	private final String PROPERTYVAL_POSTAG          = "POSTAG";
	private final String PROPERTYVAL_CPOSTAG         = "CPOSTAG";
	private final String PROPERTYVAL_LEMMA           = "LEMMA";	

	//
	private ConllDataField firstSPOSField = null;
	private ConllDataField secondSPOSField = null;

	private final ConllDataField DEFAULT_SPOS   = ConllDataField.POSTAG;
	//private final ConllDataField DEFAULT_SLEMMA = ConllDataField.LEMMA;	
	
	private final String PROJECTIVE     			  = "projective";
	private final String PRODEP         			  = "prodep";
	private final String DEP            			  = "dep";
	private final String DEPREL         			  = "deprel";
	private final String CAT            			  = "cat";
	private final String S              			  = "S";	
	public static final String TRUE       			  = "true";
	public static final String FALSE       		  	  = "false";	
	public static final String TYPE       			  = "TYPE";
	public static final String NAMESPACE			  = "NAMESPACE";
	public static final String DEFAULT_FEATURE		  = "morph";	
	
	//separator for feature annotation values
	private final String FEATURESEPARATOR = "\\|";
	
	//----------------------------------------------------------
	private Properties properties = new Properties();
	
	//property switches (initialized with default values)
	private boolean considerProjectivity = false;  
	private boolean projectiveModeIsType = true; //if false, projective mode is "namespace"

	/**
	 * Setter for the properties file of the conversion  
	 * @param propertyFile URI for the properties file
	 */
	public void setProperties(URI propertyFile) {
		if (propertyFile!=null) {
			try {
				logInfo(String.format("Trying to read property file '%s'",propertyFile.toFileString()));
				properties.load(new FileInputStream(propertyFile.toFileString()));
				considerProjectivity = !properties.getProperty(PROPERTYKEY_PROJECTIVITY, TRUE).equalsIgnoreCase(TRUE);
				projectiveModeIsType = !properties.getProperty(PROPERTYKEY_PROJECTIVEMODE, TYPE).equalsIgnoreCase(NAMESPACE);
			} catch (IOException e) {
				logWarning("property file for mapping CoNLL to Salt could not be read; default values are used");
			}
		}
		else {
			logWarning("URI of property file for mapping CoNLL to Salt is NULL; default values are used");
		}
	}

	/**
	 * Getter for the properties file
	 * @return properties the Properties
	 */
	public Properties getProperties() {
		return properties;
	}
	//----------------------------------------------------------

	//the Salt document graph; it will contain the data after conversion
	private SDocumentGraph sDocumentGraph = null; 

	/**
	 * Getter for the Salt document graph
	 * @return sDocumentGraph the Salt document graph
	 */
	public SDocumentGraph getSDocumentGraph() {
		return sDocumentGraph;
	}
	//----------------------------------------------------------
	
	//the URI for the CoNLL file
	private URI inFileURI = null;

	//setter for the URI
	private void setInFileURI(URI inFileURI) {
		this.inFileURI = inFileURI;
	}
	
	/**
	 * Getter for the URI for the CoNLL (input) file
	 * @return inFileURI the URI
	 */
	public URI getInFileURI() {
		return inFileURI;
	}
	//----------------------------------------------------------

	//the Salt document 
	private SDocument sDocument = null;
	
	//Setter for the Salt document
	private void setSDocument(SDocument sDocument) {
		this.sDocument = sDocument;
	}
	
	/**
	 * Getter for the Salt document
	 * @return sDocument the Salt document
	 */
	public SDocument getSDocument() {
		return this.sDocument;
	}
	//----------------------------------------------------------
	
	//the name of the Salt document graph
	private String sDocumentGraphName = null;
	
	/**
	 * Setter for the name of the Salt document graph
	 * @param sDocumentGraphName the name
	 */
	public void setSDocumentGraphName(String sDocumentGraphName) {
		this.sDocumentGraphName = sDocumentGraphName;
	}
	
	/**
	 * Getter for the name of the Salt document graph
	 * @return sDocumentGraphName the name
	 */
	public String getSDocumentGraphName() {
		return sDocumentGraphName;
	}
	//----------------------------------------------------------
	
	// the log service
	private LogService logService = null;
	
	/**
	 * Getter for the log service
	 * @return logService the log service
	 */
	public LogService getLogService() {
		return logService;
	}

	/**
	 * Setter for the log service
	 * @param logService the log service
	 */
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	//used by logError, logWarning, logInfo and logDebug
	private void log(int logLevel, String logText) {
		if (this.getLogService()!=null) {
			String fileInfo = "";
			if (this.getInFileURI()!=null) {
				fileInfo = "<" + this.getInFileURI().toFileString() + "> "; 
			}
			this.getLogService().log(logLevel, fileInfo + logText);
		}
	}
	
	//logging methods
	private void logError  (String logText) { this.log(LogService.LOG_ERROR,   logText); }
	private void logWarning(String logText) { this.log(LogService.LOG_WARNING, logText); }
	private void logInfo   (String logText) { this.log(LogService.LOG_INFO,    logText); }
	@SuppressWarnings("unused")
	private void logDebug  (String logText) { this.log(LogService.LOG_DEBUG,   logText); }
	//----------------------------------------------------------
	
	
	
	boolean splitFeatures;
	//retrieves whether to split pipe separated feature values or not
	private boolean getSplitFeatures() {
		String propVal = properties.getProperty(PROPERTYKEY_SPLIT_FEATURES, FALSE);
		return (propVal.equalsIgnoreCase(TRUE));
	}
	
	boolean useSLemmaAnnotation;
	//retrieves whether or not to use SLemmaAnnoations  
	private boolean getUseSLemmaAnnotation() {
		String propVal = properties.getProperty(PROPERTYKEY_SLEMMA, PROPERTYVAL_LEMMA); 
		if (propVal.equals(PROPERTYVAL_LEMMA))	return true;
		if (propVal.equals(PROPERTYVAL_NONE))	return false;
		logWarning(String.format("Invalid value '%s' for property '%s'. Default value '%s' is used.",propVal,PROPERTYKEY_SLEMMA,PROPERTYVAL_LEMMA));
		return defaultUseSLemmaAnnoation;
	}

	
	boolean useSPOSAnnotation;	
	//retrieves whether or not to use SPOSAnnoations and sets the values for 'firstPOSField' and 'secondPOSField'
	private boolean getUseSPOSAnnotation() {
		this.firstSPOSField=null;		
		this.secondSPOSField=null;

		if (!properties.containsKey(PROPERTYKEY_SPOS)) {
			if (defaultUseSPOSAnnoation) {
				this.firstSPOSField=DEFAULT_SPOS;
			}
			return defaultUseSPOSAnnoation;
		}
		String propVal = properties.getProperty(PROPERTYKEY_SPOS);
		String[] propVals = propVal.split(",");
		
		if (propVals.length>2) {
			logWarning(String.format("Found '%s' for property '%s'. Only two values are regarded, the rest will be ignored.",propVal,PROPERTYKEY_SPOS));
		}
		
		if ((propVals.length>1) && (propVals[0].equals(PROPERTYVAL_NONE))) {
			logWarning(String.format("Found '%s' for property '%s'. With this setting, no SPOSAnnotation will ever be created.",propVal,PROPERTYKEY_SPOS));
		}
		
		String val = propVals[0].trim();
		if (val.equals(PROPERTYVAL_NONE)) {
			return false;
		}
		else if ((val.equals(PROPERTYVAL_POSTAG))||(val.equals(PROPERTYVAL_CPOSTAG))) {
			if (val.equals(PROPERTYVAL_POSTAG)) {
				this.firstSPOSField = ConllDataField.POSTAG;
			}
			else if (val.equals(PROPERTYVAL_CPOSTAG)) {
				this.firstSPOSField = ConllDataField.CPOSTAG;	
			}
		}
		else {
			if (propVals.length==1) {
				logWarning(String.format("Invalid value '%s' for property '%s'. Using default value.",val,PROPERTYKEY_SPOS));
				this.firstSPOSField=DEFAULT_SPOS;
			}
			else {
				logWarning(String.format("Invalid value '%s' for property '%s'. Using alternative value.",val,PROPERTYKEY_SPOS));	
			}
		}
		
		if (propVals.length>=2) {
			val = propVals[1].trim();
			if (val.equals(PROPERTYVAL_NONE)) {
			}
			else if ((val.equals(PROPERTYVAL_POSTAG))||(val.equals(PROPERTYVAL_CPOSTAG))) {
				ConllDataField field = null; 
				if (val.equals(PROPERTYVAL_POSTAG)) {
					field = ConllDataField.POSTAG;	
				}
				else if (val.equals(PROPERTYVAL_CPOSTAG)) {
					field = ConllDataField.CPOSTAG;
				}

				if (this.firstSPOSField==null) {
					this.firstSPOSField=field;
				}
				else {
					this.secondSPOSField=field;
				}
			}
			else {
				if (this.firstSPOSField==null) {
					logWarning(String.format("Invalid alternative value '%s' for property '%s'. Using default value.",val,PROPERTYKEY_SPOS));	
					this.firstSPOSField=DEFAULT_SPOS;
				} 
				else {
					logWarning(String.format("Invalid alternative value '%s' for property '%s'.",val,PROPERTYKEY_SPOS));
				}
			}
		}
		
		
		return (this.firstSPOSField!=null); 
	}
	
	private void createPOSandCPOSAnnotation(ArrayList<String> fieldValues, SToken sToken) {
		{
			if (!this.useSPOSAnnotation) {			
				ConllDataField[] posFields = {ConllDataField.POSTAG,ConllDataField.CPOSTAG};
				for (int index=0; index<posFields.length; index++) {
					ConllDataField field      = posFields[index];
					String         fieldValue = fieldValues.get(field.getFieldNum()-1);
					if (fieldValue!=null) {
						SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setSName(properties.getProperty(field.getPropertyKey_Name(), field.name())); //use user specified name for field, or default: the field�s ConLL name
						sAnnotation.setSValue(fieldValue);
						sToken.addSAnnotation(sAnnotation);
					}
				}
			}
			else {
				
//				if (this.firstSPOSField!=null)
//					System.out.println(this.firstSPOSField.toString());
				
//				if (this.secondSPOSField!=null)
//					System.out.println(this.secondSPOSField.toString());
				
				
				int SPOSAnnotationIndex = -1;
				ConllDataField[] bothFields = {this.firstSPOSField,this.secondSPOSField};
				ConllDataField field = null;				
				for (int index=0; index<bothFields.length; index++) {
					if (SPOSAnnotationIndex==-1) {
						field = bothFields[index]; 
						if (field!=null) {
							String fieldVal = fieldValues.get(field.getFieldNum()-1);
							if (fieldVal!=null) {
								SPOSAnnotation anno = SaltSemanticsFactory.eINSTANCE.createSPOSAnnotation();
								anno.setSValue(fieldVal);
								sToken.addSAnnotation(anno);
								SPOSAnnotationIndex=index;
							}
						}
					}
				}

				for (int index=0; index<bothFields.length; index++) {
					if (SPOSAnnotationIndex!=index) {
						field = bothFields[index]; 
						if (field!=null) {
							String fieldVal = fieldValues.get(field.getFieldNum()-1);
							if (fieldVal!=null) {
								SAnnotation anno = SaltFactory.eINSTANCE.createSAnnotation();
								anno.setSName(properties.getProperty(field.getPropertyKey_Name(), field.name())); //use user specified name for field, or default: the field�s ConLL name
								anno.setSValue(fieldVal);
								sToken.addSAnnotation(anno);
								SPOSAnnotationIndex=index;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Maps the content of an input file (given as URI) in CoNLL format to a Salt document
	 * @param CoNLLFileURI the file URI containing the input data
	 * @param sDocument the Salt document; destination of the conversion
	 */
	public void map(URI inFileURI, SDocument sDocument) {
		
		setInFileURI(inFileURI);
		setSDocument(sDocument);
		
		TupleReader tupleReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
		// try reading the input file 
		try   {
			tupleReader.setFile(new File(this.getInFileURI().toFileString()));
			tupleReader.readFile();
		}
		catch (IOException e) {
			String errorMessage = "input file could not be read. Abort conversion of file "+inFileURI+".";
			logError(errorMessage);
			throw new ConllConversionInputFileException(errorMessage); 
		}

		sDocumentGraph = SaltFactory.eINSTANCE.createSDocumentGraph();
		sDocumentGraph.setSName(getSDocumentGraphName());
		getSDocument().setSDocumentGraph(sDocumentGraph);
		STextualDS sTextualDS = SaltFactory.eINSTANCE.createSTextualDS();
		sTextualDS.setSDocumentGraph(sDocumentGraph);
 
		ArrayList<SToken> tokenList = new ArrayList<SToken>();
		HashMap<SPointingRelation,Integer> pointingRelationMap = new HashMap<SPointingRelation,Integer>();
		ArrayList<String> fieldValues = new ArrayList<String>();
		
		Collection<String> tuple = null;
		int numOfTuples = tupleReader.getNumOfTuples();
		int lastTupleIndex = numOfTuples-1;
		int tupleSize = 0;
		int numOfColumnsExpected = ConllDataField.values().length;
		int fieldNum = 1;

		this.useSLemmaAnnotation = getUseSLemmaAnnotation();
		this.useSPOSAnnotation   = getUseSPOSAnnotation();
		this.splitFeatures       = getSplitFeatures();
		
		//this list is used to collect lines numbers where number of categories does not match expected number of categories 
		ArrayList<Integer> nonMatchingCategoryNumberLines = new ArrayList<Integer>();
		
		// using a StringBuilder for the iteratively updated raw text 
		int stringBuilderCharBufferSize = tupleReader.characterSize(ConllDataField.FORM.getFieldNum()-1) + numOfTuples;
		StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);
		
		// iteration over all data rows (the complete input-file)
		for (int rowIndex=0; rowIndex<numOfTuples; rowIndex++) {
			try {
				tuple = tupleReader.getTuple();
			} 
			catch (IOException e) {
				String errorMessage = String.format("line %d of input file could not be read. Abort conversion of file "+inFileURI+".",rowIndex+1);
				logError(errorMessage);
				throw new ConllConversionInputFileException(errorMessage);
			}
			
			tupleSize = tuple.size();
			fieldValues.clear();

			if (!((tupleSize==1)||(tupleSize==numOfColumnsExpected))) {
				String errorMessage = String.format("invalid format in line %d of input file. lines must be empty or contain %d columns of data. Abort conversion of file "+inFileURI+".",rowIndex+1,numOfColumnsExpected);
				logError(errorMessage);
				throw new ConllConversionInputFileException(errorMessage);
			}
			
			if (tupleSize>1) { //if true, this is a data row, else it is a sentence separating line

				// read all field values
				fieldNum = 1;
				for (Iterator<String> iter=tuple.iterator(); iter.hasNext(); fieldNum++) {
					ConllDataField field = ConllDataField.getFieldByNum(fieldNum);
					String fieldValue = iter.next();
					if (fieldValue.equals(field.getDummyValue())) {
						fieldValue = null;
						if (field.isMandatory()) {
							String errorMessage = String.format("mandatory value for %s missing in line %d of input file '"+inFileURI+"'!",field.toString(),rowIndex+1);
							logError(errorMessage);
							throw new ConllConversionMandatoryValueMissingException(errorMessage);
						}
					}
					fieldValues.add(fieldValue);
				} // for (Iterator<String> iter=tuple.iterator(); iter.hasNext(); fieldNum++)
				
				// create token and add to local token list
				SToken sToken = SaltFactory.eINSTANCE.createSToken();
				sToken.setSDocumentGraph(sDocumentGraph);
				tokenList.add(sToken);
				
				// update primary text (sTextualDS.sText will be set after completely reading the input file)
				int tokenTextStartOffset = primaryText.length();
				primaryText.append(fieldValues.get(ConllDataField.FORM.getFieldNum()-1)).append(" "); //update primary text data, tokens separated by space
				int tokenTextEndOffset = primaryText.length()-1;

				// create textual relation
				STextualRelation sTextualRelation = SaltFactory.eINSTANCE.createSTextualRelation();
				sTextualRelation.setSSource(sToken);
				sTextualRelation.setSTarget(sTextualDS);
				sTextualRelation.setSStart(tokenTextStartOffset);
				sTextualRelation.setSEnd(tokenTextEndOffset);
				sTextualRelation.setSDocumentGraph(sDocumentGraph);

				//Lemma
				{
					ConllDataField field = ConllDataField.LEMMA;
					String fieldValue = fieldValues.get(field.getFieldNum()-1);
					if (fieldValue!=null) {
						SAnnotation sAnnotation = null;
						if (useSLemmaAnnotation) {
							sAnnotation = SaltSemanticsFactory.eINSTANCE.createSLemmaAnnotation();
						}
						else {
							sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
							sAnnotation.setSName(properties.getProperty(field.getPropertyKey_Name(), field.name())); //use user specified name for field, or default: the field�s ConLL name								
						}
						
						sAnnotation.setSValue(fieldValue);
						sToken.addSAnnotation(sAnnotation);					
					}
				}
					
				//POS and CPOS
				{
					createPOSandCPOSAnnotation(fieldValues, sToken);
				}
				///POS and CPOS
					
				// create annotation for span
				SAnnotation sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
				sAnnotation.setSName(CAT);
				sAnnotation.setSValue(S);
				
				// create span and add span annotation
				SSpan sSpan = SaltFactory.eINSTANCE.createSSpan();
				sSpan.setGraph(sDocumentGraph);
				sSpan.addSAnnotation(sAnnotation);

				// create spanning relation, set span as source and token as target
				SSpanningRelation sSpanningRelation = SaltFactory.eINSTANCE.createSSpanningRelation();
				sSpanningRelation.setGraph(sDocumentGraph);
				sSpanningRelation.setSource(sSpan);
				sSpanningRelation.setTarget(sToken);

				// features
				String featureValue = fieldValues.get(ConllDataField.FEATS.getFieldNum()-1);
				
				if ((featureValue!=null)&&(featureValue.length()>0)) {// (featureString!=null)
					// check whether rule for feature category is defined. POSTAG (fine grained) gets priority over
					// CPOSTAG (coarse grained). if neither one is defined, use default
					String ruleKey = PROPERTYKEY_FIELD6_POSTAG_ + fieldValues.get(ConllDataField.POSTAG.getFieldNum()-1);
					if (!properties.containsKey(ruleKey)) {
						ruleKey = PROPERTYKEY_FIELD6_CPOSTAG_ + fieldValues.get(ConllDataField.CPOSTAG.getFieldNum()-1);
						if (!properties.containsKey(ruleKey)) {
							ruleKey = PROPERTYKEY_FIELD6_DEFAULT;
						}
					}
					String featureKey = properties.getProperty(ruleKey, DEFAULT_FEATURE);

					boolean doSplit = this.splitFeatures;
					String[] featureKeys=null;
					if (doSplit) {
						featureKeys = featureKey.split(FEATURESEPARATOR);
						if (ruleKey==PROPERTYKEY_FIELD6_DEFAULT) {
							doSplit = (featureKeys.length>1);
						}
					}
					if (doSplit) {
						String[] featureValues = featureValue.split(FEATURESEPARATOR);
						for (int idx=0; idx<Math.min(featureKeys.length,featureValues.length); idx++) {
							sToken.createSAnnotation(null, featureKeys[idx], featureValues[idx]);
						}
						if (featureKeys.length!=featureValues.length)	{
							nonMatchingCategoryNumberLines.add(rowIndex+1);							
						}
					} else {
						//no splitting
						sToken.createSAnnotation(null, featureKey, featureValue);	
					}
				} // (featureString!=null)

				// get ID of current token
				String tokenIDStr = fieldValues.get(ConllDataField.ID.getFieldNum()-1);
				Integer tokenID = null;
				try {
					tokenID = Integer.parseInt(tokenIDStr);
				}
				catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid integer value '%s' for ID in line %d of input file. Abort conversion of file "+inFileURI+".",tokenIDStr,rowIndex+1); 
					logError(errorMessage);
					throw new ConllConversionInputFileException();
				}
				
				// get ID of current token�s head token
				String headIDStr = fieldValues.get(ConllDataField.HEAD.getFieldNum()-1);
				Integer headID = null;
				try {
					headID = Integer.parseInt(headIDStr);
				}
				catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid integer value '%s' for HEAD in line %d of input file '"+inFileURI+"'. Abort conversion of file "+inFileURI+".",headIDStr,rowIndex+1); 
					logError(errorMessage);
					throw new ConllConversionInputFileException(errorMessage);
				}
				
				// create pointing relation, pointing from head to dependent
				if (headID>0) {
					// create annotation for pointing relation
					sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
					sAnnotation.setSName(DEPREL);
					
					String annoValue = fieldValues.get(ConllDataField.DEPREL.getFieldNum()-1);
					sAnnotation.setSValue(annoValue);
					
					SPointingRelation sPointingRelation = SaltFactory.eINSTANCE.createSPointingRelation();
					//sAnnotation.setSAnnotatableElement(sPointingRelation);
					sPointingRelation.setSDocumentGraph(sDocumentGraph);
					sPointingRelation.addSType(DEP);
					sPointingRelation.setTarget(sToken);
					sPointingRelation.addSAnnotation(sAnnotation);
					
					if (headID<=tokenID) {
						sPointingRelation.setSource(tokenList.get(headID-1));
					}
					else {
						pointingRelationMap.put(sPointingRelation,headID);						
					}
				}
				
				if (considerProjectivity)
				{
					// get ID of current token�s projective head token
					String proheadIDStr = fieldValues.get(ConllDataField.PHEAD.getFieldNum()-1);
					Integer proheadID = null;
					try {
						proheadID = Integer.parseInt(proheadIDStr);
					}
					catch (NumberFormatException e) {
						String errorMessage = String.format("invalid integer value '%s' for PHEAD in line %d of input file. Abort conversion of file "+inFileURI+".",proheadIDStr,rowIndex+1); 
						logError(errorMessage);
						throw new ConllConversionInputFileException();
					}
					
					// create pointing relation, pointing from phead to dependent
					if (proheadID>0) {
						// create annotation for pointing relation
						sAnnotation = SaltFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setSName(DEPREL);
						sAnnotation.setSValue(fieldValues.get(ConllDataField.PDEPREL.getFieldNum()-1));

						SPointingRelation sPointingRelation = SaltFactory.eINSTANCE.createSPointingRelation();
						sPointingRelation.setSDocumentGraph(sDocumentGraph);
						sPointingRelation.addSAnnotation(sAnnotation);
						sPointingRelation.setTarget(sToken);
						
						if (projectiveModeIsType) {
							sPointingRelation.addSType(PRODEP);
						}
						else {
							sAnnotation.setNamespace(PROJECTIVE);
							sPointingRelation.addSType(DEP);
						}
						
						if (proheadID<=tokenID) {
							sPointingRelation.setSource(tokenList.get(proheadID-1));
						}
						else {
							pointingRelationMap.put(sPointingRelation,proheadID);						
						}
					}
				}
			
			} // if (tupleSize>1)
				
			if ((tupleSize==1)||(rowIndex==lastTupleIndex)) { // if true, this is a sentence separating row or the last row in the input file 
				// pointingRelationMap has pointing relations as keys and corresponding source node IDs as values
				// set the actual node as source for each pointing relation 
				for (Entry<SPointingRelation, Integer> entry : pointingRelationMap.entrySet()) {
					entry.getKey().setSource(tokenList.get(entry.getValue()-1)); //index=ID-1
				}
				tokenList.clear();
				pointingRelationMap.clear();
			}

		} // for (int rowIndex=0; rowIndex<numOfTuples; rowIndex++)

		// ### file is completely read now
		
		// delete last char of primary text (a space character) and set it as text for TextualDS
		primaryText.deleteCharAt(primaryText.length()-1);
		sTextualDS.setSText(primaryText.toString());
		
		if (nonMatchingCategoryNumberLines.size()>0) {
			logWarning("Number of feature values doesn't match number of categories in lines: " + nonMatchingCategoryNumberLines.toString());			
		}
		
	} // map

} // ConllDep2SaltMapper



