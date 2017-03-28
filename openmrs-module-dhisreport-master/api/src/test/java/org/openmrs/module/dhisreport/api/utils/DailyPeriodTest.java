package org.openmrs.module.dhisreport.api.utils;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by ICCHANGE on 18/Jan/2017.
 */
public class DailyPeriodTest
{

    /**
     * Test of getStart method, of class DailyPeriod.
     */
    @Test
    public void testGetStart()
        throws ParseException
    {
        DailyPeriod instance = new DailyPeriod( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2012-10-19" ) );
        Date expResult = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).parse( "2012-10-19T00:00:00" );
        Date result = instance.getStartDate();
        assertEquals( expResult, result );
    }

    /**
     * Test of getEnd method, of class DailyPeriod.
     */
    @Test
    public void testGetEnd()
        throws ParseException
    {
        DailyPeriod instance = new DailyPeriod( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2012-10-19" ) );
        Date expResult = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).parse( "2012-10-19T23:59:60" );
        long time = expResult.getTime() - 1;
        expResult.setTime( time );

        Date result = instance.getEndDate();
        assertEquals( expResult, result );
    }

    /**
     * Test of getAsIsoString method, of class DailyPeriod.
     */
    @Test
    public void testGetAsIsoString()
        throws ParseException
    {
        DailyPeriod instance = new DailyPeriod( new SimpleDateFormat( "yyyy-MM-dd" ).parse( "2012-10-19" ) );
        String expResult = "20121019";
        String result = instance.getAsIsoString();
        assertEquals( expResult, result );

        DailyPeriod instance2 = new DailyPeriod( new SimpleDateFormat( "MM/dd/yyyy" ).parse( "10/19/2012" ) );
        String expResult2 = "20121019";
        String result2 = instance2.getAsIsoString();
        assertEquals( expResult2, result2 );
    }
}
