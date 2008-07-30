package de.incunabulum.owl4java.generator.writer;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl4java.jmodel.JClass;
import de.incunabulum.owl4java.utils.StringUtils;

public class InterfaceHelper {
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(InterfaceHelper.class);

	private JClass cls;

	public InterfaceHelper(JClass cls) {
		this.cls = cls;
	}

	public String getExtends() {
		String ret = new String();

		// no super classes > base.Thing
		List<JClass> superClasses = cls.getSuperClasses();
		Iterator<JClass> superClassesIt = superClasses.iterator();
		while (superClassesIt.hasNext()) {
			JClass i = (JClass) superClassesIt.next();
			String str = StringUtils.indentText(i.getJavaInterfaceName(), 2);
			if (superClassesIt.hasNext())
				str += ",\n";
			ret += str;
		}
		return ret;
	}


	



	


	
}
