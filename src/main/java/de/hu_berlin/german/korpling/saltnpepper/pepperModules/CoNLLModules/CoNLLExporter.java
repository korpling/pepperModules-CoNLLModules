package de.hu_berlin.german.korpling.saltnpepper.pepperModules.CoNLLModules;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.FormatDefinition;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperInterfaceFactory;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This is a sample PepperExporter, which can be used for creating individual Exporters for the 
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
//TODO /1/: change the name of the component, for example use the format name and the ending Exporter (FORMATExporterComponent)
@Component(name="SampleExporterComponent", factory="PepperExporterComponentFactory")
@Service(value=PepperExporter.class)
public class CoNLLExporter extends PepperExporterImpl implements PepperExporter
{
	public CoNLLExporter()
	{
		super();
		
		{//setting name of module
			//TODO /2/: change the name of the module, for example use the format name and the ending Exporter (FORMATExporter)
			this.name= "SampleExporter";
		}//setting name of module
		
		{//for testing the symbolic name has to be set without osgi
			if (	(this.getSymbolicName()==  null) ||
					(this.getSymbolicName().equalsIgnoreCase("")))
				//TODO /3/: change the symbolic name to ${project.groupId}.${project.artifactId} (see pom.xml) 
				this.setSymbolicName("de.hu_berlin.german.korpling.saltnpepper.pepperModules.SampleModules");
		}//for testing the symbolic name has to be set without osgi
		
		{//set list of formats supported by this module
			this.supportedFormats= new BasicEList<FormatDefinition>();
			FormatDefinition formatDef= PepperInterfaceFactory.eINSTANCE.createFormatDefinition();
			//TODO /4/:change "sample" with format name
			formatDef.setFormatName("sample");
			//TODO /5/:change 1.0 with format version to support
			formatDef.setFormatVersion("1.0");
			this.supportedFormats.add(formatDef);
		}
		
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
	 * PepperExporter of this method:
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
	 * This method is called by method start() of superclass PepperExporter, if the method was not overriden
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
