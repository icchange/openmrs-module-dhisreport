package org.openmrs.module.dhisreport.api.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Created by ICCHANGE on 26/Jan/2017.
 */
@XmlType( name = "metaDataValueTemplate", propOrder = { "disaggregation", "query" } )
@XmlRootElement( name = "metaDataValueTemplate" )
public class MetaDataValueTemplate
    implements Serializable
{

    // Regex testing for update/delete
    private static final String SQL_SANITY_CHECK = ".*((?i)update|delete).*";

    protected Integer id;

    protected ReportDefinition reportDefinition;

    protected Disaggregation disaggregation;

    protected String query;

    @XmlTransient
    public ReportDefinition getReportDefinition()
    {
        return reportDefinition;
    }

    public void setReportDefinition( ReportDefinition reportDefinition )
    {
        this.reportDefinition = reportDefinition;
    }

    @XmlAttribute( name = "disaggregation", required = true )
    @XmlIDREF
    public Disaggregation getDisaggregation()
    {
        return disaggregation;
    }

    public void setDisaggregation( Disaggregation disaggregation )
    {
        this.disaggregation = disaggregation;
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

    @XmlElement( name = "annotation", required = false )
    public String getQuery()
    {
        return query;
    }

    public void setQuery( String query )
    {
        this.query = query;
    }

    public boolean potentialUpdateDelete()
    {
        return query.matches( SQL_SANITY_CHECK );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final MetaDataValueTemplate other = (MetaDataValueTemplate) obj;

        if ( this.disaggregation != other.disaggregation
            && (this.disaggregation == null || !this.disaggregation.equals( other.disaggregation )) )
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (this.disaggregation != null ? this.disaggregation.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return "MDVT: " + this.getId() + " : " + this.getDisaggregation().getName();
    }

}
