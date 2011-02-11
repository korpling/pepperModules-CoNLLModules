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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperManipulator;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This is a sample PepperManipulator, which can be used for creating individual manipulators for the 
 * Pepper Framework. Therefor you have to take a look to todo�s and adapt the code.
 * 
 * <ul>
 *  <li>the salt model to fill, manipulate or export can be accessed via SaltProject::this.getSaltProject()</li>
 * 	<li>special parameters given by Pepper workflow can be accessed via URI::this.getSpecialParams()</li>
 *  <li>a place to store temprorary datas for processing can be accessed via URI::this.getTemproraries()</li>
 *  <li>a place where resources of this bundle are, can be accessed via URL::this.getResources()</li>
 *  <li>a logService can be accessed via LogService::this.getLogService()</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 *
 */
//TODO /1/: change the name of the component, for example use the format name and the ending manipulator (FORMATManipulatorComponent)
@Component(name="SampleManipulatorComponent", factory="PepperManipulatorComponentFactory")
@Service(value=PepperManipulator.class)
public class CoNLLManipulator extends PepperManipulatorImpl 
{
	public CoNLLManipulator()
	{
		super();
		
		{//setting name of module
			//TODO /2/: change the name of the module, for example use the format name and the ending Manipulator (FORMATManipulator)
			this.name= "SampleManipulator";
		}//setting name of module
		
		{//for testing the symbolic name has to be set without osgi
			if (	(this.getSymbolicName()==  null) ||
					(this.getSymbolicName().equalsIgnoreCase("")))
				//TODO /3/: change the symbolic name to ${project.groupId}.${project.artifactId} (see pom.xml) 
				this.setSymbolicName("de.hu_berlin.german.korpling.saltnpepper.pepperModules.SampleModules");
		}//for testing the symbolic name has to be set without osgi
		
		{//just for logging: to say, that the current module has been loaded
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
		}//just for logging: to say, that the current module has been loaded
	}
	
	/**
	 * If this method is not really implemented, it will call the Method start(sElementId) for every document 
	 * and corpus, which shall be processed. If it is not really implemented, the method-call will be serial and
	 * and not parallel. To implement a parallelization override this method and take care, that your code is
	 * thread-safe. 
	 * For getting an impression how to implement this method, here is a snipplet of super class 
	 * PepperManipulator of this method:
	 * <br/>
	 * boolean isStart= true;
	 * SElementId sElementId= null;
	 * while ((isStart) || (sElementId!= null))
	 * {	
	 *  isStart= false;
	 *		sElementId= this.getPepperModuleController().get();
	 *		if (sElementId== null)
	 *			break;
	 *		
	 *		//call for using push-method
	 *		this.start(sElementId);
	 *		
	 *		if (this.returningMode== RETURNING_MODE.PUT)
	 *		{	
	 *			this.getPepperModuleController().put(sElementId);
	 *		}
	 *		else if (this.returningMode== RETURNING_MODE.FINISH)
	 *		{	
	 *			this.getPepperModuleController().finish(sElementId);
	 *		}
	 *		else 
	 *			throw new PepperModuleException("An error occurs in this module (name: "+this.getName()+"). The returningMode isn�t correctly set (it�s "+this.getReturningMode()+"). Please contact module supplier.");
	 *		this.end();
	 *	}
	 * After all documents were processed this method of super class will call the method end().
	 */
	@Override
	public void start() throws PepperModuleException
	{
		//TODO /7/: delete this, if you want to parallelize processing 
		super.start();
	}
	
	/**
	 * This method is called by method start() of superclass PepperManipulator, if the method was not overriden
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
			//TODO /8/: create your own mapping
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
//================================ start: methods used by OSGi
}
