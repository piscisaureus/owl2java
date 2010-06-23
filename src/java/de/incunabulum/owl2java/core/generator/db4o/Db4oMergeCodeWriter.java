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
import de.incunabulum.owl2java.core.model.jmodel.JModel;
import de.incunabulum.owl2java.core.model.jmodel.utils.NamingUtils;
import de.incunabulum.owl2java.core.utils.JavaUtils;

public class Db4oMergeCodeWriter {
	private static Log log = LogFactory.getLog(Db4oMergeCodeWriter.class);
	private static final String TEMPLATE_NAME = "mergeCode.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;
	private ICodeWriterFactory codeWriterFactory;

	private String instanceName;
	private String toolsPackage;

	public Db4oMergeCodeWriter(VelocityEngine vEngine, VelocityContext vContext, ICodeWriterFactory codeWriterFactory) {
		this.vEngine = vEngine;
		this.vContext = vContext;
		this.codeWriterFactory = codeWriterFactory;
	}

	public void writeInstance(JModel jmodel, String baseDir,
			String basePackage) {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = instanceName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating merge code " + outPath);

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

		try {
			Writer writer = codeWriterFactory.getCodeWriter(outPath);
			template.merge(vContext, writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setInstanceName(String vocabularyName) {
		this.instanceName = vocabularyName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}



}
