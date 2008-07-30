package de.incunabulum.jakuzi.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.JModel;

public class JModelPreparation {

	private static Log log = LogFactory.getLog(JModelPreparation.class);
	
	private boolean reasignDomainlessProperties;
	private JModel jmodel;
	
	public JModel prepareModel(JModel model) {
		this.jmodel = model;
		log.info("Prepaing model for class writer");
		
		// assign properties without domain to restrictions
		if (reasignDomainlessProperties)
			reasignProperties();
		
		// add all parent properties to a class
		aggregateProperties();
		
		// dito for the restrictions
		aggregateRestrictions();
		
		// remove duplicate restrictions; cleanup
		cleanupRestrictions();
		
		return this.jmodel;
	}
	
	protected void reasignProperties() {
		
	}
	
	protected void aggregateProperties() {
		
	}
	
	protected void aggregateRestrictions() {
		
	}
	
	protected void  cleanupRestrictions() {
		
	}



	public void setReasignDomainlessProperties(boolean reasignDomainlessProperties) {
		this.reasignDomainlessProperties = reasignDomainlessProperties;
	}
}
