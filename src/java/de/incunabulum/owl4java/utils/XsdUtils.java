package de.incunabulum.owl4java.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XsdUtils {
	
	private static Log log = LogFactory.getLog(XsdUtils.class);
	
	// Source: http://projects.semwebcentral.org/cgi-bin/viewcvs.cgi/kazuki/doc/design.txt?cvsroot=kazuki&rev=1.4
	public static Map<String, String> xsdTypes;


	static {
		xsdTypes = new HashMap<String, String>();
		// TODO: replace kazuki types with my types /  jena types
		xsdTypes.put("http://www.w3.org/2001/XMLSchema#ENTITY", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#ID", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#IDREF", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#NCName", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#NOTATION", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#Name", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#QName", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#anyURI", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#base64Binary", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#boolean", "java.lang.Boolean");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#byte", "java.lang.Byte");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#date", "java.util.Calendar");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#dateTime", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#decimal", "java.math.BigDecimal");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#double", "java.lang.Double");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#duration", "com.hp.hpl.jena.datatypes.xsd.XSDDuration");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#float", "java.lang.Float");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#gDay", "org.daml.kazuki.datatypes.XSDgDay");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#gMonth", "org.daml.kazuki.datatypes.XSDgMonth");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "org.daml.kazuki.datatypes.XSDgMonthDay");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#gYear", "org.daml.kazuki.datatypes.XSDgYear");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "org.daml.kazuki.datatypes.XSDgYearMonth");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#hexBinary", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#int", "java.lang.Integer");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#integer", "java.math.BigInteger");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#language", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#long", "java.lang.Long");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "java.math.BigInteger");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "java.math.BigInteger");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "java.math.BigInteger");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#normalizedString", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "java.math.BigInteger");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#short", "java.lang.Short");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#string", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#time", "java.util.Calendar");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#token", "java.lang.String");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#unsignedByte", "java.lang.Short");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#unsignedInt", "java.lang.Long");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#unsignedLong", "java.lang.Long");
        xsdTypes.put("http://www.w3.org/2001/XMLSchema#unsignedShort", "java.lang.Integer");
        xsdTypes.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "java.lang.String");				
	}
	
	
	public static String xsd2Java(String xsdUri) {
		if (xsdTypes.containsKey(xsdUri))
			return xsdTypes.get(xsdUri);
		return new String();
	}

	@SuppressWarnings("unchecked")
	public static Class xsd2JavaClass(String xsdUri) {
		String clsName = xsdTypes.get(xsdUri);
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			log.error("Xsd2JavaClass mapping: No valid class found for class name " + clsName);
			e.printStackTrace();
		}
		return null;
	}
	
	
	

}
