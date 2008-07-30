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
 * A simple implementation of a gMonth
 * TODO: timezone
 */
public class XSDgMonth {

    private int month = 0;
    
    private static final int MIN_MONTH = 1;
    private static final int MAX_MONTH = 12;

    /**
     * 
     */
    public XSDgMonth(XSDDateTime dt) {
        super();
        setMonth(dt.getMonths());
    }
    
    public XSDgMonth(int month) {
        super();
        setMonth(month);
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
        s.append("--");
        if (month < 10) {
            s.append("0");
            s.append(month);
        } else {
            s.append(month);
        }
        s.append("--");
        return s.toString();
    }

    //////////////////////////////////////////////////
    // Object Mapper for XSDgMonth
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getgMonth(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDgMonth.");
            }
        }
    }    
}
