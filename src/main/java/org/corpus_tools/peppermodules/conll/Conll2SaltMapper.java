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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.peppermodules.CoNLLModules.CoNLLCorefMarkable;
import org.corpus_tools.peppermodules.CoNLLModules.CoNLLImporterProperties;
import org.corpus_tools.peppermodules.CoNLLModules.DefaultDict;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructuredNode;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
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

	private static final String ANNO_NAME_TOKEN_ID = "token_id";
	private static final String ANNO_NAME_HEAD_ID = "head_id";
	private static final String ANNO_NS_IDS = "sentence";

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
        String secondPosName;
        String lemmaName;
        String edgeAnnoName;
        String edgeAnnoNS;
        String edgeLayer;
        String edgeType;
        String enhancedEdgeType;
        String featuresNamespace;
        String miscNamespace;
        boolean dependenciesHaveLayers;
        boolean splitEnhancedDeprels;
        boolean noDuplicateEdeps;
        String ellipsisAnnoString;
        String ellipsisTokAnno;
        String ellipsisTokAnnoNS;
        String metaPrefix;
        String[] sentAnnos;
        String markNamespace;
        SLayer markLayer;
        String markAnnotation;
        String[] markLabels;
        Integer grpIdIndex;  // position of coref group identifier in span annotations

        // ArrayList to hold CoNLL-style bracketed spans from MISC field
        ArrayList<CoNLLCorefMarkable> markables = new ArrayList<>();
        
        private String getEdgeType(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_EDGETYPE_NAME, "dep");
        }
        
        private String getEnhancedEdgeType(){
            return ((CoNLLImporterProperties) getProperties()).getEnhancedEdgeType();
        }

        private String getEdgeLayer(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_EDGELAYER_NAME, "dep");
        }

         private String getEdgeAnnoName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_EDGEANNO_NAME, "deprel");
        }

         private String getEdgeAnnoNS(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_EDGEANNO_NS, "dep");
        }
         
         private String getMetaPrefix(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.META_PREFIX, "meta_");
         }

         private String[] getSentAnnos(){
            return ((CoNLLImporterProperties) getProperties()).getSentAnnos();
         }

         private String getMarkNamespace(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.MARK_NS, null);
         }

         private String getMarkAnnotation(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.MARK_ANNO, null);
         }

         private String[] getMarkLabels(){
            return ((CoNLLImporterProperties) getProperties()).getMarkLabels();
         }

         private String getMiscNamespace(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_MISC_NAMESPACE, null);
        }
        
         private String getFeaturesNamespace(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_FEATURES_NAMESPACE, null);
        }
        
        private String getPosName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_POS_NAME, "");
        }
        
        private String getSecondPosName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_SECOND_POS_NAME, "");
        }
        
        private String getLemmaName(){
            return (String) getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_LEMMA_NAME, "");
        }
        
        private boolean dependenciesHaveLayers() {
        	return ((CoNLLImporterProperties) getProperties()).dependenciesHaveLayers();
        }
        
        private boolean splitEnhancedDeprels() {
        	return ((CoNLLImporterProperties) getProperties()).splitEnhancedDeprels();
        }
        
        private boolean noDuplicateEdeps() {
        	return ((CoNLLImporterProperties) getProperties()).noDuplicateEdeps();                
        }
        
        private String getEllipsisAnno(){
            return ((CoNLLImporterProperties) getProperties()).getEllipsisAnno();
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
                ConllDataField field2;
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
					} else if(secondPosName!=null) {
                                                // User asked for both POS tags with separate name for 'other' tag
                                                if (bothFields[SPOSAnnotationIndex]==ConllDataField.POSTAG){
                                                    field2 = ConllDataField.CPOSTAG; // other tag is CPOS
                                                } else {
                                                    field2 = ConllDataField.POSTAG; // other tag is POS
                                                }
                                            
						if (field2 != null) {
							String fieldVal = fieldValues.get(field2.getFieldNum() - 1);
							if (fieldVal != null) {
                                                                SAnnotation anno;
                                                                if (secondPosName.length() > 0){
                                                                    // This is a second POS annotation, use a regular nameable SAnnotation
                                                                    anno = SaltFactory.createSAnnotation();
                                                                    anno.setName(secondPosName);                                                                                                                             
                                                                    anno.setValue(fieldVal);                                                                
                                                                    sToken.addAnnotation(anno);
                                                                }
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
								anno.setName(getProperties().getProperties().getProperty(field.getPropertyKey_Name(), field.name())); // use user specified name for field, or default: the field's ConLL name
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
		HashMap<SPointingRelation, String> pointingRelationMap = new HashMap<SPointingRelation, String>();
		ArrayList<String> fieldValues = new ArrayList<String>();
                HashMap<String,Integer> SentTokMap = new HashMap<>();

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
        this.secondPosName = getSecondPosName();
        this.lemmaName = getLemmaName();
        this.edgeType = getEdgeType();
        this.enhancedEdgeType = getEnhancedEdgeType();
        this.ellipsisAnnoString = getEllipsisAnno();
        this.ellipsisTokAnno = null;
        this.ellipsisTokAnnoNS = null;
        if (this.ellipsisAnnoString != null){
            if (this.ellipsisAnnoString.contains(":")){
                String[] parts = this.ellipsisAnnoString.split(":",2);
                this.ellipsisTokAnnoNS = parts[0];
                this.ellipsisTokAnno = parts[1];
            }
            else{
                this.ellipsisTokAnno = this.ellipsisAnnoString;
            }
        }
        this.edgeLayer = getEdgeLayer();
        this.edgeAnnoName = getEdgeAnnoName();
        this.edgeAnnoNS = getEdgeAnnoNS();
        this.metaPrefix = getMetaPrefix();
        this.sentAnnos = getSentAnnos();
        this.markNamespace = getMarkNamespace();
        this.featuresNamespace = getFeaturesNamespace();
        this.miscNamespace = getMiscNamespace();
        this.dependenciesHaveLayers = dependenciesHaveLayers();
        this.splitEnhancedDeprels = splitEnhancedDeprels();
        this.noDuplicateEdeps = noDuplicateEdeps();
        if (this.markNamespace != null){
           this.markLayer = SaltFactory.createSLayer();
           this.markLayer.setName(this.markNamespace);
           this.markLayer.setGraph(getDocument().getDocumentGraph());
       }
        this.markAnnotation = getMarkAnnotation();
        this.markLabels = getMarkLabels();
        this.grpIdIndex = Arrays.asList(this.markLabels).indexOf("GRP");

        // regex patterns to match coref Info with opening brackets, closing, or both
        Pattern patOpen = Pattern.compile("\\(([^|()]+)");
        Pattern patClose = Pattern.compile("([^|()]+)\\)");
        Pattern patDouble = Pattern.compile("\\(([^|()]+)\\)");
        boolean nestedClosed = true;
        String group;
        int tok_counter=0;

        // markable containers
        DefaultDict<Integer,List<CoNLLCorefMarkable>> markstart_dict = new DefaultDict<>(ArrayList.class);
        DefaultDict<Integer,List<CoNLLCorefMarkable>> markend_dict = new DefaultDict<>(ArrayList.class);
        LinkedHashMap<String,LinkedList<CoNLLCorefMarkable>> last_mark_by_group = new LinkedHashMap<>();        
        LinkedHashMap<String,LinkedList<CoNLLCorefMarkable>> open_marks_by_group = new LinkedHashMap<>();        
        DefaultDict<String,String> mark_text_by_group = new DefaultDict<>(String.class);        
        
        
        boolean considerProjectivity = (Boolean) getProperties().getProperty(CoNLLImporterProperties.PROP_CONSIDER_PROJECTIVITY).getValue();
        boolean projectiveModeIsType = !getProperties().getProperties().getProperty(CoNLLImporterProperties.PROP_PROJECTIVE_MODE, TYPE).equalsIgnoreCase(NAMESPACE);

		// this list is used to collect lines numbers where number of categories
		// does not match expected number of categories
		ArrayList<Integer> nonMatchingCategoryNumberLines = new ArrayList<Integer>();

		// using a StringBuilder for the iteratively updated raw text
		int stringBuilderCharBufferSize = tupleReader.characterSize(ConllDataField.FORM.getFieldNum() - 1) + numOfTuples;
		StringBuilder primaryText = new StringBuilder(stringBuilderCharBufferSize);

                // layer for dependency edges
                SLayer lyr = SaltFactory.createSLayer();
                lyr.setName(edgeLayer);
               
		List<SToken> sentenceToken = new LinkedList<>();	
                
                List<SAnnotation> sentAnnos = new LinkedList<>();	
                
		// iteration over all data rows (the complete input-file)
        String deprelAtTokenAnnoName = ((CoNLLImporterProperties) getProperties()).getDeprelTokenAnnoName();
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


                                String tokenIDStr = fieldValues.get(ConllDataField.ID.getFieldNum() - 1);				
                                if (tokenIDStr.contains("-")) {  // Skip multiword (a.k.a. MWT) supertokens
                                        continue;
                                }
                                
				// create token and add to local token list
				SToken sToken = SaltFactory.createSToken();
				sToken.setGraph(getDocument().getDocumentGraph());
				tokenList.add(sToken);

                                // update primary text (sTextualDS.sText will be set after
				// completely reading the input file)
                                
                                int tokenTextStartOffset = primaryText.length();
                                String tokText;
                                if (tokenIDStr.contains(".") && this.ellipsisTokAnno != null){
                                    String ellipsisToken = fieldValues.get(ConllDataField.FORM.getFieldNum() - 1);
                                    SAnnotation sa = SaltFactory.createSAnnotation();
                                    sa.setName(this.ellipsisTokAnno);
                                    sa.setNamespace(this.ellipsisTokAnnoNS);
                                    sa.setValue(ellipsisToken);
                                    sToken.addAnnotation(sa);
                                    primaryText.append(" ").append(" "); // ellipsis token and properties configured to import form field as annotation
                                    tokText = " ";
                                } else{
                                    tokText = fieldValues.get(ConllDataField.FORM.getFieldNum() - 1);
                                    primaryText.append(tokText).append(" "); // update primary text data, tokens separated by space
                                }
                                
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
				
				
				/* BEGIN OF DEPENDENCIES */				
				// get ID of current token
				Float tokenID = null;
				try {
					tokenID = Float.parseFloat(tokenIDStr);
				} catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid numerical value '%s' for ID in line %d of input file. Abort conversion of file " + this.getResourceURI() + ".", tokenIDStr, rowIndex + 1);

					throw new PepperModuleDataException(this, errorMessage);
				}

                                // map potentially float ID to position in token list
                                SentTokMap.put(tokenID.toString(),tokenList.size()-1);

                                
				// get ID of current token's head token
                boolean importIDs = ((CoNLLImporterProperties) getProperties()).importIDs();
				String headIDStr = fieldValues.get(ConllDataField.HEAD.getFieldNum() - 1);
				String headID = null;
				try {
					headID = headIDStr.matches("[0-9]+(\\.[0-9]+)?")? headIDStr : "-1";
					headID = ((Float) Float.parseFloat(headID)).toString(); // make all string IDs decimal
					if (importIDs) {
						sToken.createAnnotation(textName == null? ANNO_NS_IDS : textName, ANNO_NAME_TOKEN_ID, Float.toString(tokenID).split("\\.")[0]);
						sToken.createAnnotation(textName == null? ANNO_NS_IDS : textName, ANNO_NAME_HEAD_ID, headID.split("\\.")[0]);
					}
				} catch (NumberFormatException e) {
					String errorMessage = String.format("Invalid numerical value '%s' for HEAD in line %d of input file '" + this.getResourceURI() + "'. Abort conversion of file " + this.getResourceURI() + ".", headIDStr, rowIndex + 1);
					throw new PepperModuleDataException(this, errorMessage);
				}
                                
                                Integer headIDPosition = null;

				// create pointing relation, pointing from head to dependent
				Pair<String, String> primaryDependency = null;
				if (Float.parseFloat(headID) > 0) {
					// create annotation for pointing relation
					String annoValue = fieldValues.get(ConllDataField.DEPREL.getFieldNum() - 1);
					if (annoValue != null && deprelAtTokenAnnoName != null) {
						sToken.createAnnotation(textName, deprelAtTokenAnnoName, annoValue);
					}
					
					primaryDependency = Pair.of(headID, annoValue);
					if (Float.parseFloat(headID) <= tokenID) {
                                            headIDPosition = SentTokMap.get(headID.toString());
                                            if (edgeAnnoName != DEPREL){
						modifyPointingRelation(null, tokenList.get(headIDPosition), sToken, edgeType, edgeAnnoName, annoValue);
                                            } else{
                                                modifyPointingRelation(null, tokenList.get(headIDPosition), sToken, edgeType, DEPREL, annoValue);
                                            }
					} else {
                                            if (edgeAnnoName != DEPREL){
						pointingRelationMap.put(modifyPointingRelation(null, sToken, sToken, edgeType, edgeAnnoName, annoValue), headID);
                                            } else{
						pointingRelationMap.put(modifyPointingRelation(null, sToken, sToken, edgeType, DEPREL, annoValue), headID);
                                            }
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
                                                if (edgeAnnoName != DEPREL){  // custom edge anno name
                                                    sAnnotation.setName(edgeAnnoName);
                                                }
                                                else{						
                                                    sAnnotation.setName(DEPREL);
                                                }
                                        
						sAnnotation.setValue(fieldValues.get(ConllDataField.PDEPREL.getFieldNum() - 1));

						SPointingRelation sPointingRelation = SaltFactory.createSPointingRelation();
						sPointingRelation.addAnnotation(sAnnotation);
                                                if (!(dependenciesHaveLayers)){
                                                    sPointingRelation.addLayer(lyr);
                                                }
						sPointingRelation.setSource(sToken);
						sPointingRelation.setTarget(sToken);
						sPointingRelation.setGraph(getDocument().getDocumentGraph());

						if (projectiveModeIsType) {
							sPointingRelation.setType(PRODEP);
						} else {
                                                    if (edgeType != DEP){
                                                        sPointingRelation.setType(edgeType);
                                                    }else{
                                                        sPointingRelation.setType(DEP);
                                                    }
                                                    if (edgeAnnoNS != DEP){
                                                        sAnnotation.setNamespace(edgeAnnoNS);
                                                    }else{
                                                        sAnnotation.setNamespace(PROJECTIVE);
                                                    }                                                    
						}

						if (proheadID <= tokenID) {
							sPointingRelation.setSource(tokenList.get(proheadID - 1));
						} else {
							pointingRelationMap.put(sPointingRelation, proheadID.toString());
						}
					}
				}
				else if (enhancedEdgeType != null) {
					String enhancedSpec = fieldValues.get(ConllDataField.PHEAD.getFieldNum() - 1);
					if (enhancedSpec != null) {						
						int i = 0;
						String[] segments = enhancedSpec.split("\\|");					
						for (String depSpec : segments) {
							i++;
							Float eHead;
							try {
								eHead = Float.parseFloat(depSpec.substring(0, depSpec.indexOf(':')));
							} catch (NumberFormatException e) {
								throw new PepperModuleDataException(this, "Could not parse head id from enhanced dependency specification `" + depSpec + "` for token with id " + tokenIDStr);
							}
							if (eHead == 0) {
								continue;
							}
							String[] eDepRels;  // for one defined head multiple relations are possible
							try {
                                                                if (splitEnhancedDeprels){
                                                                    // split relations like "obl:with" into two labels, "obl" and "with"
                                                                    eDepRels = depSpec.substring(depSpec.indexOf(':') + 1).split(":"); 								
                                                                }else{  // treat relations like "obl:with" as monolithic labels
                                                                    eDepRels = depSpec.substring(depSpec.indexOf(':') + 1).split(" "); 								
                                                                }                                                               
							} catch (ArrayIndexOutOfBoundsException e) {
								throw new PepperModuleDataException(this, "Enhanced dependency `" + depSpec + "` does not specify (a) proper relation label(s) or head id for token with id " + tokenIDStr);
							}
							for (String eDepRel : eDepRels) {
                                                                if (primaryDependency == null){
                                                                    // for ellipsis tokens, set up artificial primary dependency, guaranteed to be distinct from edeps
                                                                    primaryDependency = Pair.of("0", "_"); 
                                                                }
								if ((eHead != Float.parseFloat(primaryDependency.getLeft()) || !eDepRel.equals(primaryDependency.getRight())) || !noDuplicateEdeps) {
                                                                        Integer eHeadPosition = null;
                                                                        
									// create dependency relation											
									if (eHead <= tokenID) {
                                                                                eHeadPosition = SentTokMap.get(eHead.toString());
                                                                                if (edgeAnnoName != DEPREL){  // custom edge anno name
                                                                                    modifyPointingRelation(null, tokenList.get(eHeadPosition), sToken, enhancedEdgeType, edgeAnnoName, eDepRel);
                                                                                }
                                                                                else{
                                                                                    modifyPointingRelation(null, tokenList.get(eHeadPosition), sToken, enhancedEdgeType, DEPREL, eDepRel);
                                                                                }
									} else {
                                                                            if (edgeAnnoName != DEPREL){  // custom edge anno name
										pointingRelationMap.put(modifyPointingRelation(null, sToken, sToken, enhancedEdgeType, edgeAnnoName, eDepRel), eHead.toString());
                                                                            }else{
										pointingRelationMap.put(modifyPointingRelation(null, sToken, sToken, enhancedEdgeType, DEPREL, eDepRel), eHead.toString());
                                                                            }
									}
									break;  // for the same head only one relation is allowed
								}
							}
						}
					}
				}
                                if (!considerProjectivity){  // attepmt to read MISC annos
                                    String miscStr = fieldValues.get(ConllDataField.PDEPREL.getFieldNum() - 1);
                                    if (miscStr!=null){
                                        String annos[] =  miscStr.split("\\|");
                                        for (String anno : annos){
                                            if (anno.contains("=")){
                                                String parts[] = anno.split("=",2);
                                                String key = parts[0].trim();
                                                String val = parts[1].trim();
                                                if (key.equals(this.markAnnotation)) { // bracket markable like Entity=(person

                                                    // Find single token markables;
                                                    Matcher m = patDouble.matcher(val);                        
                                                    while (m.find()) {
                                                        group = m.group(1);
                                                        CoNLLCorefMarkable new_mark = new CoNLLCorefMarkable(tok_counter);
                                                        new_mark.setEnd(tok_counter);
                                                        new_mark.setAnnoString(group); // save contents of bracketed markable
                                                        if (this.grpIdIndex > -1){
                                                            group = group.split("-")[this.grpIdIndex];
                                                        }
                                                        new_mark.setGroup(group);  // save only the group part, if a GRP part is specified
                                                        if (last_mark_by_group.containsKey(group)){
                                                            if (last_mark_by_group.get(group).size()>0){
                                                                new_mark.antecedent = last_mark_by_group.get(group).get(last_mark_by_group.get(group).size()-1);
                                                            }
                                                        }else{
                                                            LinkedList<CoNLLCorefMarkable> emptyList = new LinkedList<>();
                                                            last_mark_by_group.put(group, emptyList);
                                                        }
                                                        last_mark_by_group.get(group).push(new_mark);
                                                        markables.add(new_mark);
                                                        markstart_dict.get(tok_counter).add(new_mark);
                                                        markend_dict.get(tok_counter).add(new_mark);
                                                    }
                                                    val = val.replaceAll("\\(([^|()]+)\\)","");
                                                    // Find opening markables;
                                                    m = patOpen.matcher(val);                        
                                                    while (m.find()) {
                                                        group = m.group(1);
                                                        CoNLLCorefMarkable new_mark = new CoNLLCorefMarkable(tok_counter);
                                                        new_mark.setAnnoString(group);
                                                        if (this.grpIdIndex > -1){
                                                            group = group.split("-")[this.grpIdIndex];
                                                        }
                                                        new_mark.setGroup(group);
                                                        markables.add(new_mark);
                                                        if (last_mark_by_group.containsKey(group)){
                                                            if (last_mark_by_group.get(group).size()>0){
                                                                new_mark.antecedent = last_mark_by_group.get(group).get(last_mark_by_group.get(group).size()-1);
                                                            }
                                                        }else{
                                                            LinkedList<CoNLLCorefMarkable> emptyList = new LinkedList<>();
                                                            last_mark_by_group.put(group, emptyList);
                                                        }
                                                        if (!open_marks_by_group.containsKey(group)){
                                                            LinkedList<CoNLLCorefMarkable> emptyList = new LinkedList<>();
                                                            open_marks_by_group.put(group, emptyList);
                                                        }
                                                        open_marks_by_group.get(group).push(new_mark);
                                                        last_mark_by_group.get(group).push(new_mark);
                                                        mark_text_by_group.put(group,mark_text_by_group.get(group) + tokText + " ");
                                                        markstart_dict.get(tok_counter).add(new_mark);
                                                    }
                                                    val = val.replaceAll("\\(([^|()]+)","");
                                                    // Find closing markables;
                                                    m = patClose.matcher(val);       
                                                    CoNLLCorefMarkable mark;
                                                    while (m.find()) {
                                                        group = m.group(1);
                                                        if (this.grpIdIndex > -1){
                                                            group = group.split("-")[this.grpIdIndex];
                                                        }
                                                        if (open_marks_by_group.containsKey(group)){
                                                            //mark = last_mark_by_group.get(group).pop();
                                                            //open_marks_by_group.get(group).removeFirst();
                                                            mark = open_marks_by_group.get(group).pop();
                                                        } else{
                                                            throw new PepperModuleDataException(this, "Found closing bracket " + group + " but group was not opened!");
                                                        }
                                                        mark.setText(mark_text_by_group.get(group).trim());
                                                        mark.setEnd(tok_counter);
                                                        markend_dict.get(tok_counter).add(mark);
                                                    }
                                                    for (String g : mark_text_by_group.keySet()){
                                                        if (!mark_text_by_group.get(g).equals("")) {
                                                            mark_text_by_group.put(g,mark_text_by_group.get(g) + tokText + " ");
                                                        }
                                                    }

                                                }
                                                else{  // regular MISC annotation
                                                    SAnnotation sa = SaltFactory.createSAnnotation();
                                                    sa.setName(key);
                                                    sa.setValue(val);
                                                    if (this.miscNamespace!=null){
                                                        sa.setNamespace(this.miscNamespace);
                                                    }
                                                    sToken.addAnnotation(sa);
                                                }
                                            }
                                        }
                                    }
                                }
                            tok_counter++;
			} // if (tupleSize>1)
			else
			{
                            Iterator<String> iter = tuple.iterator();
                            String lineString = iter.next();
                            if (lineString.startsWith("#") && lineString.contains("=")){
                                String parts[] = lineString.split("=",2);
                                String key = parts[0].replaceFirst("#", "").trim();
                                String val = parts[1].trim();
                                if (key.startsWith(this.metaPrefix)){
                                    key = key.replaceFirst(this.metaPrefix, "");
                                    getDocument().createMetaAnnotation(null, key, val);
                                } else{
                                    if (ArrayUtils.contains(this.sentAnnos, key) || this.sentAnnos == null){
                                        SAnnotation anno = SaltFactory.createSAnnotation();
                                        anno.setName(key);
                                        anno.setValue(val);
                                        sentAnnos.add(anno);
                                    }
                                }
                            }
                            
			  if(!sentenceToken.isEmpty() 
			      && ((CoNLLImporterProperties) getProperties()).isSentence())
        {
          // create span and add span annotation
          SSpan sSpan = getDocument().getDocumentGraph().createSpan(sentenceToken);
          sSpan.createAnnotation(null, CAT, S);
          for (SAnnotation sAnno : sentAnnos){
              sSpan.addAnnotation(sAnno);
          }
          sentAnnos.clear();
        }

			  sentenceToken.clear();                          
			} // end if/else tupleSize > 1

			if ((tupleSize == 1) || (rowIndex == lastTupleIndex)) { // if true,
																	// file
				// pointingRelationMap has pointing relations as keys and
				// corresponding source node IDs as values
				// set the actual node as source for each pointing relation
				for (Entry<SPointingRelation, String> entry : pointingRelationMap.entrySet()) {
                                    Integer position = SentTokMap.get(entry.getValue());
                                    SToken src = tokenList.get(position);
                                    entry.getKey().setSource(src); // index=ID-1
				}
				tokenList.clear();
                                SentTokMap.clear();  // new sentence, clear mapping
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
                
                
    List<SToken> tokens = getDocument().getDocumentGraph().getTokens();
    
    // Import any spans encoded in CoNLL-U MISC field as grouped brackets
            // add all covered tokens to markables
        for (CoNLLCorefMarkable mark : markables){
            for (int i = mark.getStart(); i <= mark.getEnd(); i++){
                mark.addToken(tokens.get(i));
            }
        }
 
        // keep a mapping of Markables to SSpans to link edges later
        LinkedHashMap<CoNLLCorefMarkable,SSpan> marks2spans = new LinkedHashMap<>();
        
        // create sSpans for all markables and link to antecedents if necessary
        for (CoNLLCorefMarkable mark : markables){
            SSpan sSpan = getDocument().getDocumentGraph().createSpan(mark.getTokens());
            if (this.markNamespace != null){
                if (sSpan == null){
                    throw new PepperModuleDataException(this, "Null span detected, created from markable object: " + mark.toString());
                }
                sSpan.addLayer(this.markLayer);
            }
            if (this.markLabels != null){
                int i=0;
                for (String subval : mark.getAnnoString().split("-")){                
                    if (this.markLabels.length < i){
                        break; // undeclared annotation value, ignore
                    }
                    String annoName = this.markLabels[i];
                    i++;
                    if (annoName.length() == 0 || subval.length()== 0 || annoName.equals("GRP")){
                        continue;  // ignore zero length annotations
                    }
                    if (annoName.equals("EDGE")){
                        mark.setEdgeType(subval);
                        continue;
                    }
                    SAnnotation annotation = SaltFactory.createSAnnotation();
                    annotation.setName(annoName);
                    annotation.setValue(subval);
                    if (this.markNamespace != null){
                        annotation.setNamespace(this.markNamespace);
                    }                
                    sSpan.addAnnotation(annotation);
                }
            }
            if (mark.getNodeName()!=null){
                sSpan.setName(mark.getNodeName());
            }
            
            // remember SSpan object belonging to this markID
            marks2spans.put(mark, sSpan);
        }
        
        // add edges
        for (CoNLLCorefMarkable mark : markables){
             if (mark.antecedent != null){
                SPointingRelation sRel = SaltFactory.createSPointingRelation();  
                sRel.setSource(marks2spans.get(mark));
                sRel.setTarget(marks2spans.get(mark.antecedent));
                sRel.setType("coref");
                SAnnotation relAnno = SaltFactory.createSAnnotation();
                relAnno.setName("type");
                relAnno.setValue(mark.getEdgeType());
                sRel.addAnnotation(relAnno);
                if (this.markNamespace != null){
                    sRel.addLayer(this.markLayer);
                }
                getDocument().getDocumentGraph().addRelation(sRel);
             }
        }

		if (nonMatchingCategoryNumberLines.size() > 0) {
			logger.warn("Number of feature values doesn't match number of categories in lines: " + nonMatchingCategoryNumberLines.toString());
		}
		return (DOCUMENT_STATUS.COMPLETED);
	} // map
	
	private SPointingRelation mapDependency() {
		return null;
	}
	
	Map<String, SLayer> layerMap = new HashMap<>();
	
	private SPointingRelation modifyPointingRelation(SPointingRelation rel, SStructuredNode source, SStructuredNode target, String type, String annoName, String annoVal) {
		boolean newRel = rel == null;
		if (newRel) {
			rel = SaltFactory.createSPointingRelation();
		}
		rel.setSource(source);
		rel.setTarget(target);
		rel.setType(type);
		if (annoName != null && annoVal != null) {
			rel.createAnnotation(edgeAnnoNS, annoName, annoVal);
		}
		if (newRel) {
			rel.setGraph(getDocument().getDocumentGraph());
		}
		if (dependenciesHaveLayers) {
			SLayer layer;
			if (!layerMap.containsKey(type)) {
				layer = SaltFactory.createSLayer();
				layer.setName(type);
				layerMap.put(type, layer);
                                getDocument().getDocumentGraph().addLayer(layer);
			}
			layer = layerMap.get(type);
			rel.addLayer(layer);
			//rel.getSource().addLayer(layer);
			//rel.getTarget().addLayer(layer);
		}
		return rel;
	}
	
	

} // ConllDep2SaltMapper
