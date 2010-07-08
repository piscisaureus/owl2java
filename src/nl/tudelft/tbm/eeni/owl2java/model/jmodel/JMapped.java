package nl.tudelft.tbm.eeni.owl2java.model.jmodel;

import nl.tudelft.tbm.eeni.owl2java.utils.IName;
import nl.tudelft.tbm.eeni.owl2java.utils.IReporting;
import nl.tudelft.tbm.eeni.owl2java.utils.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class JMapped implements IReporting, IName {

	private static Log log = LogFactory.getLog(JMapped.class);

	private String name;
	private String comment;
	private String mapUri;

	public JMapped(String name, String mappedTo) {
		assert mappedTo != null;
		this.name = name;
		this.mapUri = mappedTo;
	}

	public String getJModelReport() {
		log.warn("JMapped.toReport not implemented");
		return null;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getMapUri() {
		return mapUri;
	}

	public String getJavaNameCaps() {
		return StringUtils.toFirstUpperCase(getName());
	}

	public boolean equals(Object other) {
		// both instance of JMapped and Same MapUri
		return other instanceof JMapped && (((JMapped) other).getMapUri().equals(getMapUri()));
	}

	public int hashCode() {
		int hash = 12;
		hash = hash + mapUri.hashCode();
		return hash;
	}

}
