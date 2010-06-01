package de.incunabulum.owl2java.core.generator;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;

import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.JPackage;
import de.incunabulum.owl2java.core.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public abstract class AbstractWriter {

	static Log log = LogFactory.getLog(AbstractWriter.class);

	static String templateDirBase = "";

	protected String baseDir;
	protected String basePackage;
	protected String toolsPackage;

	protected JModel jmodel;
	protected VelocityEngine vEngine;

	public abstract void generate(JModel model, String baseDir, String basePackage);

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
			if (pkg.listJClasses().size() > 0) {
				String pkgName = pkg.getPackageName();
				String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
				log.debug("Creating directory for package " + pkgName);
				success &= new File(pkgDir).mkdirs();
			}
		}
		// finally for the tools package
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String pkgDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		log.debug("Creating tools directory for package " + pkgName);
		success &= new File(pkgDir).mkdirs();
	}

	protected void initVelocityEngine(String baseDir) {
		log.info("Init velocity engine");

		vEngine = new VelocityEngine();

		vEngine.setProperty("resource.loader", "class");
		vEngine.setProperty("class.resource.loader.description",
				"Velocity Classpath Resource Loasder");
		vEngine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

		// see http://minaret.biz/tips/tomcatLogging.html
		vEngine.setProperty("runtime.log.logsystem.class",
				"org.apache.velocity.runtime.log.Log4JLogSystem");
		vEngine.setProperty("velocimacro.library", "/" + baseDir + "/" + "macros.vm");
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
