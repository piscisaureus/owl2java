package de.incunabulum.jakuzi.generator.writer;

import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.incunabulum.jakuzi.jmodel.JClass;
import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.utils.JavaUtils;

public class ClassWriter {
	
	// TODO: Templates: Parent properties, restrictions
	// TODO: Templates: inverses & equivalent properties, equivalent classes not used
	
	private static Log log = LogFactory.getLog(ClassWriter.class);

	private static String TEMPLATE_NAME = "class.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;

	public ClassWriter(VelocityEngine vEngine, VelocityContext vContext) {
		this.vEngine = vEngine;
		this.vContext = vContext;
	}

	public void writeClass(JModel jmodel, JClass cls, String baseDir) {
		String outDir = JavaUtils.toDirectoryFromPackage(cls.getJavaPackageName(), baseDir);
		String outName = cls.getJavaClassName();
		String outPath = outDir + "/" + outName + ".java";
		log.info("Creating class " + outName);
		log.debug("Creating class as " + outPath);

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

		vContext.put("cls", cls);

		try {
			FileWriter fWriter = new FileWriter(outPath);
			template.merge(vContext, fWriter);
			fWriter.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
