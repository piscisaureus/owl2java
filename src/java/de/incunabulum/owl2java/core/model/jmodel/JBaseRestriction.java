package de.incunabulum.owl2java.core.model.jmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.core.model.jmodel.utils.LogUtils;
import de.incunabulum.owl2java.core.utils.IReporting;

public class JBaseRestriction implements IReporting {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JBaseRestriction.class);

	JModel jModel;
	JClass onClass;
	JProperty onProperty;

	// true, if only a placeholder is there (no significant restrictions) apply
	boolean isEmpty = true;

	public JBaseRestriction(JClass onClass, JProperty onProperty) {
		this.jModel = onClass.getJModel();
		this.onClass = onClass;
		this.onProperty = onProperty;
	}

	@Override
	public String getJModelReport() {
		return (LogUtils.toLogName(this) + ": Base restriction");
	}

	public JProperty getOnProperty() {
		return onProperty;
	}

	public JClass getOnClass() {
		return onClass;
	}

}
