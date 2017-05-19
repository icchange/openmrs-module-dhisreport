package org.openmrs.module.dhisreport.api.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Created by ICCHANGE on 24/Jan/2017.
 */
@XmlType( name = "dataElementQuery", propOrder = { "uid", "query" } )
@XmlRootElement( name = "dataElementQuery" )
public class DataElementQuery
    implements Serializable, Identifiable
{

    private static final String SQL_SANITY_CHECK = ".*((?i)update|delete).*";

    protected Integer id;

    protected String uid;

    protected String query;

    protected String prefix;

    protected String codeprefix;

    protected ReportDefinition reportDefinition;

    protected Disaggregation disaggregation;

    @XmlTransient
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

    @XmlAttribute( name = "uid", required = true )
    @Override
    public String getUid()
    {
        return uid;
    }

    @Override
    public void setUid( String uid )
    {
        this.uid = uid;
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

    @XmlAttribute( name = "prefix", required = true )
    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix( String prefix )
    {
        this.prefix = prefix;
    }

    @XmlAttribute( name = "codeprefix", required = true )
    public String getCodeprefix()
    {
        return codeprefix;
    }

    public void setCodeprefix( String codeprefix )
    {
        this.codeprefix = codeprefix;
    }

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

    public boolean potentialUpdateDelete()
    {
        return query.matches( SQL_SANITY_CHECK );
    }

}
