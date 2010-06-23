package de.incunabulum.owl2java.core.generator.jena;

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

public class JenaVocabularyWriter {
	private static Log log = LogFactory.getLog(JenaVocabularyWriter.class);
	private static final String TEMPLATE_NAME = "vocabulary.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;
	private ICodeWriterFactory codeWriterFactory;

	private String vocabularyName;
	private String toolsPackage;

	public JenaVocabularyWriter(VelocityEngine vEngine, VelocityContext vContext, ICodeWriterFactory codeWriterFactory) {
		this.vEngine = vEngine;
		this.vContext = vContext;
		this.codeWriterFactory = codeWriterFactory;
	}

	public void writeVocabulary(JModel jmodel, String baseDir,
			String basePackage) {
		String pkgName = NamingUtils.getJavaPackageName(basePackage, toolsPackage);
		String outDir = JavaUtils.toDirectoryFromPackage(pkgName, baseDir);
		String outName = vocabularyName;
		String outPath = outDir + "/" + outName + ".java";
		log.debug("Creating vocabulary " + outPath);

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


		try {
			Writer writer = codeWriterFactory.getCodeWriter(outPath);
			template.merge(vContext, writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}

	public void setToolsPackage(String toolsPackage) {
		this.toolsPackage = toolsPackage;
	}

}
