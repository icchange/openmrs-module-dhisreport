package org.openmrs.module.dhisreport.api.dxf2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "organisationUnit" )
@XmlAccessorType( XmlAccessType.FIELD )
public class OrganizationUnit
{
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String code;

    @XmlAttribute
    private String id;

    public String getId()
    {
        return id;
    }

    public void setId( String id )
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

    public String getCode()
    {
        return code;
    }

    public void setCode( String code )
    {
        this.code = code;
    }
}
