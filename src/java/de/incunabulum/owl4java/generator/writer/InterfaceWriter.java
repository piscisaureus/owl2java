package de.incunabulum.owl4java.generator.writer;

import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import de.incunabulum.owl4java.jmodel.JClass;
import de.incunabulum.owl4java.jmodel.JModel;
import de.incunabulum.owl4java.utils.JavaUtils;

public class InterfaceWriter {

	private static Log log = LogFactory.getLog(InterfaceWriter.class);

	private static String TEMPLATE_NAME = "interface.vm";

	private VelocityEngine vEngine;
	private VelocityContext vContext;

	public InterfaceWriter(VelocityEngine vEngine, VelocityContext vContext) {
		this.vEngine = vEngine;
		this.vContext = vContext;
		
	}

	public void writeInterface(JModel jmodel, JClass cls, String baseDir) {
		String outDir = JavaUtils.toDirectoryFromPackage(cls.getJavaPackageName(), baseDir);
		String outName = cls.getJavaInterfaceName();
		String outPath = outDir + "/" + outName + ".java";
		log.info("Creating interface " + outName);
		log.debug("Creating interface as " + outPath);

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

		InterfaceHelper ifaceHelper = new InterfaceHelper(cls);
		vContext.put("ifh", ifaceHelper);
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
