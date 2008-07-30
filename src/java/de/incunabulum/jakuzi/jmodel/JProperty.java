package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntProperty;

import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.model.XsdUtils;
import de.incunabulum.jakuzi.utils.StringUtils;

public class JProperty extends JMapped {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JProperty.class);

	public static String DataTypeProperty = "DataTypeProperty";
	public static String ObjectProperty = "ObjectProperty";

	private JModel jmodel;
	private OntProperty ontProperty;

	protected String propertyType;
	protected boolean isFunctional = false;
	protected List<JProperty> subProps = new ArrayList<JProperty>();
	protected List<JProperty> superProps = new ArrayList<JProperty>();
	protected List<JProperty> inverseProps = new ArrayList<JProperty>();

	// propertyRange is of type:
	// - List<String> for a datatype property
	// - List<JClass> for a object property
	@SuppressWarnings("unchecked")
	protected List propertyRange = new ArrayList();

	protected List<JClassRestriction> restrictions = new ArrayList<JClassRestriction>();

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

	public void addInverse(JProperty prop) {
		if (!this.inverseProps.contains(prop))
			this.inverseProps.add(prop);
		if (!prop.inverseProps.contains(this))
			prop.inverseProps.add(this);
	}

	public boolean hasInverseProperty(JProperty prop) {
		return this.inverseProps.contains(prop);
	}

	public boolean hasInverseProperties() {
		return (!this.inverseProps.isEmpty());
	}

	public boolean hasParentProperties() {
		return (!this.superProps.isEmpty());
	}

	public JProperty(JModel model, String name, String mappedTo) {
		super(name, mappedTo);
		this.jmodel = model;
	}

	public void addSuperProperty(JProperty prop) {
		if (!superProps.contains(prop))
			superProps.add(prop);
		if (!prop.subProps.contains(this))
			prop.subProps.add(this);
	}

	public void addSubProperty(JProperty prop) {
		if (!subProps.contains(prop))
			subProps.add(prop);
		if (!prop.superProps.contains(this))
			prop.superProps.add(this);
	}

	public String getJavaName() {
		return NamingUtils.getPropertyName(ontProperty);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getReport() {
		String rng = new String();

		// Super, sub
		for (JProperty superProperty : superProps)
			rng += superProperty.getJavaName() + ", ";
		String report = StringUtils.indentText("Parent Properties: " + rng + "\n", 3);

		for (JProperty subProperty : subProps)
			rng += subProperty.getJavaName() + ", ";
		report = StringUtils.indentText("Child Properties: " + rng + "\n", 3);

		String range = new String();
		if (propertyType == DataTypeProperty) {
			Iterator it = propertyRange.iterator();
			while (it.hasNext()) {
				String rangeUri = (String) it.next();
				range += (String) rangeUri + ", ";
			}
		} else {
			Iterator it = propertyRange.iterator();
			while (it.hasNext()) {
				JClass cls = (JClass) it.next();
				range += cls.getJavaInterfaceFullName() + ", ";
			}

		}

		report = StringUtils.indentText("Range: " + range + "\n", 3);

		report += StringUtils.indentText("Inverse Properties: " + "\n", 3);
		for (JProperty property : inverseProps)
			report += StringUtils.indentText(property.getJavaName() + "\n", 3);

		report += StringUtils.indentText("Property Restrictions\n", 3);
		for (JClassRestriction restriction : restrictions) {
			report += restriction.getReport();
		}

		report += StringUtils.indentText("Functional: " + isFunctional + "\n", 3);
		return report;
	}

	public boolean isFunctional() {
		return isFunctional;
	}

	public boolean isDataTypeProperty() {
		return (propertyType == JProperty.DataTypeProperty);
	}

	public boolean isObjectProperty() {
		return (propertyType == JProperty.ObjectProperty);
	}

	public void setFunctional(boolean isFunctional) {
		this.isFunctional = isFunctional;
	}

	public void addRestriction(JClassRestriction res) {
		if (!this.restrictions.contains(res))
			this.restrictions.add(res);
	}

	public OntProperty getOntProperty() {
		return ontProperty;
	}

	public void setOntProperty(OntProperty ontProperty) {
		this.ontProperty = ontProperty;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	@SuppressWarnings("unchecked")
	public List<JClass> listRange() {
		if (propertyType == JProperty.ObjectProperty)
			return propertyRange;
		return null;
	}

	public boolean equals(Object other) {
		// same URI
		return other instanceof JProperty && ((JProperty) other).getMapUri().equals(getMapUri());
	}

	public String getRangeUri() {
		String rangeUri;
		if (this.getPropertyType() == JProperty.DataTypeProperty)
			if (propertyRange.isEmpty()) {
				log.info(NamingUtils.toLogName(this) + ": Range is empty! Setting range to XMLLiteral");
				rangeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
			} else if (propertyRange.size() > 1) {
				// TODO: set range to the nearest common parent of the type hierarchy?
				log.warn(NamingUtils.toLogName(this) + ": multiple range! Setting range to XMLLiteral");
				rangeUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
			} else {
				rangeUri = (String) propertyRange.get(0);
				log.info(NamingUtils.toLogName(this) + ": Setting range to " + rangeUri);
			}
		else {
			if (propertyRange.isEmpty()) {
				log.info(NamingUtils.toLogName(this) + ": Range is empty! Setting range to BaseThing Uri");
				rangeUri = jmodel.baseThingUri;
			} else if (propertyRange.size() > 1) {
				log.error(NamingUtils.toLogName(this) + ": Multiple range! This should have been "
						+ "be handled before. Aborting");
				System.exit(1);
				rangeUri = jmodel.baseThingUri;
			} else {
				JClass cls = (JClass) propertyRange.get(0);
				rangeUri = cls.getMapUri();
			}
		}
		return rangeUri;
	}

	public String getRangeJava() {
		String rangeName = new String();
		;
		String rangeUri = getRangeUri();
		if (this.isDataTypeProperty()) {
			rangeName = XsdUtils.getJavaClassName(rangeUri);
			// go from java.lang.String to String
			rangeName = rangeName.substring(rangeName.lastIndexOf(".") + 1);
		} else {
			JClass cls = jmodel.getJClass(rangeUri);
			rangeName = cls.getJavaClassName();
		}
		return rangeName;
	}
	
	public String getDataRangeMethod() {
		String rangeUri = getRangeUri();
		return XsdUtils.getAccessMethod(rangeUri);
	}

	public String getRangeJavaFull() {
		String rangeName = new String();
		;
		String rangeUri = getRangeUri();
		if (isDataTypeProperty()) {
			rangeName = XsdUtils.getJavaClassName(rangeUri);
		} else {
			JClass cls = jmodel.getJClass(rangeUri);
			rangeName = cls.getJavaClassFullName();
		}
		return rangeName;
	}

}
