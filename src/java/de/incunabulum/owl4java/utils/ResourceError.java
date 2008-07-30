package de.incunabulum.owl4java.utils;

import com.hp.hpl.jena.ontology.OntResource;

import de.incunabulum.owl4java.jmodel.utils.NamingUtils;

public class ResourceError implements IReporting {
	protected String string;
	protected OntResource item;
	
	public ResourceError(OntResource item, String string) {
		this.string = string;
		this.item = item;
	}
	
	
	@Override
	public String getReport() {
		return NamingUtils.toLogName(item) + ": " + string;
	}
	

}
