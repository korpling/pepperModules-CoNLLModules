/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
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
package org.corpus_tools.peppermodules.conll;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.peppermodules.CoNLLModules.CoNLLImporterProperties;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps input data from CoNLL format to Salt format
 * 
 * @author hildebax
 *
 */
public class Conll2SaltMapper extends PepperMapperImpl {
	private static final Logger logger = LoggerFactory.getLogger(Conll2SaltMapper.class);

	//
	private ConllDataField firstSPOSField = null;
	private ConllDataField secondSPOSField = null;

	private final ConllDataField DEFAULT_SPOS = ConllDataField.POSTAG;

	private static final String PROJECTIVE = "projective";
	private static final String PRODEP = "prodep";
	private static final String DEP = "dep";
	private static final String DEPREL = "deprel";
	private static final String CAT = "cat";
	private static final String S = "S";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String TYPE = "TYPE";
	public static final String NAMESPACE = "NAMESPACE";
	public static final String DEFAULT_FEATURE = "morph";

	// separator for feature annotation values
	private final String FEATURESEPARATOR = "\\|";


	boolean splitFeatures;
	boolean keyValFeatures;
	
	public Conll2SaltMapper()
	{
	  setProperties(new CoNLLImporterProperties());
	}

	// retrieves whether to split pipe separated feature values or not
	private boolean getSplitFeatures() {
	  return (Boolean) getProperties().getProperty(CoNLLImporterProperties.PROP_SPLIT_FEATURES).getValue();
	}

        // retrieves whether to split pipe separated feature values and expect key names as in: Case=Gen|Number=Plur
	private boolean getKeyValFeatures() {
	  return (Boolean) getProperties().getProperty(CoNLLImporterProperties.PROP_KEYVAL_FEATURES).getValue();
	}

        // check for user-defined edge type, POS and lemma annotation names
        String posName;
        String lemmaName;
        String edgeType;
        String featuresNamespace;
        
        private String getEdgeType(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_EDGETYPE_NAME, "dep");
        }

        private String getFeaturesNamespace(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_FEATURES_NAMESPACE, null);
        }
        
        private String getPosName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_POS_NAME, "");
        }
        
        private String getLemmaName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_LEMMA_NAME, "");
        }

        
	boolean useSLemmaAnnotation;

	// retrieves whether or not to use SLemmaAnnoations
	private boolean getUseSLemmaAnnotation() {
		String propVal = (String) getProperties().getProperty(CoNLLImporterProperties.PROP_SLEMMA).getValue();
		if (propVal.equals(CoNLLImporterProperties.PROPERTYVAL_LEMMA))
			return true;
		if (propVal.equals(CoNLLImporterProperties.PROPERTYVAL_NONE))
			return false;
		logger.warn(String.format("Invalid value '%s' for property '%s'. Default value '%s' is used.", propVal, CoNLLImporterProperties.PROP_SLEMMA, CoNLLImporterProperties.PROPERTYVAL_LEMMA));
		return CoNLLImporterProperties.defaultUseSLemmaAnnoation;
	}

	boolean useSPOSAnnotation;

	// retrieves whether or not to use SPOSAnnoations and sets the values for
	// 'firstPOSField' and 'secondPOSField'
	private boolean getUseSPOSAnnotation() {
		this.firstSPOSField = null;
		this.secondSPOSField = null;

		if(getProperties().getProperty(CoNLLImporterProperties.PROP_SPOS) == null) {
			if (CoNLLImporterProperties.defaultUseSPOSAnnoation) {
				this.firstSPOSField = DEFAULT_SPOS;
			}
			return CoNLLImporterProperties.defaultUseSPOSAnnoation;
		}
		String propVal = (String) getProperties().getProperty(CoNLLImporterProperties.PROP_SPOS).getValue();
		String[] propVals = propVal.split(",");

		if (propVals.length > 2) {
			logger.warn(String.format("Found '%s' for property '%s'. Only two values are regarded, the rest will be ignored.", propVal, CoNLLImporterProperties.PROP_SPOS));
		}

		if ((propVals.length > 1) && (propVals[0].equals(CoNLLImporterProperties.PROPERTYVAL_NONE))) {
			logger.warn(String.format("Found '%s' for property '%s'. With this setting, no SPOSAnnotation will ever be created.", propVal, CoNLLImporterProperties.PROP_SPOS));
		}

		String val = propVals[0].trim();
		if (val.equals(CoNLLImporterProperties.PROPERTYVAL_NONE)) {
			return false;
		} else if ((val.equals(CoNLLImporterProperties.PROPERTYVAL_POSTAG)) || (val.equals(CoNLLImporterProperties.PROPERTYVAL_CPOSTAG))) {
			if (val.equals(CoNLLImporterProperties.PROPERTYVAL_POSTAG)) {
				this.firstSPOSField = ConllDataField.POSTAG;
			} else if (val.equals(CoNLLImporterProperties.PROPERTYVAL_CPOSTAG)) {
				this.firstSPOSField = ConllDataField.CPOSTAG;
			}
		} else {
			if (propVals.length == 1) {
				logger.warn(String.format("Invalid value '%s' for property '%s'. Using default value.", val, CoNLLImporterProperties.PROP_SPOS));
				this.firstSPOSField = DEFAULT_SPOS;
			} else {
				logger.warn(String.format("Invalid value '%s' for property '%s'. Using alternative value.", val, CoNLLImporterProperties.PROP_SPOS));
			}
		}

		if (propVals.length >= 2) {
			val = propVals[1].trim();
			if (val.equals(CoNLLImporterProperties.PROPERTYVAL_NONE)) {
			} else if ((val.equals(CoNLLImporterProperties.PROPERTYVAL_POSTAG)) || (val.equals(CoNLLImporterProperties.PROPERTYVAL_CPOSTAG))) {
				ConllDataField field = null;
				if (val.equals(CoNLLImporterProperties.PROPERTYVAL_POSTAG)) {
					field = ConllDataField.POSTAG;
				} else if (val.equals(CoNLLImporterProperties.PROPERTYVAL_CPOSTAG)) {
					field = ConllDataField.CPOSTAG;
				}

				if (this.firstSPOSField == null) {
					this.firstSPOSField = field;
				} else {
					this.secondSPOSField = field;
				}
			} else {
				if (this.firstSPOSField == null) {
					logger.warn(String.format("Invalid alternative value '%s' for property '%s'. Using default value.", val, CoNLLImporterProperties.PROP_SPOS));
					this.firstSPOSField = DEFAULT_SPOS;
				} else {
					logger.warn(String.format("Invalid alternative value '%s' for property '%s'.", val, CoNLLImporterProperties.PROP_SPOS));
				}
			}
		}

		return (this.firstSPOSField != null);
	}

	private void createPOSandCPOSAnnotation(ArrayList<String> fieldValues, SToken sToken) {
		{
			if (this.useSPOSAnnotation) {
				int SPOSAnnotationIndex = -1;
				ConllDataField[] bothFields = { this.firstSPOSField, this.secondSPOSField };
				ConllDataField field = null;
				for (int index = 0; index < bothFields.length; index++) {
					if (SPOSAnnotationIndex == -1) {
						field = bothFields[index];
						if (field != null) {
							String fieldVal = fieldValues.get(field.getFieldNum() - 1);
							if (fieldVal != null) {
                                                                SAnnotation anno;
                                                                if (posName != null && posName.length() > 0){
                                                                    // This is a custom names POS annotation, use a regular nameable SAnnotation
                                                                    anno = SaltFactory.createSAnnotation();
                                                                    anno.setName(posName);                                                             
                                                                }
                                                                else{
                                                                    // Standard Salt Semantics POS tag - make anno into a new SPOSAnnotation 
                                                                    anno = SaltFactory.createSPOSAnnotation();
                                                                }
								anno.setValue(fieldVal);                                                                
								sToken.addAnnotation(anno);
								SPOSAnnotationIndex = index;
							}
						}
					}
				}

				for (int index = 0; index < bothFields.length; index++) {
					if (SPOSAnnotationIndex != index) {
						field = bothFields[index];
						if (field != null) {
							String fieldVal = fieldValues.get(field.getFieldNum() - 1);
							if (fieldVal != null) {
								SAnnotation anno = SaltFactory.createSAnnotation();
								anno.setName(getProperties().getProperties().getProperty(field.getPropertyKey_Name(), field.name())); // use
																													// user
																													// specified
																													// name
																													// for
																													// field,
																													// or
																													// default:
																													// the
																													// field�s
																													// ConLL
																													// name
								anno.setValue(fieldVal);
								sToken.addAnnotation(anno);
								SPOSAnnotationIndex = index;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc PepperMapper#setDocument(SDocument)}
	 * 
	 * OVERRIDE THIS METHOD FOR CUSTOMIZED MAPPING.
	 */
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getDocument().getDocumentGraph() == null)
			getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());

		TupleReader tupleReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
		// try reading the input file
		try {
			tupleReader.setFile(new File(this.getResourceURI().toFileString()));
			tupleReader.readFile();
		} catch (IOException e) {
			String errorMessage = "input file could not be read. Abort conversion of file " + this.getResourceURI() + ".";
			logger.error(errorMessage);
			throw new PepperModuleDataException(this, errorMessage);
		}

		STextualDS sTextualDS = SaltFactory.createSTextualDS();
		String textName = ((CoNLLImporterProperties) getProperties()).getTextName();
		if(textName != null && !textName.isEmpty()) {
			sTextualDS.setName(textName);
		}
		sTextualDS.setGraph(getDocument().getDocumentGraph());

		ArrayList<SToken> tokenList = new ArrayList<SToken>();
		HashMap<SPointingRelation, Integer> pointingRelationMap = new HashMap<SPointingRelation, Integer>();
		ArrayList<String> fieldValues = new ArrayList<String>();

		Collection<String> tuple = null;
		int numOfTuples = tupleReader.getNumOfTuples();
		int lastTupleIndex = numOfTuples - 1;
		int tupleSize = 0;
		int numOfColumnsExpected = ConllDataField.values().length;
		int fieldNum = 1;

		this.useSLemmaAnnotation = getUseSLemmaAnnotation();
		this.useSPOSAnnotation = getUseSPOSAnnotation();
		this.splitFeatures = getSplitFeatures();
		this.keyValFeatures = getKeyValFeatures();
                this.posName = getPosName();
                this.lemmaName = getLemmaName();
                this.edgeType = getEdgeType();
                this.featuresNamespace = getFeaturesNamespace();

                boolean considerProjectivity = (Boolean) getProperties().getProperty(CoNLLImporterProperties.PROP_CONSIDER_PROJECTIVITY).getValue();
                boolean projectiveModeIsType = !getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_PROJECTIVE_MODE, TYPE).equalsIgnoreCase(NAMESPACE);

		// this list is used to collect lines numbers where number of categories
		// does not match expected number of categories
		ArrayList<Integer> nonMatchingCategoryNumberLines = new ArrayList<Integer>();

		// using a StringBuilder for the iteratively updated raw text
		int stringBuilderCharBufferSize = tupleReader.characterSize(ConllDataField.FORM.getFieldNum() - 1) + numOfTuples;
		StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);

		List<SToken> sentenceToken = new LinkedList<>(); 

		
		// iteration over all data rows (the complete input-file)
		for (int rowIndex = 0; rowIndex < numOfTuples; rowIndex++) {
			try {
				tuple = tupleReader.getTuple();
			} catch (IOException e) {
				String errorMessage = String.format("line %d of input file could not be read. Abort conversion of file " + this.getResourceURI() + ".", rowIndex + 1);
				throw new PepperModuleDataException(this, errorMessage);
			}

			tupleSize = tuple.size();
			fieldValues.clear();

			if (!((tupleSize == 1) || (tupleSize == numOfColumnsExpected))) {
				String errorMessage = String.format("invalid format in line %d of input file. lines must be empty or contain %d columns of data. Abort conversion of file " + this.getResourceURI() + ".", rowIndex + 1, numOfColumnsExpected);
				throw new PepperModuleDataException(this, errorMessage);
			}

			if (tupleSize > 1) { // if true, this is a data row, else it is a sentence separating line

				// read all field values
				fieldNum = 1;
				for (Iterator<String> iter = tuple.iterator(); iter.hasNext(); fieldNum++) {
					ConllDataField field = ConllDataField.getFieldByNum(fieldNum);
					String fieldValue = iter.next();
					if (fieldValue.equals(field.getDummyValue())) {
						fieldValue = null;
						if (field.isMandatory()) {
							String errorMessage = String.format("mandatory value for %s missing in line %d of input file '" + this.getResourceURI() + "'!", field.toString(), rowIndex + 1);
							throw new PepperModuleDataException(this, errorMessage);
						}
					}
					fieldValues.add(fieldValue);
				} // for (Iterator<String> iter=tuple.iterator();
					// iter.hasNext(); fieldNum++)

				// create token and add to local token list
				SToken sToken = SaltFactory.createSToken();
				sToken.setGraph(getDocument().getDocumentGraph());
				tokenList.add(sToken);

				// update primary text (sTextualDS.sText will be set after
				// completely reading the input file)
				int tokenTextStartOffset = primaryText.length();
				primaryText.append(fieldValues.get(ConllDataField.FORM.getFieldNum() - 1)).append(" "); // update
																										// primary
																										// text
																										// data,
																										// tokens
																										// separated
																										// by
																										// space
				int tokenTextEndOffset = primaryText.length() - 1;

				// create textual relation
				STextualRelation sTextualRelation = SaltFactory.createSTextualRelation();
				sTextualRelation.setSource(sToken);
				sTextualRelation.setTarget(sTextualDS);
				sTextualRelation.setStart(tokenTextStartOffset);
				sTextualRelation.setEnd(tokenTextEndOffset);
				sTextualRelation.setGraph(getDocument().getDocumentGraph());
				
				sentenceToken.add(sToken);

				// Lemma
				{
					ConllDataField field = ConllDataField.LEMMA;
					String fieldValue = fieldValues.get(field.getFieldNum() - 1);
					if (fieldValue != null) {
						SAnnotation sAnnotation = SaltFactory.createSAnnotation();
						if (useSLemmaAnnotation) {
                                                        if (lemmaName != null && lemmaName.length() > 0){
                                                            sAnnotation.setName(lemmaName);                                                             
                                                        }
                                                        else{
                                                            sAnnotation = SaltFactory.createSLemmaAnnotation();
                                                        }
                                                        sAnnotation.setValue(fieldValue);
                                                        sToken.addAnnotation(sAnnotation);                                                        
						}                                                 
					}
				}

				// POS and CPOS
				{
					createPOSandCPOSAnnotation(fieldValues, sToken);
				}
				/// POS and CPOS
				
				// features
				String featureValue = fieldValues.get(ConllDataField.FEATS.getFieldNum() - 1);

				if ((featureValue != null) && (featureValue.length() > 0)) {// (featureString!=null)
					// check whether rule for feature category is defined.
					// POSTAG (fine grained) gets priority over
					// CPOSTAG (coarse grained). if neither one is defined, use
					// default
					String ruleKey = CoNLLImporterProperties.PROP_FIELD6_POSTAG + fieldValues.get(ConllDataField.POSTAG.getFieldNum() - 1);
					if (!getProperties().getProperties().containsKey(ruleKey)) {
						ruleKey = CoNLLImporterProperties.PROP_FIELD6_CPOSTAG + fieldValues.get(ConllDataField.CPOSTAG.getFieldNum() - 1);
						if (!getProperties().getProperties().containsKey(ruleKey)) {
							ruleKey = CoNLLImporterProperties.PROP_FIELD6_DEFAULT;
						}
					}
					String featureKey = getProperties().getProperties().getProperty(ruleKey, DEFAULT_FEATURE);

					boolean doSplit = this.splitFeatures;
					String[] featureKeys = null;
					if (doSplit) {
						featureKeys = featureKey.split(FEATURESEPARATOR);
						if (ruleKey == CoNLLImporterProperties.PROP_FIELD6_DEFAULT) {
							doSplit = (featureKeys.length > 1);
						}
					}
                                        if (this.keyValFeatures){ // conll-u style key=val|key2=val2|...
						String[] featureValues = featureValue.split("\\|");
                                                for (String KeyVal:featureValues){
                                                    if (KeyVal.contains("=")){
                                                        String[] Parts = KeyVal.split("=",2);
                                                        if (Parts.length == 2){
                                                            String AnnoKey = Parts[0];
                                                            String AnnoVal = Parts[1];
                                                            sToken.createAnnotation(this.featuresNamespace, AnnoKey, AnnoVal);
                                                        }
                                                    }
                                                }
                                        }
                                        else if (doSplit) {
						String[] featureValues = featureValue.split(FEATURESEPARATOR);
						for (int idx = 0; idx < Math.min(featureKeys.length, featureValues.length); idx++) {
							sToken.createAnnotation(this.featuresNamespace, featureKeys[idx], featureValues[idx]);
						}
						if (featureKeys.length != featureValues.length) {
							nonMatchingCategoryNumberLines.add(rowIndex + 1);
						}
					} else {
						// no splitting
						sToken.createAnnotation(this.featuresNamespace, featureKey, featureValue);
					}
				} // (featureString!=null)

				// get ID of current token
				String tokenIDStr = fieldValues.get(ConllDataField.ID.getFieldNum() - 1);
				Integer tokenID = null;
				try {
					tokenID = Integer.parseInt(tokenIDStr);
				} catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid integer value '%s' for ID in line %d of input file. Abort conversion of file " + this.getResourceURI() + ".", tokenIDStr, rowIndex + 1);

					throw new PepperModuleDataException(this, errorMessage);
				}

				// get ID of current token�s head token
				String headIDStr = fieldValues.get(ConllDataField.HEAD.getFieldNum() - 1);
				Integer headID = null;
				try {
					headID = Integer.parseInt(headIDStr);
				} catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid integer value '%s' for HEAD in line %d of input file '" + this.getResourceURI() + "'. Abort conversion of file " + this.getResourceURI() + ".", headIDStr, rowIndex + 1);
					throw new PepperModuleDataException(this, errorMessage);
				}

				// create pointing relation, pointing from head to dependent
				if (headID > 0) {
					// create annotation for pointing relation
					SAnnotation sAnnotation = SaltFactory.createSAnnotation();
					sAnnotation.setName(DEPREL);

					String annoValue = fieldValues.get(ConllDataField.DEPREL.getFieldNum() - 1);
					sAnnotation.setValue(annoValue);

					SPointingRelation sPointingRelation = SaltFactory.createSPointingRelation();
                                        if (edgeType != DEP){
                                            sPointingRelation.setType(edgeType);
                                        }else{
                                            sPointingRelation.setType(DEP);
                                        }
					sPointingRelation.setSource(sToken);
					sPointingRelation.setTarget(sToken);
					sPointingRelation.addAnnotation(sAnnotation);
					sPointingRelation.setGraph(getDocument().getDocumentGraph());

					if (headID <= tokenID) {
						sPointingRelation.setSource(tokenList.get(headID - 1));
					} else {
						pointingRelationMap.put(sPointingRelation, headID);
					}
				}
				
				if (considerProjectivity) {
					// get ID of current token�s projective head token
					String proheadIDStr = fieldValues.get(ConllDataField.PHEAD.getFieldNum() - 1);
					Integer proheadID = null;
					try {
						System.out.println("Entered critical scope");
						proheadID = "_".equals( proheadIDStr.trim() )? -1 : Integer.parseInt(proheadIDStr);
					} catch (NumberFormatException e) {
						String errorMessage = String.format("invalid integer value '%s' for PHEAD in line %d of input file. Abort conversion of file " + this.getResourceURI() + ".", proheadIDStr, rowIndex + 1);
						throw new PepperModuleDataException(this, errorMessage);
					}

					// create pointing relation, pointing from phead to
					// dependent
					if (proheadID > 0) {
						// create annotation for pointing relation
						SAnnotation sAnnotation = SaltFactory.createSAnnotation();
						sAnnotation.setName(DEPREL);
						sAnnotation.setValue(fieldValues.get(ConllDataField.PDEPREL.getFieldNum() - 1));

						SPointingRelation sPointingRelation = SaltFactory.createSPointingRelation();
						sPointingRelation.addAnnotation(sAnnotation);
						sPointingRelation.setSource(sToken);
						sPointingRelation.setTarget(sToken);
						sPointingRelation.setGraph(getDocument().getDocumentGraph());

						if (projectiveModeIsType) {
							sPointingRelation.setType(PRODEP);
						} else {
							sAnnotation.setNamespace(PROJECTIVE);
							sPointingRelation.setType(DEP);
						}

						if (proheadID <= tokenID) {
							sPointingRelation.setSource(tokenList.get(proheadID - 1));
						} else {
							pointingRelationMap.put(sPointingRelation, proheadID);
						}
					}
				}

			} // if (tupleSize>1)
			else
			{
			  if(!sentenceToken.isEmpty() 
			      && ((CoNLLImporterProperties) getProperties()).isSentence())
        {
          // create span and add span annotation
          SSpan sSpan = getDocument().getDocumentGraph().createSpan(sentenceToken);
          sSpan.createAnnotation(null, CAT, S);
        }
			  
			  sentenceToken.clear();
			} // end if/else tupleSize > 1

			if ((tupleSize == 1) || (rowIndex == lastTupleIndex)) { // if true,
																	// this is a
																	// sentence
																	// separating
																	// row or
																	// the last
																	// row in
																	// the input
																	// file
				// pointingRelationMap has pointing relations as keys and
				// corresponding source node IDs as values
				// set the actual node as source for each pointing relation
				for (Entry<SPointingRelation, Integer> entry : pointingRelationMap.entrySet()) {
					entry.getKey().setSource(tokenList.get(entry.getValue() - 1)); // index=ID-1
				}
				tokenList.clear();
				pointingRelationMap.clear();
			}

		} // for (int rowIndex=0; rowIndex<numOfTuples; rowIndex++)
		
		// also add the last sentence
		if(!sentenceToken.isEmpty() 
        && ((CoNLLImporterProperties) getProperties()).isSentence())
    {
      // create span and add span annotation
      SSpan sSpan = getDocument().getDocumentGraph().createSpan(sentenceToken);
      sSpan.createAnnotation(null, CAT, S);
    }

		// ### file is completely read now

		// delete last char of primary text (a space character) and set it as
		// text for TextualDS
		primaryText.deleteCharAt(primaryText.length() - 1);
		sTextualDS.setText(primaryText.toString());

		if (nonMatchingCategoryNumberLines.size() > 0) {
			logger.warn("Number of feature values doesn't match number of categories in lines: " + nonMatchingCategoryNumberLines.toString());
		}
		return (DOCUMENT_STATUS.COMPLETED);
	} // map

} // ConllDep2SaltMapper
