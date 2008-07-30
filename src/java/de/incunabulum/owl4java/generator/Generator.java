package de.incunabulum.owl4java.generator;



import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import de.incunabulum.owl4java.jmodel.JModel;
import de.incunabulum.owl4java.utils.IReporting;
import de.incunabulum.owl4java.utils.IStatistics;

public class Generator implements IStatistics, IReporting{

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Generator.class);
	
	private JModel jmodel;
	
	// config options
	private String anonClassBase = "AnonClass";
	private String vocabularyName = "Vocabulary";
	private String factoryName = "Factory";
	private String toolsPackage = "tools";

	public void generate(OntModel model, String baseDir, String basePackage) {
		Date startDate = new Date();
		
		// analyze the model
		Owl2JModelReader mAnalyzer = new Owl2JModelReader();
		mAnalyzer.setAnonClassBase(anonClassBase);
		mAnalyzer.setBasePackage(basePackage);
		this.jmodel = mAnalyzer.generateJModel(model);

		// report
		String report = jmodel.getReport();
		log.error(report);
		
		report = jmodel.getStatistics();
		log.error(report);

		// write the model
		JModel2JavaWriter mWriter = new JModel2JavaWriter();
		mWriter.setVocabularyName(vocabularyName);
		mWriter.setToolsPackage(toolsPackage);
		mWriter.setFactoryName(factoryName);
		mWriter.generate(jmodel, baseDir, basePackage);
		
		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Generator finished (" + elapse + " ms)");
		

	}

	@Override
	public String getStatistics() {
		if (this.jmodel != null)
			return jmodel.getStatistics();
		return "";
	}

	@Override
	public String getReport() {
		if (this.jmodel != null)
			return jmodel.getReport();
		return "";
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

}
