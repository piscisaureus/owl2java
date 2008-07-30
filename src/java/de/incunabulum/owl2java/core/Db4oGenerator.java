package de.incunabulum.owl2java.core;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.incunabulum.owl2java.core.generator.ModelPreparation;
import de.incunabulum.owl2java.core.generator.OwlReader;
import de.incunabulum.owl2java.core.generator.db4o.Db4oWriter;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public class Db4oGenerator extends AbstractGenerator {
	
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Db4oGenerator.class);

	
	public static final int ClassBasedGeneration = 1;
	public static final int InterfaceBasedGeneration = 2;
	
	private int	generationType = ClassBasedGeneration;
	private boolean generateMergeCode = true;
	private String instanceClassName = "MergeCode";
	
	public Db4oGenerator() {
		log.info("Adding 'uriInstance', 'uriClass', 'activator' to reserved java keywords");
		JavaUtils.reservedWords.add("uriInstance");
		JavaUtils.reservedWords.add("uriClass");
		JavaUtils.reservedWords.add("activator");
	}

	@Override
	public void generate(String uri, String altLocation, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlDocMgr.addAltEntry(uri, altLocation);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	@Override
	public void generate(String uri, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	@Override
	public void generate(OntModel model, String baseDir, String basePackage) {
		this.model = model;

		startJModedlCreation = new Date();
		if (startAll == null) {
			startAll = startJModedlCreation;
		}
		// read the model
		OwlReader mReader = new OwlReader();
		mReader.setBasePackage(basePackage);
		this.jmodel = mReader.generateJModel(model);

		// prepare the model
		startPreparation = new Date();
		ModelPreparation mPrep = new ModelPreparation();
		mPrep.setReasignDomainlessProperties(reasignDomainlessProperties);
		this.jmodel = mPrep.prepareModel(jmodel);
		
		// test the model
		boolean modelOk  = testModel();
		log.info("--> Model test passed: " + modelOk);
		
		if (!modelOk) {
			log.error("ABORTING ABORTING ABORTING ABORTING ABORTING ABORTING");
			startToDisk = new Date();
			stopAll = new Date();
			return;
		}
		
		// write the model
		startToDisk = new Date();
		Db4oWriter mWriter = new Db4oWriter();
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setInstanceClassName(instanceClassName);
		mWriter.setGenerateMergeCode(generateMergeCode);
		mWriter.setGenerationType(generationType);
		mWriter.generate(this.jmodel, baseDir, basePackage);
		stopAll = new Date();
	}

	protected boolean testModel() {
		boolean modelOk = true;
		if (generationType == Db4oGenerator.ClassBasedGeneration) {
			// class based generation does not support multiple inheritance etc.
			log.info("");
			log.info("Testing model");
			for (JClass cls : jmodel.listJClasses()) {
				if (cls.listDirectSuperClasses().size() > 1) {
					log.error(LogUtils.toLogName(cls) + ": multipe super classes present. Not supported!");
					modelOk = false;
				}
			}
		}
		return modelOk;
		
		// TODO: what to test for InterfaceBasedGeneration?
	}

	public void setInstanceClassName(String instanceClassName) {
		this.instanceClassName = instanceClassName;
	}

	public void setGenerationType(int generationType) {
		this.generationType = generationType;
	}

	public void setGenerateMergeCode(boolean generateMergeCode) {
		this.generateMergeCode = generateMergeCode;
	}

}
