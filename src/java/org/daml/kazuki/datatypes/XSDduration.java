/*
 * Created on Feb 19, 2004
 * By drager
 *
 * BBN Technologies
 * Copyright 2004
 */
package org.daml.kazuki.datatypes;

import org.daml.kazuki.Datatypes;

import com.hp.hpl.jena.datatypes.xsd.XSDDuration;

/**
 * @author drager
 * 
 * A simple implementation of a mutable XSD duration.
 * 
 * 
 */
public class XSDduration {
    
    private int years = 0;
    private int months = 0;
    private int days = 0;
    private int hours = 0;
    private int minutes = 0;
    private double seconds = 0;
    private int sign = 1;
    

    public XSDduration(XSDDuration duration) {
        super();
        setDuration(duration);
    }
    
    public XSDduration(boolean pos, int years, int months, int days, int hours, int minutes, double seconds) {
        super();
        setDuration(pos, years, months, days, hours, minutes, seconds);
    }
    
    public void setDuration(XSDDuration duration) {
        setPositive(!duration.toString().startsWith("-"));
        setYears(duration.getYears()*sign);
        setMonths(duration.getMonths()*sign);
        setDays(duration.getDays()*sign);
        setHours(duration.getHours()*sign);
        setMinutes(duration.getMinutes()*sign);
        setSeconds(duration.getSeconds()*sign);
    }
    
    
    public void setDuration(boolean pos, int years, int months, int days, int hours, int minutes, double seconds) {
        setPositive(pos);
        setYears(years);
        setMonths(months);
        setDays(days);
        setHours(hours);
        setMinutes(minutes);
        setSeconds(seconds);
    }
    
    
    public void setPositive(boolean pos) {
        if (pos)
            sign = 1;
        else
            sign = -1;
    }
    
    public void setYears(int years) {
        if (years >= 0) this.years = years;
    }
    
    public void setMonths(int months) {
        if (months >= 0) this.months = months;
    }
    
    public void setDays(int days) {
        if (days >= 0) this.days = days;
    }
    
    public void setHours(int hours) {
        if (hours >= 0) this.hours = hours;
    }
    
    public void setMinutes(int minutes) {
        if (minutes >= 0) this.minutes = minutes;
    }
    
    public void setSeconds(double seconds) {
        if (seconds >= 0) this.seconds = seconds;
    }
    
    public boolean isPositive() {
        return (sign == 1);
    }
    
    public int getYears() {
        return years;
    }
    
    public int getMonths() {
        return months;
    }
    
    public int getDays() {
        return days;
    }
    
    public int getHours() {
        return hours;
    }
    
    public int getMinutes() {
        return minutes;
    }
    
    public double getSeconds() {
        return seconds;
    }

    
    public String toString() {
        StringBuffer s = new StringBuffer();
        
        if (sign < 0) {
            s.append('-');
        }
        
        s.append('P');
        s.append(years);
        s.append('Y');
        s.append(months);
        s.append('M');
        s.append(days);
        s.append('D');
        s.append('T');
        s.append(hours);
        s.append('H');
        s.append(minutes);
        s.append('M');
        s.append(seconds);
        s.append('S');

        return s.toString();
    }

    //////////////////////////////////////////////////
    // Object Mapper for XSDduration
    //////////////////////////////////////////////////

    public static com.hp.hpl.jena.util.iterator.Map1 objectMapper = new ObjectMapper();

    protected static class ObjectMapper implements com.hp.hpl.jena.util.iterator.Map1 {
        public Object map1(Object x) {
            if (x instanceof com.hp.hpl.jena.rdf.model.Statement) {
                com.hp.hpl.jena.rdf.model.Literal l = ((com.hp.hpl.jena.rdf.model.Statement)x).getLiteral();                
                return org.daml.kazuki.Datatypes.getDuration(l);
            }
            else {
                throw new IllegalArgumentException("RDF Statement expected while converting an XSDDuration.");
            }
        }
    }
}
