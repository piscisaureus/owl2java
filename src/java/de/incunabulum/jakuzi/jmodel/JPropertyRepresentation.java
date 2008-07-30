package de.incunabulum.jakuzi.jmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.utils.IReporting;

public class JPropertyRepresentation implements IReporting {


	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JPropertyRepresentation.class);

	private JProperty onProperty;
	private JAllValuesRestriction allValuesRestriction;
	private JOtherRestriction otherRestriction;
	private JCardinalityRestriction cardinalityRestriction;

	public JPropertyRepresentation(JProperty onProperty) {
		this.onProperty = onProperty;
	}
	

	@Override
	public String getReport() {
		String ret = new String();
		ret += onProperty.getReport();
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
		this.allValuesRestriction = allValuesRestriction;
	}


	public JOtherRestriction getOtherRestriction() {
		return otherRestriction;
	}


	public void setOtherRestriction(JOtherRestriction otherRestriction) {
		this.otherRestriction = otherRestriction;
	}


	public JCardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}


	public void setCardinalityRestriction(JCardinalityRestriction cardinalityRestriction) {
		this.cardinalityRestriction = cardinalityRestriction;
	}


	
	
	

	

}
