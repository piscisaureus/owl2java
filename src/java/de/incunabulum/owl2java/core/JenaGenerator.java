package de.incunabulum.owl2java.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.incunabulum.owl2java.core.generator.ModelPreparation;
import de.incunabulum.owl2java.core.generator.OwlReader;
import de.incunabulum.owl2java.core.generator.jena.JenaWriter;
import de.incunabulum.owl2java.core.model.jmodel.JModel;

public class JenaGenerator {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JenaGenerator.class);
	
	// config options
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String testClassName = "Test";

	private boolean createTestClass = true;

	String toolsPackage = "tools";

	Date startAll;

	Date startJModedlCreation;

	Date startPreparation;

	Date startToDisk;

	Date stopAll;

	OntModel model;

	private Map<String, String> mappings = new HashMap<String, String>();

	JModel jmodel;

	protected boolean reasignDomainlessProperties = true;

	protected boolean enableCodeFormatting = true;

	protected Properties codeFormatterOptions = null;

	public void generate(String uri, String altLocation, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlDocMgr.addAltEntry(uri, altLocation);
		addMappings(owlDocMgr, uri);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	public void generate(String uri, String baseDir, String basePackage) {
		startAll = new Date();

		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		addMappings(owlDocMgr, uri);
		owlModel.read(uri);

		generate(owlModel, baseDir, basePackage);
	}

	public void generate(OntModel model, String baseDir, String basePackage) {
		// Note: model needs to be OWL_DL_MEM !!! 
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
		mWriter.setEnableCodeFormatting(enableCodeFormatting);
		mWriter.setCodeFormatterOptions(codeFormatterOptions);
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

	public boolean addMappings(String uri, String altLocation) {
		if (uri == null || altLocation == null) {
			return false;
		}
		if (mappings == null) {
			mappings = new HashMap<String, String>();
		}
		if (mappings.containsKey(uri)) {
			return false;
		}
		mappings.put(uri, altLocation);
		return true;
	}

	protected void addMappings(OntDocumentManager owlDocMgr, String top_uri) {
		if (mappings != null && !mappings.isEmpty()) {
			int lastSlash = top_uri == null ? 0 : top_uri.lastIndexOf('/');
			String baseuri = lastSlash >= 0 ? top_uri.substring(0, lastSlash) : top_uri;
			for (int i = 0; i < mappings.size(); i++) {
				Iterator<String> itr = mappings.keySet().iterator();
				while (itr.hasNext()) {
					String uri = itr.next();
					String altloc = baseuri + "/" + mappings.get(uri);
					owlDocMgr.addAltEntry(uri, altloc);
					System.out.println("uri=" + uri + "; altloc=" + altloc);
				}
			}
		}
	}

	public String getStatistics() {
		String str = new String();
		if (this.jmodel != null)
			str += jmodel.getStatistics() + "\n";
		str += "Total Time: " + (stopAll.getTime() - startAll.getTime()) + " ms\n";
		str += "Reading the Owl Model: " + (startJModedlCreation.getTime() - startAll.getTime()) + " ms\n";
		str += "Creating the JModel: " + (startPreparation.getTime() - startJModedlCreation.getTime()) + " ms\n";
		str += "Preparing the JModel: " + (startToDisk.getTime() - startPreparation.getTime()) + " ms\n";
		str += "Writting the classes: " + (stopAll.getTime() - startToDisk.getTime()) + " ms\n";
	
		return str;
	}

	public String getJModelReport() {
		if (this.jmodel != null)
			return jmodel.getJModelReport();
		return new String();
	}

	public OntModel getOntModel() {
		return model;
	}

	public JModel getJModel() {
		return jmodel;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}

	public void setEnableCodeFormating(boolean enableCodeFormatting) {
		this.enableCodeFormatting = enableCodeFormatting;
	}

	public void setCodeFormatterOptions(Properties codeFormatterOptions) {
		this.codeFormatterOptions = codeFormatterOptions;
	}

}
