package de.incunabulum.owl4java.jmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntProperty;

import de.incunabulum.owl4java.jmodel.utils.NamingUtils;
import de.incunabulum.owl4java.utils.StringUtils;
import de.incunabulum.owl4java.utils.XsdUtils;

public class JProperty extends JMapped {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JProperty.class);

	public static String DataTypeProperty = "DataTypeProperty";
	public static String ObjectProperty = "ObjectProperty";

	private OntProperty ontProperty;

	protected String propertyType;
	protected List<JProperty> subProps = new ArrayList<JProperty>();
	protected List<JProperty> superProps = new ArrayList<JProperty>();
	protected List<JProperty> inverseProps = new ArrayList<JProperty>();

	// TODO: Merge datatypeRange, objectRange? 
	protected List<String> datatypeRange = new ArrayList<String>();
	protected List<JClass> propertyRange = new ArrayList<JClass>();
	protected List<JClassRestriction> restrictions = new ArrayList<JClassRestriction>();
	protected boolean isFunctional = false;

	public void addObjectRange(JClass range) {
		if (!this.propertyRange.contains(range))
			this.propertyRange.add(range);
	}

	public void addDatatypeRange(String range) {
		if (!this.datatypeRange.contains(range))
			this.datatypeRange.add(range);
	}

	public void addInverse(JProperty prop) {
		if (!this.inverseProps.contains(prop))
			this.inverseProps.add(prop);
		if (!prop.inverseProps.contains(this))
			prop.inverseProps.add(this);
	}

	public boolean hasInverse(JProperty prop) {
		return this.inverseProps.contains(prop);
	}

	public JProperty(String name, String mappedTo) {
		super(name, mappedTo);
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
	
	public String getJavaFullName() {
		return NamingUtils.getPropertyName(ontProperty);
	}

	@Override
	public String getReport() {
		String rng = "";

		// Super, sub
		for (JProperty superProperty : superProps)
			rng += superProperty.getJavaFullName() + ", ";
		String report = StringUtils.indentText("Parent Properties: " + rng + "\n", 3);

		for (JProperty subProperty : subProps)
			rng += subProperty.getJavaFullName() + ", ";
		report = StringUtils.indentText("Child Properties: " + rng + "\n", 3);

		rng = "";
		for (JClass cls : propertyRange) {
			rng += cls.getJavaInterfaceFullName() + ", ";
		}
		report = StringUtils.indentText("Range: " + rng + "\n", 3);

		report += StringUtils.indentText("Inverse Properties: " + "\n", 3);
		for (JProperty property : inverseProps)
			report += StringUtils.indentText(property.getJavaFullName() + "\n", 3);

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

	public List<JClass> getObjectRange() {
		return propertyRange;
	}

	public List<String> getDatatypeRange() {
		return datatypeRange;
	}

	public boolean equals(Object other) {
		return other instanceof JProperty && (
		// same URI
				(JProperty) other).getMapUri().equals(getMapUri());
	}

	public String getRangeString() {
		String rangeStr;
		if (this.getPropertyType() == JProperty.DataTypeProperty)
			if (datatypeRange.size() > 1 || datatypeRange.isEmpty()) {
				log.info(NamingUtils.toLogName(this) + ": Multiple datatypes as range or empty! Setting range to Object");
				rangeStr = "Object";
			} else {
				rangeStr = getDatatypeRange().get(0);
				rangeStr = XsdUtils.xsd2Java(rangeStr);
				log.info(NamingUtils.toLogName(this) + ": Setting range to " + rangeStr);
			}
		else {
			if (propertyRange.size() > 1 || propertyRange.isEmpty()) {
				log.warn(NamingUtils.toLogName(this) + ": Multiple objects as range or empty! Setting range to Object");
				rangeStr = "Object";
			} else {
				rangeStr = propertyRange.get(0).getJavaInterfaceName();
			}
		}
		return rangeStr;
	}

}
