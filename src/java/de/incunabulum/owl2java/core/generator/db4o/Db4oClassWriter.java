package de.incunabulum.owl2java.core.generator.db4o;

import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.incunabulum.owl2java.core.generator.ICodeWriterFactory;
import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public class Db4oClassWriter {

	private static Log log = LogFactory.getLog(Db4oClassWriter.class);

	private static String TEMPLATE_NAME = "class.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;
	private ICodeWriterFactory codeWriterFactory;

	public Db4oClassWriter(VelocityEngine vEngine, VelocityContext vContext, ICodeWriterFactory codeWriterFactory) {
		this.vEngine = vEngine;
		this.vContext = vContext;
		this.codeWriterFactory = codeWriterFactory;
	}
	
	public void writeClass(JModel jmodel, JClass cls, String baseDir) {
		String outDir = JavaUtils.toDirectoryFromPackage(cls.getJavaPackageName(), baseDir);
		String outName = cls.getJavaClassName();
		String outPath = outDir + "/" + outName + ".java";
		log.info("Creating class " + outName);
		log.debug("Creating class as " + outPath);

		Template template;
		try {
			String templatePath = Db4oWriter.getTemplatePath(TEMPLATE_NAME);
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
			Writer writer = codeWriterFactory.getCodeWriter(outPath);
			template.merge(vContext, writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	


}
