package de.incunabulum.jakuzi.model;

import java.util.HashMap;
import java.util.Map;
import de.incunabulum.jakuzi.model.XsdSchema;

public class XsdMapConfig {

	/**
	 * Usage / Design: - We map xsd types to java type names; - Then for each java type name the corresponding method to
	 * get the value can be derived. This method is used to get the method name, type... from the templates.
	 */
	public static Map<String, String> xsd2javaName = new HashMap<String, String>();
	public static Map<String, String> javaName2Method = new HashMap<String, String>();

	static {
		xsd2javaName.put(XsdSchema.xsdENTITY, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdID, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdIDREF, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdNCName, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdNMTOKEN, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdNOTATION, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdName, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdQName, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdanyURI, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdbase64Binary, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdboolean, "java.lang.Boolean");
		xsd2javaName.put(XsdSchema.xsdbyte, "java.lang.Byte");
		xsd2javaName.put(XsdSchema.xsddate, "java.util.Calendar");
		xsd2javaName.put(XsdSchema.xsddateTime, "java.util.Calendar");
		xsd2javaName.put(XsdSchema.xsddecimal, "java.math.BigDecimal");
		xsd2javaName.put(XsdSchema.xsddouble, "java.lang.Double");
		xsd2javaName.put(XsdSchema.xsdduration, "com.hp.hpl.jena.datatypes.xsd.XSDDuration");
		xsd2javaName.put(XsdSchema.xsdfloat, "java.lang.Float");
		xsd2javaName.put(XsdSchema.xsdgDay, "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put(XsdSchema.xsdgMonth, "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put(XsdSchema.xsdgMonthDay, "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put(XsdSchema.xsdgYear, "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put(XsdSchema.xsdgYearMonth, "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put(XsdSchema.xsdhexBinary, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdint, "java.lang.Integer");
		xsd2javaName.put(XsdSchema.xsdinteger, "java.math.BigInteger");
		xsd2javaName.put(XsdSchema.xsdlanguage, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdlong, "java.lang.Long");
		xsd2javaName.put(XsdSchema.xsdnegativeInteger, "java.math.BigInteger");
		xsd2javaName.put(XsdSchema.xsdnonNegativeInteger, "java.math.BigInteger");
		xsd2javaName.put(XsdSchema.xsdnonPositiveInteger, "java.math.BigInteger");
		xsd2javaName.put(XsdSchema.xsdnormalizedString, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdpositiveInteger, "java.math.BigInteger");
		xsd2javaName.put(XsdSchema.xsdshort, "java.lang.Short");
		xsd2javaName.put(XsdSchema.xsdstring, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdtime, "java.util.Calendar");
		xsd2javaName.put(XsdSchema.xsdtoken, "java.lang.String");
		xsd2javaName.put(XsdSchema.xsdunsignedByte, "java.lang.Short");
		xsd2javaName.put(XsdSchema.xsdunsignedInt, "java.lang.Integer");
		xsd2javaName.put(XsdSchema.xsdunsignedLong, "java.lang.Long");
		xsd2javaName.put(XsdSchema.xsdunsignedShort, "java.lang.Integer");
		xsd2javaName.put(XsdSchema.xsdLiteral, "java.lang.String");
	}

	static {
		javaName2Method.put("java.math.BigDecimal", "de.incunabulum.jakuzi.model.XsdUtils.getBigDecimal");
		javaName2Method.put("java.math.BigInteger", "de.incunabulum.jakuzi.model.XsdUtils.getBigInteger");
		javaName2Method.put("java.lang.Boolean", "de.incunabulum.jakuzi.model.XsdUtils.getBoolean");
		javaName2Method.put("java.lang.Byte", "de.incunabulum.jakuzi.model.XsdUtils.getByte");
		javaName2Method.put("java.lang.Character", "de.incunabulum.jakuzi.model.XsdUtils.getCharacter");
		javaName2Method.put("java.lang.Double", "de.incunabulum.jakuzi.model.XsdUtils.getDouble");
		javaName2Method.put("java.lang.Float", "de.incunabulum.jakuzi.model.XsdUtils.getFloat");
		javaName2Method.put("java.lang.Integer", "de.incunabulum.jakuzi.model.XsdUtils.getInteger");
		javaName2Method.put("java.lang.Long", "de.incunabulum.jakuzi.model.XsdUtils.getLong");
		javaName2Method.put("java.lang.Short", "de.incunabulum.jakuzi.model.XsdUtils.getShort");
		javaName2Method.put("java.lang.String", "de.incunabulum.jakuzi.model.XsdUtils.getString");
		javaName2Method.put("java.util.Calendar", "de.incunabulum.jakuzi.model.XsdUtils.getCalendar");
		javaName2Method.put("com.hp.hpl.jena.datatypes.xsd.XSDDuration",
				"de.incunabulum.jakuzi.model.XsdUtils.getXSDDuration");
		javaName2Method.put("com.hp.hpl.jena.datatypes.xsd.XSDDateTime",
				"de.incunabulum.jakuzi.model.XsdUtils.getXSDDateTime");
	}

}
