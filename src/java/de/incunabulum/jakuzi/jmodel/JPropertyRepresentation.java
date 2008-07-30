package de.incunabulum.jakuzi.jmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.utils.IReporting;

public class JPropertyRepresentation implements IReporting {

	// Also used to capture default representation of a property. In this
	// case all restrictions are NULL

	private static final String deprecated = "@Deprecated";
	private static final String suffix = "As";

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JPropertyRepresentation.class);

	private JProperty onProperty;

	private boolean hasDefaultPropertyRange = true;
	private boolean isDefaultDeprecated = false;

	private JAllValuesRestriction allValuesRestriction;
	private JOtherRestriction otherRestriction;
	private JCardinalityRestriction cardinalityRestriction;

	public JPropertyRepresentation(JProperty onProperty) {
		this.onProperty = onProperty;
	}

	public JPropertyRepresentation clone() {
		JPropertyRepresentation pr = new JPropertyRepresentation(onProperty);
		pr.hasDefaultPropertyRange = hasDefaultPropertyRange;
		if (allValuesRestriction != null)
			pr.allValuesRestriction = allValuesRestriction.clone();
		if (otherRestriction != null)
			pr.otherRestriction = otherRestriction.clone();
		if (cardinalityRestriction != null)
			pr.cardinalityRestriction = cardinalityRestriction.clone();
		return pr;
	}

	public boolean equals(JPropertyRepresentation pr) {
		if (!onProperty.getMapUri().equals(pr.getOnProperty().getMapUri()))
			return false;
		if (!isDefaultDeprecated == pr.isDefaultDeprecated)
			return false;
		if (!hasDefaultPropertyRange == pr.hasDefaultPropertyRange)
			return false;

		if (allValuesRestriction != null) {
			if (!allValuesRestriction.equals(pr.getAllValuesRestriction()))
				return false;
		} else {
			if (pr.getAllValuesRestriction() != null)
				return false;
		}

		if (otherRestriction != null) {
			if (!otherRestriction.equals(pr.getOtherRestriction()))
				return false;
		} else {
			if (pr.getOtherRestriction() != null)
				return false;
		}

		if (cardinalityRestriction != null) {
			if (!cardinalityRestriction.equals(pr.getCardinalityRestriction()))
				return false;
		} else {
			if (pr.getCardinalityRestriction() != null)
				return false;
		}
		return true;
	}

	public void setDeprecated(boolean deprecated) {
		if (cardinalityRestriction == null)
			isDefaultDeprecated = deprecated;
		else {
			cardinalityRestriction.setMultipleDeprecated(true);
			cardinalityRestriction.setSingleDeprecated(true);
		}

	}

	@Override
	public String getReport() {
		String ret = new String();

		ret += "Property Representation for " + LogUtils.toLogName(onProperty) + "\n";
		ret += "  default range: " + hasDefaultPropertyRange + ", default deprecated: " + isDefaultDeprecated + "\n";
		if (allValuesRestriction != null)
			ret += allValuesRestriction.getReport() + "\n";
		if (otherRestriction != null)
			ret += otherRestriction.getReport() + "\n";
		if (cardinalityRestriction != null)
			ret += cardinalityRestriction.getReport() + "\n";
		return ret;
	}

	public JProperty getOnProperty() {
		return onProperty;
	}

	public void setOnProperty(JProperty onProperty) {
		this.onProperty = onProperty;
	}

	public JAllValuesRestriction getAllValuesRestriction() {
		return allValuesRestriction;
	}

	public void setAllValuesRestriction(JAllValuesRestriction allValuesRestriction) {
		hasDefaultPropertyRange = false;
		this.allValuesRestriction = allValuesRestriction;
	}

	public JOtherRestriction getOtherRestriction() {
		return otherRestriction;
	}

	public void setOtherRestriction(JOtherRestriction otherRestriction) {
		hasDefaultPropertyRange = false;
		this.otherRestriction = otherRestriction;
	}

	public JCardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}

	public void setCardinalityRestriction(JCardinalityRestriction cardinalityRestriction) {
		hasDefaultPropertyRange = false;
		this.cardinalityRestriction = cardinalityRestriction;
	}

	public boolean isMultipleDeprecated() {
		if (cardinalityRestriction == null) {
			if (!onProperty.isFunctional())
				return true;
			else
				return isDefaultDeprecated;
		}
		return cardinalityRestriction.isMultipleDeprecated();
	}

	public String getMultipleDeprecated() {
		if (isMultipleDeprecated())
			return deprecated;
		return "";
	}

	public String getSingleDeprecated() {
		if (isSingleDeprecated())
			return deprecated;
		return "";
	}

	public boolean isMultipleEnabled() {
		if (cardinalityRestriction == null)
			return (!onProperty.isFunctional());
		return cardinalityRestriction.isMultipleEnabled();
	}

	public boolean isSingleDeprecated() {
		if (cardinalityRestriction == null)
			if (onProperty.isFunctional())
				return true;
			else
				return isDefaultDeprecated;
		else
			return cardinalityRestriction.isSingleDeprecated();
	}

	public boolean isSingleEnabled() {
		if (cardinalityRestriction == null)
			return (onProperty.isFunctional());
		return cardinalityRestriction.isSingleEnabled();
	}

	public String getJavaMethodSuffix() {
		if (allValuesRestriction == null)
			return "";

		if (allValuesRestriction.hasAllValues()) {
			JClass allValues = allValuesRestriction.getAllValues();
			return suffix + allValues.getJavaClassName();
		}
		return "";
	}
	
	public String getRangeJava() {
		if (allValuesRestriction == null)
			return onProperty.getRangeJava();

		if (allValuesRestriction.hasAllValues()) {
			// return the range as defined by allValues
			JClass allValues = allValuesRestriction.getAllValues();
			return allValues.getJavaClassName();
		}
		// return the default range of the property
		return onProperty.getRangeJava();
	}
	
	public String getRangeUri() {
		if (allValuesRestriction == null) {
			return onProperty.getRangeUri();
		}
		
		if (allValuesRestriction.hasAllValues()) {
			// object property
			return "";
		}
		return onProperty.getRangeUri();
	}

	public String getRangeJavaFull() {
		if (allValuesRestriction == null)
			return onProperty.getRangeJavaFull();

		if (allValuesRestriction.hasAllValues()) {
			// return the range as defined by allValues
			JClass allValues = allValuesRestriction.getAllValues();
			return allValues.getJavaClassFullName();
		}
		// return the default range of the property
		return onProperty.getRangeJavaFull();
	}

	public String getPropertyType() {
		return onProperty.getPropertyType();
	}

	public boolean hasDefaultPropertyRange() {
		return hasDefaultPropertyRange;
	}

	public void setHasDefaultPropertyRange(boolean hasDefaultPropertyRange) {
		this.hasDefaultPropertyRange = hasDefaultPropertyRange;
	}

}
