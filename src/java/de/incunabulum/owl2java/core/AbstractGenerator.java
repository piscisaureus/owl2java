package de.incunabulum.owl2java.core;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.utils.IReporting;
import de.incunabulum.owl2java.core.utils.IStatistics;

public abstract class AbstractGenerator implements IStatistics, IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(AbstractGenerator.class);

	String toolsPackage = "owl2java";

	Date startAll;
	Date startJModedlCreation;
	Date startPreparation;
	Date startToDisk;
	Date stopAll;
	OntModel model;
	JModel jmodel;

	protected boolean reasignDomainlessProperties = true;

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
	
	public abstract void generate(String uri, String altLocation, String baseDir, String basePackage);
	public abstract void generate(String uri, String baseDir, String basePackage);
	public abstract void generate(OntModel model, String baseDir, String basePackage);

	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}
	
	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}


}
