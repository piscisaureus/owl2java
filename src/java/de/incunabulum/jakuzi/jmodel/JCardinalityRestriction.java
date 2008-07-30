package de.incunabulum.jakuzi.jmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.utils.IReporting;

public class JCardinalityRestriction extends JBaseRestriction implements IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JCardinalityRestriction.class);

	private int maxCardinality = -1;
	private int minCardinality = 0;

	// multipleXXX = true -> we need accessor methods for this type; 
	private boolean multipleEnabled = true;
	private boolean singleEnabled = false;
	// deprecated status is set depending on the max and min values
	// multipleDeprecated == true does not mean multipe = true 
	private boolean multipleDeprecated = false;
	private boolean singleDeprecated = false;

	public JCardinalityRestriction(JClass onClass, JProperty onProperty) {
		super(onClass, onProperty);
		// disable multiple stuff for functionals
		if (onProperty.isFunctional()) {
			multipleEnabled = false;
			singleEnabled = true;
		}
	}

	public void mergeParent(JCardinalityRestriction parent) {
		// empty parent cardinality restrictions (aka no restrictions) are ignored
		if (!parent.isEmpty) {
			setMaxCardinality(parent.getMaxCardinality());
			setMinCardinality(parent.getMinCardinality());
			// set the enabled status
			multipleEnabled = multipleEnabled || parent.multipleEnabled;
			singleEnabled = singleEnabled || parent.singleEnabled;
			// set the deprecated and enabled status on merge cardinalities
			updateAccessorStati();
		}
	}

	protected void updateAccessorStati() {
		if (maxCardinality == 1) {
			multipleDeprecated = true;
			singleEnabled = true;
		}
		if (maxCardinality == 0 || (minCardinality > maxCardinality)) {
			multipleDeprecated = true;
			singleDeprecated = true;
		}
	}

	public void setMaxCardinality(int max) {
		if (maxCardinality == -1) 
			maxCardinality = max;
		else {
			if (maxCardinality > max)
				maxCardinality = max;
		}
		isEmpty = false;
		updateAccessorStati();
	}

	public void setMinCardinality(int min) {
		if (minCardinality == 0)
			minCardinality = min;
		else {
			if (minCardinality < min)
				minCardinality = min;
		}
		isEmpty = false;
		updateAccessorStati();
	}

	public void setCardinality(int cardinality) {
		setMaxCardinality(cardinality);
		setMinCardinality(cardinality);
		updateAccessorStati();
	}



	public JCardinalityRestriction clone() {
		JCardinalityRestriction restriction = new JCardinalityRestriction(onClass, onProperty);
		restriction.maxCardinality = maxCardinality;
		restriction.minCardinality = minCardinality;
		restriction.multipleEnabled = multipleEnabled;
		restriction.multipleDeprecated = multipleDeprecated;
		restriction.singleEnabled = singleEnabled;
		restriction.singleDeprecated = singleDeprecated;

		return restriction;
	}


	@Override
	public String getReport() {
		String ret = LogUtils.toLogName(this) + ": ";
		if (isEmpty) 
			return ret + "Empty cardinality restriction";
		ret += "Max " + maxCardinality + ", Min " + minCardinality +"; ";
		ret += "multiple " + multipleEnabled + ", deprecated " + multipleDeprecated + "; ";
		ret += "single " + singleEnabled + ", deprecate " + singleDeprecated+ "; ";
		return ret;
	}

	public int getMaxCardinality() {
		return maxCardinality;
	}

	public int getMinCardinality() {
		return minCardinality;
	}

}
