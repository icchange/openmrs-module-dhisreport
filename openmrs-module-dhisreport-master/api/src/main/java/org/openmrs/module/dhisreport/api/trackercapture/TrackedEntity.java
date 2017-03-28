package org.openmrs.module.dhisreport.api.trackercapture;

import org.openmrs.Person;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Created by ICCHANGE on 16/Mar/2017.
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement( name = "attribute" )
public class TrackedEntity
{

    protected String uid;

    protected Map<String, String> attributes;

    public TrackedEntity()
    {
    }

    public TrackedEntity( String id, Map<String, String> attr )
    {
        uid = id;
        attributes = attr;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }

    public String getUid()
    {
        return uid;
    }

    public void setAttributes( Map<String, String> attributes )
    {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes()
    {
        return attributes;
    }
}
