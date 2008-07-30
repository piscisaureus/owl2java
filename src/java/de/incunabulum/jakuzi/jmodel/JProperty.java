package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultEdge;

import com.hp.hpl.jena.ontology.OntProperty;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.model.XsdGraph;
import de.incunabulum.jakuzi.model.XsdTypeMapper;
import de.incunabulum.jakuzi.utils.StringUtils;

public class JProperty extends JMapped {

	public static final String DataTypeProperty = "DataTypeProperty";
	public static final String ObjectProperty = "ObjectProperty";

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JProperty.class);

	private OntProperty ontProperty;
	private JModel jmodel;

	protected List<JClass> propertyDomain = new ArrayList<JClass>();
	// propertyRange is of type List<String> for a datatype property, List<JClass> for a object property
	@SuppressWarnings("unchecked")
	protected List propertyRange = new ArrayList();
	private String propertyType;

	private List<JProperty> equivalentProps = new ArrayList<JProperty>();
	private List<JProperty> inverseProps = new ArrayList<JProperty>();
	
	private Map<JClass, JRestrictionsContainer> classRestrictions = new HashMap<JClass, JRestrictionsContainer>();

	private boolean isFunctional = false;
	private boolean isInverseFunctional = false;
	private boolean isSymetric = false;
	private boolean isTransitive = false;

	public JProperty(JModel model, String name, String mappedTo) {
		super(name, mappedTo);
		this.jmodel = model;
		this.jmodel.getPropertyGraph().addVertex(this);
	}

	public void addDomain(JClass domainCls) {
		log.debug(LogUtils.toLogName(domainCls) + ": Adding domain property " + LogUtils.toLogName(this));
		if (!this.propertyDomain.contains(domainCls))
			this.propertyDomain.add(domainCls);
		if (!domainCls.hasDomainProperty(this))
			domainCls.addDomainProperty(this);
	}

	public void addEquivalentProperty(JProperty property) {
		if (!this.equivalentProps.contains(property))
			this.equivalentProps.add(property);
		if (!property.equivalentProps.contains(this))
			property.equivalentProps.add(this);
	}

	public boolean hasEquivalentProperty(JProperty property) {
		return equivalentProps.contains(property);
	}

	public void addInverseProperty(JProperty prop) {
		if (!this.inverseProps.contains(prop))
			this.inverseProps.add(prop);
		if (!prop.inverseProps.contains(this))
			prop.inverseProps.add(this);
	}

	@SuppressWarnings("unchecked")
	public void addRange(JClass range) {
		if (propertyType == DataTypeProperty) {
			log.warn("Adding a JClass object to a Datatype property. Ignored");
			return;
		}
		if (!this.propertyRange.contains(range))
			this.propertyRange.add(range);
	}

	@SuppressWarnings("unchecked")
	public void addRange(String range) {
		if (propertyType == ObjectProperty) {
			log.warn("Adding a String to a Objectproperty. Ignored");
			return;
		}

		if (!this.propertyRange.contains(range))
			this.propertyRange.add(range);
	}


	public void addSubProperty(JProperty prop) {
		jmodel.getPropertyGraph().addChildVertex(this, prop);
	}

	public void addSuperProperty(JProperty prop) {
		jmodel.getPropertyGraph().addParentVertex(this, prop);
	}

	public boolean equals(Object other) {
		// same URI
		return other instanceof JProperty && ((JProperty) other).getMapUri().equals(getMapUri());
	}

	public String getDataRangeMethod() {
		String rangeUri = getRangeUri();
		return XsdTypeMapper.getAccessMethod(rangeUri);
	}

	public String getJavaName() {
		return NamingUtils.getPropertyName(ontProperty);
	}

	public OntProperty getOntProperty() {
		return ontProperty;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public String getRangeJava() {
		String rangeName = new String();;
		String rangeUri = getRangeUri();
		if (this.isDataTypeProperty()) {
			rangeName = XsdTypeMapper.getJavaClassName(rangeUri);
			// go from java.lang.String to String
			rangeName = rangeName.substring(rangeName.lastIndexOf(".") + 1);
		} else {
			JClass cls = jmodel.getJClass(rangeUri);
			rangeName = cls.getJavaClassName();
		}
		return rangeName;
	}

	public String getRangeJavaFull() {
		String rangeName = new String();;
		String rangeUri = getRangeUri();
		if (isDataTypeProperty()) {
			rangeName = XsdTypeMapper.getJavaClassName(rangeUri);
		} else {
			JClass cls = jmodel.getJClass(rangeUri);
			rangeName = cls.getJavaClassFullName();
		}
		return rangeName;
	}

	@SuppressWarnings("unchecked")
	public String getRangeUri() {
		String rangeUri;
		if (this.getPropertyType() == JProperty.DataTypeProperty)
			if (propertyRange.isEmpty()) {
				log.info(LogUtils.toLogName(this) + ": Range is empty! Setting range to XMLLiteral");
				rangeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
			} else if (propertyRange.size() > 1) {
				rangeUri = XsdGraph.findBestSuperXsdType(propertyRange);
				log
						.info(LogUtils.toLogName(this) + ": multiple range! Setting range to best super type "
								+ rangeUri);
			} else {
				rangeUri = (String) propertyRange.get(0);
				log.info(LogUtils.toLogName(this) + ": Setting range to " + rangeUri);
			}
		else {
			if (propertyRange.isEmpty()) {
				log.info(LogUtils.toLogName(this) + ": Range is empty! Setting range to BaseThing Uri");
				rangeUri = jmodel.getBaseThingUri();
			} else if (propertyRange.size() > 1) {
				log.error(LogUtils.toLogName(this) + ": Multiple range! This should have been "
						+ "be handled before. Aborting");
				System.exit(1);
				rangeUri = jmodel.getBaseThingUri();
			} else {
				JClass cls = (JClass) propertyRange.get(0);
				rangeUri = cls.getMapUri();
			}
		}
		return rangeUri;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getReport() {
		String report = new String();
		String rng = new String();

		// Super, sub
		Iterator it;
		Set<DefaultEdge> edgesIn = jmodel.getPropertyGraph().incomingEdgesOf(this);
		it = edgesIn.iterator();
		while (it.hasNext()) {
			DefaultEdge edge = (DefaultEdge) it.next();
			JProperty src = jmodel.getPropertyGraph().getEdgeSource(edge);
			rng += src.getJavaName() + ", ";
		}
		report += StringUtils.indentText("Parent Properties: " + rng + "\n", 3);
		
		Set<DefaultEdge> edgesOut = jmodel.getPropertyGraph().outgoingEdgesOf(this);
		it = edgesOut.iterator();
		rng = new String();
		while (it.hasNext()) {
			DefaultEdge edge = (DefaultEdge) it.next();
			JProperty src = jmodel.getPropertyGraph().getEdgeTarget(edge);
			rng += src.getJavaName() + ",";
		}
		report += StringUtils.indentText("Child Properties: " + rng + "\n", 3);		
		
		rng = new String();
		for (JProperty equProperty : equivalentProps)
			rng += equProperty.getJavaName() + ", ";
		report += StringUtils.indentText("Equivalent Properties: " + rng + "\n", 3);

		String range = new String();
		if (propertyType == DataTypeProperty) {
			it = propertyRange.iterator();
			while (it.hasNext()) {
				String rangeUri = (String) it.next();
				range += (String) rangeUri + ", ";
			}
		} else {
			it = propertyRange.iterator();
			while (it.hasNext()) {
				JClass cls = (JClass) it.next();
				range += cls.getJavaInterfaceFullName() + ", ";
			}

		}

		report += StringUtils.indentText("Range: " + range + "\n", 3);

		report += StringUtils.indentText("Inverse Properties: " + "\n", 3);
		for (JProperty property : inverseProps)
			report += StringUtils.indentText(property.getJavaName() + "\n", 3);

		report += StringUtils.indentText("Property Restrictions\n", 3);
		
		for (JRestrictionsContainer rc: classRestrictions.values()) {
			report += StringUtils.indentText(rc.getReport(), 1);
		}
		report += StringUtils.indentText("Functional: " + isFunctional + "\n", 3);
		return report;
	}

	public boolean hasInverseProperties() {
		return (!this.inverseProps.isEmpty());
	}

	public boolean hasInverseProperty(JProperty prop) {
		return this.inverseProps.contains(prop);
	}

	public boolean isDataTypeProperty() {
		return (propertyType == JProperty.DataTypeProperty);
	}

	public boolean isFunctional() {
		return isFunctional;
	}

	public boolean isObjectProperty() {
		return (propertyType == JProperty.ObjectProperty);
	}

	@SuppressWarnings("unchecked")
	public List<JClass> listRange() {
		if (propertyType == JProperty.ObjectProperty)
			return propertyRange;
		return null;
	}

	public void removeDomain(JClass domainCls) {
		log.debug(LogUtils.toLogName(domainCls) + ": Removing domain property " + LogUtils.toLogName(this));
		domainCls.listDirectDomainProperties().remove(this);
		propertyDomain.remove(domainCls);
	}

	public void setFunctional(boolean isFunctional) {
		this.isFunctional = isFunctional;
	}

	public void setOntProperty(OntProperty ontProperty) {
		this.ontProperty = ontProperty;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public void setSymetric(boolean isSymetric) {
		this.isSymetric = isSymetric;
	}

	public boolean isTransitive() {
		return isTransitive;
	}

	public void setTransitive(boolean isTransitive) {
		this.isTransitive = isTransitive;
	}

	public boolean isSymetric() {
		return isSymetric;
	}

	public boolean isInverseFunctional() {
		return isInverseFunctional;
	}

	public void setInverseFunctional(boolean isInverseFunctional) {
		this.isInverseFunctional = isInverseFunctional;
	}
	
	public String toString() {
		return getJavaName();
	}
	
	public void addRestrictionsContainer(JClass cls, JRestrictionsContainer rc) {
		if (!classRestrictions.containsKey(cls))
			classRestrictions.put(cls, rc);
	}
	
	public JRestrictionsContainer  getRestrictionsContainer(JClass cls) {
		return classRestrictions.get(cls);
	}
	
	public boolean hasRestrictionsContainer(JRestrictionsContainer restriction) {
		return classRestrictions.containsValue(restriction);
	}

	public boolean hasRestrictionsContainer(JClass cls) {
		return classRestrictions.containsKey(cls);
	}


}
