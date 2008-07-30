package de.incunabulum.owl4java.generator;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.incunabulum.owl4java.jmodel.JModel;
import de.incunabulum.owl4java.utils.IReporting;
import de.incunabulum.owl4java.utils.IStatistics;

public class Generator implements IStatistics, IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Generator.class);

	private Date startDate;
	private Date modelReadDate;
	private Date stopDate;
	private OntModel model;
	private JModel jmodel;

	// config options
	private String anonClassBase = "AnonClass";
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String toolsPackage = "owl4java";

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
		// analyze the model
		Owl2JModelReader mAnalyzer = new Owl2JModelReader();
		mAnalyzer.setAnonClassBase(anonClassBase);
		mAnalyzer.setBasePackage(basePackage);
		this.jmodel = mAnalyzer.generateJModel(model);

		// write the model
		JModel2JavaWriter mWriter = new JModel2JavaWriter();
		mWriter.setVocabularyName(vocabularyName);
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setFactoryName(factoryName);
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

	public void setAnonClassBase(String anonClassBase) {
		this.anonClassBase = anonClassBase;
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

}
