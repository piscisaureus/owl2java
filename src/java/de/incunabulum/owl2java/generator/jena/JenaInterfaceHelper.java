package de.incunabulum.owl2java.generator.jena;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.core.model.jmodel.JClass;
import de.incunabulum.owl2java.core.utils.StringUtils;

public class JenaInterfaceHelper {
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(JenaInterfaceHelper.class);

	private JClass cls;

	public JenaInterfaceHelper(JClass cls) {
		this.cls = cls;
	}

	public String getExtends() {
		String ret = new String();

		// no super classes > base.Thing
		List<JClass> superClasses = cls.listDirectSuperClasses();
		Iterator<JClass> superClassesIt = superClasses.iterator();
		while (superClassesIt.hasNext()) {
			JClass i = (JClass) superClassesIt.next();
			String str = StringUtils.indentText(i.getJavaInterfaceFullName(), 2);
			if (superClassesIt.hasNext())
				str += ",\n";
			ret += str;
		}
		return ret;
	}


	



	


	
}
