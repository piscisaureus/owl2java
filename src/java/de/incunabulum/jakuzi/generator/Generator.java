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

	private Date startDate;
	private Date modelReadDate;
	private Date stopDate;
	private OntModel model;
	private JModel jmodel;

	// config options
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String toolsPackage = "jakuzi";
	private String testClassName = "JakuziTest";
	
	private boolean reasignDomainlessProperties = true;
	private boolean createTestClass = true;

	public void generate(String uri, String baseDir, String basePackage) {
		startDate = new Date();
		
		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlModel.read(uri);
		
		modelReadDate = new Date();
		
		generate(owlModel, baseDir, basePackage);
	}

	public void generate(OntModel model, String baseDir, String basePackage) {
		this.model = model;
		
		if (startDate == null) {
			startDate = new Date();
		}
		
		// read the model
		OwlReader mReader = new OwlReader();
		mReader.setBasePackage(basePackage);
		mReader.addForbiddenPrefix(toolsPackage);
		this.jmodel = mReader.generateJModel(model);

		// prepare the model
		JModelPreparation mPrep = new JModelPreparation();
		mPrep.setReasignDomainlessProperties(reasignDomainlessProperties);
		this.jmodel = mPrep.prepareModel(jmodel);
		
		// write the model
		JavaWriter mWriter = new JavaWriter();
		mWriter.setVocabularyName(vocabularyName);
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setFactoryName(factoryName);
		mWriter.setCreateTestClass(createTestClass);
		mWriter.setTestClassName(testClassName);
		mWriter.generate(jmodel, baseDir, basePackage);

		stopDate = new Date();
	}

	@Override
	public String getStatistics() {
		String str = new String();
		if (this.jmodel != null)
			str += jmodel.getStatistics() +"\n";
		str += "Total Time: " + (stopDate.getTime() - startDate.getTime()) + " ms\n";
		str += "Reading the Owl Model: " + (modelReadDate.getTime() - startDate.getTime()) + " ms\n";
		str += "Generating the classes: " + (stopDate.getTime() - modelReadDate.getTime()) + " ms\n";

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
