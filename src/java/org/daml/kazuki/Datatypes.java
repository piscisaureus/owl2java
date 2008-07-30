/*
 * Created on Feb 17, 2004
 * By drager
 *
 * BBN Technologies
 * Copyright 2004, 2005
 */
package org.daml.kazuki;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.daml.kazuki.datatypes.*;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

// TOOD: replace kazuki types with my own implementation

/**
 * @author drager
 */
public class Datatypes {
    
    private String [][] defaultTypes = new String[][] {
//        {"ENTITIES", null},
        {"http://www.w3.org/2001/XMLSchema#ENTITY", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#ID", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#IDREF", "java.lang.String"},
//        {"IDREFS", null},
        {"http://www.w3.org/2001/XMLSchema#NCName", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#NMTOKEN", "java.lang.String"},
//        {"NMTOKENS", null},
        {"http://www.w3.org/2001/XMLSchema#NOTATION", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#Name", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#QName", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#anyURI", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#base64Binary", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#boolean", "boolean"},
        {"http://www.w3.org/2001/XMLSchema#byte", "byte"},
        {"http://www.w3.org/2001/XMLSchema#date", "java.util.Calendar"},
        {"http://www.w3.org/2001/XMLSchema#dateTime", "java.util.Calendar"},
        {"http://www.w3.org/2001/XMLSchema#decimal", "java.math.BigDecimal"},
        {"http://www.w3.org/2001/XMLSchema#double", "double"},
        {"http://www.w3.org/2001/XMLSchema#duration", "org.daml.kazuki.datatypes.XSDduration"},
        {"http://www.w3.org/2001/XMLSchema#float", "float"},
        {"http://www.w3.org/2001/XMLSchema#gDay", "org.daml.kazuki.datatypes.XSDgDay"},
        {"http://www.w3.org/2001/XMLSchema#gMonth", "org.daml.kazuki.datatypes.XSDgMonth"},
        {"http://www.w3.org/2001/XMLSchema#gMonthDay", "org.daml.kazuki.datatypes.XSDgMonthDay"},
        {"http://www.w3.org/2001/XMLSchema#gYear", "org.daml.kazuki.datatypes.XSDgYear"},
        {"http://www.w3.org/2001/XMLSchema#gYearMonth", "org.daml.kazuki.datatypes.XSDgYearMonth"},
        {"http://www.w3.org/2001/XMLSchema#hexBinary", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#int", "int"},
        {"http://www.w3.org/2001/XMLSchema#integer", "java.math.BigInteger"},
        {"http://www.w3.org/2001/XMLSchema#language", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#long", "long"},
        {"http://www.w3.org/2001/XMLSchema#negativeInteger", "java.math.BigInteger"},
        {"http://www.w3.org/2001/XMLSchema#nonNegativeInteger", "java.math.BigInteger"},
        {"http://www.w3.org/2001/XMLSchema#nonPositiveInteger", "java.math.BigInteger"},
        {"http://www.w3.org/2001/XMLSchema#normalizedString", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#positiveInteger", "java.math.BigInteger"},
        {"http://www.w3.org/2001/XMLSchema#short", "short"},
        {"http://www.w3.org/2001/XMLSchema#string", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#time", "java.util.Calendar"},
        {"http://www.w3.org/2001/XMLSchema#token", "java.lang.String"},
        {"http://www.w3.org/2001/XMLSchema#unsignedByte", "short"},
        {"http://www.w3.org/2001/XMLSchema#unsignedInt", "long"},
        {"http://www.w3.org/2001/XMLSchema#unsignedLong", "long"},
        {"http://www.w3.org/2001/XMLSchema#unsignedShort", "int"},
        {"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "java.lang.String"}
    };
    
    /**
     * Mapping from XSD Datatype names to Java type names.
     */
    @SuppressWarnings("unchecked")
	private HashMap typeMapping = new HashMap();  
    
    
    /**
     * Built in methods to extract java types from Literals.
     */
    private String [][] defaultMethods = new String[][] {
        {"java.math.BigDecimal", "org.daml.kazuki.Datatypes.getBigDecimal"},
        {"java.math.BigInteger", "org.daml.kazuki.Datatypes.getBigInteger"},
        {"java.lang.Boolean", "org.daml.kazuki.Datatypes.getBoolean"},
        {"java.lang.Byte", "org.daml.kazuki.Datatypes.getByte"},
        {"java.lang.Character", "org.daml.kazuki.Datatypes.getCharacter"},
        {"java.lang.Double", "org.daml.kazuki.Datatypes.getDouble"},
        {"java.lang.Float", "org.daml.kazuki.Datatypes.getFloat"},
        {"java.lang.Integer", "org.daml.kazuki.Datatypes.getInteger"},
        {"java.lang.Long", "org.daml.kazuki.Datatypes.getLong"},
        {"java.lang.Short", "org.daml.kazuki.Datatypes.getShort"},
        {"java.lang.String", "org.daml.kazuki.Datatypes.getString"},
        {"boolean", "org.daml.kazuki.Datatypes.getboolean"},
        {"byte", "org.daml.kazuki.Datatypes.getbyte"},
        {"double", "org.daml.kazuki.Datatypes.getdouble"},
        {"char", "org.daml.kazuki.Datatypes.getchar"},
        {"float", "org.daml.kazuki.Datatypes.getfloat"},
        {"int", "org.daml.kazuki.Datatypes.getint"},
        {"long", "org.daml.kazuki.Datatypes.getlong"},
        {"short", "org.daml.kazuki.Datatypes.getshort"},
        {"java.util.Calendar", "org.daml.kazuki.Datatypes.getCalendar"},
        {"org.daml.kazuki.datatypes.XSDgYear", "org.daml.kazuki.Datatypes.getgYear"},
        {"org.daml.kazuki.datatypes.XSDgMonth", "org.daml.kazuki.Datatypes.getgMonth"},
        {"org.daml.kazuki.datatypes.XSDgDay", "org.daml.kazuki.Datatypes.getgDay"},
        {"org.daml.kazuki.datatypes.XSDgYearMonth", "org.daml.kazuki.Datatypes.getgYearMonth"},
        {"org.daml.kazuki.datatypes.XSDgMonthDay", "org.daml.kazuki.Datatypes.getgMonthDay"}, 
        {"org.daml.kazuki.datatypes.XSDduration", "org.daml.kazuki.Datatypes.getDuration"}
    };
    
    
    /**
     * Mapping from Java type names to methods that can extract types from Literals.
     */
    @SuppressWarnings("unchecked")
	private HashMap methodMapping = new HashMap();



    /**
     * Create a new Datatype object with the default datatype and method mappings.
     */
    public Datatypes() {
        restoreDefaultTypes();
        restoreDefaultMethods();
    }

    /**
     * Restore the default datatype mappings
     */
    @SuppressWarnings("unchecked")
	public void restoreDefaultTypes() {
        typeMapping.clear();
        for (int i = 0; i < defaultTypes.length; i++) {
            typeMapping.put(defaultTypes[i][0], defaultTypes[i][1]);
        }
    }
    
    
    /**
     * Restore the default method mappings
     */
    @SuppressWarnings("unchecked")
	public void restoreDefaultMethods() {
        methodMapping.clear();
        for (int i = 0; i < defaultMethods.length; i++) {
            methodMapping.put(defaultMethods[i][0], defaultMethods[i][1]);
        }
    }

    
    /**
     * Print the datatype mapping values.
     * @param w - the print writer to use as output.
     */
    @SuppressWarnings("unchecked")
	public void printTypeMapping(PrintWriter w) {
        for (Iterator i = typeMapping.keySet().iterator(); i.hasNext();) {
            String dt = (String)i.next();
            String type = (String)typeMapping.get(dt);
            w.write(dt + " >> " + type + "\n");
        }
        w.flush();
   
    }
    
    /**
     * Print the method mapping values.
     * @param w - the print writer to use as output
     */
    @SuppressWarnings("unchecked")
	public void printMethodMapping(PrintWriter w) {
        for (Iterator i = methodMapping.keySet().iterator(); i.hasNext();) {
            String dt = (String)i.next();
            String type = (String)methodMapping.get(dt);
            w.write(dt + " >> " + type + "\n");
        }
        w.flush();
   
    }

    
    /**
     * Add or change a datatype mapping from a XSD datatype to a java type.
     * @param datatype - URI of an XSD (or other) datatype
     * @param javatype - The full java class name or primitive datatype name.
     */
    @SuppressWarnings("unchecked")
	public void setType(String datatype, String javatype) {
        typeMapping.put(datatype, javatype);
    }
    
    
    /**
     * Get the java type used for the given datatype.
     * @param datatype URI
     * @return the java class or primitive used to represent the datatype. 
     */
    public String getType(String datatype) {
        return (String)typeMapping.get(datatype);
    }
    
    
    /**
     * Parse a file containing mapping values.
     * Format of the file is <datatype URI><whitespace><javatype> per line.
     * Augments or modified current mapping.
     * TODO: Not called by anything yet
     * @param filename
     */
    public void parseTypeMapping(String filename) 
        throws IOException 
    {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        boolean stop = false;
        int i = 0;
        while (!stop) {
            String line = reader.readLine();
            i++;
            if (line == null) {
                stop = true;
            } else {
                StringTokenizer st = new StringTokenizer(line, " \t");
                if (st.hasMoreTokens()) {
                    String firstToken = st.nextToken();
                    if (st.hasMoreTokens()) {
                        String secondToken = st.nextToken();
                        if (st.hasMoreTokens()) {
                            System.err.println("Type Mapping file format is incorrect at line "+i);
                        }
                        // TODO: Check to see if secondToken is a valid class?
                        setType(firstToken, secondToken);
                    } else {
                        System.err.println("Type Mapping file format is incorrect at line "+i);
                    }
                }
            }
        }
    }
    
    
    /**
     * Add or change a method mapping.
     * The mapping is from Java class or primitive to a method that
     * takes a Literal and returns the value of the literal in the
     * form of the java type.
     * @param javatype - java class name as string
     * @param method - java method to extract a javaclass value from a Literal.
     */
    @SuppressWarnings("unchecked")
	public void setMethod(String javatype, String method) {
        methodMapping.put(javatype, method);
    }
    
    
    /**
     * Get the method name to extract a java type from a literal
     * @param javatype - string name of a java class or primitive
     * @return method name of the method to extract the type javatype from a literal
     */
    public String getMethod(String javatype) {
        return (String)methodMapping.get(javatype);
    }
    

    
    
    ///////////////////////////////////////////////////////
    // Built in methods to extract a literal value as a
    // specific java type.
    ///////////////////////////////////////////////////////
    
    /**
     * Return the value of a Literal as a String.
     */
    public static String getString(Literal l) {
        return l.getString();
    }
    
    /**
     * Return the value of a Literal as an int.
     */
    public static int getint(Literal l) {
        return l.getInt();
    }
    
    
    /**
     * Return the value of a Literal as a byte.
     */
    public static byte getbyte(Literal l) {
        return l.getByte();
    }
    
    /**
     * Return the value of a Literal as a short.
     */
    public static short getshort(Literal l) {
        return l.getShort();
    }
    
    /**
     * Return the value of a Literal as a double.
     */
    public static double getdouble(Literal l) {
        return l.getDouble();
    }
    
    /**
     * Return the value of a Literal as a long.
     */
    public static long getlong(Literal l) {
        return l.getLong();
    }
    
    /**
     * Return the value of a Literal as a float.
     */
    public static float getfloat(Literal l) {
        return l.getFloat();
    }
    
    /**
     * Return the value of a Literal as a boolean.
     */
    public static boolean getboolean(Literal l) {
        return l.getBoolean();
    }
    
    /**
     * Return the value of a Literal as a char.
     */
    public static char getchar(Literal l) {
        return l.getChar();
    }
    
    /**
     * Return the value of a Literal as a Integer.
     */
    public static Integer getInteger(Literal l) {
        return new Integer(l.getInt());
    }
    
    /**
     * Return the value of a Literal as a Byte.
     */
    public static Byte getByte(Literal l) {
        return new Byte(l.getByte());
    }
    
    /**
     * Return the value of a Literal as a Short.
     */
    public static Short getShort(Literal l) {
        return new Short(l.getShort());
    }
    
    /**
     * Return the value of a Literal as a Double.
     */
    public static Double getDouble(Literal l) {
        return new Double(l.getDouble());
    }
    
    /**
     * Return the value of a Literal as a Long.
     */
    public static Long getLong(Literal l) {
        return new Long(l.getLong());
    }
    
    /**
     * Return the value of a Literal as a Float.
     */
    public static Float getFloat(Literal l) {
        return new Float(l.getFloat());
    }
    
    /**
     * Return the value of a Literal as a Boolean.
     */
    public static Boolean getBoolean(Literal l) {
        return new Boolean(l.getBoolean());
    }
    
    /**
     * Return the value of a Literal as a Character.
     */
    public static Character getCharacter(Literal l) {
        return new Character(l.getChar());
    }
    
    /**
     * Return the value of a Literal as a Calendar
     * Returns null if the value is not valid.
     */
    public static Calendar getCalendar(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof Calendar) {
                return (Calendar)o;
            } else if (o instanceof XSDDateTime) {
                // Will be fixed with next release of Jena.
                Calendar c = ((XSDDateTime)o).asCalendar();
//                c.set(Calendar.MONTH, c.get(Calendar.MONTH)-1);
                return c;
            }
        } catch (DatatypeFormatException e) {
        }
        return null;

    }

    /**
     * Return the value of a Literal as a XSDgYear.
     * Returns null if value is not valid.
     */
    public static XSDgYear getgYear(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDgYear) {
                return (XSDgYear)o;
            } else if (o instanceof XSDDateTime) {
                return new XSDgYear((XSDDateTime)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    }

    /**
     * Return the value of a Literal as a XSDgMonth.
     * Returns null if value is not valid.
     */
    public static XSDgMonth getgMonth(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDgMonth) {
                return (XSDgMonth)o;
            } else if (o instanceof XSDDateTime) {
                return new XSDgMonth((XSDDateTime)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    }

    /**
     * Return the value of a Literal as a XSDgDay.
     * Returns null if value is not valid.
     */
    public static XSDgDay getgDay(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDgDay) {
                return (XSDgDay)o;
            } else if (o instanceof XSDDateTime) {
                return new XSDgDay((XSDDateTime)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    }

    /**
     * Return the value of a Literal as a XSDgYearMonth.
     * Returns null if value is not valid.
     */
    public static XSDgYearMonth getgYearMonth(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDgYearMonth) {
                return (XSDgYearMonth)o;
            } else if (o instanceof XSDDateTime) {
                return new XSDgYearMonth((XSDDateTime)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    }

    /**
     * Return the value of a Literal as a XSDgMonthDay.
     * Returns null if the value is not valid.
     */
    public static XSDgMonthDay getgMonthDay(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDgMonthDay) {
                return (XSDgMonthDay)o;
            } else if (o instanceof XSDDateTime) {
                return new XSDgMonthDay((XSDDateTime)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    }

    /**
     * Return the value of a Literal as a XSDduration.
     * Returns null if the value is not valid.
     */
    public static XSDduration getDuration(Literal l) {
        try {
            Object o = l.getValue();
            if (o instanceof XSDduration) {
                return (XSDduration)o;
            } else if (o instanceof XSDDuration) {
                return new XSDduration((XSDDuration)o);
            }
        } catch (DatatypeFormatException e) {
        }
        return null;
    } 
    
    /**
     * Return the value of the literal as a BigInteger.
     * If the value can not be represented as a BigInteger the
     * null is returned.
     * @param l
     * @return the value of literal l as a BigInteger
     */
    public static BigInteger getBigInteger(Literal l) {
        Object o = l.getValue();
        if (o instanceof BigInteger) {
            return (BigInteger)o;
        } else if (o instanceof Number) {
            Number n = (Number)o;
            try {
                // Throws NumberFormatException
                return new BigInteger(n.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Return the value of the literal as a BigDecimal.
     * If the value can not be represented as a BigDecimal the
     * null is returned.
     * @param l
     * @return the value of literal l as a BigDemical
     */
    public static BigDecimal getBigDecimal(Literal l) {
        Object o = l.getValue();
        if (o instanceof BigDecimal) {
            return (BigDecimal)o;
        } else if (o instanceof Number) {
            Number n = (Number)o;
            try {
                // Throws NumberFormatException
                return new BigDecimal(n.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    
    ///////////////////////////////////////////////////////////////
    // Built in methods to create typed literals from primitives
    // by converting the primitive to a class.
    // Also an object version so the interface is consistent.
    // Note: These do not validate the value to make sure it is
    // valid for the datatype.
    ///////////////////////////////////////////////////////////////
    
    /**
     * Return a typed literal whose value is the object and type is
     * the supplied datatype.  Validates the value against the datatype
     * and throws a DatatypeFormatException if validation fails.
     * @param m - The model used to create the literal
     * @param o - The object used for the value of the literal
     * @param dt - The RDFDatatype URI for the literal.
     */
    public static Literal createTypedLiteral(Model m, Object o, String dt) {
        RDFDatatype rdfdt = TypeMapper.getInstance().getTypeByName(dt);
        if (o instanceof Calendar) {
            String lex = CalendarToXSD((Calendar)o, rdfdt);
            if (rdfdt.isValidValue(lex)) {
                return m.createTypedLiteral(lex, dt);
            } else {
                throw new DatatypeFormatException(lex, rdfdt, "Value does not match datatype.");
            }
//        } else if (rdfdt.isValidValue(o)) {
        } else if (rdfdt.isValid(o.toString())) {
            // Don't want to store the object directly because the model should not contain
            // mutable objects.
            return m.createTypedLiteral(o.toString(), dt);
        } else {
            throw new DatatypeFormatException(o.toString(), rdfdt, "Value does not match datatype.");
        }
    }
    
    /**
     * Create a typed literal from a boolean value.
     */
    public static Literal createTypedLiteral(Model m, boolean b, String dt) {
        return createTypedLiteral(m, new Boolean(b), dt);
    }

    /**
     * Create a typed literal from a byte value.
     */
    public static Literal createTypedLiteral(Model m, byte b, String dt) {
        return createTypedLiteral(m, new Byte(b), dt);
    }

    /**
     * Create a typed literal from a char value.
     */
    public static Literal createTypedLiteral(Model m, char c, String dt) {
        return createTypedLiteral(m, new Character(c), dt);
    }
    
    /**
     * Create a typed literal from a double value.
     * A bug in Jena 2.1 requires the double to be converted to a Float, so 
     * values beyond float will be lost.
     */
    public static Literal createTypedLiteral(Model m, double d, String dt) {
        return createTypedLiteral(m, new Double(d), dt);
    }

    /**
     * Create a typed literal from a float value.
     */
    public static Literal createTypedLiteral(Model m, float f, String dt) {
        return createTypedLiteral(m, new Float(f), dt);
    }

    /**
     * Create a typed literal from a int value.
     */
    public static Literal createTypedLiteral(Model m, int i, String dt) {
        return createTypedLiteral(m, new Integer(i), dt);
    }

    /**
     * Create a typed literal from a long value.
     */
    public static Literal createTypedLiteral(Model m, long l, String dt) {
        return createTypedLiteral(m, new Long(l), dt);
    }

    /**
     * Create a typed literal from a short value.
     */
    public static Literal createTypedLiteral(Model m, short s, String dt) {
        return createTypedLiteral(m, new Short(s), dt);
    }
    
    
    /////////////////////////////////////////////////////
    // Helper function to convert from Calendar to various xsd date/time strings
    /////////////////////////////////////////////////////
    
    // CCYY-MM-DD (w/ optional time zone)
    // timezone is "[+|-]hh:mm" or "Z" for UTC
    private static final SimpleDateFormat XSD_date = new SimpleDateFormat("yyyy-MM-ddZ");

    // CCYY-MM-DDThh:mm:ss (w/ optional time zone)
    // TODO: allow fractional seconds (any number of places).
    private static final SimpleDateFormat XSD_dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    // hh:mm:ss (w/ optional time zone)
    // TODO: allow fractional seconds (any number of places) hh:mm:ss.sss...
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
     * Convenience function to generate lexical values from a Calendar
     * object based on a specified RDF datatype.
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
        sb.insert(sb.length()-2, ':');
        return sb.toString();
    }

}
