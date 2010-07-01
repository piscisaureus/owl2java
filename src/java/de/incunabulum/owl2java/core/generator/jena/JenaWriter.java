package de.incunabulum.owl2java.core.generator.jena;

import java.io.File;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import de.incunabulum.owl2java.core.formatter.CodeFormattingWriter;
import de.incunabulum.owl2java.core.generator.AbstractCodeWriterFactory;
import de.incunabulum.owl2java.core.generator.ICodeWriterFactory;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.JPackage;
import de.incunabulum.owl2java.core.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.core.model.xsd.XsdMapTestData;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public class JenaWriter {
	
	static Log log = LogFactory.getLog(JenaWriter.class);
	public static final String templateDirJena = "jenaTemplates"; 

	private String vocabularyName;
	private String factoryName;
	private String testClassName;
	private boolean generateTestClass;
	protected String baseDir;
	protected String basePackage;
	protected String toolsPackage;
	private boolean enableCodeFormatting;
	private Properties codeFormatterOptions;
	protected JModel jmodel;
	protected VelocityEngine vEngine;
	
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
		JenaTestWriter tWriter = new JenaTestWriter(vEngine, getBaseVelocityContext(), getCodeWriterFactory());
		tWriter.setTestClassName(testClassName);
		tWriter.setToolsPackage(toolsPackage);
		tWriter.writeTestCases(jmodel, baseDir, basePackage);
	}

	protected void createFactory() {
		log.info("Creating Factory");
		JenaFactoryWriter fWriter = new JenaFactoryWriter(vEngine, getBaseVelocityContext(), getCodeWriterFactory());
		fWriter.setFactoryName(factoryName);
		fWriter.setToolsPackage(toolsPackage);
		fWriter.writeFactory(jmodel, baseDir, basePackage);
	}

	protected void createVocabulary() {
		log.info("Creating vocabulary");
		JenaVocabularyWriter vWriter = new JenaVocabularyWriter(vEngine, getBaseVelocityContext(), getCodeWriterFactory());
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

		JenaInterfaceWriter ifaceWriter = new JenaInterfaceWriter(vEngine, getBaseVelocityContext(), getCodeWriterFactory());

		Iterator<JClass> clsIt = jmodel.listJClasses().iterator();
		while (clsIt.hasNext()) {
			JClass cls = clsIt.next();
			ifaceWriter.writeInterface(jmodel, cls, baseDir);
		}
	}

	protected void createClasses() {
		log.info("Creating java classes");
		JenaClassWriter clsWriter = new JenaClassWriter(vEngine, getBaseVelocityContext(), getCodeWriterFactory());

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


	/**
	 * Creates a factory for stream writers that write source code to a specified file
	 */
	protected ICodeWriterFactory getCodeWriterFactory() {
		return new AbstractCodeWriterFactory() {
			@Override
			public Writer getCodeWriter(Writer destination) {
				if (enableCodeFormatting) {
					// If source code formatting is enabled, wrap the writer in a source code formatting writer
					return new CodeFormattingWriter(destination, codeFormatterOptions); 
				} else {
					// Otherwise just return the destination writer
					return destination;
				}
			}
		};
	}


	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}


	public void setEnableCodeFormatting(boolean enableCodeFormatting) {
		this.enableCodeFormatting = enableCodeFormatting;
	}


	public void setCodeFormatterOptions(Properties codeFormatterOptions) {
		this.codeFormatterOptions = codeFormatterOptions;
	}

}
