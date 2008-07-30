/*
 * Created on Feb 19, 2004
 * By drager
 *
 * BBN Technologies
 * Copyright 2004
 */
package org.daml.kazuki.datatypes;

import org.daml.kazuki.Datatypes;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

/**
 * @author drager
 * 
 * A simple implementation for gYearMonth.
 * TODO: timezone
 */
public class XSDgYearMonth {
    
    private int year = 0;
    private int month = 0;
    
    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;


    public XSDgYearMonth(XSDDateTime dt) {
        super();
        setYear(dt.getYears());
        setMonth(dt.getMonths());
    }
    
    public XSDgYearMonth(int year, int month) {
        super();
        setYear(year);
        setMonth(month);
    }

    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public void setMonth(int month) {
        if (MIN_MONTH <= month && month <= MAX_MONTH) {
            this.month = month;
        }
    }
        
    public int getMonth() {
        return month;
    }
    


    
    public String toString() {
        StringBuffer s = new StringBuffer();

        // Year
        int y = getYear();
        if (y < 0) {
            s.append("-");
            y = y * -1;
        }
        if (y == 0)
            s.append("0000");
        else if (y < 10) {
            s.append("000");
            s.append(y);
        } else if (y < 100) {
            s.append("00");
            s.append(y);
        } else if (y < 1000) {
            s.append("0");
            s.append(y);
        } else {
            s.append(y);
        }

        // Month
        s.append("-");
        if (month < 10) {
            s.append("0");
            s.append(month);
        } else {
            s.append(month);
        }

        return s.toString();
    }

    //////////////////////////////////////////////////
    // Object Mapper for XSDgYearMonth
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getgYearMonth(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDgYearMonth.");
            }
        }
    }    
}
