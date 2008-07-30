package de.incunabulum.owl2java.core.model.jenautils;

import com.hp.hpl.jena.ontology.OntResource;

import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.core.utils.IReporting;

public class ResourceError implements IReporting {
	protected String string;
	protected OntResource item;
	
	public ResourceError(OntResource item, String string) {
		this.string = string;
		this.item = item;
	}
	
	
	@Override
	public String getJModelReport() {
		return LogUtils.toLogName(item) + ": " + string;
	}
	

}
