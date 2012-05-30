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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.CoNLLModules;

import java.io.IOException;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.RETURNING_MODE;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.Conll2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

@Component(name="CoNLLImporterComponent", factory="PepperImporterComponentFactory")
@Service(value=PepperImporter.class)
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
		this.name= FORMATNAME;
		//set list of formats supported by this module
		this.addSupportedFormat(FORMATNAME, FORMATVERSION, null);
	}

	
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;

	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws PepperModuleException
	{
		this.setSCorpusGraph(corpusGraph);
		if (this.getSCorpusGraph()== null)
			throw new PepperModuleException(this.name+": Cannot start with importing corpus, because salt project isn�t set.");
		
		if (this.getCorpusDefinition()== null)
			throw new PepperModuleException(this.name+": Cannot start with importing corpus, because no corpus definition to import is given.");
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new PepperModuleException(this.name+": Cannot start with importing corpus, because the path of given corpus definition is null.");
		if (this.getCorpusDefinition().getCorpusPath().isFile())
		{
			if (	(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("/")) || 
					(this.getCorpusDefinition().getCorpusPath().toFileString().endsWith("\\")))
			{//clean uri in corpus path (if it is a folder and ends with/, / has to be removed)
				this.getCorpusDefinition().setCorpusPath(this.getCorpusDefinition().getCorpusPath().trimSegments(1));
			}//clean uri in corpus path (if it is a folder and ends with/, / has to be removed)
			
			try {
				this.documentResourceTable= this.createCorpusStructure(this.getCorpusDefinition().getCorpusPath(), null, null);
			} catch (IOException e) {
				throw new PepperModuleException(this.name+": Cannot start with importing corpus, because saome exception occurs: ",e);
			}
		}	
	}
	
	@Override
	public void start() throws PepperModuleException
	{
		//TODO /7/: delete this, if you want to parallelize processing 
		super.start();
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			if (sElementId.getSIdentifiableElement() instanceof SDocument) {
				this.returningMode= RETURNING_MODE.PUT;
				Conll2SaltMapper conll2SaltMapper = new Conll2SaltMapper();
				conll2SaltMapper.setProperties(this.getSpecialParams());
				conll2SaltMapper.setLogService(this.getLogService());
				conll2SaltMapper.map(this.documentResourceTable.get(sElementId),(SDocument)sElementId.getSIdentifiableElement());
			}
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
	
	/**
	 * This method is called by method start() of super class PepperModule. If you do not implement
	 * this method, it will call start(sElementId), for all super corpora in current SaltProject. The
	 * sElementId refers to one of the super corpora. 
	 */
	@Override
	public void end() throws PepperModuleException
	{
		//TODO /9/: implement this method when necessary 
		super.end();
	}
	
//================================ start: methods used by OSGi
	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets activated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void activate(ComponentContext componentContext) 
	{
		this.setSymbolicName(componentContext.getBundleContext().getBundle().getSymbolicName());
		{//just for logging: to say, that the current module has been activated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is activated...");
		}//just for logging: to say, that the current module has been activated
	}

	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets deactivated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void deactivate(ComponentContext componentContext) 
	{
		{//just for logging: to say, that the current module has been deactivated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is deactivated...");
		}	
	}
//================================ end: methods used by OSGi
}
