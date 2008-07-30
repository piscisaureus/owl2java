package de.incunabulum.owl2java.core.generator.jena;

import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public class JenaClassWriter {
	
	
	private static Log log = LogFactory.getLog(JenaClassWriter.class);

	private static String TEMPLATE_NAME = "class.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;

	public JenaClassWriter(VelocityEngine vEngine, VelocityContext vContext) {
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
			String templatePath = JenaWriter.getTemplatePath(TEMPLATE_NAME);
			template = vEngine.getTemplate(templatePath);
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
