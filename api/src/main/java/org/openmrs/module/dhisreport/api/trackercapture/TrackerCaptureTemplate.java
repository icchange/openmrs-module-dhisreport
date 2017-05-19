package org.openmrs.module.dhisreport.api.trackercapture;

import org.openmrs.module.dhisreport.api.model.DataValueTemplate;
import org.openmrs.module.dhisreport.api.model.Identifiable;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureAttribute;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by ICCHANGE on 10/Mar/2017.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = { "name", "uid", "query", "attributekey", "trackerCaptureAttributeList",
    "trackerCaptureEnrollmentList" } )
@XmlRootElement( name = "trackerCapture" )
public class TrackerCaptureTemplate
    implements Serializable, Identifiable
{

    @XmlTransient
    protected Integer id;

    @XmlElement( name = "reportName" )
    protected String name;

    @XmlElement( name = "trackedEntity" )
    protected String uid;

    @XmlElement( name = "query" )
    protected String query;

    @XmlElement( name = "attributeKey" )
    protected String attributekey;

    @XmlElementWrapper( name = "attributes", required = true )
    @XmlElement( name = "attribute" )
    protected Set<TrackerCaptureAttribute> trackerCaptureAttributeList;

    @XmlElementWrapper( name = "programs", required = true )
    @XmlElement( name = "program" )
    protected Set<TrackerCaptureEnrollment> trackerCaptureEnrollmentList;

    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId( Integer id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery( String query )
    {
        this.query = query;
    }

    public String getAttributekey()
    {
        return attributekey;
    }

    public void setAttributekey( String attributekey )
    {
        this.attributekey = attributekey;
    }

    public Set<TrackerCaptureAttribute> getTrackerCaptureAttributeList()
    {
        return trackerCaptureAttributeList;
    }

    public void setTrackerCaptureAttributeList( Set<TrackerCaptureAttribute> trackerCaptureAttributeList )
    {
        this.trackerCaptureAttributeList = trackerCaptureAttributeList;
    }

    public void addTrackerCaptureAttribute( TrackerCaptureAttribute trackerCaptureAttribute )
    {
        trackerCaptureAttribute.setTemplate( this );
        trackerCaptureAttributeList.add( trackerCaptureAttribute );
    }

    public void removeTrackerCaptureAttribute( TrackerCaptureAttribute trackerCaptureAttribute )
    {
        trackerCaptureAttributeList.remove( trackerCaptureAttribute );
    }

    public Set<TrackerCaptureEnrollment> getTrackerCaptureEnrollmentList()
    {
        return trackerCaptureEnrollmentList;
    }

    public void setTrackerCaptureEnrollmentList( Set<TrackerCaptureEnrollment> trackerCaptureEnrollmentList )
    {
        this.trackerCaptureEnrollmentList = trackerCaptureEnrollmentList;
    }

    public void addTrackerCaptureEnrollment( TrackerCaptureEnrollment trackerCaptureEnrollment )
    {
        trackerCaptureEnrollment.setTemplate( this );
        trackerCaptureEnrollmentList.add( trackerCaptureEnrollment );
    }

    public void removeTrackerCaptureEnrollment( TrackerCaptureEnrollment trackerCaptureEnrollment )
    {
        trackerCaptureEnrollmentList.remove( trackerCaptureEnrollment );
    }

    public TrackerCaptureAttribute getKeyAttribute()
    {
        for ( TrackerCaptureAttribute attr : trackerCaptureAttributeList )
        {
            if ( attr.getName().equals( attributekey ) )
            {
                return attr;
            }
        }
        return null;
    }

}
