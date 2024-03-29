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
	public final static String PROP_EDGELAYER_NAME = PREFIX + "EDGE.LAYER";
	public final static String PROP_EDGEANNO_NS = PREFIX + "EDGE.ANNO.NS";
	public final static String PROP_EDGEANNO_NAME = PREFIX + "EDGE.ANNO.NAME";
																			// at
																					// the
																					// end
	public final static String PROP_POS_NAME = PREFIX + "POS.NAME";
	public final static String PROP_SECOND_POS_NAME = PREFIX + "SECOND.POS.NAME";
	public final static String PROP_LEMMA_NAME = PREFIX + "LEMMA.NAME";
	public final static String PROP_EDGETYPE_NAME = PREFIX + "EDGE.TYPE";
	public final static String PROP_FEATURES_NAMESPACE = PREFIX + "FEATURES.NAMESPACE";
	public final static String PROP_MISC_NAMESPACE = PREFIX + "MISC.NAMESPACE";
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

	public final static String PROP_TEXT_NAME = PREFIX + "textName";
	
	/** This determines the edge type for enhanced dependencies as coded by the conll-u standard. The edge type needs to be distinct from the edge type set for regular dependencies. If this property is set, parsing the file for enhanced dependencies will be activated.  Does not work together with 'considerProjectivity'.*/
	public static final String PROP_ENHANCED_EDGE_TYPE = PREFIX + "enhanced.EDGE.TYPE";
	
	/** If set to true, this property triggers a creation of SLayers for dependency annotations including edges and tree nodes. Layer names are the edge types. */
	public static final String PROP_DEPS_WITH_LAYERS = PREFIX + "dependency.layers";
        
        /** If set to true, enhanced dependencies are split by ':' into multiple labels, for example 'obl:with' becomes two labels, 'obl' and 'with'. */
	public static final String PROP_SPLIT_EDEPS = PREFIX + "split.edeps";

        /** If set to true, enhanced dependencies with identical non-enhanced counterparts are ignored. */
	public static final String PROP_NO_DUP_EDEPS = PREFIX + "no.duplicate.edeps";

        /** If set, ellipsis token values are imported as annotations, and replaced in base text by a blank space. Use colon to specify a namespace. */
	public static final String ELLIPSIS_TOK_ANNO = PREFIX + "ellipsis.tok.annotation";

        /** Prefix for comment line annotations interpreted as metadata. Default: meta:: */
	public static final String META_PREFIX = PREFIX + "meta.prefix";

        /** Comma separated list of sentence annotations to import */
	public static final String SENT_ANNOS = PREFIX + "sentence.annotations";

        /** Namespace for CoNLL-Coref-style markables in MISC field if present. */
	public static final String MARK_NS = PREFIX + "markable.namespace";
        
        /** Annotation key containing CoNLL-Coref-style markables in MISC field, e.g. Entity in Entity=(person */
	public static final String MARK_ANNO = PREFIX + "markable.annotation";

        /** Annotation key names (hyphen-separated) for CoNLL-Coref-style markables in MISC field if present.  GRP denotes edge cluster, EDGE denotes edge type. Default: entity-GRP-identity */
	public static final String MARK_LABELS = PREFIX + "markable.labels";
	
	/** If this is set to true, the importer imports token ID and head ID as token annotations. **/
	public static final String PROP_IMPORT_IDS = PREFIX + "import.ids";
	
	/** Additionally import deprel as token annotation */
	public static final String PROP_DEPREL_AT_TOKEN = PREFIX + "deprel.at.token";
        
	public CoNLLImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(PROP_SPOS, String.class,
				"States which CoNLL field´s data to use for the SPOSAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SPOSAnnotations at all. If the field designated by [FIELD] contains no data, [ALTERNATIVEFIELD] (if given), is used. If that field contains no data, no SPOSAnnotation is created for the particular salt token.",
				PROPERTYVAL_POSTAG, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SLEMMA, String.class,
				"States which CoNLL field´s data to use for the SLemmaAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SLemmaAnnotations at all. If the field designated by [FIELD] contains no data, no SLemmaAnnotation is created for the particular salt token. The default value for this attribute is LEMMA.",
				PROPERTYVAL_LEMMA, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_CONSIDER_PROJECTIVITY, Boolean.class,
				"States whether to create a salt pointing relation from projective head of tokens to the dependent.",
				false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_PROJECTIVE_MODE, String.class,
				"This attribute only applies if  is set TRUE! Usage: conll.projectiveMode=[VALUE] Possible values are TYPE and NAMESPACE Default value for this attribute is TYPE configures how projectivity is modelled in the salt representation. Generally, there will be a salt pointing relation and an annotation with the name 'deprel' on that relation. If the mode is set TYPE, the relation´s type will be 'prodep'. If the mode is set NAMESPACE, the relation´s type will be 'dep' and the annotation´s namespace will be set to 'projective'. ",
				"TYPE", false));
		
		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_POSTAG, String.class,
				"This is not only a single property, but a class of properties. Multiple entries of this type may be given in a properties file, but [TAG] must be unique. A property of this type applies for any input data row that contains the given [TAG] as value for the POSTAG field. The corresponding salt token will get a SAnnotation with [VALUE] as name and the input data row´s FEATS field as value.",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_CPOSTAG, String.class,
				"This attribute works like , but instead of POSTAG, the CPOSTAG value of data rows is utilized. ",
				false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FIELD6_DEFAULT, String.class,
				"Allowed values are any single category name or pipe separated sequences of category names", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POS_NAME, String.class,
				"A string specifying a valid annotation name for the POS annotation", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SECOND_POS_NAME, String.class,
				"A string specifying a valid annotation name for the second POS annotation, if desired", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LEMMA_NAME, String.class,
				"A string specifying a valid annotation name for the lemma annotation", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_EDGETYPE_NAME, String.class,
				"A string specifying a valid edge type name for the dependency edges (e.g. 'dep')", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_SPLIT_FEATURES, Boolean.class,
				"If [VALUE] is set TRUE, any data row´s FEATS field will be split into it´s pipe separated elements to create multiple annotations on the corresponding salt token (see POSTAG, CPOSTAG and default). If a field contains a different number of pipe separated elements than defined in the POSTAG, CPOSTAG or default attribute, the lesser number of annotations will be created, while the additional elements will be lost! If VALUE is FALSE, no splitting is done.",
				false, false));
                this.addProperty(new PepperModuleProperty<String>(PROP_EDGELAYER_NAME, String.class, "A string specifying a valid layer name for the dependency edges (e.g. 'dep')", false));
                this.addProperty(new PepperModuleProperty<String>(PROP_EDGEANNO_NAME, String.class, "A string specifying a name for the edge annotation (e.g. 'func')", false));
                this.addProperty(new PepperModuleProperty<String>(PROP_EDGEANNO_NS, String.class, "A string specifying a namespace for the edge annotation (e.g. 'dep' in 'dep:func')", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_KEYVAL_FEATURES, Boolean.class,
				"If [VALUE] is set TRUE, it is assumed that the FEATS column contains pipe-delimited annotation names and values such as Case=Gen|Number=Plur.",
				false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_FEATURES_NAMESPACE, String.class,
				"Namespace to assign to feature annotations in column 6.", null, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_MISC_NAMESPACE, String.class,
				"Namespace to assign to feature annotations in column 10.", null, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_SENTENCE, Boolean.class,
				"If [VALUE] is set TRUE add a sentence annotation (cat=S) to the data.", true, false));
		
		this.addProperty(PepperModuleProperty.create().withName(PROP_TEXT_NAME).withType(String.class)
				.withDescription("Name of the text").withDefaultValue(null).isRequired(false).build());
		
		addProperty(PepperModuleProperty.create()
				.withName(PROP_ENHANCED_EDGE_TYPE)
				.withType(String.class)
				.withDescription("This determines the edge type for enhanced dependencies as coded by the conll-u standard. If this property is set, parsing the file for enhanced dependencies will be activated. Does not work together with 'considerProjectivity'.")
				.build());
		addProperty(PepperModuleProperty.create()
				.withName(PROP_DEPS_WITH_LAYERS)
				.withType(Boolean.class)
				.withDescription("If set to true, this property triggers a creation of SLayers for dependency annotations including edges and tree nodes. Layer names are the edge types.")
				.withDefaultValue(false)
				.build());
		addProperty(PepperModuleProperty.create()
				.withName(PROP_SPLIT_EDEPS)
				.withType(Boolean.class)
				.withDescription("If set to true, enhanced dependencies are split by ':' into multiple labels, for example 'obl:with' becomes two labels, 'obl' and 'with'.")
				.withDefaultValue(false)
				.build());
		addProperty(PepperModuleProperty.create()
				.withName(PROP_NO_DUP_EDEPS)
				.withType(Boolean.class)
				.withDescription("If set to true, enhanced dependencies with identical non-enhanced counterparts are ignored.")
				.withDefaultValue(false)
				.build());
		addProperty(PepperModuleProperty.create()
				.withName(ELLIPSIS_TOK_ANNO)
				.withType(String.class)
				.withDescription("If set, ellipsis token values are imported as annotations, and replaced in base text by a blank space. Use colon to specify a namespace.")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(META_PREFIX)
				.withType(String.class)
				.withDescription("Prefix for comment line annotations interpreted as metadata. Default: meta:: ")
                                .withDefaultValue("meta::")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(SENT_ANNOS)
				.withType(String.class)
				.withDescription("Comma separated list of sentence annotations to import")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(MARK_NS)
				.withType(String.class)
				.withDescription("Namespace for CoNLL-Coref-style markables in MISC field if present")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(MARK_ANNO)
				.withType(String.class)
				.withDescription("Annotation key containing CoNLL-Coref-style markables in MISC field, e.g. Entity in Entity=(person")
                                .withDefaultValue("Entity")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(MARK_LABELS)
				.withType(String.class)
				.withDescription("Annotation key names (hyphen-separated) for CoNLL-Coref-style markables in MISC field if present. GRP denotes edge cluster, EDGE denotes edge type. Default: entity-GRP-identity")
                                .withDefaultValue("entity-GRP-identity")
				.build());	
		addProperty(PepperModuleProperty.create()
				.withName(PROP_IMPORT_IDS)
				.withType(Boolean.class)
				.withDescription("Import token and head id of tokens.")
				.withDefaultValue(false)
				.build());		
		addProperty(PepperModuleProperty.create()
				.withName(PROP_DEPREL_AT_TOKEN)
				.withType(String.class)
				.withDescription("If an annotation name is provided, the dependency relation of a token will be annotated additionally at the token directly using the provided annotation name.")
				.build());
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

	public String getSecondPosName() {
		return ((String) this.getProperty(PROP_SECOND_POS_NAME).getValue());
	}

	public String getLemmaName() {
		return ((String) this.getProperty(PROP_LEMMA_NAME).getValue());
	}

	public String getEdgeTypeName() {
		return ((String) this.getProperty(PROP_EDGETYPE_NAME).getValue());
	}

        public String getEdgeLayerName() {
		return ((String) this.getProperty(PROP_EDGELAYER_NAME).getValue());
	}
        public String getEdgeAnnoName() {
		return ((String) this.getProperty(PROP_EDGEANNO_NAME).getValue());
	}
        public String getEdgeAnnoNS() {
		return ((String) this.getProperty(PROP_EDGEANNO_NS).getValue());
	}

        public String getFeaturesNamespace() {
		return ((String) this.getProperty(PROP_FEATURES_NAMESPACE).getValue());
	}

        public String getMiscNamespace() {
		return ((String) this.getProperty(PROP_MISC_NAMESPACE).getValue());
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

	public String getTextName() {
		Object val = this.getProperty(PROP_TEXT_NAME).getValue();
		return val instanceof String ? (String) val : null;
	}
	
	public String getEnhancedEdgeType() {
		Object val = getProperty(PROP_ENHANCED_EDGE_TYPE).getValue();
		return val == null? null : (String) val;
	}
	
	public Boolean dependenciesHaveLayers() {
		return (Boolean) getProperty(PROP_DEPS_WITH_LAYERS).getValue();
	}
        
        public Boolean splitEnhancedDeprels() {
		return (Boolean) getProperty(PROP_SPLIT_EDEPS).getValue();
	}

        public Boolean noDuplicateEdeps() {
		return (Boolean) getProperty(PROP_NO_DUP_EDEPS).getValue();
	}

        public String getEllipsisAnno() {
		Object val = getProperty(ELLIPSIS_TOK_ANNO).getValue();
		return val == null? null : (String) val;
	}

        public String getMetaPrefix() {
		Object val = getProperty(META_PREFIX).getValue();
		return val == null? null : (String) val;
	}

        public String[] getSentAnnos() {
		Object val = getProperty(SENT_ANNOS).getValue();
		return val == null? null : ((String) val).split(",");
	}
        public String getMarkNamespace() {
		Object val = getProperty(MARK_NS).getValue();
		return val == null? null : (String) val;
	}

        public String getMarkAnnotation() {
		Object val = getProperty(MARK_ANNO).getValue();
		return val == null? null : (String) val;
	}

        public String[] getMarkLabels() {
		Object val = getProperty(MARK_LABELS).getValue();
		return val == null? null : ((String) val).split("-",-1);
	}
        
    public Boolean importIDs() {
    	return (Boolean) getProperty(PROP_IMPORT_IDS).getValue();
    }
    
    public String getDeprelTokenAnnoName() {
    	Object val = getProperty(PROP_DEPREL_AT_TOKEN).getValue();
    	return val == null? null : (String) val;
    }

}
