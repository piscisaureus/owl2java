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
 * Very simple gYear implementation
 * TODO: Timezone
 */
public class XSDgYear {
    
    private int year = 0;

    /**
     * 
     */
    public XSDgYear(XSDDateTime dt) {
        super();
        setYear(dt.getYears());
    }
    
    public XSDgYear(int year) {
        super();
        setYear(year);
    }

    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public String toString() {

        StringBuffer s = new StringBuffer();

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

        return s.toString();
    }

    //////////////////////////////////////////////////
    // Object Mapper for XSDgYear
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getgYear(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDgYear.");
            }
        }
    }    
}
