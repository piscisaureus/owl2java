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
import de.incunabulum.owl2java.core.generator.jena.JenaWriter;

public class JenaGenerator extends AbstractGenerator {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JenaGenerator.class);
	
	// config options
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String testClassName = "Owl2JavaTest";


	private boolean createTestClass = true;

	public void generate(String uri, String altLocation, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlDocMgr.addAltEntry(uri, altLocation);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	public void generate(String uri, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	public void generate(OntModel model, String baseDir, String basePackage) {
		this.model = model;

		startJModedlCreation = new Date();
		if (startAll == null) {
			startAll = startJModedlCreation;
		}
		// read the model
		OwlReader mReader = new OwlReader();
		mReader.setBasePackage(basePackage);
		mReader.addForbiddenPrefix(toolsPackage);
		this.jmodel = mReader.generateJModel(model);

		// prepare the model
		startPreparation = new Date();
		ModelPreparation mPrep = new ModelPreparation();
		mPrep.setReasignDomainlessProperties(reasignDomainlessProperties);
		this.jmodel = mPrep.prepareModel(jmodel);

		// write the model
		startToDisk = new Date();
		JenaWriter mWriter = new JenaWriter();
		mWriter.setVocabularyName(vocabularyName);
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setFactoryName(factoryName);
		mWriter.setCreateTestClass(createTestClass);
		mWriter.setTestClassName(testClassName);
		mWriter.generate(this.jmodel, baseDir, basePackage);

		stopAll = new Date();
	}

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}


	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public void setCreateTestClass(boolean createTestClass) {
		this.createTestClass = createTestClass;
	}

	public void setTestcaseName(String testcaseName) {
		this.testClassName = testcaseName;
	}

}
