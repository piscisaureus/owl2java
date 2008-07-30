package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;

import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.model.NamespaceUtils;
import de.incunabulum.jakuzi.model.ResourceError;
import de.incunabulum.jakuzi.utils.IReporting;
import de.incunabulum.jakuzi.utils.IStatistics;
import de.incunabulum.jakuzi.utils.StringUtils;

public class JModel implements IReporting, IStatistics {

	private static Log log = LogFactory.getLog(JModel.class);

	private static String THINGNAME = "Thing";
	public static String BASEPREFIX = "";

	private Map<String, String> ns2prefix = new HashMap<String, String>();
	private Map<String, String> ns2javaPkgName = new HashMap<String, String>();
	private Map<String, JPackage> pkgName2Package = new HashMap<String, JPackage>();
	private Map<String, JClass> uri2class = new HashMap<String, JClass>();
	private Map<String, JProperty> uri2property = new HashMap<String, JProperty>();
	private JInheritanceGraph<JProperty, DefaultEdge> propertyGraph;
	private JInheritanceGraph<JClass, DefaultEdge> classGraph;
	private List<ResourceError> ontResourceErrors = new ArrayList<ResourceError>();

	private String baseThingUri;
	private OntModel ontModel;

	public JModel() {
		propertyGraph = new JInheritanceGraph<JProperty, DefaultEdge>(DefaultEdge.class);
		classGraph = new JInheritanceGraph<JClass, DefaultEdge>(DefaultEdge.class);
	}
	public void addOntResourceError(ResourceError error) {
		ontResourceErrors.add(error);
	}

	public void addPackage(String uri, String pkgName) {
		ns2javaPkgName.put(uri, pkgName);
	}
	
	public boolean isBaseThing(JClass cls) {
		String clsUri = cls.getMapUri();
		String baseUri = getBaseThingUri();
		return (clsUri.equals(baseUri));
	}


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


	@SuppressWarnings("unchecked")
	public JClass getAnonymousJClass(List<JClass> operandClasses) {
		// loop over all classes in the model
		Iterator<String> clsUris = uri2class.keySet().iterator();
		while (clsUris.hasNext()) {
			String uri = (String) clsUris.next();
			JClass cls = uri2class.get(uri);

			// if class is not anonymous > continue
			if (!cls.isAnonymous())
				continue;

			// if both anonymous classes have identical super classes
			// > identical > return it
			List<JClass> superClasses = cls.listDirectSuperClasses();
			// different size > super classes are not identical
			if (superClasses.size() != operandClasses.size())
				continue;

			// difference of list is empty > lists are identical > return
			if (ListUtils.subtract(superClasses, operandClasses).isEmpty())
				return cls;
		}

		return null;

	}

	@SuppressWarnings("unchecked")
	public JClass getAnonymousJClassUnion(UnionClass unionClass) {
		// find all super classes (operands) of the anonymous class
		List<JClass> operandClasses = new ArrayList<JClass>();
		Iterator operandIt = unionClass.listOperands();
		while (operandIt.hasNext()) {
			OntClass cls = (OntClass) operandIt.next();
			String clsUri = cls.getURI();
			if (uri2class.containsKey(clsUri)) {
				operandClasses.add(uri2class.get(clsUri));
			}
		}
		return getAnonymousJClass(operandClasses);
	}

	@SuppressWarnings("unchecked")
	public JClass getAnonymousJClassIntersection(IntersectionClass intersectionClass) {
		// find all super classes (operands) of the anonymous class
		List<JClass> operandClasses = new ArrayList<JClass>();
		Iterator operandIt = intersectionClass.listOperands();
		while (operandIt.hasNext()) {
			OntClass cls = (OntClass) operandIt.next();
			String clsUri = cls.getURI();
			if (uri2class.containsKey(clsUri)) {
				operandClasses.add(uri2class.get(clsUri));
			}
		}
		return getAnonymousJClass(operandClasses);
	}

	public boolean hasPackage(String packageName) {
		return this.pkgName2Package.containsKey(packageName);
	}

	public static String getBaseThingName() {
		return THINGNAME;
	}

	public List<String> listNamespaces() {
		List<String> nss = new ArrayList<String>();
		Iterator<String> it = ns2prefix.keySet().iterator();
		while (it.hasNext()) {
			String nsUri = (String) it.next();
			nss.add(nsUri);
		}
		return nss;
	}

	public List<JPackage> listPackages() {
		List<JPackage> pkgs = new ArrayList<JPackage>();
		Iterator<String> it = pkgName2Package.keySet().iterator();
		while (it.hasNext()) {
			String pkgName = (String) it.next();
			JPackage pkg = pkgName2Package.get(pkgName);
			pkgs.add(pkg);
		}
		return pkgs;
	}

	@SuppressWarnings("unchecked")
	public List<String> listImportURIs() {
		Set<String> importsSet = new HashSet<String>();
		Iterator<String> nsIt = ns2prefix.keySet().iterator();
		while (nsIt.hasNext()) {
			String ns = (String) nsIt.next();
			if (!NamespaceUtils.defaultNs2UriMapping.containsKey(ns)) {
				// Strip trailing namespace #
				ns = ns.substring(0, ns.length() - 1);
				importsSet.add(ns);
			}
		}
		return new ArrayList<String>(importsSet);
	}

	@Override
	public String getReport() {
		String report = new String();
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
		for (ResourceError res : ontResourceErrors) {
			report += res.getReport() + "\n";
		}
		return report;
	}

	public String getPrefixFromImport(String imp) {
		return getPrefix(imp + "#");
	}

	public String getPrefix(String namespace) {
		String prefix = ns2prefix.get(namespace);
		if (prefix == null)
			return "";
		return prefix;
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

	public void addNamespacePrefix(String ns, String prefix) {
		ns2prefix.put(ns, prefix);
	}

	public String getBaseNamespace() {
		return getNamespace(BASEPREFIX);
	}

	public static String getBasePrefix() {
		return BASEPREFIX;
	}

	public boolean hasJClass(String uri) {
		return this.uri2class.containsKey(uri);
	}

	public JClass getJClass(String uri) {
		return this.uri2class.get(uri);
	}

	public List<JClass> listJClasses() {
		List<JClass> classes = new ArrayList<JClass>();
		Iterator<String> it = uri2class.keySet().iterator();
		while (it.hasNext()) {
			String uri = (String) it.next();
			JClass cls = uri2class.get(uri);
			classes.add(cls);
		}
		return classes;
	}

	public List<JProperty> listJProperties() {
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
		JClass cls = new JClass(this, clsName, clsUri);
		uri2class.put(clsUri, cls);

		getJPackage(pkgName).addClass(cls);
		log.debug(LogUtils.toLogName(cls) + ": Creating class " + cls.getName() + " in package "
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

	public boolean hasNamespace(String uri) {
		return ns2prefix.containsKey(uri);
	}

	public JProperty getJProperty(String uri) {
		return this.uri2property.get(uri);
	}

	public void createJProperty(OntProperty ontProp) {
		String propName = NamingUtils.getPropertyName(ontProp);
		JProperty p = new JProperty(this, propName, ontProp.getURI());
		uri2property.put(ontProp.getURI(), p);

		log.debug(LogUtils.toLogName(ontProp) + ": Creating property " + p.getName());
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
		String ret = new String();
		ret += StringUtils.toHeader("Statistics");
		ret += "Classes: " + uri2class.size() + "\n";
		ret += "Properties: " + uri2property.size() + "\n";

		int restrictionCount = 0;
		Iterator<String> clsIt = uri2class.keySet().iterator();
		while (clsIt.hasNext()) {
			String clsUri = (String) clsIt.next();
			JClass cls = uri2class.get(clsUri);
			restrictionCount += cls.listRestrictionContainers().size();
		}
		ret += "Restrictions: " + restrictionCount + "\n";
		ret += "Errors: " + ontResourceErrors.size();
		return ret;
	}

	public void setOntModel(OntModel ontModel) {
		this.ontModel = ontModel;
	}

	public OntModel getOntModel() {
		return ontModel;
	}

	public String getBaseThingUri() {
		return baseThingUri;
	}

	public void setBaseThingUri(String baseThingUri) {
		this.baseThingUri = baseThingUri;
	}
	public JInheritanceGraph<JProperty, DefaultEdge> getPropertyGraph() {
		return propertyGraph;
	}
	public JInheritanceGraph<JClass, DefaultEdge> getClassGraph() {
		return classGraph;
	}
	public Map<String, JProperty> getUri2property() {
		return uri2property;
	}
	public JClass getBaseThing() {
		return uri2class.get(baseThingUri);
	}

}
