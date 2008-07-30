package de.incunabulum.jakuzi.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;

public class XsdUtils {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(XsdUtils.class);

	/**
	 * Usage / Design: We map xsd types to java type names; then for each java type name the corresponding method to get
	 * the value can be derived. This method is used to get the method name, type... from the templates.
	 */
	public static Map<String, String> xsd2javaName = new HashMap<String, String>();
	public static Map<String, String> javaName2Method = new HashMap<String, String>();

	static {
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#ENTITY", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#ID", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#IDREF", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#NCName", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#NMTOKEN", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#NOTATION", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#Name", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#QName", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#anyURI", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#base64Binary", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#boolean", "java.lang.Boolean");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#byte", "java.lang.Byte");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#date", "java.util.Calendar");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#dateTime", "java.util.Calendar");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#decimal", "java.math.BigDecimal");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#double", "java.lang.Double");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#duration", "com.hp.hpl.jena.datatypes.xsd.XSDDuration");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#float", "java.lang.Float");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#gDay", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#gMonth", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#gMonthDay", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#gYear", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#gYearMonth", "com.hp.hpl.jena.datatypes.xsd.XSDDateTime");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#hexBinary", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#int", "java.lang.Integer");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#integer", "java.math.BigInteger");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#language", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#long", "java.lang.Long");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#negativeInteger", "java.math.BigInteger");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "java.math.BigInteger");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "java.math.BigInteger");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#normalizedString", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#positiveInteger", "java.math.BigInteger");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#short", "java.lang.Short");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#string", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#time", "java.util.Calendar");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#token", "java.lang.String");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#unsignedByte", "java.lang.Short");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#unsignedInt", "java.lang.Integer");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#unsignedLong", "java.lang.Long");
		xsd2javaName.put("http://www.w3.org/2001/XMLSchema#unsignedShort", "java.lang.Integer");
		xsd2javaName.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "java.lang.String");
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
		javaName2Method.put("com.hp.hpl.jena.datatypes.xsd.XSDDuration", "de.incunabulum.jakuzi.model.XsdUtils.getXSDDuration");
		javaName2Method.put("com.hp.hpl.jena.datatypes.xsd.XSDDateTime", "de.incunabulum.jakuzi.model.XsdUtils.getXSDDateTime");

	}

	public static Literal createTypedLiteral(OntModel m, Object o, String dt) {
		RDFDatatype rdfdt = TypeMapper.getInstance().getTypeByName(dt);
		if (o instanceof Calendar) {
			String lex = CalendarToXSD((Calendar) o, rdfdt);
			if (rdfdt.isValidValue(lex)) {
				return m.createTypedLiteral(lex, dt);
			} else {
				throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
			}
			// } else if (rdfdt.isValidValue(o)) {
		} else if (rdfdt.isValid(o.toString())) {
			// Don't want to store the object directly because the model should not contain
			// mutable objects.
			return m.createTypedLiteral(o.toString(), dt);
		} else {
			throw new DatatypeFormatException(o.toString(), rdfdt, "Value does not match datatype.");
		}
	}

	public static String getJavaClassName(String xsdUri) {
		return xsd2javaName.get(xsdUri);
	}
	
	public static String getAccessMethod(String xsdUri) {
		String javaClass = xsd2javaName.get(xsdUri);
		return javaName2Method.get(javaClass);
	}

	/**
	 * Methods to extra value of a specific java class / type from a literal
	 */

	public static String getString(Literal l) {
		return l.getString();
	}

	public static Integer getInteger(Literal l) {
		return new Integer(l.getInt());
	}

	public static Byte getByte(Literal l) {
		return new Byte(l.getByte());
	}

	public static Short getShort(Literal l) {
		return new Short(l.getShort());
	}

	public static Double getDouble(Literal l) {
		return new Double(l.getDouble());
	}

	public static Long getLong(Literal l) {
		return new Long(l.getLong());
	}

	public static Float getFloat(Literal l) {
		return new Float(l.getFloat());
	}

	public static Boolean getBoolean(Literal l) {
		return new Boolean(l.getBoolean());
	}

	public static Character getCharacter(Literal l) {
		return new Character(l.getChar());
	}

	public static Calendar getCalendar(Literal l) {
		try {
			Object o = l.getValue();
			if (o instanceof Calendar) {
				return (Calendar) o;
			} else if (o instanceof XSDDateTime) {
				// Will be fixed with next release of Jena.
				Calendar c = ((XSDDateTime) o).asCalendar();
				// c.set(Calendar.MONTH, c.get(Calendar.MONTH)-1);
				return c;
			}
		} catch (DatatypeFormatException e) {
			log.error("Error in getCalendar for literal " + l.toString());
		}
		return null;
	}

	public static XSDDateTime getXSDDateTime(Literal l) {
		Object o = l.getValue();
		if (o instanceof XSDDateTime) {
			XSDDateTime dt = (XSDDateTime) o;
			return dt;
		}
		return null;
	}

	public static XSDDuration getXSDDuration(Literal l) {
		Object o = l.getValue();
		if (o instanceof XSDDuration) {
			XSDDuration d = (XSDDuration) o;
			return d;
		}
		return null;
	}

	public static BigInteger getBigInteger(Literal l) {
		Object o = l.getValue();
		if (o instanceof BigInteger) {
			return (BigInteger) o;
		} else if (o instanceof Number) {
			Number n = (Number) o;
			try {
				// Throws NumberFormatException
				return new BigInteger(n.toString());
			} catch (NumberFormatException e) {
				log.error("Error in getBigInteger for literal " + l.toString());
				return null;
			}
		} else {

			return null;
		}
	}

	public static BigDecimal getBigDecimal(Literal l) {
		Object o = l.getValue();
		if (o instanceof BigDecimal) {
			return (BigDecimal) o;
		} else if (o instanceof Number) {
			Number n = (Number) o;
			try {
				// Throws NumberFormatException
				return new BigDecimal(n.toString());
			} catch (NumberFormatException e) {
				log.error("Error in getBigDecimal for literal " + l.toString());
				return null;
			}
		} else {
			return null;
		}
	}

	// ///////////////////////////////////////////////////
	// Helper function to convert from Calendar to various xsd date/time strings
	// ///////////////////////////////////////////////////

	// CCYY-MM-DD (w/ optional time zone)
	// timezone is "[+|-]hh:mm" or "Z" for UTC
	private static final SimpleDateFormat XSD_date = new SimpleDateFormat("yyyy-MM-ddZ");

	// CCYY-MM-DDThh:mm:ss (w/ optional time zone)
	// XXX: allow fractional seconds (any number of places).
	private static final SimpleDateFormat XSD_dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	// hh:mm:ss (w/ optional time zone)
	// XXX: allow fractional seconds (any number of places) hh:mm:ss.sss...
	private static final SimpleDateFormat XSD_time = new SimpleDateFormat("HH:mm:ss.SSSZ");

	// ---DD (w/ optional time zone)
	private static final SimpleDateFormat XSD_gDay = new SimpleDateFormat("---ddZ");

	// --MM-- (w/ optional time zone)
	private static final SimpleDateFormat XSD_gMonth = new SimpleDateFormat("--MM--Z");

	// --MM-DD (w/ optional time zone)
	private static final SimpleDateFormat XSD_gMonthDay = new SimpleDateFormat("--MM-ddZ");

	// CCYY (w/ optional time zone)
	private static final SimpleDateFormat XSD_gYear = new SimpleDateFormat("yyyyZ");

	// CCYY-MM (w/ optional time zone)
	private static final SimpleDateFormat XSD_gYearMonth = new SimpleDateFormat("yyyy-MMZ");

	/**
	 * Convenience function to generate lexical values from a Calendar object based on a specified RDF datatype.
	 */
	public static String CalendarToXSD(Calendar c, RDFDatatype dt) {
		String time = null;
		if (dt.equals(XSDDatatype.XSDdate)) {
			time = XSD_date.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDdateTime)) {
			time = XSD_dateTime.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgDay)) {
			time = XSD_gDay.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgMonth)) {
			time = XSD_gMonth.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgMonthDay)) {
			time = XSD_gMonthDay.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgYear)) {
			time = XSD_gYear.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDgYearMonth)) {
			time = XSD_gYearMonth.format(c.getTime());
		} else if (dt.equals(XSDDatatype.XSDtime)) {
			time = XSD_time.format(c.getTime());
		} else {
			return c.toString();
		}
		StringBuffer sb = new StringBuffer(time);
		sb.insert(sb.length() - 2, ':');
		return sb.toString();
	}

}
