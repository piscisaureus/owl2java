package de.incunabulum.jakuzi.jmodel.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.UnionClass;

import de.incunabulum.jakuzi.jmodel.JClass;
import de.incunabulum.jakuzi.jmodel.JModel;
import de.incunabulum.jakuzi.jmodel.JPackage;
import de.incunabulum.jakuzi.jmodel.JProperty;
import de.incunabulum.jakuzi.model.NamespaceUtils;
import de.incunabulum.jakuzi.utils.JavaUtils;
import de.incunabulum.jakuzi.utils.StringUtils;

public class NamingUtils {

	public static String unionClassPrefix = "Union";
	public static String intersectionClassPrefix = "Intersection";

	public static String classNameAddOn = "";
	// %c = AddOn, %n = Name, %p = Prefix
	public static String classNamingSchema = "%c%n%p";
	public static String interfaceNameAddOn = "I";

	// %i = AddOn, %n = Name, %p = Prefix
	public static String interfaceNamingSchema = "%i%n%p";
	public static List<String> propertyIgnoredPrefixes;

	public static String propertyNamingSchema = "%n%p";
	public static boolean propertyStripPrefix = true;

	static {
		propertyIgnoredPrefixes = new ArrayList<String>();
		propertyIgnoredPrefixes.add("has");
		propertyIgnoredPrefixes.add("is");
	}

	public static String getJavaClassName(OntClass ontClass) {
		String nsUri = ontClass.getNameSpace();
		String prefix = ontClass.getModel().getNsURIPrefix(nsUri);

		// ignore base prefixes and namespaces
		if (NamespaceUtils.defaultNs2UriMapping.containsKey(nsUri))
			prefix = JModel.getBasePrefix();

		String localName = ontClass.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.getBasePrefix();
		}

		// to naming schema
		String name = classNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%c", classNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaClassName(String localName, String prefix) {
		String name = classNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%c", classNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaFullName(JPackage pkg, String className) {
		String name = pkg.getJavaName() + "." + className;
		return name;
	}

	public static String getJavaInterfaceName(OntClass ontClass) {
		String nsUri = ontClass.getNameSpace();
		String prefix = ontClass.getModel().getNsURIPrefix(nsUri);

		// ignore base prefixes and namespaces
		if (NamespaceUtils.defaultNs2UriMapping.containsKey(nsUri))
			prefix = JModel.getBasePrefix();

		String localName = ontClass.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.getBasePrefix();
		}

		// to naming schema
		String name = interfaceNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%i", interfaceNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaInterfaceName(String localName, String prefix) {
		String name = interfaceNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		name = name.replace("%i", interfaceNameAddOn);
		return StringUtils.toFirstUpperCase(name);
	}

	public static String getJavaPackageName(String basePackage, String prefix) {
		if (prefix != JModel.getBasePrefix())
			return basePackage + "." + prefix;
		return basePackage;
	}

	public static String getPropertyName(OntProperty ontProperty) {
		String nsUri = ontProperty.getNameSpace();
		String prefix = ontProperty.getModel().getNsURIPrefix(nsUri);
		String localName = ontProperty.getLocalName();
		if (prefix != null) {
			prefix = StringUtils.toFirstUpperCase(prefix);
		} else {
			prefix = JModel.getBasePrefix();
		}

		if (propertyStripPrefix)
			localName = stripPropertyPrefixes(localName);

		// to naming schema
		String name = propertyNamingSchema;
		name = name.replace("%n", localName);
		name = name.replace("%p", prefix);
		return StringUtils.toFirstLowerCase(name);
	}

	public static String getValidJavaName(String aName) {
		return JavaUtils.toValidJavaName(aName);
	}

	public static String stripPropertyPrefixes(String string) {
		for (String prefix : propertyIgnoredPrefixes)
			string = string.replace(prefix, JModel.getBasePrefix());
		return string;
	}

	public static String toLogName(JClass cls) {
		return cls.getJavaPackageName() + "." + cls.getName();
	}

	public static String toLogName(JProperty prop) {
		return prop.getName();
	}

	public static String toLogName(OntResource res) {
		String ns = res.getNameSpace();
		if (res.getModel().getNsURIPrefix(ns) != null) {
			return res.getModel().getNsURIPrefix(res.getNameSpace()) + "#"
					+ res.getLocalName();
		}
		return ns + res.getLocalName();
	}

	@SuppressWarnings("unchecked")
	public static String createUnionClassName(UnionClass cls) {
		String name = unionClassPrefix;
		Iterator operandIt = cls.listOperands();
		while (operandIt.hasNext()) {
			OntClass c = (OntClass) operandIt.next();
			name += StringUtils.toFirstUpperCase(c.getLocalName());
		}
		return name;
	}
	
	public static String createUnionClassName(JClass cls, List<JClass> operandClasses) {
		String name = unionClassPrefix;
		for (JClass c : operandClasses) {
			name += c.getJavaClassName();
		}
		return name;
	}

	@SuppressWarnings("unchecked")
	public static String createIntersectionClassName(IntersectionClass cls) {
		String name = intersectionClassPrefix;
		Iterator operandIt = cls.listOperands();
		while (operandIt.hasNext()) {
			OntClass c = (OntClass) operandIt.next();
			name += StringUtils.toFirstUpperCase(c.getLocalName());
		}
		return name;
	}
	
	public static String createIntersectionClassName(JClass cls, List<JClass> operandClasses) {
		String name = intersectionClassPrefix;
		for (JClass c : operandClasses) {
			name += c.getJavaClassName();
		}
		return name;
	}}
