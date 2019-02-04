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
package org.corpus_tools.peppermodules.CoNLLModules;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link EXMARaLDAImporter}.
 * 
 * @author Florian Zipser
 *
 */
public class CoNLLImporterProperties extends PepperModuleProperties {
	public static final String PREFIX = "conll.";

	// property defaults
	public static final boolean defaultUseSPOSAnnoation = true;
	public static final boolean defaultUseSLemmaAnnoation = true;

	// some values and keys used in this class
	public final static String PROP_SPOS = PREFIX + "SPOS";
	public final static String PROP_SLEMMA = PREFIX + "SLEMMA";
	public final static String PROP_CONSIDER_PROJECTIVITY = PREFIX + "considerProjectivity";
	public final static String PROP_PROJECTIVE_MODE = PREFIX + "projectiveMode";

	public final static String PROP_FIELD6_POSTAG = PREFIX + "field6.POSTAG."; // the
																				// dot
																				// at
																				// the
																				// end
																				// is
																				// correct
	public final static String PROP_FIELD6_CPOSTAG = PREFIX + "field6.CPOSTAG."; // the
																					// dot
																					// at
																					// the
																					// end
	public final static String PROP_POS_NAME = PREFIX + "POS.NAME";
	public final static String PROP_LEMMA_NAME = PREFIX + "LEMMA.NAME";
	public final static String PROP_EDGETYPE_NAME = PREFIX + "EDGE.TYPE";
	public final static String PROP_FEATURES_NAMESPACE = PREFIX + "FEATURES.NAMESPACE";
	public final static String PROP_KEYVAL_FEATURES = PREFIX + "KeyValFeatures";
																					// is
																					// correct
	public final static String PROP_FIELD6_DEFAULT = PREFIX + "field6.default";
	public final static String PROP_SPLIT_FEATURES = PREFIX + "splitFeatures";

	public final static String PROPERTYVAL_NONE = "NONE";
	public final static String PROPERTYVAL_POSTAG = "POSTAG";
	public final static String PROPERTYVAL_CPOSTAG = "CPOSTAG";
	public final static String PROPERTYVAL_LEMMA = "LEMMA";
	
	public final static String PROP_SENTENCE = PREFIX + "SENTENCE";

	public CoNLLImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(PROP_SPOS, String.class, "States which CoNLL field´s data to use for the SPOSAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SPOSAnnotations at all. If the field designated by [FIELD] contains no data, [ALTERNATIVEFIELD] (if given), is used. If that field contains no data, no SPOSAnnotation is created for the particular salt token.", PROPERTYVAL_POSTAG, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SLEMMA, String.class, "States which CoNLL field´s data to use for the SLemmaAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SLemmaAnnotations at all. If the field designated by [FIELD] contains no data, no SLemmaAnnotation is created for the particular salt token. The default value for this attribute is LEMMA.", PROPERTYVAL_LEMMA, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CONSIDER_PROJECTIVITY, Boolean.class, "States whether to create a salt pointing relation from projective head of tokens to the dependent.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_PROJECTIVE_MODE, String.class, "This attribute only applies if  is set TRUE! Usage: conll.projectiveMode=[VALUE] Possible values are TYPE and NAMESPACE Default value for this attribute is TYPE configures how projectivity is modelled in the salt representation. Generally, there will be a salt pointing relation and an annotation with the name 'deprel' on that relation. If the mode is set TYPE, the relation´s type will be 'prodep'. If the mode is set NAMESPACE, the relation´s type will be 'dep' and the annotation´s namespace will be set to 'projective'. ", "TYPE", false));

		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_POSTAG, String.class, "This is not only a single property, but a class of properties. Multiple entries of this type may be given in a properties file, but [TAG] must be unique. A property of this type applies for any input data row that contains the given [TAG] as value for the POSTAG field. The corresponding salt token will get a SAnnotation with [VALUE] as name and the input data row´s FEATS field as value.", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_CPOSTAG, String.class, "This attribute works like , but instead of POSTAG, the CPOSTAG value of data rows is utilized. ", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_DEFAULT, String.class, "Allowed values are any single category name or pipe separated sequences of category names", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POS_NAME, String.class, "A string specifying a valid annotation name for the POS annotation", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LEMMA_NAME, String.class, "A string specifying a valid annotation name for the lemma annotation", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGETYPE_NAME, String.class, "A string specifying a valid edge type name for the dependency edges (e.g. 'dep')", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_SPLIT_FEATURES, Boolean.class, "If [VALUE] is set TRUE, any data row´s FEATS field will be split into it´s pipe separated elements to create multiple annotations on the corresponding salt token (see POSTAG, CPOSTAG and default). If a field contains a different number of pipe separated elements than defined in the POSTAG, CPOSTAG or default attribute, the lesser number of annotations will be created, while the additional elements will be lost! If VALUE is FALSE, no splitting is done.", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_KEYVAL_FEATURES, Boolean.class, "If [VALUE] is set TRUE, it is assumed that the FEATS column contains pipe-delimited annotation names and values such as Case=Gen|Number=Plur.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FEATURES_NAMESPACE, String.class, "Namespace to assign to features annotations in column 6.", null, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_SENTENCE, Boolean.class, "If [VALUE] is set TRUE add a sentence annotation (cat=S) to the data.", true, false));

	}

	public String getSPos() {
		return ((String) this.getProperty(PROP_SPOS).getValue());
	}

	public String getSLemma() {
		return ((String) this.getProperty(PROP_SLEMMA).getValue());
	}

	public Boolean isSConsiderProjectivity() {
		return ((Boolean) this.getProperty(PROP_CONSIDER_PROJECTIVITY).getValue());
	}

	public String getSProjectiveMode() {
		return ((String) this.getProperty(PROP_PROJECTIVE_MODE).getValue());
	}

	public String getField6Postag() {
		return ((String) this.getProperty(PROP_FIELD6_POSTAG).getValue());
	}

	public String getField6CPostag() {
		return ((String) this.getProperty(PROP_FIELD6_CPOSTAG).getValue());
	}

	public String getFiel6dDefault() {
		return ((String) this.getProperty(PROP_FIELD6_DEFAULT).getValue());
	}

        public String getPosName() {
		return ((String) this.getProperty(PROP_POS_NAME).getValue());
	}
	public String getLemmaName() {
		return ((String) this.getProperty(PROP_LEMMA_NAME).getValue());
	}
	public String getEdgeTypeName() {
		return ((String) this.getProperty(PROP_EDGETYPE_NAME).getValue());
	}
	public String getFeaturesNamespace() {
		return ((String) this.getProperty(PROP_FEATURES_NAMESPACE).getValue());
	}

	public Boolean isSplitFeatures() {
		return ((Boolean) this.getProperty(PROP_SPLIT_FEATURES).getValue());
	}

	public Boolean isKeyValFeatures() { 
		return ((Boolean) this.getProperty(PROP_KEYVAL_FEATURES).getValue());
	}

	public Boolean isSentence() {
    return ((Boolean) this.getProperty(PROP_SENTENCE).getValue());
  }

}
