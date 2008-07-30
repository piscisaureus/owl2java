package de.incunabulum.jakuzi.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.incunabulum.jakuzi.generator.writer.ClassWriter;
import de.incunabulum.jakuzi.generator.writer.FactoryWriter;
import de.incunabulum.jakuzi.generator.writer.InterfaceWriter;
import de.incunabulum.jakuzi.generator.writer.TestWriter;
import de.incunabulum.jakuzi.generator.writer.VocabularyWriter;
import de.incunabulum.jakuzi.jmodel.JClass;
import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.jmodel.JPackage;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.model.XsdMapTestData;
import de.incunabulum.jakuzi.utils.JavaUtils;

public class JavaWriter {

	private static Log log = LogFactory.getLog(JavaWriter.class);

	VelocityEngine vEngine;

	private String baseDir;
	private String basePackage;
	private String vocabularyName;
	private String factoryName;
	private String toolsPackage;
	private String testClassName;
	private boolean generateTestClass;

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

		// write interfaces
		createInterfaces();

		// write classes
		createClasses();

		// write vocabulary
		createVocabulary();

		// write factory
		createFactory();
		
		// write testclass
		if (generateTestClass) {
			createTestClass();
		}
	}
	
	protected void createTestClass() {
		log.info("Creating Testclass");
		TestWriter tWriter = new TestWriter(vEngine, getBaseVelocityContext());
		tWriter.setTestClassName(testClassName);
		tWriter.setToolsPackage(toolsPackage);
		tWriter.writeTestCases(jmodel, baseDir, basePackage);		
	}

	protected void createFactory() {
		log.info("Creating Factory");
		FactoryWriter fWriter = new FactoryWriter(vEngine, getBaseVelocityContext());
		fWriter.setFactoryName(factoryName);
		fWriter.setToolsPackage(toolsPackage);
		fWriter.writeFactory(jmodel, baseDir, basePackage);
	}

	protected void createVocabulary() {
		log.info("Creating vocabulary");
		VocabularyWriter vWriter = new VocabularyWriter(vEngine, getBaseVelocityContext());
		vWriter.setVocabularyName(vocabularyName);
		vWriter.setToolsPackage(toolsPackage);
		vWriter.writeVocabulary(jmodel, baseDir, basePackage);
	}

	protected void initVelocityEngine() {
		log.info("Init velocity engine");

		vEngine = new VelocityEngine();
		vEngine.setProperty("resource.loader", "class");
		vEngine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// see http://minaret.biz/tips/tomcatLogging.html
		vEngine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogSystem");
		vEngine.setProperty("velocimacro.library", "macros.vm");
		try {
			vEngine.init();
		} catch (Exception e) {
			throw new RuntimeException();
		}

	}

	protected VelocityContext getBaseVelocityContext() {
		// add some default stuff to our context. These are reused over all writers
		VelocityContext vContext = new VelocityContext();
		Calendar c = Calendar.getInstance();
		vContext.put("now", SimpleDateFormat.getInstance().format(c.getTime()));
		vContext.put("pkgBase", basePackage);
		vContext.put("pkgTools", toolsPackage);
		vContext.put("jmodel", jmodel);
		vContext.put("factoryName", factoryName);
		vContext.put("factoryPkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		vContext.put("vocabName", vocabularyName);
		vContext.put("vocabPkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		vContext.put("testcaseName", testClassName);
		vContext.put("testcasePkg", NamingUtils.getJavaPackageName(basePackage, toolsPackage));
		XsdMapTestData xsdMap = new XsdMapTestData();
		vContext.put("xsdMap", xsdMap);
		return vContext;
	}

	@SuppressWarnings("unchecked")
	protected void createInterfaces() {
		log.info("Creating java interfaces");

		InterfaceWriter ifaceWriter = new InterfaceWriter(vEngine, getBaseVelocityContext());

		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls =  clsIt.next();
			ifaceWriter.writeInterface(jmodel, cls, baseDir);
		}
	}

	@SuppressWarnings("unchecked")
	protected void createClasses() {
		log.info("Creating java classes");
		ClassWriter clsWriter = new ClassWriter(vEngine, getBaseVelocityContext());
		
		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
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

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}


	public void setCreateTestClass(boolean createTestClass) {
		this.generateTestClass = createTestClass;
	}

	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

}
