package de.incunabulum.owl4java.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XsdUtils {
	
	private static Log log = LogFactory.getLog(XsdUtils.class);
	
	// Source: http://209.85.129.104/search?q=cache:SKIx_au0JPIJ:projects.semwebcentral.org/cgi-bin/viewcvs.cgi/kazuki/doc/design.txt%3Fcvsroot%3Dkazuki%26rev%3D1.4+xsd+type+java+jena+float&hl=en&ct=clnk&cd=3
	// Source: http://projects.semwebcentral.org/cgi-bin/viewcvs.cgi/kazuki/doc/design.txt?cvsroot=kazuki&rev=1.4
	public static Map<String, String> uri2java;

	static {
		uri2java = new HashMap<String, String>();
		// TODO: replace kazuki types with my types /  jena types
		uri2java.put("http://www.w3.org/2001/XMLSchema#ENTITY", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#ID", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#IDREF", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#NCName", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#NOTATION", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#Name", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#QName", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#anyURI", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#base64Binary", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#boolean", "java.lang.Boolean");
        uri2java.put("http://www.w3.org/2001/XMLSchema#byte", "java.lang.Byte");
        uri2java.put("http://www.w3.org/2001/XMLSchema#date", "java.util.Calendar");
        uri2java.put("http://www.w3.org/2001/XMLSchema#dateTime", "java.util.Calendar");
        uri2java.put("http://www.w3.org/2001/XMLSchema#decimal", "java.math.BigDecimal");
        uri2java.put("http://www.w3.org/2001/XMLSchema#double", "java.lang.Double");
        uri2java.put("http://www.w3.org/2001/XMLSchema#duration", "com.hp.hpl.jena.datatypes.xsd.XSDDuration");
        uri2java.put("http://www.w3.org/2001/XMLSchema#float", "java.lang.Float");
        uri2java.put("http://www.w3.org/2001/XMLSchema#gDay", "org.daml.kazuki.datatypes.XSDgDay");
        uri2java.put("http://www.w3.org/2001/XMLSchema#gMonth", "org.daml.kazuki.datatypes.XSDgMonth");
        uri2java.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "org.daml.kazuki.datatypes.XSDgMonthDay");
        uri2java.put("http://www.w3.org/2001/XMLSchema#gYear", "org.daml.kazuki.datatypes.XSDgYear");
        uri2java.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "org.daml.kazuki.datatypes.XSDgYearMonth");
        uri2java.put("http://www.w3.org/2001/XMLSchema#hexBinary", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#int", "java.lang.Integer");
        uri2java.put("http://www.w3.org/2001/XMLSchema#integer", "java.math.BigInteger");
        uri2java.put("http://www.w3.org/2001/XMLSchema#language", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#long", "java.lang.Long");
        uri2java.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "java.math.BigInteger");
        uri2java.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "java.math.BigInteger");
        uri2java.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "java.math.BigInteger");
        uri2java.put("http://www.w3.org/2001/XMLSchema#normalizedString", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "java.math.BigInteger");
        uri2java.put("http://www.w3.org/2001/XMLSchema#short", "java.lang.Short");
        uri2java.put("http://www.w3.org/2001/XMLSchema#string", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#time", "java.util.Calendar");
        uri2java.put("http://www.w3.org/2001/XMLSchema#token", "java.lang.String");
        uri2java.put("http://www.w3.org/2001/XMLSchema#unsignedByte", "java.lang.Short");
        uri2java.put("http://www.w3.org/2001/XMLSchema#unsignedInt", "java.lang.Long");
        uri2java.put("http://www.w3.org/2001/XMLSchema#unsignedLong", "java.lang.Long");
        uri2java.put("http://www.w3.org/2001/XMLSchema#unsignedShort", "java.lang.Integer");
        uri2java.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "java.lang.String");				
	}
	
	public static String xsd2Java(String xsdUri) {
		if (uri2java.containsKey(xsdUri))
			return uri2java.get(xsdUri);
		return new String();
	}

	@SuppressWarnings("unchecked")
	public static Class xsd2JavaClass(String xsdUri) {
		String clsName = uri2java.get(xsdUri);
		try {
			return Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			log.error("Xsd2JavaClass mapping: No valid class found for class name " + clsName);
			e.printStackTrace();
		}
		return null;
	}
	
	

}
