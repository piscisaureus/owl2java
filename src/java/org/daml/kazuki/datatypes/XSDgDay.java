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
 * A very simple gDay implementation
 * TODO: Timezone
 */
public class XSDgDay {
    
    private int day = 0;
    
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;

    /**
     * 
     */
    public XSDgDay(XSDDateTime dt) {
        super();
        setDay(dt.getDays());
    }
    
    public XSDgDay(int day) {
        super();
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
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("---");
        if (day < 10) {
            s.append("0");
            s.append(day);
        } else {
            s.append(day);
        }
        return s.toString();
    }
        
    //////////////////////////////////////////////////
    // Object Mapper for XSDgDay
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getgDay(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDgDay.");
            }
        }
    }
}
