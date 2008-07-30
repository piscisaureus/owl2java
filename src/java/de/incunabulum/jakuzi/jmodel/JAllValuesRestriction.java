package de.incunabulum.jakuzi.jmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.utils.IReporting;

public class JAllValuesRestriction extends JBaseRestriction implements IReporting {
	

	private static Log log = LogFactory.getLog(JAllValuesRestriction.class);

	protected JClass allValues = null;
	// AllValues restrictions of parent classes are deprecated in PropertyRepresentation aggregation step
	public JAllValuesRestriction(JClass onClass, JProperty onProperty) {
		super(onClass, onProperty);
	}

	public boolean equals(Object other) {
		if (!(other instanceof JAllValuesRestriction))
			return false;
		JAllValuesRestriction ar = (JAllValuesRestriction) other;
		if (!(allValues.equals(ar.allValues)))
			return false;
		return true;
	}
	
	public JAllValuesRestriction clone() {
		JAllValuesRestriction r = new JAllValuesRestriction(onClass, onProperty);
		r.allValues = allValues;
		return r;
	}

	@Override
	public String getReport() {
		return (LogUtils.toLogName(this) + ": AllValues set to class " + LogUtils.toLogName(allValues));
	}


	public void setAllValues(JClass cls) {
		isEmpty = false;
		if (allValues != null)
			log.warn(LogUtils.toLogName(this) + ": AllValues already set. Overwriting it with "
					+ LogUtils.toLogName(cls) + "!");
		allValues = cls;
	}

	public boolean hasAllValues() {
		return (!(allValues == null));
	}
	
	public JClass getAllValues() {
		return allValues;
	}
	
	public boolean equals (JAllValuesRestriction restriction) {
		if (allValues == restriction.getAllValues())
			return true;
		return false;
	}
	
	
}
