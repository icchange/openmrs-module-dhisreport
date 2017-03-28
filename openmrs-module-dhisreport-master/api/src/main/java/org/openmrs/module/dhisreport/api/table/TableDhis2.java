package org.openmrs.module.dhisreport.api.table;

import org.openmrs.Location;
import org.openmrs.Role;
import org.openmrs.module.dhisreport.api.model.DataValueTemplate;
import org.openmrs.module.dhisreport.api.model.Identifiable;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
@XmlType( name = "reportTable", propOrder = { "name", "uid", "href", "roles" } )
@XmlRootElement( name = "reportTable" )
public class TableDhis2
    implements Serializable
{
    protected Integer id;

    protected String name;

    protected String uid;

    protected String href;

    @XmlElementWrapper( name = "roles" )
    @XmlElement( name = "role" )
    protected Set<Role> roles = new HashSet<Role>();

    @XmlElementWrapper( name = "locations" )
    @XmlElement( name = "location" )
    protected Set<Location> locations = new HashSet<Location>();

    public TableDhis2()
    {
    }

    public TableDhis2( String n, String i, String h )
    {
        name = n;
        uid = i;
        href = h;
    }

    @XmlTransient
    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    @XmlAttribute( name = "uid", required = true )
    @XmlIDREF
    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }

    @XmlAttribute( name = "name", required = true )
    @XmlIDREF
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    @XmlAttribute( name = "href", required = true )
    @XmlIDREF
    public String getHref()
    {
        return href;
    }

    public void setHref( String href )
    {
        this.href = href;
    }

    public Set<Role> getRoles()
    {
        return roles;
    }

    public void setRoles( Set<Role> roles )
    {
        this.roles = roles;
    }

    public void addRole( Role role )
    {
        roles.add( role );
    }

    public void removeRole( Integer roleIndex )
    {
        roles.remove( roleIndex );
    }

}
