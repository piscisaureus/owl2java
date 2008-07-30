package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.utils.IReporting;
import de.incunabulum.jakuzi.utils.StringUtils;

public class JRestrictionsContainer implements IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JRestrictionsContainer.class);

	private JClass onClass;
	private JProperty onProperty;

	private JCardinalityRestriction cardinalityRestriction;
	private List<JAllValuesRestriction> allValuesRestrictions = new ArrayList<JAllValuesRestriction>();
	private JOtherRestriction otherRestriction;

	public JRestrictionsContainer(JClass onClass, JProperty onProperty) {
		this.onClass = onClass;
		this.onProperty = onProperty;
		cardinalityRestriction = new JCardinalityRestriction(onClass, onProperty);
		otherRestriction = new JOtherRestriction(onClass, onProperty);
		
		onClass.addRestrictionsContainer(onProperty, this);
		onProperty.addRestrictionsContainer(onClass, this);
	}
	
	public boolean hasCardinalityRestriction() {
		if (cardinalityRestriction == null)
			return true;
		return false;
	}
	
	public boolean hasOtherRestriction() {
		if (otherRestriction == null)
			return true;
		return false;
	}

	public JOtherRestriction getOtherRestriction() {
		return otherRestriction;
	}

	public List<JAllValuesRestriction> listAllValuesRestrictions() {
		return allValuesRestrictions;
	}

	public void aggregateRestrictions(List<JClass> parentClasses) {
		for (JClass cls : parentClasses) {
			log.debug(LogUtils.toLogName(onClass, onProperty)
					+ ": Aggregating restrictions of parent class " + LogUtils.toLogName(cls));
			JRestrictionsContainer restrictions = cls.getRestrictionsContainer(onProperty);
			// continue if we have no restrictions for this property on the parent class
			if (restrictions == null)
				continue;
			// cardinality
			cardinalityRestriction.mergeParent(restrictions.getCardinalityRestriction());
			// otherRestriction
			otherRestriction.mergeParent(restrictions.getOtherRestriction());
			// allValues
			for (JAllValuesRestriction restriction : restrictions.listAllValuesRestrictions()) {
				if (!allValuesRestrictions.contains(restriction))
					allValuesRestrictions.add(restriction.clone());
			}
		}
	}

	public JCardinalityRestriction getCardinalityRestriction() {
		return cardinalityRestriction;
	}

	public JRestrictionsContainer clone() {
		JRestrictionsContainer rc = new JRestrictionsContainer(onClass, onProperty);
		rc.cardinalityRestriction = cardinalityRestriction.clone();
		rc.otherRestriction = otherRestriction.clone();
		for (JAllValuesRestriction avr : allValuesRestrictions) {
			JAllValuesRestriction r = avr.clone();
			rc.allValuesRestrictions.add(r);
		}
		return rc;
	}

	@Override
	public String getReport() {
		String report = LogUtils.toLogName(onClass, onProperty) + " Restriction Container:\n";
		report += StringUtils.indentText(cardinalityRestriction.getReport() + "\n", 1);
		report += StringUtils.indentText(otherRestriction.getReport() + "\n", 1);
		for (JAllValuesRestriction r : allValuesRestrictions) {
			report += StringUtils.indentText(r.getReport() +"\n", 1);
		}
		return report;
	}

	public void addAllValuesRestriction(JClass allValuesJClass) {
		JAllValuesRestriction avr = new JAllValuesRestriction(onClass, onProperty);
		avr.setAllValues(allValuesJClass);
		allValuesRestrictions.add(avr);
	}

}
