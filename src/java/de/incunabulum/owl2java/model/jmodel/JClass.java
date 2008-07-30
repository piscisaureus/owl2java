package de.incunabulum.owl2java.model.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;

import com.hp.hpl.jena.ontology.OntClass;

import de.incunabulum.owl2java.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.utils.StringUtils;

public class JClass extends JMapped {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JClass.class);

	private OntClass ontClass;
	private JModel jModel;
	private JPackage jPkg;
	private boolean anonClass = false;

	private List<JProperty> domainProperties = new ArrayList<JProperty>();
	private List<JProperty> aggregatedProperties = new ArrayList<JProperty>();
	private List<JClass> equivalentClasses = new ArrayList<JClass>();

	private Map<JProperty, JRestrictionsContainer> domainPropertyRestrictions = new HashMap<JProperty, JRestrictionsContainer>();
	private Map<JProperty, JRestrictionsContainer> aggregatedPropertyRestrictions = new HashMap<JProperty, JRestrictionsContainer>();
	// PropRepresentation = (0..1) CardRestriction + 1 AllValuesRestriction + (0..1) + OtherRestriction
	private List<JPropertyRepresentation> domainPropertyRepresentations = new ArrayList<JPropertyRepresentation>();
	private List<JPropertyRepresentation> aggregatedPropertyRepresentations = new ArrayList<JPropertyRepresentation>();

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
		if (!domainPropertyRestrictions.containsKey(prop)) {
			log.debug(LogUtils.toLogName(this, prop) + ": Creating RestrictionsContainer");
			JRestrictionsContainer rc = new JRestrictionsContainer(this, prop);
			domainPropertyRestrictions.put(prop, rc);
		}
	}

	public void addEquivalentClass(JClass cls) {
		if (!equivalentClasses.contains(cls))
			equivalentClasses.add(cls);
		if (!cls.equivalentClasses.contains(this))
			cls.equivalentClasses.add(this);
	}

	public void addDomainRestrictionsContainer(JProperty property, JRestrictionsContainer rc) {
		if (!domainPropertyRestrictions.containsKey(property))
			domainPropertyRestrictions.put(property, rc);
	}

	public void addSubClass(JClass cls) {
		jModel.getClassGraph().addChildVertex(this, cls);
	}

	public void addSuperClass(JClass cls) {
		jModel.getClassGraph().addParentVertex(this, cls);
	}

	public int aggegrateAll() {
		int aggregateActions = 0;
		if (jModel.isBaseThing(this)) {
			log.debug(LogUtils.toLogName(this) + ": No aggregation for BaseThing");
			return aggregateActions;
		}
		log.debug(LogUtils.toLogName(this) + ": Aggregating all definitions from parent classes");
		List<JClass> parentClasses = jModel.getClassGraph().listDirectParents(this);
		aggregateActions += aggregateProperties(parentClasses);
		aggregateActions += aggregateRestrictionContainers(parentClasses);
		aggregateActions += aggegrateRestrictionDefinitions(parentClasses);
		aggregateActions += aggregatePropertyRepresentations(parentClasses);
		return aggregateActions;
	}

	protected int aggregatePropertyRepresentations(List<JClass> parentClasses) {
		int aggregateActions = 0;
		// copy local domain property representations to all representations
		for (JPropertyRepresentation rep : domainPropertyRepresentations) {
			// already aggregated in previous run
			if (hasAggregatedPropertyRepresentation(rep.getOnProperty()))
				continue;
			aggregatedPropertyRepresentations.add(rep);
			aggregateActions++;
		}

		// copy parent representations to all representations
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating property presentations of parent class "
					+ LogUtils.toLogName(cls));

			for (JPropertyRepresentation representation : cls.listAggregatedPropertyRepresentations()) {

				// clone the representation and add it to our list of aggregated representations
				JPropertyRepresentation pr = representation.clone();


				// Check for identity. Otherwise, two parent provide the same representation
				// We ignore the deprecated status as this can change later on
				boolean prExists = hasAggregatedPropertyRepresentation(pr);
				if (prExists) {
					log.debug(LogUtils.toLogName(this) + ": Skipping identical representation from parent class "
							+ LogUtils.toLogName(cls));
				} else {
					// if we have a domain representation for a property -> mark deprecated + add
					if (hasAggregatedPropertyRepresentation(pr.getOnProperty())) {
						log.debug(LogUtils.toLogName(this) + ": Setting parent representation to deprecated");
						pr.setDeprecated(true);
					}
					aggregatedPropertyRepresentations.add(pr);
					aggregateActions++;
					log.debug(LogUtils.toLogName(this) + ": Adding parent representation");
				}
			}
		}
		return aggregateActions;
	}

	public List<JPropertyRepresentation> listAggregatedPropertyRepresentations() {
		return aggregatedPropertyRepresentations;
	}

	protected int aggegrateRestrictionDefinitions(List<JClass> parentClasses) {
		int aggregateActions = 0;
		Iterator<JProperty> it = aggregatedPropertyRestrictions.keySet().iterator();
		while (it.hasNext()) {
			JProperty property = (JProperty) it.next();
			JRestrictionsContainer propertyRestrictionContainer = getAggregatedRestrictionsContainer(property
					.getMapUri());
			propertyRestrictionContainer.aggregateRestrictions(parentClasses);
			log.debug(LogUtils.toLogName(this) + ": Merging restriction definitions for property "
					+ LogUtils.toLogName(property));
		}
		return aggregateActions;
	}

	protected int aggregateProperties(List<JClass> parentClasses) {
		int aggregateActions = 0;
		// copy local domain properties to allProperites
		for (JProperty property : domainProperties) {
			if (hasAggregatedProperty(property.getMapUri()))
				continue;
			aggregateActions++;
			aggregatedProperties.add(property);
		}

		// copy parent properties to allProperties
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating domain properties of parent class "
					+ LogUtils.toLogName(cls));

			for (JProperty property : cls.listAggregatedProperties()) {
				if (!hasAggregatedProperty(property.getMapUri())) {
					aggregateActions++;
					aggregatedProperties.add(property);
				}
			}
		}
		return aggregateActions;
	}

	protected int aggregateRestrictionContainers(List<JClass> parentClasses) {
		int aggregateActions = 0;
		// add our restrictions
		for (JProperty property : domainPropertyRestrictions.keySet()) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating domain restriction containers ");
			JRestrictionsContainer rc = domainPropertyRestrictions.get(property);
			// already added in previous run
			if (hasAggregatedRestrictionsContainer(property.getMapUri()))
				continue;
			aggregateActions++;
			aggregatedPropertyRestrictions.put(property, rc);
		}

		// add parent class stuff
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(this) + ": Aggregating restriction containers of parent class "
					+ LogUtils.toLogName(cls));

			// aggregate the restriction containers from parent classes
			for (JProperty property : cls.listAggregatedProperties()) {
				// already present -> skip (handled in aggegateRestrictionDefinitions)
				if (hasAggregatedRestrictionsContainer(property.getMapUri()))
					continue;
				// copy restriction container to here
				JRestrictionsContainer rc = cls.getAggregatedRestrictionsContainer(property);
				aggregatedPropertyRestrictions.put(property, rc.clone());
				aggregateActions++;
			}
		}
		return aggregateActions;
	}

	public void createDomainPropertyRepresentations() {
		log.debug(LogUtils.toLogName(this) + ": Creating property representations for domain properties");
		// loop over all properties

		for (JProperty property : domainPropertyRestrictions.keySet()) {
			log.debug(LogUtils.toLogName(this) + ": Creating domain property representations for "
					+ LogUtils.toLogName(property));
			JRestrictionsContainer rc = domainPropertyRestrictions.get(property);

			// create the default representation based on the property range
			// if no allValues restriction for this property
			// -> otherwise the domain property has a allValues additional restriction
			// and is handled below
			if (rc.listAllValuesRestrictions().isEmpty()) {
				log.debug(LogUtils.toLogName(property) + ": Creating default representation");
				JPropertyRepresentation jpr = new JPropertyRepresentation(property);
				jpr.setHasDefaultPropertyRange(true);
				if (rc.hasCardinalityRestriction())
					jpr.setCardinalityRestriction(rc.getCardinalityRestriction().clone());
				if (rc.hasOtherRestriction())
					jpr.setOtherRestriction(rc.getOtherRestriction().clone());
				domainPropertyRepresentations.add(jpr);
			}

			// for each AllValues create a single representation
			for (JAllValuesRestriction avr : rc.listAllValuesRestrictions()) {
				log.debug(LogUtils.toLogName(property) + ": Creating representation for AllValues "
						+ LogUtils.toLogName(avr.getAllValues()));
				JPropertyRepresentation pr = new JPropertyRepresentation(property);
				pr.setHasDefaultPropertyRange(false);
				pr.setAllValuesRestriction(avr);
				pr.setCardinalityRestriction(rc.getCardinalityRestriction());
				pr.setOtherRestriction(rc.getOtherRestriction());
				domainPropertyRepresentations.add(pr);
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

	public List<JPropertyRepresentation> getDomainPropertyRepresentations() {
		return domainPropertyRepresentations;
	}

	public List<JPropertyRepresentation> getAggregatedPropertyRepresentations() {
		return aggregatedPropertyRepresentations;
	}

	@Override
	public String getJModelReport() {
		String report = new String();
		report += StringUtils.toSubHeader("Class " + getJavaClassFullName());

		report += StringUtils.indentText("Parent Classes\n");
		Iterator<DefaultEdge> parentIt = jModel.getClassGraph().incomingEdgesOf(this).iterator();
		while (parentIt.hasNext()) {
			DefaultEdge edge = parentIt.next();
			JClass parent = jModel.getClassGraph().getEdgeSource(edge);
			report += StringUtils.indentText(parent.getJavaClassFullName(), 2) + "\n";
		}

		report += StringUtils.indentText("Child Classes\n");
		Iterator<DefaultEdge> childIt = jModel.getClassGraph().outgoingEdgesOf(this).iterator();
		while (childIt.hasNext()) {
			DefaultEdge edge = childIt.next();
			JClass parent = jModel.getClassGraph().getEdgeTarget(edge);
			report += StringUtils.indentText(parent.getJavaClassFullName(), 2) + "\n";
		}

		report += StringUtils.indentText("Equivalent Classes\n");
		for (JClass child : equivalentClasses) {
			report += StringUtils.indentText(child.getJavaClassFullName(), 2) + "\n";
		}

		report += StringUtils.indentText("Domain Properties\n");
		for (JProperty prop : domainProperties) {
			report += StringUtils.indentText(prop.getJavaName(), 2) + "\n";
			report += prop.getJModelReport();
		}

		report += StringUtils.indentText("Class Restrictions\n");
		for (JRestrictionsContainer restr : domainPropertyRestrictions.values()) {
			report += StringUtils.indentText(restr.getJModelReport(), 3) + "\n";
		}
		return report;
	}

	public JRestrictionsContainer getDomainRestrictionsContainer(JProperty prop) {
		return getDomainRestrictionsContainer(prop.getMapUri());
	}
	public JRestrictionsContainer getAggregatedRestrictionsContainer(JProperty prop) {
		return getAggregatedRestrictionsContainer(prop.getMapUri());
	}

	public JRestrictionsContainer getDomainRestrictionsContainer(String uri) {
		for (JProperty property : domainPropertyRestrictions.keySet()) {
			if (property.getMapUri().equals(uri))
				return domainPropertyRestrictions.get(property);
		}
		return null;
	}

	public JRestrictionsContainer getAggregatedRestrictionsContainer(String uri) {
		for (JProperty property : aggregatedPropertyRestrictions.keySet()) {
			if (property.getMapUri().equals(uri))
				return aggregatedPropertyRestrictions.get(property);
		}
		return null;
	}

	public boolean hasDomainProperty(JProperty property) {
		return hasDomainProperty(property.getMapUri());
	}

	public boolean hasDomainProperty(String uri) {
		for (JProperty domainProp : domainProperties) {
			if (domainProp.getMapUri().equals(uri))
				return true;
		}
		return false;
	}

	public boolean hasAggregatedProperty(String uri) {
		for (JProperty prop : aggregatedProperties) {
			if (prop.getMapUri().equals(uri))
				return true;
		}
		return false;
	}

	public boolean hasDomainPropertyRepresentation(JPropertyRepresentation representation) {
		return domainPropertyRepresentations.contains(representation);
	}

	public boolean hasAggregatedPropertyRepresentation(JProperty property) {
		for (JPropertyRepresentation pr : aggregatedPropertyRepresentations) {
			if (pr.getOnProperty().getMapUri().equals(property.getMapUri()))
				return true;
		}
		return false;
	}

	private boolean hasAggregatedPropertyRepresentation(JPropertyRepresentation representation) {
		for (JPropertyRepresentation pr : aggregatedPropertyRepresentations) {
			if (pr.equalsIgnoreDeprecated(representation))
				return true;
		}
		return false;
	}

	public boolean hasDomainRestrictionsContainer(String propertyUri) {
		for (JProperty prop : domainPropertyRestrictions.keySet()) {
			JRestrictionsContainer rc = domainPropertyRestrictions.get(prop);
			if (prop.getMapUri().equals(propertyUri) && rc != null)
				return true;
		}
		return false;
	}

	public boolean hasAggregatedRestrictionsContainer(String propertyUri) {
		for (JProperty prop : aggregatedPropertyRestrictions.keySet()) {
			JRestrictionsContainer rc = aggregatedPropertyRestrictions.get(prop);
			if (prop.getMapUri().equals(propertyUri) && rc != null)
				return true;
		}
		return false;
	}

	public boolean hasDomainRestrictionsContainer(JProperty property) {
		return hasDomainRestrictionsContainer(property.getMapUri());
	}

	public boolean hasDomainRestrictionsContainer(JRestrictionsContainer restriction) {
		return domainPropertyRestrictions.containsValue(restriction);
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

	public List<JProperty> listAggregatedProperties() {
		return aggregatedProperties;
	}

	public List<JProperty> listDomainProperties() {
		return domainProperties;
	}

	public List<JPropertyRepresentation> listDomainPropertyRepresentations() {
		return domainPropertyRepresentations;
	}

	public List<JPropertyRepresentation> listDomainPropertyRepresentations(JProperty property) {
		List<JPropertyRepresentation> representations = new ArrayList<JPropertyRepresentation>();
		for (JPropertyRepresentation rep : domainPropertyRepresentations) {
			if (rep.getOnProperty().getMapUri().equals(property.getMapUri()))
				representations.add(rep);
		}
		return representations;
	}

	public List<JPropertyRepresentation> listAggregatedPropertyRepresentations(JProperty property) {
		List<JPropertyRepresentation> representations = new ArrayList<JPropertyRepresentation>();
		for (JPropertyRepresentation rep : aggregatedPropertyRepresentations) {
			if (rep.getOnProperty().getMapUri().equals(property.getMapUri()))
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
	
	public JClass getFirstDirectSuperClass() {
		return listDirectSuperClasses().get(0);
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

	public List<JRestrictionsContainer> listDomainRestrictionContainers() {
		List<JRestrictionsContainer> restrictions = new ArrayList<JRestrictionsContainer>();
		restrictions.addAll(domainPropertyRestrictions.values());
		return restrictions;
	}

	public void removeAllPropertyRepresentations() {
		domainPropertyRepresentations.clear();
	}

	public void removeDomainProperty(JProperty property) {
		if (domainProperties.contains(property))
			domainProperties.remove(property);
	}

	public void removeDomainRestrictionsContainer(JProperty property) {
		if (domainPropertyRestrictions.containsKey(property))
			domainPropertyRestrictions.remove(property);
	}

	public void removeSubClassRelation(JClass subCls) {
		jModel.getClassGraph().removeAllEdges(this, subCls);
		// jModel.getClassGraph().removeVertex(subCls);
	}

	public void removeSuperClassRelation(JClass superCls) {
		jModel.getClassGraph().removeAllEdges(superCls, this);
		// jModel.getClassGraph().removeVertex(superCls);
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
