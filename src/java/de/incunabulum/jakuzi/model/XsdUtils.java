package de.incunabulum.jakuzi.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

	// ///////////////////////////////////////////////////
	// Helper function to convert from Calendar to various xsd date/time strings
	// ///////////////////////////////////////////////////

	// CCYY-MM-DD (w/ optional time zone)
	// timezone is "[+|-]hh:mm" or "Z" for UTC
	private static final SimpleDateFormat XSD_date = new SimpleDateFormat("yyyy-MM-ddZ");

	// CCYY-MM-DDThh:mm:ss (w/ optional time zone)
	private static final SimpleDateFormat XSD_dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	// hh:mm:ss (w/ optional time zone)
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
	
	public static Literal createTypedLiteral(OntModel ontModel, Object obj, String dataType) {
		RDFDatatype rdfdt = TypeMapper.getInstance().getTypeByName(dataType);
		if (obj instanceof Calendar) {
			String lex = CalendarToXSD((Calendar) obj, rdfdt);
			if (rdfdt.isValidValue(lex)) {
				return ontModel.createTypedLiteral(lex, dataType);
			} else {
				throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
			}
			// } else if (rdfdt.isValidValue(o)) {
		} else if (rdfdt.isValid(obj.toString())) {
			// Don't want to store the object directly because the model should not contain
			// mutable objects.
			return ontModel.createTypedLiteral(obj.toString(), dataType);
		} else {
			throw new DatatypeFormatException(obj.toString(), rdfdt, "Value does not match datatype.");
		}
	}
	
	/**
	 * Methods to extract value of a specific java class / type from a literal
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


}
