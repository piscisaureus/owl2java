package de.incunabulum.owl2java.core.model.jmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.core.utils.IReporting;
import de.incunabulum.owl2java.core.utils.StringUtils;

public class JRestrictionsContainer implements IReporting {
	
//	Restrictions
//	- Multiple allValues restrictions are handled independently as multiple
// restrictions of type JAllValuesRestriction 


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
		
		onClass.addDomainRestrictionsContainer(onProperty, this);
		onProperty.addRestrictionsContainer(onClass, this);
	}
	
	public boolean hasCardinalityRestriction() {
		if (cardinalityRestriction == null)
			return false;
		return true;
	}
	
	public boolean hasOtherRestriction() {
		if (otherRestriction == null)
			return false;
		return true;
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
			JRestrictionsContainer restrictions = cls.getAggregatedRestrictionsContainer(onProperty);
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
	public String getJModelReport() {
		String report = LogUtils.toLogName(onClass, onProperty) + " Restriction Container:\n";
		report += StringUtils.indentText(cardinalityRestriction.getJModelReport()+"\n", 1) ;
		report += StringUtils.indentText(otherRestriction.getJModelReport(), 1);
		if (!allValuesRestrictions.isEmpty())
			report += "\n";
		for (JAllValuesRestriction r : allValuesRestrictions) {
			report += StringUtils.indentText(r.getJModelReport()+"\n" , 1);
		}
		return report;
	}

	public void addAllValuesRestriction(JClass allValuesJClass) {
		JAllValuesRestriction avr = new JAllValuesRestriction(onClass, onProperty);
		avr.setAllValues(allValuesJClass);
		allValuesRestrictions.add(avr);
	}

}
