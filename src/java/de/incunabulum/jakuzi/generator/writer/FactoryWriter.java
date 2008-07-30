package de.incunabulum.jakuzi.generator.writer;

import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.jmodel.utils.NamingUtils;
import de.incunabulum.jakuzi.utils.JavaUtils;

public class FactoryWriter {
	private static Log log = LogFactory.getLog(FactoryWriter.class);
	private static final String TEMPLATE_NAME = "factory.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;
	private String factoryName;
	private String toolsPackage;

	public FactoryWriter(VelocityEngine vEngine, VelocityContext vContext) {
		this.vEngine = vEngine;
		this.vContext = vContext;
	}

	public void writeFactory(JModel jmodel, String baseDir, String basePackage) {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = factoryName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating factory " + outPath);

		Template template;
		try {
			template = vEngine.getTemplate(TEMPLATE_NAME);
		} catch (ResourceNotFoundException e) {
			throw new RuntimeException();
		} catch (ParseErrorException e) {
			throw new RuntimeException();
		} catch (Exception e) {
			throw new RuntimeException();
		}

		try {
			FileWriter fWriter = new FileWriter(outPath);
			template.merge(vContext, fWriter);
			fWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

}
