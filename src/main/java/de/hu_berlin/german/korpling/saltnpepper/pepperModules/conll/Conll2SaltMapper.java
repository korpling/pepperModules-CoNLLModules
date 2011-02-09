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
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusDocumentRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;

public class Conll2SaltMapper{

	//----------------------------------------------------------
	private Properties properties = new Properties();
	
	//property switches (initialized with default values)
	private boolean considerProjectivity = false;  
	private boolean projectiveModeIsType = true; //if false, projective mode is "namespace"

	public void setProperties(URI propertyFile) {
		if (propertyFile!=null)  {
			try {
				properties.load(new FileInputStream(propertyFile.toFileString()));
				considerProjectivity = !properties.getProperty("conll.considerProjectivity", "NO").equals("NO");
				projectiveModeIsType = properties.getProperty("conll.projectiveMode", "TYPE").equals("TYPE");
			} catch (IOException e) {
				logWarning("no property file");
				//throw new ConllConversionPropertiesMissingException();
			}
		}
	}

	public Properties getProperties() {
		return properties;
	}
	//----------------------------------------------------------

	private SDocumentGraph sDocumentGraph = null; 

	public SDocumentGraph getSDocumentGraph() {
		return sDocumentGraph;
	}
	//----------------------------------------------------------
	
	private URI inFileURI = null;
	
	public void setInFile(URI inFileURI) {
		this.inFileURI = inFileURI;
	}

	public URI getInFileURI() {
		return inFileURI;
	}
	//----------------------------------------------------------

	private SDocument sDocument = null;
	
	public SDocument getSDocument() {
		return this.sDocument;
	}
	//----------------------------------------------------------

	private SCorpus sCorpus = null;
	
	public SCorpus getSCorpus() {
		return this.sCorpus;
	}
	//----------------------------------------------------------
	
	private String sDocumentGraphName = null;
	
	public void setSDocumentGraphName(String sDocumentGraphName) {
		this.sDocumentGraphName = sDocumentGraphName;
	}
	
	public String getSDocumentGraphName() {
		return sDocumentGraphName;
	}
	//----------------------------------------------------------
	
	private LogService logService = null;
	
	public LogService getLogService() {
		return logService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	private void log(int logLevel, String logText) {
		if (this.getLogService()!=null) {
			this.getLogService().log(logLevel, logText);
		}
	}
	
	private void logError  (String logText) { this.log(LogService.LOG_ERROR,   logText); }
	private void logWarning(String logText) { this.log(LogService.LOG_WARNING, logText); }
	@SuppressWarnings("unused")
	private void logInfo   (String logText) { this.log(LogService.LOG_INFO,    logText); }
	@SuppressWarnings("unused")
	private void logDebug  (String logText) { this.log(LogService.LOG_DEBUG,   logText); }
	//----------------------------------------------------------
	
	public void convert(SDocument sDocument) {
		
		TupleReader tupleReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
		// try reading the input file 
		try   {
			tupleReader.setFile(new File(inFileURI.toFileString()));
			tupleReader.readFile();
		}
		catch (IOException e) {
			logError("input file could not be read. abort conversion.");
			throw new ConllConversionInputFileException(); 
		}

		//TODO das saltProject wird bereits von Pepper erzeugt
//		SaltProject saltProject  = SaltCommonFactory.eINSTANCE.createSaltProject();
		//TODO der SCorpusGraph wird bereits von Pepper erzeugt
//		SCorpusGraph sCorpusGraph = SaltCommonFactory.eINSTANCE.createSCorpusGraph();
		//TODO das SCorpus und SDocument wird bereits durch die Methode importCorpusStructure() erzeugt,
		// wenn ich das richtig sehe, benutzt Du alles was hier erzeugt wird spaeter auch gar nicht 
		{
//			SCorpus sCorpus = SaltCommonFactory.eINSTANCE.createSCorpus();
//			SCorpusDocumentRelation sDocumentRelation = SaltCommonFactory.eINSTANCE.createSCorpusDocumentRelation();
//			sCorpusGraph.setSName(getSDocumentGraphName() + "_corpusGraph");
//			saltProject.getSCorpusGraphs().add(sCorpusGraph);
//			sCorpusGraph.addSNode(sCorpus);
//			sCorpusGraph.addSNode(sDocument);
//			sDocumentRelation.setSCorpus(sCorpus);
//			sDocumentRelation.setSDocument(sDocument);
		}
		

		
		sDocumentGraph = SaltCommonFactory.eINSTANCE.createSDocumentGraph();
		sDocumentGraph.setSName(getSDocumentGraphName());
		sDocument.setSDocumentGraph(sDocumentGraph);
		STextualDS sTextualDS = SaltCommonFactory.eINSTANCE.createSTextualDS();
		sTextualDS.setSDocumentGraph(sDocumentGraph);
 
		ArrayList<SToken> tokenList = new ArrayList<SToken>();
		HashMap<SPointingRelation,Integer> pointingRelationMap = new HashMap<SPointingRelation,Integer>();
		ArrayList<String> fieldValues = new ArrayList<String>();
		
		Collection<String> tuple = null;
		int numOfTuples = tupleReader.getNumOfTuples();
		int lastTupleIndex = numOfTuples-1;
		int tupleSize = 0;
		int fieldNum = 1;

		// using a StringBuilder for the iteratively updated raw text 
		int stringBuilderCharBufferSize = tupleReader.characterSize(ConllDataField.FORM.getFieldNum()-1) + numOfTuples;
		StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);
		
		// iteration over all data rows (the complete input-file)
		for (int rowIndex=0; rowIndex<numOfTuples; rowIndex++) {
			try {
				tuple = tupleReader.getTuple();
			} 
			catch (IOException e) {
				logError(String.format("line %d of input file could not be read. abort conversion.",rowIndex+1));
				throw new ConllConversionInputFileException();
			}
			
			tupleSize = tuple.size();
			fieldValues.clear();

			if (!((tupleSize==1)||(tupleSize==10))) {
				logError(String.format("invalid format in line %d of input file. lines must be empty or contain 10 columns of data. abort conversion.",rowIndex+1));
				throw new ConllConversionInputFileException();
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
							logError(String.format("mandatory value for %s missing in line %d of input file!",field.toString(),rowIndex+1));
							throw new ConllConversionMandatoryValueMissingException();
						}
					}
					fieldValues.add(fieldValue);
				} // for (Iterator<String> iter=tuple.iterator(); iter.hasNext(); fieldNum++)
				
				// create token and add to local token list
				SToken sToken = SaltCommonFactory.eINSTANCE.createSToken();
				sToken.setSDocumentGraph(sDocumentGraph);
				tokenList.add(sToken);
				
				// update primary text (sTextualDS.sText will be set after completely reading the input file)
				int tokenTextStartOffset = primaryText.length();
				primaryText.append(fieldValues.get(ConllDataField.FORM.getFieldNum()-1)).append(" "); //update primary text data, tokens separated by space
				int tokenTextEndOffset = primaryText.length()-1;

				// create textual relation
				STextualRelation sTextualRelation = SaltCommonFactory.eINSTANCE.createSTextualRelation();
				sTextualRelation.setSource(sToken);
				sTextualRelation.setTarget(sTextualDS);
				sTextualRelation.setSStart(tokenTextStartOffset);
				sTextualRelation.setSEnd(tokenTextEndOffset);
				sTextualRelation.setSDocumentGraph(sDocumentGraph);
				
				// annotations for lemma, cpos, pos
				ConllDataField[] tokenAnnotationFields = {
						ConllDataField.LEMMA,
						ConllDataField.CPOSTAG,
						ConllDataField.POSTAG
					};
				
				for (ConllDataField field : tokenAnnotationFields) {
					String fieldValue = fieldValues.get(field.getFieldNum()-1);
					if (fieldValue!=null) { // lemma may be null, cpos and pos must not
						SAnnotation sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setName(properties.getProperty(field.getPropertyKey_Name(), field.name())); //use user specified name for field, or default: the field�s ConLL name 
						sAnnotation.setValueString(fieldValue);
						sToken.addSAnnotation(sAnnotation);
					}
				}
				
				// create annotation for span
				SAnnotation sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
				sAnnotation.setName("cat"); //toDo: variable for "cat"
				sAnnotation.setValueString("S"); //toDo: variable for "S"
				
				// create span and add span annotation
				SSpan sSpan = SaltCommonFactory.eINSTANCE.createSSpan();
				sSpan.setGraph(sDocumentGraph);
				sSpan.addSAnnotation(sAnnotation);

				// create spanning relation, set span as source and token as target
				SSpanningRelation sSpanningRelation = SaltCommonFactory.eINSTANCE.createSSpanningRelation();
				sSpanningRelation.setGraph(sDocumentGraph);
				sSpanningRelation.setSource(sSpan);
				sSpanningRelation.setTarget(sToken);

				// features
				String featureString = fieldValues.get(ConllDataField.FEATS.getFieldNum()-1);
				if (featureString!=null) {
					// check whether rule for feature category is defined. POSTAG (fine grained) gets priority over
					// CPOSTAG (coarse grained). if neither one is defined, use default
					String key = "conll.field6.POSTAG." + fieldValues.get(ConllDataField.POSTAG.getFieldNum()-1);
					if (!properties.containsKey(key)) {
						key = "conll.field6.CPOSTAG." + fieldValues.get(ConllDataField.CPOSTAG.getFieldNum()-1);
						if (!properties.containsKey(key)) {
							key = "conll.field6.default";
						}
					}
					// toDo: check if default is defined! 
					String[] featureKeys = properties.getProperty(key, "morph").split("\\|"); 
					String[] featureValues = featureString.split("\\|");

					for (int idx=0; idx<Math.min(featureKeys.length,featureValues.length); idx++) {
						sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setName(featureKeys[idx]);
						sAnnotation.setValueString(featureValues[idx]);
						sToken.addSAnnotation(sAnnotation);
					}
					if (featureKeys.length!=featureValues.length)	{
						logWarning(String.format("number of feature values doesn't match number of categories in line %d of input file!", rowIndex+1));
					}
				
				} // (featureString!=null)
				

				
				// get ID of current token
				String tokenIDStr = fieldValues.get(ConllDataField.ID.getFieldNum()-1);
				Integer tokenID = null;
				try {
					tokenID = Integer.parseInt(tokenIDStr);
				}
				catch (NumberFormatException e) {
					logError(String.format("invalid integer value '%s' for ID in line %d of input file. abort conversion.",tokenIDStr,rowIndex+1));
					throw new ConllConversionInputFileException();
				}


				
				// get ID of current token�s head token
				String headIDStr = fieldValues.get(ConllDataField.HEAD.getFieldNum()-1);
				Integer headID = null;
				try {
					headID = Integer.parseInt(headIDStr);
				}
				catch (NumberFormatException e) {
					logError(String.format("invalid integer value '%s' for HEAD in line %d of input file. abort conversion.",headIDStr,rowIndex+1));
					throw new ConllConversionInputFileException();
				}
				
				// create pointing relation, pointing from head to dependent
				if (headID>0) {
					// create annotation for pointing relation
					sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
					sAnnotation.setSName("deprel"); //toDo: variable for "deprel"
					sAnnotation.setValueString(fieldValues.get(ConllDataField.DEPREL.getFieldNum()-1));

					SPointingRelation sPointingRelation = SaltCommonFactory.eINSTANCE.createSPointingRelation();
					sPointingRelation.setSDocumentGraph(sDocumentGraph);
					sPointingRelation.addSAnnotation(sAnnotation);
					sPointingRelation.addSType("dep"); //toDo: variable for "dep"
					sPointingRelation.setTarget(sToken);
					
					if (headID<=tokenID) {
						sPointingRelation.setSource(tokenList.get(headID-1));
					}
					else {
						pointingRelationMap.put(sPointingRelation,headID);						
					}
				}
				
				
				
				if (considerProjectivity){
					// get ID of current token�s projective head token
					String proheadIDStr = fieldValues.get(ConllDataField.PHEAD.getFieldNum()-1);
					Integer proheadID = null;
					try {
						proheadID = Integer.parseInt(proheadIDStr);
					}
					catch (NumberFormatException e) {
						logError(String.format("invalid integer value '%s' for PHEAD in line %d of input file. abort conversion.",proheadIDStr,rowIndex+1));
						throw new ConllConversionInputFileException();
					}
					
					// create pointing relation, pointing from phead to dependent
					if (proheadID>0) {
						// create annotation for pointing relation
						sAnnotation = SaltCommonFactory.eINSTANCE.createSAnnotation();
						sAnnotation.setSName("deprel"); //toDo: variable for "deprel"
						sAnnotation.setValueString(fieldValues.get(ConllDataField.PDEPREL.getFieldNum()-1));

						SPointingRelation sPointingRelation = SaltCommonFactory.eINSTANCE.createSPointingRelation();
						sPointingRelation.setSDocumentGraph(sDocumentGraph);
						sPointingRelation.addSAnnotation(sAnnotation);
						sPointingRelation.setTarget(sToken);
						
						if (projectiveModeIsType) {
							sPointingRelation.addSType("prodep"); //toDo: variable for "prodep"
						}
						else {
							sAnnotation.setNamespace("projective"); //toDo: variable for "projective" (default namespace is "graph")
							sPointingRelation.addSType("dep"); //toDo: variable for "dep"
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
		
	} // convert()

} // ConllDep2SaltMapper



