package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;

import com.hp.hpl.jena.ontology.OntClass;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.utils.StringUtils;

public class JClass extends JMapped {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JClass.class);

	private OntClass ontClass;
	private JModel jModel;
	private JPackage jPkg;
	private boolean anonClass = false;

	private List<JProperty> domainProperties = new ArrayList<JProperty>();
	private List<JProperty> allProperties = new ArrayList<JProperty>();
	private List<JClass> equivalentClasses = new ArrayList<JClass>();
	private Map<JProperty, JRestrictionsContainer> propertyRestrictions = new HashMap<JProperty, JRestrictionsContainer>();

	private List<JPropertyRepresentation> propertyRepresentations = new ArrayList<JPropertyRepresentation>();

	public JClass(JModel model, String name, String mapUri) {
		super(name, mapUri);
		this.jModel = model;
		this.jModel.getClassGraph().addVertex(this);
	}

	
	public void addDomainProperty(JProperty prop) {
		if (!domainProperties.contains(prop))
			domainProperties.add(prop);
		if (!prop.propertyDomain.contains(this))
			prop.propertyDomain.add(this);
		if (!propertyRestrictions.containsKey(prop)) {
			log.debug(LogUtils.toLogName(this, prop) + ": Creating RestrictionsContainer");
			JRestrictionsContainer rc = new JRestrictionsContainer(this, prop);
			propertyRestrictions.put(prop, rc);
		}
	}

	public void addEquivalentClass(JClass cls) {
		if (!equivalentClasses.contains(cls))
			equivalentClasses.add(cls);
		if (!cls.equivalentClasses.contains(this))
			cls.equivalentClasses.add(this);
	}

	public void addPropertyRepresentation(JPropertyRepresentation pr) {
		if (!propertyRepresentations.contains(pr))
			propertyRepresentations.add(pr);
	}


	public void addRestrictionsContainer(JProperty property, JRestrictionsContainer rc) {
		if (!propertyRestrictions.containsKey(property))
			propertyRestrictions.put(property, rc);
	}

	public void addSubClass(JClass cls) {
		jModel.getClassGraph().addChildVertex(this, cls);
	}

	public void addSuperClass(JClass cls) {
		jModel.getClassGraph().addParentVertex(this, cls);
	}

	public void aggegrateAll() {
		if (jModel.isBaseThing(this)) {
			log.info(LogUtils.toLogName(this) + ": No aggregation for BaseThing");
			return;
		}
		log.info(LogUtils.toLogName(this) + ": Aggregating all definitions from parent classes");
		List<JClass> parentClasses = jModel.getClassGraph().listDirectParents(this);
		aggregateAllProperties(parentClasses);
		aggregateAllRestrictionContainers(parentClasses);
		aggegrateAllRestrictionDefinitions(parentClasses);

	}
	protected void aggegrateAllRestrictionDefinitions(List<JClass> parentClasses) {
		Iterator<JProperty> it = propertyRestrictions.keySet().iterator();
		while (it.hasNext()) {
			JProperty property = (JProperty) it.next();
			JRestrictionsContainer propertyRestrictionContainer = propertyRestrictions.get(property);
			propertyRestrictionContainer.aggregateRestrictions(parentClasses);
			log.debug(LogUtils.toLogName(this) + ": Merging restriction definitions for property "
					+ LogUtils.toLogName(property));
		}
	}

	protected void aggregateAllProperties(List<JClass> parentClasses) {
		// copy local domain properties to allProperites
		allProperties.addAll(domainProperties);

		// copy parent properties to allProperties
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating domain properties of parent class "
					+ LogUtils.toLogName(cls));
			allProperties.addAll(cls.listAllProperties());
		}
	}

	protected void aggregateAllRestrictionContainers(List<JClass> parentClasses) {
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating restriction containers of parent class "
					+ LogUtils.toLogName(cls));

			// aggregate the restriction containers from parent classes
			for (JProperty prop : cls.listAllProperties()) {
				// already present -> skip (handled in aggegateRestrictionDefinitions)
				if (propertyRestrictions.containsKey(prop))
					continue;
				// copy restriction container to here
				JRestrictionsContainer rc = cls.getRestrictionsContainer(prop);
				propertyRestrictions.put(prop, rc.clone());
			}
		}
	}

	public void createPropertyRepresentations() {
		log.info(LogUtils.toLogName(this) + ": Creating property representations");		
		// loop over all properties
		for (JProperty property : allProperties) {
			log.debug(LogUtils.toLogName(this) + ": Creating property representations for " + LogUtils.toLogName(property));
			JRestrictionsContainer rc = propertyRestrictions.get(property);
			
			// for each AllValues create a single representation
			for (JAllValuesRestriction avr : rc.listAllValuesRestrictions()) {
				log.debug(LogUtils.toLogName(property) + ": Creating representation for AllValues " + LogUtils.toLogName(avr.getAllValues()));
				JPropertyRepresentation pr = new JPropertyRepresentation(property);
				pr.setAllValuesRestriction(avr);
				pr.setCardinalityRestriction(rc.getCardinalityRestriction());
				pr.setOtherRestriction(rc.getOtherRestriction());
				propertyRepresentations.add(pr);
			}
		}
	}

	public String getJavaClassFullName() {
		return NamingUtils.getJavaFullName(jPkg, getJavaClassName());

	}

	public String getJavaClassName() {
		if (this.ontClass != null)
			return NamingUtils.getJavaClassName(this.ontClass);

		// no OntClass given (base.thing e. g. )
		return NamingUtils.getJavaClassName(getName(), JModel.BASEPREFIX);
	}

	public String getJavaInterfaceFullName() {
		return NamingUtils.getJavaFullName(jPkg, getJavaInterfaceName());
	}

	public String getJavaInterfaceName() {
		if (this.ontClass != null)
			return NamingUtils.getJavaInterfaceName(this.ontClass);

		// no OntClass given (base.thing e. g. )
		String javaName = NamingUtils.getJavaInterfaceName(getName(), JModel.BASEPREFIX);
		return javaName;
	}

	public String getJavaPackageName() {
		return jPkg.getPackageName();
	}

	public JModel getJModel() {
		return jModel;
	}

	public OntClass getOntClass() {
		return ontClass;
	}

	public JPackage getPackage() {
		return jPkg;
	}

	public List<JPropertyRepresentation> getPropertyRepresentations() {
		return propertyRepresentations;
	}

	@Override
	public String getReport() {
		String report = new String();
		report += StringUtils.toSubHeader("Class " + getJavaClassFullName());

		report += StringUtils.indentText("Parent Classes\n");
		Iterator<DefaultEdge> parentIt = jModel.getClassGraph().incomingEdgesOf(this).iterator();
		while (parentIt.hasNext()) {
			DefaultEdge edge = parentIt.next();
			JClass parent = jModel.getClassGraph().getEdgeSource(edge);
			report += StringUtils.indentText(parent.getJavaClassFullName() + "\n", 2);
		}

		report += StringUtils.indentText("Child Classes\n");
		Iterator<DefaultEdge> childIt = jModel.getClassGraph().outgoingEdgesOf(this).iterator();
		while (childIt.hasNext()) {
			DefaultEdge edge = childIt.next();
			JClass parent = jModel.getClassGraph().getEdgeTarget(edge);
			report += StringUtils.indentText(parent.getJavaClassFullName() + "\n", 2);
		}

		report += StringUtils.indentText("Equivalent Classes\n");
		for (JClass child : equivalentClasses) {
			report += StringUtils.indentText(child.getJavaClassFullName() + "\n", 2);
		}

		report += StringUtils.indentText("Domain Properties\n");
		for (JProperty prop : domainProperties) {
			report += StringUtils.indentText(prop.getJavaName() + "\n", 2);
			report += prop.getReport();
		}

		report += StringUtils.indentText("Class Restrictions\n");
		for (JRestrictionsContainer restr : propertyRestrictions.values()) {
			report += StringUtils.indentText(restr.getReport() + "\n", 2);
		}
		return report;
	}

	public JRestrictionsContainer getRestrictionsContainer(JProperty prop) {
		return propertyRestrictions.get(prop);
	}

	public boolean hasDomainProperty(JProperty property) {
		return domainProperties.contains(property);
	}

	public boolean hasDomainProperty(String uri) {
		for (JProperty domainProp : domainProperties) {
			if (domainProp.getMapUri() == uri)
				return true;
		}
		return false;
	}

	public boolean hasPropertyRepresentation(JPropertyRepresentation representation) {
		return propertyRepresentations.contains(representation);
	}

	public boolean hasRestrictionsContainer(JProperty property) {
		return propertyRestrictions.containsKey(property);
	}

	public boolean hasRestrictionsContainer(JRestrictionsContainer restriction) {
		return propertyRestrictions.containsValue(restriction);
	}

	public boolean hasSubClass(JClass cls, boolean recursive) {
		return jModel.getClassGraph().hasChild(this, cls, recursive);
	}

	public boolean hasSuperClass(JClass cls, boolean recursive) {
		return jModel.getClassGraph().hasParent(this, cls, recursive);
	}

	public boolean hasSuperClasses() {
		if (jModel.getClassGraph().incomingEdgesOf(this).size() > 0)
			return true;
		return false;
	}

	public boolean isAnonymous() {
		return anonClass;
	}

	public boolean isRootClass() {
		return (!hasSuperClasses());
	}

	public List<JProperty> listAllProperties() {
		return allProperties;
	}

	public List<JProperty> listDirectDomainProperties() {
		return domainProperties;
	}

	public List<JPropertyRepresentation> listDirectPropertyRepresentations(JProperty property) {
		List<JPropertyRepresentation> representations = new ArrayList<JPropertyRepresentation>();
		for (JPropertyRepresentation rep : propertyRepresentations) {
			if (rep.getOnProperty() == property)
				representations.add(rep);
		}
		return representations;
	}

	public List<JClass> listDirectSubClasses() {
		List<JClass> subClasses = new ArrayList<JClass>();
		Iterator<DefaultEdge> children = jModel.getClassGraph().outgoingEdgesOf(this).iterator();
		while (children.hasNext()) {
			DefaultEdge edge = (DefaultEdge) children.next();
			JClass child = jModel.getClassGraph().getEdgeTarget(edge);
			subClasses.add(child);
		}

		return subClasses;
	}

	public List<JClass> listDirectSuperClasses() {
		List<JClass> superClasses = new ArrayList<JClass>();
		Iterator<DefaultEdge> parents = jModel.getClassGraph().incomingEdgesOf(this).iterator();
		while (parents.hasNext()) {
			DefaultEdge edge = (DefaultEdge) parents.next();
			JClass parent = jModel.getClassGraph().getEdgeSource(edge);
			superClasses.add(parent);
		}
		return superClasses;
	}

	public List<JProperty> listDomainProperties(boolean recursive) {
		if (recursive == false)
			return domainProperties;

		Map<String, JProperty> mapUri2prop = listDomainPropertiesAsMap(true);

		List<JProperty> props = new ArrayList<JProperty>();
		props.addAll(mapUri2prop.values());
		return props;
	}

	public Map<String, JProperty> listDomainPropertiesAsMap(boolean recursive) {
		Map<String, JProperty> uri2prop = new HashMap<String, JProperty>();

		// add current props, if not present
		for (JProperty p : domainProperties) {
			if (!uri2prop.containsKey(p.getMapUri()))
				uri2prop.put(p.getMapUri(), p);
		}

		if (recursive) {
			List<JClass> superClasses = jModel.getClassGraph().listDirectParents(this);
			for (JClass sCls : superClasses) {
				uri2prop.putAll(sCls.listDomainPropertiesAsMap(recursive));
			}
		}
		return uri2prop;
	}

	public List<JRestrictionsContainer> listRestrictionContainers() {
		List<JRestrictionsContainer> restrictions = new ArrayList<JRestrictionsContainer>();
		restrictions.addAll(propertyRestrictions.values());
		return restrictions;
	}

	public void removeAllPropertyRepresentations() {
		propertyRepresentations.clear();
	}

	public void removeDomainProperty(JProperty property) {
		if (domainProperties.contains(property))
			domainProperties.remove(property);
	}

	public void removeRestrictionsContainer(JProperty property) {
		if (propertyRestrictions.containsKey(property))
			propertyRestrictions.remove(property);
	}

	public void removeSubClass(JClass subCls) {
		jModel.getClassGraph().removeVertex(subCls);
	}

	public void removeSuperClass(JClass superCls) {
		jModel.getClassGraph().removeVertex(superCls);
	}

	public void setAnonymous(boolean anon) {
		log.debug(LogUtils.toLogName(this) + ": Marking as anonymous class");
		anonClass = anon;
	}

	public void setOntClass(OntClass ontClass) {
		this.ontClass = ontClass;
	}

	public void setPackage(JPackage pkg) {
		this.jPkg = pkg;
		pkg.addClass(this);
	}

	public String toString() {
		return getJavaClassName();
	}

}
