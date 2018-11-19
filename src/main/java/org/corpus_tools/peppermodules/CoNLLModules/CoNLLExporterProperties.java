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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.peppermodules.conll.ConllDataField;

import com.google.common.base.Joiner;

public class CoNLLExporterProperties extends PepperModuleProperties{
	
	/** In this string the annotation names (and collapse instructions) for the CoNLL columns are encoded. 
	 * These are comma-separated values. A CoNLL-U table looks like the following:
	 * <pre>
	 * ID	FORM	LEMMA	CPOS	POS	FEATS	HEAD	REL	MISC	TOK_INFO
	 * </pre> 
	 * The following configuration provides the annotation names for the first 6 (ID, FORM not counted)
	 * columns and collapses MISC and TOK_INFO (tokenization information):
	 * <pre>
	 * cols=Lemma,_,Pos,Morph,Dep,,
	 * </pre> 
	 * Further CPOS is filled with dashes. FORM is always taken from the Salt tokenization, so ID, FORM and HEAD
	 * are always omitted.
	 * 
	 * If you want to use default values for single annotations, use <pre>*</pre>:
	 * <pre>
	 * cols=*,*,*,*,*,*,*
	 * </pre>
	 * is the trivial case of only using defaults. This only changes the annotation name for POS and dashes out
	 * the last two values:
	 * <pre>
	 * cols=*,*,pos,*,*,_,_
	 * </pre>
	 * 
	 * */
	public static final String PROP_COL_CONFIG = "cols";
	
	/** This property contains all the annotations that will be found on spans over the tokens, but not the token itself. */
	public static final String PROP_SPAN_ANNOS = "spanAnnotations";
	
	public static final String COLLAPSE_VALUE = " ";
	private static final String[] DEFAULTS = {"salt::lemma", "_", "salt::pos", "_", "func", "_", "_"};
	/** this marker is supposed to be used when the user does not want to reconfigure a value and stick to the default.*/
	public static final String MARKER_USE_DEFAULT = "*";
	/** if provided, a specific segmentation is selected rather than all tokens found in a document */
	public static final String PROP_SEGMENTATION_NAME = "segmentation.name";
	
	public CoNLLExporterProperties(){
		this.addProperty(new PepperModuleProperty<String>(PROP_COL_CONFIG, String.class, "In this string the annotation names (and collapse instructions) for the CoNLL columns are encoded.", Joiner.on(",").join(DEFAULTS), false));
		this.addProperty(new PepperModuleProperty<String>(PROP_SPAN_ANNOS, String.class, "This property contains all the annotations that will be found on spans over the tokens, but not the token itself.", "", false));
		//this.addProperty(PepperModuleProperty.with);
	}
	
	public Map<ConllDataField, String> getColumns(){
		String columnStr = getProperty(PROP_COL_CONFIG).getValue().toString();
		String[] columns = columnStr.split(",");
		ConllDataField[] header = {ConllDataField.LEMMA, ConllDataField.CPOSTAG, ConllDataField.POSTAG, ConllDataField.FEATS, ConllDataField.DEPREL, ConllDataField.PHEAD, ConllDataField.PDEPREL};
		HashMap<ConllDataField, String> colInfo = new HashMap<>();
		for (int i = 0; i<columns.length; i++){			
			colInfo.put(header[i], MARKER_USE_DEFAULT.equals(columns[i])? DEFAULTS[i] : columns[i]);
		}
		if (columns.length < 7){
			if (columns.length < 6){
				return null;
			}
			colInfo.put(header[header.length-1], COLLAPSE_VALUE);
		}
		return colInfo;
	}
	
	public Set<String> getSpanAnnotations(){
		HashSet<String> spanAnnos = new HashSet<String>();
		String propVal = getProperty(PROP_SPAN_ANNOS).getValue().toString();
		if (propVal.startsWith("{")){
			propVal = propVal.substring(1);
		}
		if (propVal.endsWith("}")){
			propVal = propVal.substring(0, propVal.length()-1);
		}
		for (String a : propVal.split(",")){
			spanAnnos.add(a.trim());
		}
		return spanAnnos;
	}
}
