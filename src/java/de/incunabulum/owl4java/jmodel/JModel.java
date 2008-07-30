package de.incunabulum.owl4java.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;

import de.incunabulum.owl4java.jmodel.utils.NamingUtils;
import de.incunabulum.owl4java.utils.IReporting;
import de.incunabulum.owl4java.utils.IStatistics;
import de.incunabulum.owl4java.utils.StringUtils;

public class JModel implements IReporting, IStatistics {

	private static Log log = LogFactory.getLog(JModel.class);

	public static String THINGNAME = "Thing";

	public Map<String, String> ns2prefix = new HashMap<String, String>();
	public Map<String, String> ns2javaPkgName = new HashMap<String, String>();
	public Map<String, JPackage> pkgName2Package = new HashMap<String, JPackage>();
	public Map<String, JClass> uri2class = new HashMap<String, JClass>();
	public Map<String, JProperty> uri2property = new HashMap<String, JProperty>();

	public List<OntResource> ontResourceErrors = new ArrayList<OntResource>();
	public String baseThingUri;

	public void addPackage(String pkgName, JPackage pkg) {
		if (pkgName2Package.containsKey(pkgName)) {
			log.warn("Package exists: " + pkgName);
			return;
		}
		this.pkgName2Package.put(pkgName, pkg);
	}

	public JPackage getJPackage(String packageName) {
		return this.pkgName2Package.get(packageName);
	}

	public boolean hasPackage(String packageName) {
		return this.pkgName2Package.containsKey(packageName);
	}

	@Override
	public String getReport() {
		String report = "";
		report += StringUtils.toHeader("JModel report");

		report += StringUtils.toSubHeader("Owl Namespaces and Prefixes");
		Iterator<String> it = ns2prefix.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			report += StringUtils.indentText(key + ": " + ns2prefix.get(key) + "\n");
		}

		report += StringUtils.toSubHeader("Owl Namespaces and Java Packages ");
		it = ns2javaPkgName.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			report += StringUtils.indentText(key + ": " + ns2prefix.get(key) + "\n");
		}

		report += StringUtils.toHeader("Classes");
		it = uri2class.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			JClass cls = uri2class.get(key);
			report += cls.getReport();
		}

		report += StringUtils.toHeader("Errors");
		for (OntResource res : ontResourceErrors) {
			report += res.toString() + "\n";
		}
		return report;
	}

	public String getNamespace(String prefix) {
		Iterator<String> it = ns2prefix.keySet().iterator();
		while (it.hasNext()) {
			String ns = (String) it.next();
			String value = ns2prefix.get(ns);
			if (value == prefix)
				return ns;
		}
		return null;
	}

	public boolean hasJClass(String uri) {
		return this.uri2class.containsKey(uri);
	}

	public JClass getJClass(String uri) {
		return this.uri2class.get(uri);
	}

	public List<JClass> getJClasses() {
		List<JClass> classes = new ArrayList<JClass>();
		Iterator<String> it = uri2class.keySet().iterator();
		while (it.hasNext()) {
			String uri = (String) it.next();
			JClass cls = uri2class.get(uri);
			classes.add(cls);
		}
		return classes;
	}

	public List<JProperty> getJProperties() {
		List<JProperty> props = new ArrayList<JProperty>();
		Iterator<String> it = uri2property.keySet().iterator();
		while (it.hasNext()) {
			String uri = (String) it.next();
			JProperty prop = uri2property.get(uri);
			props.add(prop);
		}
		return props;
	}

	public void createJClass(String clsName, String clsUri, String pkgName) {
		JClass cls = new JClass(clsName, clsUri);
		uri2class.put(clsUri, cls);

		getJPackage(pkgName).addClass(cls);
		log.debug(NamingUtils.toLogName(cls) + ": Creating class " + cls.getName() + " in package "
				+ cls.getJavaPackageName());
	}

	public void createJClass(OntClass ontClass, String basePackage) {
		// class already exists > return it
		if (uri2class.containsKey(ontClass.getURI())) {
			log.warn("Class exists: " + ontClass.getURI());
		}

		// create an class for the class, add it to our mapping and
		// also add it to the relevant package
		String clsName = ontClass.getLocalName();
		String name = NamingUtils.getValidJavaName(clsName);
		String uri = ontClass.getURI();
		String ns = ontClass.getNameSpace();
		String pkgName = ns2javaPkgName.get(ns);

		// make sure that we have a valid package name; no class for
		// owl:Thing... created
		createJClass(name, uri, pkgName);

	}

	public boolean hasJProperty(String uri) {
		return this.uri2property.containsKey(uri);
	}

	public JProperty getJProperty(String uri) {
		return this.uri2property.get(uri);
	}

	public void createJProperty(OntProperty ontProp) {
		String propName = NamingUtils.getPropertyName(ontProp);
		JProperty p = new JProperty(propName, ontProp.getURI());
		uri2property.put(ontProp.getURI(), p);

		log.debug(NamingUtils.toLogName(ontProp) + ": Creating property " + p.getName());
	}

	public String createNewPrefix() {
		String prefixBase = "p";
		int id = 1;
		String prefix = prefixBase + id;
		while (ns2prefix.containsValue(prefix)) {
			id++;
			prefix = prefixBase + id;
		}
		return prefix;
	}

	@Override
	public String getStatistics() {
		String ret = "";
		ret += StringUtils.toHeader("Statistics");
		ret += "Classes: " + uri2class.size() + "\n";
		ret += "Properties: " + uri2property.size() + "\n";

		int restrictionCount = 0;
		Iterator<String> clsIt = uri2class.keySet().iterator();
		while (clsIt.hasNext()) {
			String clsUri = (String) clsIt.next();
			JClass cls = uri2class.get(clsUri);
			restrictionCount += cls.restrictions.size();
		}
		ret += "Restrictions: " + restrictionCount;
		return ret;
	}

}
