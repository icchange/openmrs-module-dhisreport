package org.openmrs.module.dhisreport.api.trackercapture;

import org.openmrs.module.dhisreport.api.model.Identifiable;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Created by ICCHANGE on 13/Mar/2017.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement( name = "attribute" )
public class TrackerCaptureAttribute
    implements Serializable, Identifiable
{

    @XmlTransient
    protected Integer id;

    @XmlAttribute( required = true )
    protected String uid;

    @XmlAttribute( required = true )
    protected String name;

    @XmlAttribute( required = true )
    protected String query;

    @XmlTransient
    protected TrackerCaptureTemplate template;

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

    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
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

    public TrackerCaptureTemplate getTemplate()
    {
        return template;
    }

    public void setTemplate( TrackerCaptureTemplate template )
    {
        this.template = template;
    }
}
