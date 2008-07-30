package de.incunabulum.owl2java.model.jmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.utils.IReporting;

public class JOtherRestriction extends JBaseRestriction implements IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JOtherRestriction.class);

	public List<JClass> someValues = new ArrayList<JClass>();
	public List<String> hasValues = new ArrayList<String>();

	public JOtherRestriction(JClass onClass, JProperty onProperty) {
		super(onClass, onProperty);
	}

	public boolean equals(Object other) {
		if (!(other instanceof JOtherRestriction))
			return false;
		JOtherRestriction or = (JOtherRestriction) other;
		if (!(isEmpty == or.isEmpty))
			return false;
		if (!(hasValues.equals(or.hasValues)))
			return false;
		if (!someValues.equals(or.someValues))
			return false;
		return true;
	}

	public boolean hasSomeValues() {
		return (!someValues.isEmpty());
	}

	public boolean hasHasValues() {
		return (!hasValues.isEmpty());
	}

	public List<JClass> listSomeValues() {
		return someValues;
	}

	public List<String> listHasValues() {
		return hasValues;
	}

	public void mergeParent(JOtherRestriction parent) {
		// empty parent cardinality restrictions (aka no restrictions) are ignored
		if (!parent.isEmpty) {
			someValues.addAll(parent.listSomeValues());
			hasValues.addAll(parent.listHasValues());
			isEmpty = false;
		}
	}

	public void addSomeValues(JClass cls) {
		isEmpty = false;
		someValues.add(cls);
	}

	public void addHasValue(String uri) {
		isEmpty = false;
		hasValues.add(uri);
	}

	public JOtherRestriction clone() {
		JOtherRestriction r = new JOtherRestriction(onClass, onProperty);
		r.isEmpty = isEmpty;
		r.someValues.addAll(someValues);
		r.hasValues.addAll(hasValues);
		return r;
	}

	@Override
	public String getJModelReport() {
		String ret = LogUtils.toLogName(this) + ": ";
		if (isEmpty)
			return ret + "Empty other restriction";

		ret += "SomeValues: ";
		for (JClass cls : someValues) {
			ret += LogUtils.toLogName(cls) + ", ";
		}
		ret += "; hasValue: ";
		for (String uri : hasValues) {
			ret += uri + ", ";
		}
		return ret;
	}

}
