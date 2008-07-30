package de.incunabulum.jakuzi.jmodel;

import com.hp.hpl.jena.ontology.Restriction;

import de.incunabulum.jakuzi.utils.IReporting;
import de.incunabulum.jakuzi.utils.StringUtils;


public class JClassRestriction implements IReporting {
	
	@SuppressWarnings("unused")
	private JModel jmodel;
	private Restriction ontRestriction;

	protected int maxCardinality = -1;
	protected int minCardinality = 0;

	protected JProperty onProp;
	protected JClass onCls;

	protected JClass allValues;

	public JClassRestriction(JModel model, JClass cls, JProperty prop) {
		// register the restriction
		cls.addRestriction(this);
		prop.addRestriction(this);
		this.onProp = prop;
		this.onCls = cls;
		this.jmodel = model;
	}

	public void setMaxCardinality(int maxCardinality) {
		this.maxCardinality = maxCardinality;
	}
	
	public boolean hasAllValuesRestriction() {
		if (allValues != null)
			return true;
		return false;
	}

	public void setMinCardinality(int minCardinality) {
		this.minCardinality = minCardinality;
	}

	public void setAllValuesDomain(JClass cls) {
		this.allValues = cls;
	}

	@Override
	public String getReport() {
		String report = StringUtils.indentText("- On Class: " + onCls.getJavaClassFullName() + "\n",3);
		report += StringUtils.indentText("  On Property: " + onProp.getJavaName() + "\n",3);
		if (allValues != null)
			report += StringUtils.indentText("  AllValues from: " + allValues.getJavaClassFullName() + "\n",3);
		report += StringUtils.indentText("  Cardinality: Max=" + maxCardinality + ", Min=" + minCardinality + "\n",3);
		return report;
	}

	public Restriction getOntRestriction() {
		return ontRestriction;
	}

	public void setOntRestriction(Restriction ontRestriction) {
		this.ontRestriction = ontRestriction;
	}

	public JProperty getOnProp() {
		return onProp;
	}

	public JClass getOnCls() {
		return onCls;
	}

}
