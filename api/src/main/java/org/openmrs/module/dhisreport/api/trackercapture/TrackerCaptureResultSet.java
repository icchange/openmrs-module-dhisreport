package org.openmrs.module.dhisreport.api.trackercapture;

import org.openmrs.module.dhisreport.api.dxf2.DataValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ICCHANGE on 3/Apr/2017.
 */
public class TrackerCaptureResultSet
{

    protected List<TrackerCaptureTemplateMessage> dataValues = new LinkedList<TrackerCaptureTemplateMessage>();

    public void addMessage( TrackerCaptureTemplateMessage message )
    {
        dataValues.add( message );
    }

    public void setDataValues( List<TrackerCaptureTemplateMessage> dataValues )
    {
        this.dataValues = dataValues;
    }

    public List<TrackerCaptureTemplateMessage> getDataValues()
    {
        return dataValues;
    }

    public void addMessageList( List<TrackerCaptureTemplateMessage> dataValues )
    {
        if ( dataValues != null )
        {
            this.dataValues.addAll( dataValues );
        }
    }

}
