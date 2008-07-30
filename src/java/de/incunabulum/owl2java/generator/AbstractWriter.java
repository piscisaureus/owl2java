package de.incunabulum.owl2java.generator;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;

import de.incunabulum.owl2java.model.jmodel.JModel;
import de.incunabulum.owl2java.model.jmodel.JPackage;
import de.incunabulum.owl2java.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.utils.JavaUtils;

public abstract class AbstractWriter {
	
	static Log log = LogFactory.getLog(AbstractWriter.class);
	
	protected String baseDir;
	protected String basePackage;
	protected String toolsPackage;

	protected JModel jmodel;
	protected VelocityEngine vEngine;
	
	public abstract void generate(JModel model, String baseDir, String basePackage);
	
	@SuppressWarnings("unchecked")
	protected void createPackageDirectories() {
		boolean success = true;

		log.info("Creating directory structure");
		// create base directory
		log.debug("Creating base directory " + baseDir);
		success &= new File(baseDir).mkdirs();

		// dito for all package directories
		Iterator<JPackage> pkgIt = jmodel.listPackages().iterator();
		while (pkgIt.hasNext()) {
			JPackage pkg = pkgIt.next();
			String pkgName = pkg.getPackageName();
			String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
			log.debug("Creating directory for package " + pkgName);
			success &= new File(pkgDir).mkdirs();
		}
		// finally for the tools package
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		log.debug("Creating tools directory for package " + pkgName);
		success &= new File(pkgDir).mkdirs();
	}

	protected void initVelocityEngine(String templateDir) {
		log.info("Init velocity engine");

		vEngine = new VelocityEngine();

		vEngine.setProperty("resource.loader", "file");
		vEngine.setProperty("file.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		String currentpath = JavaUtils.getCurrentDirectory();
		vEngine.setProperty("file.resource.loader.path", currentpath + "/" + "bin/" + templateDir); 

		// see http://minaret.biz/tips/tomcatLogging.html
		vEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogSystem");
		vEngine.setProperty("velocimacro.library", "macros.vm");
		try {
			vEngine.init();
		} catch (Exception e) {
			throw new RuntimeException();
		}

	}
	
	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}




}
