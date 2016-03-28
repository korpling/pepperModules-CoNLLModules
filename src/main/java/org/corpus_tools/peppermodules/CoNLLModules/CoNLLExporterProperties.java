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
import org.corpus_tools.salt.util.SaltUtil;

public class CoNLLExporterProperties extends PepperModuleProperties{
	/**this property determines the dependency edge annotations' annotation name in the Salt graph.*/
	public static final String PROP_DEP_EDGE_ANNO_NAME = "depEdgeAnnoName";
	
	/**If {@value #PROP_WRITE_POS}==true, this property determines the dependency edge annotations' namespace.*/
	public static final String PROP_DEP_NS = "depNS";
	
	/**this property determines the dependency edge's STYPE in the Salt graph.*/
	public static final String PROP_DEP_RELTYPE = "depRelType";
	
	/**this property determines if lemma annotations are exported.*/
	public static final String PROP_WRITE_LEMMA = "writeLemma";
	
	/**this property determines if pos annotations are exported.*/
	public static final String PROP_WRITE_POS = "writePos";
	
	/**If {@value #PROP_WRITE_LEMMA}==true, this property determines the lemma annotations' name.*/
	public static final String PROP_LEMMA_NAME = "lemmaName";
	
	/**If {@value #PROP_WRITE_POS}==true, this property determines the pos annotations' name.*/
	public static final String PROP_POS_NAME = "posName";
	
	/**If {@value #PROP_WRITE_LEMMA}==true, this property determines the lemma annotations' namespace.*/
	public static final String PROP_LEMMA_NS = "lemmaNS";
	
	/**If {@value #PROP_WRITE_POS}==true, this property determines the pos annotations' namespace.*/
	public static final String PROP_POS_NS = "posNS";
	
	public CoNLLExporterProperties(){
		this.addProperty(new PepperModuleProperty<String>(PROP_DEP_EDGE_ANNO_NAME, String.class, "this property determines the dependency edge annotations' annotation name in the Salt graph.", "func", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_DEP_RELTYPE, String.class, "this property determines the dependency edge's STYPE in the Salt graph.", "dependency", false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_WRITE_LEMMA, Boolean.class, "this property determines if lemma annotations are exported.", false, false));
		this.addProperty(new PepperModuleProperty<Boolean>(PROP_WRITE_POS, Boolean.class, "this property determines if pos annotations are exported.", false, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LEMMA_NAME, String.class, "If writeLemma is true, this property determines the lemma annotations' name.", SaltUtil.SEMANTICS_LEMMA, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POS_NAME, String.class, "If writePos is true, this property determines the pos annotations' name.", SaltUtil.SEMANTICS_POS, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_DEP_NS, String.class, "This property determines the dependency edge annotation's namespace.", "dependencies", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_LEMMA_NS, String.class, "This property determines the lemma annotation's namespace.", SaltUtil.SALT_NAMESPACE, false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POS_NS, String.class, "This property determines the pos annotation's namespace.", SaltUtil.SALT_NAMESPACE, false));
	}
	
	public String getDependencyName(){
		return this.getProperty(PROP_DEP_EDGE_ANNO_NAME).getValue().toString();		
	}
	
	public String getDependencyRelationType(){
		return this.getProperty(PROP_DEP_RELTYPE).getValue().toString();
	}
	
	public boolean isWriteLemma(){
		return Boolean.parseBoolean(this.getProperty(PROP_WRITE_LEMMA).getValue().toString());
	}
	
	public boolean isWritePos(){
		return Boolean.parseBoolean(this.getProperty(PROP_WRITE_LEMMA).getValue().toString());
	}
	
	public String getLemmaName(){
		return this.getProperty(PROP_LEMMA_NAME).getValue().toString();
	}
	
	public String getPosName(){
		return this.getProperty(PROP_POS_NAME).getValue().toString();
	}
	
	public String getLemmaNamespace(){
		return this.getProperty(PROP_LEMMA_NS).getValue().toString();
	}
	
	public String getPosNamespace(){
		return this.getProperty(PROP_POS_NS).getValue().toString();
	}
	
	public String getDependencyNamespace(){
		return this.getProperty(PROP_DEP_NS).getValue().toString();
	}
}
