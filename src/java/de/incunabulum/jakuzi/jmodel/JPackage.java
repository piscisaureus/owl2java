package de.incunabulum.jakuzi.jmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.utils.IName;
import de.incunabulum.jakuzi.utils.IReporting;

public class JPackage implements IReporting, IName {

	private static Log log = LogFactory.getLog(JPackage.class);

	@SuppressWarnings("unused")
	private JModel jmodel;
	private String packageName;
	private List<JClass> classes = new ArrayList<JClass>();

	public JPackage(JModel model, String packageName) {
		this.packageName = packageName;
		this.jmodel = model;
	}

	public JPackage(JModel model, String basePackage, String prefix) {
		this.packageName = NamingUtils.getJavaPackageName(basePackage, prefix);
		this.jmodel = model;
	}

	@Override
	public String getReport() {
		log.warn("JPackage.toReport not implemented");
		return null;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public String getJavaName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	
	
	public List<JClass> listJClasses() {
		return classes;
	}

	public void addClass(JClass cls) {
		if (!classes.contains(cls)) {
			this.classes.add(cls);
			cls.setPackage(this);
		}
	}



	@Override
	public String getName() {
		return getPackageName();
	}
	


}