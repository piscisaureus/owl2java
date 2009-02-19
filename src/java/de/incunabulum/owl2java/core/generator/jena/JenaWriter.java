package de.incunabulum.owl2java.core.generator.jena;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;

import de.incunabulum.owl2java.core.generator.AbstractWriter;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.core.model.xsd.XsdMapTestData;

public class JenaWriter extends AbstractWriter {
	
	static Log log = LogFactory.getLog(JenaWriter.class);
	public static final String templateDirJena = "jenaTemplates"; 

	private String vocabularyName;
	private String factoryName;
	private String testClassName;
	private boolean generateTestClass;
	
	public static String getTemplatePath(String templateName) {
		return "/" + templateDirJena + "/" + templateName;
	}


	public void generate(JModel model, String baseDir, String basePackage) {
		this.baseDir = baseDir;
		this.jmodel = model;
		this.basePackage = basePackage;

		log.info("");
		log.info("Writing JModel to java");

		// create the package structure
		createPackageDirectories();

		// init the templating engine
		initVelocityEngine(templateDirJena);

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
		JenaTestWriter tWriter = new JenaTestWriter(vEngine, getBaseVelocityContext());
		tWriter.setTestClassName(testClassName);
		tWriter.setToolsPackage(toolsPackage);
		tWriter.writeTestCases(jmodel, baseDir, basePackage);
	}

	protected void createFactory() {
		log.info("Creating Factory");
		JenaFactoryWriter fWriter = new JenaFactoryWriter(vEngine, getBaseVelocityContext());
		fWriter.setFactoryName(factoryName);
		fWriter.setToolsPackage(toolsPackage);
		fWriter.writeFactory(jmodel, baseDir, basePackage);
	}

	protected void createVocabulary() {
		log.info("Creating vocabulary");
		JenaVocabularyWriter vWriter = new JenaVocabularyWriter(vEngine, getBaseVelocityContext());
		vWriter.setVocabularyName(vocabularyName);
		vWriter.setToolsPackage(toolsPackage);
		vWriter.writeVocabulary(jmodel, baseDir, basePackage);
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

	protected void createInterfaces() {
		log.info("Creating java interfaces");

		JenaInterfaceWriter ifaceWriter = new JenaInterfaceWriter(vEngine, getBaseVelocityContext());

		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
			ifaceWriter.writeInterface(jmodel, cls, baseDir);
		}
	}

	protected void createClasses() {
		log.info("Creating java classes");
		JenaClassWriter clsWriter = new JenaClassWriter(vEngine, getBaseVelocityContext());

		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
			clsWriter.writeClass(jmodel, cls, baseDir);
		}

	}

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
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
