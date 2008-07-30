package de.incunabulum.jakuzi.generator;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.utils.IReporting;
import de.incunabulum.jakuzi.utils.IStatistics;

public class Generator implements IStatistics, IReporting {
	
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Generator.class);

	private Date startAll;
	private Date startJModedlCreation;
	private Date startPreparation;
	private Date startToDisk;
	private Date stopAll;
	private OntModel model;
	private JModel jmodel;

	// config options
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String toolsPackage = "jakuzi";
	private String testClassName = "JakuziTest";
	
	private boolean reasignDomainlessProperties = true;
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

	public void generate(OntModel model,String baseDir, String basePackage) {
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
		JavaWriter mWriter = new JavaWriter();
		mWriter.setVocabularyName(vocabularyName);
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setFactoryName(factoryName);
		mWriter.setCreateTestClass(createTestClass);
		mWriter.setTestClassName(testClassName);
		mWriter.generate(this.jmodel, baseDir, basePackage);

		stopAll = new Date();
	}

	@Override
	public String getStatistics() {
		String str = new String();
		if (this.jmodel != null)
			str += jmodel.getStatistics() +"\n";
		str += "Total Time: " + (stopAll.getTime() - startAll.getTime()) + " ms\n";
		str += "Reading the Owl Model: " + (startJModedlCreation.getTime() - startAll.getTime()) + " ms\n";
		str += "Creating the JModel: " + (startPreparation.getTime() - startJModedlCreation.getTime()) + " ms\n";
		str += "Preparing the JModel: " + (startToDisk.getTime() - startPreparation.getTime()) + " ms\n";
		str += "Writting the classes: " + (stopAll.getTime() - startToDisk.getTime()) + " ms\n";

		return str;
	}

	@Override
	public String getReport() {
		if (this.jmodel != null)
			return jmodel.getReport();
		return new String();
	}

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public OntModel getModel() {
		return model;
	}

	public void setCreateTestClass(boolean createTestClass) {
		this.createTestClass = createTestClass;
	}

	public void setTestcaseName(String testcaseName) {
		this.testClassName = testcaseName;
	}

	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}

}
