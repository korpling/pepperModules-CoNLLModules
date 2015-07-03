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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.CoNLLModules;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.Conll2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="CoNLLImporterComponent", factory="PepperImporterComponentFactory")
public class CoNLLImporter extends PepperImporterImpl implements PepperImporter
{
	//-------------------------------------------------------------------------
	public static final String NAME          = "CoNLLImporter";
	public static final String FORMATNAME    = "CoNLL";
	public static final String FORMATVERSION = "1.0"; //TODO: What version? 
	//-------------------------------------------------------------------------
	
	public CoNLLImporter()
	{
		super();
		//setting name of module
		this.setName(NAME);
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-CoNLLModules"));
		setDesc("This importer transforms data in CoNLL format to a Salt model. ");
		//set list of formats supported by this module
		this.addSupportedFormat(FORMATNAME, FORMATVERSION, null);
		getSDocumentEndings().add(PepperImporter.ENDING_ALL_FILES);
		setProperties(new CoNLLImporterProperties());
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId)
	{
		Conll2SaltMapper mapper = new Conll2SaltMapper();
		return(mapper);
	}
}
