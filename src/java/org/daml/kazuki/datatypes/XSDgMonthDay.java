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
 */
public class XSDgMonthDay {

    private int day = 0;
    private int month = 0;
    
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;
    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;


    /**
     * 
     */
    public XSDgMonthDay(XSDDateTime dt) {
        super();
        setMonth(dt.getMonths());
        setDay(dt.getDays());
    }
    
    public XSDgMonthDay(int month, int day) {
        super();
        setMonth(month);
        setDay(day);
    }
    
    
    public void setDay(int day) {
        if (MIN_DAY <= day && day <= MAX_DAY) {
            this.day = day;
        }
    }
    
    public int getDay() {
        return day;
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
        
        // Month
        s.append("--");
        if (month < 10) {
            s.append("0");
            s.append(month);
        } else {
            s.append(month);
        }
        
        // Day
        s.append("-");
        if (day < 10) {
            s.append("0");
            s.append(day);
        } else {
            s.append(day);
        }

        return s.toString();
    }

    //////////////////////////////////////////////////
    // Object Mapper for XSDgMonthDay
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getgMonthDay(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDgMonthDay.");
            }
        }
    }    
}
