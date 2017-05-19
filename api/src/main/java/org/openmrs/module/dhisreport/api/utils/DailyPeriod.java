package org.openmrs.module.dhisreport.api.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ICCHANGE on 16/Jan/2017.
 */
public class DailyPeriod
    implements Period
{

    public static final String ISO_FORMAT = "yyyyMMdd";

    protected Date startDate;

    protected Date endDate;

    @Override
    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    @Override
    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    @Override
    public Date getStartDate()
    {
        return startDate;
    }

    @Override
    public Date getEndDate()
    {
        return endDate;
    }

    public DailyPeriod( Date date )
    {
        DateTime dt = new DateTime( date );
        startDate = dt.withTime( 0, 0, 0, 0 ).toDate();
        endDate = dt.withTime( 23, 59, 59, 999 ).toDate();
    }

    @Override
    public String getAsIsoString()
    {
        return (new SimpleDateFormat( ISO_FORMAT )).format( getStartDate() );
    }

    @Override
    public Period getAsIsoStringNextValue( Integer nextValue )
    {
        Date date = getStartDate();
        DateTime dt = new DateTime( date );
        dt = dt.plusDays( nextValue );
        return new DailyPeriod( dt.toDate() );
    }
}
