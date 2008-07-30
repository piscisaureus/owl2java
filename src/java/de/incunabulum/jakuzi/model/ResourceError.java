package de.incunabulum.jakuzi.model;

import com.hp.hpl.jena.ontology.OntResource;

import de.incunabulum.jakuzi.jmodel.utils.LogUtils;
import de.incunabulum.jakuzi.utils.IReporting;

public class ResourceError implements IReporting {
	protected String string;
	protected OntResource item;
	
	public ResourceError(OntResource item, String string) {
		this.string = string;
		this.item = item;
	}
	
	
	@Override
	public String getReport() {
		return LogUtils.toLogName(item) + ": " + string;
	}
	

}
