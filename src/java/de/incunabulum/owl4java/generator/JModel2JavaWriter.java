package de.incunabulum.owl4java.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.incunabulum.owl4java.generator.writer.ClassWriter;
import de.incunabulum.owl4java.generator.writer.FactoryWriter;
import de.incunabulum.owl4java.generator.writer.InterfaceWriter;
import de.incunabulum.owl4java.generator.writer.VocabularyWriter;
import de.incunabulum.owl4java.jmodel.JClass;
import de.incunabulum.owl4java.jmodel.JModel;
import de.incunabulum.owl4java.jmodel.utils.NamingUtils;
import de.incunabulum.owl4java.utils.JavaUtils;

public class JModel2JavaWriter {

	private static Log log = LogFactory.getLog(JModel2JavaWriter.class);

	VelocityEngine vEngine;
	VelocityContext vContext;

	private String baseDir;
	private String basePackage;
	private String vocabularyName;
	private String factoryName;
	private String toolsPackage;
	
	private JModel jmodel;

	public void generate(JModel model, String baseDir, String basePackage) {
		this.baseDir = baseDir;
		this.jmodel = model;
		this.basePackage = basePackage;

		log.info("");
		log.info("Writing JModel to java");

		// create the package structure
		createPackageDirectories();

		// init the templating engine
		initVelocityEngine();

		// create all interfaces
		createInterfaces();

		// write classes
		createClasses();

		// write vocabulary
		createVocabulary();

		// write factory
		createFactory();
	}

	@SuppressWarnings("unchecked")
	protected void createFactory() {
		log.info("Creating Factory");
		FactoryWriter fWriter = new FactoryWriter(vEngine, vContext);
		fWriter.setFactoryName(factoryName);
		fWriter.setToolsPackage(toolsPackage);
		fWriter.writeFactory(jmodel, baseDir, basePackage);
	}

	@SuppressWarnings("unchecked")
	protected void createVocabulary() {
		log.info("Creating vocabulary");
		VocabularyWriter vWriter = new VocabularyWriter(vEngine, vContext);
		vWriter.setVocabularyName(vocabularyName);
		vWriter.setToolsPackage(toolsPackage);
		vWriter.writeVocabulary(jmodel, baseDir, basePackage);
	}

	protected void initVelocityEngine() {
		log.info("Init velocity engine");

		vEngine = new VelocityEngine();
		vContext = new VelocityContext();

		vEngine.setProperty("resource.loader", "class");
		vEngine
				.setProperty("class.resource.loader.class",
						"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// see http://minaret.biz/tips/tomcatLogging.html
		// vEngine.setProperty("runtime.log.logsystem.class",
		// "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
		vEngine.setProperty("runtime.log.logsystem.class",
				"org.apache.velocity.runtime.log.Log4JLogSystem");
		try {
			vEngine.init();
		} catch (Exception e) {
			throw new RuntimeException();
		}

		Calendar c = Calendar.getInstance();
		vContext.put("now", SimpleDateFormat.getInstance().format(c.getTime()));
	}

	@SuppressWarnings("unchecked")
	protected void createInterfaces() {
		log.info("Creating java interfaces");

		InterfaceWriter ifaceWriter = new InterfaceWriter(vEngine, vContext);

		Iterator clsIt = jmodel.uri2class.keySet().iterator();
		while (clsIt.hasNext()) {
			String clsUri = (String) clsIt.next();
			JClass cls = jmodel.getJClass(clsUri);
			ifaceWriter.writeInterface(jmodel, cls, baseDir);
		}
	}

	@SuppressWarnings("unchecked")
	protected void createClasses() {
		log.info("Creating java classes");
		ClassWriter clsWriter = new ClassWriter(vEngine, vContext);

		Iterator clsIt = jmodel.uri2class.keySet().iterator();
		while (clsIt.hasNext()) {
			String clsUri = (String) clsIt.next();
			JClass cls = jmodel.getJClass(clsUri);
			clsWriter.writeClass(jmodel, cls, baseDir);
		}

	}

	@SuppressWarnings("unchecked")
	protected void createPackageDirectories() {
		boolean success = true;

		log.info("Creating directory structure");
		// create base directory
		log.debug("Creating base directory " + baseDir);
		success &= new File(baseDir).mkdirs();

		// dito for all package directories
		Iterator pkgNameIt = jmodel.pkgName2Package.keySet().iterator();
		while (pkgNameIt.hasNext()) {
			String pkgName = (String) pkgNameIt.next();
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

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

}
