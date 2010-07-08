package nl.tudelft.tbm.eeni.owl2java.model.jenautils;

import nl.tudelft.tbm.eeni.owl2java.model.jmodel.utils.LogUtils;
import nl.tudelft.tbm.eeni.owl2java.utils.IReporting;

import com.hp.hpl.jena.ontology.OntResource;


public class ResourceError implements IReporting {
	protected String string;
	protected OntResource item;
	
	public ResourceError(OntResource item, String string) {
		this.string = string;
		this.item = item;
	}
	
	
	public String getJModelReport() {
		return LogUtils.toLogName(item) + ": " + string;
	}
	

}
