package de.incunabulum.owl4java.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;

import de.incunabulum.owl4java.jmodel.utils.NamingUtils;
import de.incunabulum.owl4java.utils.StringUtils;

public class JClass extends JMapped {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JClass.class);

	@SuppressWarnings("unused")
	private JModel jmodel;
	private OntClass ontClass;

	protected boolean anonymous = false; 
	protected JPackage pkg;
	protected List<JClass> subClasses = new ArrayList<JClass>();
	protected List<JClass> superClasses = new ArrayList<JClass>();
	protected List<JProperty> domainProps = new ArrayList<JProperty>();
	protected List<JClassRestriction> restrictions = new ArrayList<JClassRestriction>();

	public JClass(JModel model, String name, String mapUri) {
		super(name, mapUri);
		this.jmodel = model;
	}

	public void addRestriction(JClassRestriction res) {
		if (!restrictions.contains(res))
			this.restrictions.add(res);
	}
	
	public boolean hasRestriction(JProperty property) {
		for (JClassRestriction clsRestriction : restrictions) {
			if (clsRestriction.getOnProp() == property)
				return true;
		}
		return false;
	}
	
	public boolean isAnonymous() {
		return anonymous;
	}
	
	
	public void setAnonymous(boolean anon) {
		log.debug(NamingUtils.toLogName(this) + ": Marking as anonymous class");
		anonymous = anon;
	}
	
	public boolean hasModifiesRestriction(JProperty property) {
		boolean propIsFunctional = property.isFunctional;
		List<JClassRestriction> restrictions = listJRestrictions(property);
		for (JClassRestriction clsRestriction : restrictions) {
			if (!propIsFunctional && clsRestriction.maxCardinality == 1)
				return true;
			if (clsRestriction.hasAllValuesRestriction())
				return true;
		}
		return false;
	}

	public List<JClassRestriction> listJRestrictions(JProperty property) {
		List<JClassRestriction> r = new ArrayList<JClassRestriction>();
		for (JClassRestriction clsRestriction : restrictions) {
			if (clsRestriction.getOnProp() == property)
				r.add(clsRestriction);
		}
		return r;
	}
	public void addDomainProperty(JProperty prop) {
		if (!domainProps.contains(prop))
			domainProps.add(prop);
	}

	public boolean hasDomainProperty(String uri) {
		for (JProperty domainProp : domainProps) {
			if (domainProp.getMapUri() == uri)
				return true;
		}
		return false;
	}

	public boolean hasSuperClasses() {
		return (!this.superClasses.isEmpty());
	}

	public boolean isRootClass() {
		return this.superClasses.isEmpty();
	}

	public void addSubClass(JClass cls) {
		if (!subClasses.contains(cls))
			subClasses.add(cls);
		if (!cls.superClasses.contains(this))
			cls.superClasses.add(this);
	}

	public void addSuperClass(JClass cls) {
		if (!superClasses.contains(cls))
			superClasses.add(cls);
		if (!cls.subClasses.contains(this))
			cls.subClasses.add(this);
	}

	public JPackage getPackage() {
		return pkg;
	}

	public String getJavaPackageName() {
		return pkg.getPackageName();
	}

	public void setPackage(JPackage pkg) {
		this.pkg = pkg;
		pkg.classes.add(this);
	}

	@Override
	public String getReport() {
		String report = new String();
		report += StringUtils.toSubHeader("Class " + getJavaClassFullName());
		report += StringUtils.indentText("Parent Classes\n");
		for (JClass parent : superClasses) {
			report += StringUtils.indentText(parent.getJavaClassFullName() + "\n", 2);
		}

		report += StringUtils.indentText("Child Classes\n");
		for (JClass child : subClasses) {
			report += StringUtils.indentText(child.getJavaClassFullName() + "\n", 2);
		}

		report += StringUtils.indentText("Domain Properties\n");
		for (JProperty prop : domainProps) {
			report += StringUtils.indentText(prop.getJavaName() + "\n", 2);
			report += prop.getReport();
		}

		report += StringUtils.indentText("Class Restrictions\n");
		for (JClassRestriction restriction : restrictions) {
			report += restriction.getReport();
		}

		return report;
	}

	public String getJavaInterfaceName() {
		if (this.ontClass != null)
			return NamingUtils.getJavaInterfaceName(this.ontClass);

		// no OntClass given (base.thing e. g. )
		String javaName = NamingUtils.getJavaInterfaceName(getName(), JModel.getBasePrefix());
		return javaName;
	}

	public String getJavaInterfaceFullName() {
		return NamingUtils.getJavaFullName(pkg, getJavaInterfaceName());
	}

	public String getJavaClassName() {
		if (this.ontClass != null)
			return NamingUtils.getJavaClassName(this.ontClass);

		// no OntClass given (base.thing e. g. )
		return NamingUtils.getJavaClassName(getName(), JModel.getBasePrefix());
	}

	public String getJavaClassFullName() {
		return NamingUtils.getJavaFullName(pkg, getJavaClassName());

	}

	public OntClass getOntClass() {
		return ontClass;
	}

	public void setOntClass(OntClass ontClass) {
		this.ontClass = ontClass;
	}

	public List<JClass> listSubClasses() {
		return subClasses;
	}

	public List<JProperty> listDomainProperties() {
		return domainProps;
	}

	public Map<String, JProperty> listDomainPropertiesAsMap(boolean recursive) {
		Map<String, JProperty> uri2prop = new HashMap<String, JProperty>();

		// add current props, if not present
		for (JProperty p : domainProps) {
			if (!uri2prop.containsKey(p.getMapUri()))
				uri2prop.put(p.getMapUri(), p);
		}

		if (recursive) {
			for (JClass sCls : superClasses) {
				uri2prop.putAll(sCls.listDomainPropertiesAsMap(recursive));
			}
		}

		return uri2prop;
	}

	public List<JProperty> listDomainProperties(boolean recursive) {
		if (recursive == false)
			return domainProps;

		Map<String, JProperty> uri2prop = listDomainPropertiesAsMap(true);

		List<JProperty> props = new ArrayList<JProperty>();
		props.addAll(uri2prop.values());
		return props;
	}

	public List<JClass> listSuperClasses() {
		return superClasses;
	}

	public Iterator<JClass> getSuperClassesIterator() {
		return superClasses.iterator();
	}

	

}
