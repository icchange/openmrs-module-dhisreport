package org.openmrs.module.dhisreport.api.trackercapture;

import org.openmrs.module.dhisreport.api.model.Identifiable;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by ICCHANGE on 17/Mar/2017.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement( name = "program" )
public class TrackerCaptureEnrollment
    implements Serializable, Identifiable
{

    @XmlTransient
    protected Integer id;

    @XmlAttribute( required = true )
    protected String uid;

    @XmlAttribute( required = true )
    protected String name;

    @XmlElement( required = true )
    protected String query;

    @XmlTransient
    protected TrackerCaptureTemplate template;

    @XmlTransient
    protected List<Map<String, Object>> querylist;

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

    public String getUid()
    {
        return uid;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setQuery( String query )
    {
        this.query = query;
    }

    public String getQuery()
    {
        return query;
    }

    public TrackerCaptureTemplate getTemplate()
    {
        return template;
    }

    public void setTemplate( TrackerCaptureTemplate template )
    {
        this.template = template;
    }

    public List<Map<String, Object>> getQueryList()
    {
        return querylist;
    }

    public void setQueryList( List<Map<String, Object>> querylist )
    {
        this.querylist = querylist;
    }

    public boolean checkQueryList( String id )
    {
        if ( querylist != null )
        {
            for ( Map<String, Object> map : querylist )
            {
                if ( map.get( "id" ).toString().equals( id ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getQueryListDate( String id )
    {
        if ( querylist != null )
        {
            for ( Map<String, Object> map : querylist )
            {
                if ( map.get( "id" ).toString().equals( id ) )
                {
                    return map.get( "date" );
                }
            }
        }
        return null;
    }

}
