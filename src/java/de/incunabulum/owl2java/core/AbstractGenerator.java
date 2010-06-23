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
	private Map<String, String> mappings = new HashMap<String, String>();
	JModel jmodel;

	protected boolean reasignDomainlessProperties = true;
	
	protected boolean enableCodeFormatting = true;
	protected Properties codeFormatterOptions = null;

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

	/* by Andrew Crapo, GE */
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

	public abstract void generate(String uri, String altLocation, String baseDir, String basePackage);

	public abstract void generate(String uri, String baseDir, String basePackage);

	public abstract void generate(OntModel model, String baseDir, String basePackage);

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
