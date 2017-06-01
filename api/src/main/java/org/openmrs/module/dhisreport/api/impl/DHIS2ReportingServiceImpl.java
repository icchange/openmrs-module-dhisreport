/**
 *  Copyright 2012 Society for Health Information Systems Programmes, India (HISP India)
 *
 *  This file is part of DHIS2 Reporting module.
 *
 *  DHIS2 Reporting module is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  DHIS2 Reporting module is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DHIS2 Reporting module.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package org.openmrs.module.dhisreport.api.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisreport.api.AggregatedResultSet;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.adx.AdxType;
import org.openmrs.module.dhisreport.api.adx.DataValueType;
import org.openmrs.module.dhisreport.api.adx.GroupType;
import org.openmrs.module.dhisreport.api.db.DHIS2ReportingDAO;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.model.*;
import org.openmrs.module.dhisreport.api.dxf2.DataValue;
import org.openmrs.module.dhisreport.api.dxf2.DataValueSet;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.module.dhisreport.api.utils.DailyPeriod;
import org.openmrs.module.dhisreport.api.utils.MonthlyPeriod;
import org.openmrs.module.dhisreport.api.utils.Period;
import org.openmrs.module.dhisreport.api.utils.WeeklyPeriod;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.MissingDependencyException;
import org.openmrs.module.reporting.evaluation.parameter.*;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.*;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.report.util.PeriodIndicatorReportUtil;
import org.openmrs.util.LocationUtility;

/**
 * It is a default implementation of {@link DHIS2ReportingService}.
 */
public class DHIS2ReportingServiceImpl
    extends BaseOpenmrsService
    implements DHIS2ReportingService
{

    protected final Log log = LogFactory.getLog( this.getClass() );

    private DHIS2ReportingDAO dao;

    private HttpDhis2Server dhis2Server;

    /**
     * @param dao the dao to set
     */
    public void setDao( DHIS2ReportingDAO dao )
    {
        this.dao = dao;
    }

    /**
     * @return the dao
     */
    public DHIS2ReportingDAO getDao()
    {
        return dao;
    }

    @Override
    public HttpDhis2Server getDhis2Server()
    {
        return dhis2Server;
    }

    @Override
    public void setDhis2Server( HttpDhis2Server dhis2Server )
    {
        this.dhis2Server = dhis2Server;
    }

    @Override
    public ReportDefinition fetchReportTemplates()
        throws DHIS2ReportingException
    {
        return dhis2Server.fetchReportTemplates();
    }

    @Override
    public ImportSummary postDataValueSet( DataValueSet dvset )
        throws DHIS2ReportingException
    {
        return dhis2Server.postReport( dvset );
    }

    @Override
    public ImportSummaries postAdxReport( AdxType adxReport )
        throws DHIS2ReportingException
    {
        return dhis2Server.postAdxReport( adxReport );
    }

    @Override
    public ImportSummaries postDxf2Report( AdxType adxReport )
        throws DHIS2ReportingException
    {
        return dhis2Server.postDxf2Report( adxReport );
    }

    @Override
    public ImportSummaries postMetaData( String metadata )
        throws DHIS2ReportingException
    {
        return dhis2Server.postMetaData( metadata );
    }

    @Override
    public List<String[]> getDataElements( List<Object[]> elements, String prefix )
    {
        return dhis2Server.getDataElements( elements, prefix );
    }

    @Override
    public DataElementQuery getDataElementQuery( Integer id )
    {
        return dao.getDataElementQuery( id );
    }

    @Override
    public DataElementQuery getDataElementQueryByUid( String uid )
    {
        return null;
    }

    @Override
    public DataElementQuery getDataElementQueryByCode( String code )
    {
        return null;
    }

    @Override
    public DataElementQuery saveDataElementQuery( DataElementQuery dq )
    {
        return dao.saveDataElementQuery( dq );
    }

    @Override
    public void purgeDataElementQuery( DataElementQuery dq )
    {
        dao.deleteDataElementQuery( dq );
    }

    @Override
    public DataElement getDataElement( Integer id )
    {
        return dao.getDataElement( id );
    }

    @Override
    public DataElement getDataElementByUid( String uid )
    {
        return dao.getDataElementByUid( uid );
    }

    @Override
    public DataElement getDataElementByCode( String code )
    {
        return dao.getDataElementByCode( code );
    }

    @Override
    public DataElement saveDataElement( DataElement de )
    {
        return dao.saveDataElement( de );
    }

    @Override
    public void purgeDataElement( DataElement de )
    {
        dao.deleteDataElement( de );
    }

    @Override
    public Disaggregation getDisaggregation( Integer id )
    {
        return dao.getDisaggregation( id );
    }

    @Override
    public Disaggregation saveDisaggregation( Disaggregation disagg )
    {
        return dao.saveDisaggregation( disagg );
    }

    @Override
    public ReportDefinition getReportDefinition( Integer id )
    {
        return dao.getReportDefinition( id );
    }

    public ReportDefinition getReportDefinitionByUId( String uid )
    {
        return dao.getReportDefinitionByUid( uid );
    }

    public ReportDefinition getReportDefinitionByCode( String code )
    {
        return dao.getReportDefinitionByCode( code );
    }

    public List<ReportDefinition> getReportDefinitionByPeriodType( String periodType )
    {
        return dao.getReportDefinitionByPeriodType( periodType );
    }

    @Override
    public ReportDefinition saveReportDefinition( ReportDefinition reportDefinition )
    {
        return dao.saveReportDefinition( reportDefinition );
    }

    @Override
    public Collection<DataElement> getAllDataElements()
    {
        return dao.getAllDataElements();
    }

    @Override
    public void purgeDisaggregation( Disaggregation disagg )
    {
        dao.deleteDisaggregation( disagg );
    }

    @Override
    public Collection<Disaggregation> getAllDisaggregations()
    {
        return dao.getAllDisaggregations();
    }

    @Override
    public void purgeReportDefinition( ReportDefinition rd )
    {
        dao.deleteReportDefinition( rd );
    }

    @Override
    public Collection<ReportDefinition> getAllReportDefinitions()
    {
        return dao.getAllReportDefinitions();
    }

    @Override
    public String evaluateDataValueTemplate( DataValueTemplate dv, Period period, Location location )
        throws DHIS2ReportingException
    {
        return dao.evaluateDataValueTemplate( dv, period, location );
    }

    /**
     * Create a datavalueset report TODO: handle the sql query exceptions which
     * are bound to happen
     * 
     * @param reportDefinition
     * @param period
     * @param location
     * @return
     */
    @Override
    public DataValueSet evaluateReportDefinition( ReportDefinition reportDefinition, Period period, Location location )
    {
        Collection<DataValueTemplate> templates = reportDefinition.getDataValueTemplates();
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataElementIdScheme( "code" );
        dataValueSet.setOrgUnitIdScheme( "code" );
        dataValueSet.setPeriod( period.getAsIsoString() );
        // dataValueSet.setOrgUnit( "OU_" + location.getId() ); /* Removed
        // because will set directly from the controller */
        dataValueSet.setDataSet( reportDefinition.getCode() );

        Collection<DataValue> dataValues = dataValueSet.getDataValues();

        for ( DataValueTemplate dvt : templates )
        {
            DataValue dataValue = new DataValue();
            dataValue.setDataElement( dvt.getDataelement().getCode() );
            dataValue.setDataElementName( dvt.getDataelement().getName() );
            dataValue.setDataElementCode( dvt.getDataelement().getCode() );
            dataValue.setUid( dvt.getDataelement().getUid() );
            //dataValue.setCategoryOptionCombo( dvt.getDisaggregation().getCode() );
            dataValue.setCategoryOptionCombo( dvt.getDisaggregation().getUid() );
            dataValue.setCategoryOptionComboName( dvt.getDisaggregation().getName() );
            dataValue.setCategoryOptionComboCode( dvt.getDisaggregation().getCode() );
            dataValue.setAttributeOptionCombo( dvt.getDisaggregation().getAttributeOptionCombo() );

            try
            {
                String value = dao.evaluateDataValueTemplate( dvt, period, location );
                if ( value != null )
                {
                    System.out.println( dvt.getDataelement().getName() + " '" + dvt.getDataelement().getId() + "' '"
                        + dvt.getDataelement().getCode() + "' " + dvt.getDisaggregation().getName() + " " + value );
                    dataValue.setValue( value );
                    dataValues.add( dataValue );
                }
            }
            catch ( DHIS2ReportingException ex )
            {
                // TODO: percolate this through to UI
                log.warn( ex.getMessage() );
            }
        }

        return dataValueSet;
    }

    @Override
    public List<Object[]> evaluateDataElementQueries( DataElementQuery dataElementQuery, Location location )
    {
        List<Object[]> values = new ArrayList<Object[]>();
        try
        {
            values = dao.evaluateDataElementQuery( dataElementQuery, location );
        }
        catch ( DHIS2ReportingException ex )
        {
            // TODO: percolate this through to UI
            log.warn( ex.getMessage() );
        }

        return values;
    }

    @Override
    public DataValueSet generateReportingReportDefinition( ReportDefinition reportDefinition, Period period,
        Location location )
        throws Exception
    {
        Collection<DataValueTemplate> templates = reportDefinition.getDataValueTemplates();
        DataValueSet dataValueSet = new DataValueSet();
        dataValueSet.setDataElementIdScheme( "code" );
        dataValueSet.setOrgUnitIdScheme( "code" );
        dataValueSet.setPeriod( period.getAsIsoString() );
        dataValueSet.setDataSet( reportDefinition.getCode() );
        List<Object> dsrlist = new ArrayList<Object>();
        DataSetRow dsr = null;

        Collection<DataValue> dataValues = dataValueSet.getDataValues();

        ReportService reportService = Context.getService( ReportService.class );
        org.openmrs.module.reporting.report.definition.ReportDefinition rrd = Context.getService(
            ReportDefinitionService.class ).getDefinitionByUuid( reportDefinition.getReportingReportId() );
        if ( rrd instanceof PeriodIndicatorReportDefinition )
        {
            PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) rrd;
            PeriodIndicatorReportUtil.ensureDataSetDefinition( report );
        }
        else
        {
            throw new RuntimeException( "This report is not of the right class" );
        }

        Parameterizable parameterizable = ParameterizableUtil.getParameterizable( reportDefinition
            .getReportingReportId(), PeriodIndicatorReportDefinition.class );

        if ( parameterizable != null )
        {
            ReportData results = null;
            EvaluationContext evaluationContext = new EvaluationContext();

            Map<String, Object> parameterValues = new HashMap<String, Object>();
            if ( parameterizable != null && parameterizable.getParameters() != null )
            {
                for ( Parameter p : parameterizable.getParameters() )
                {
                    if ( p.getName().equals( "startDate" ) )
                        parameterValues.put( p.getName(), period.getStartDate() );
                    if ( p.getName().equals( "endDate" ) )
                        parameterValues.put( p.getName(), period.getEndDate() );
                    if ( p.getName().equals( "location" ) )
                        parameterValues.put( p.getName(), location );
                }
            }
            evaluationContext.setParameterValues( parameterValues );

            DataSet dataSet = null;

            try
            {
                results = (ReportData) ParameterizableUtil.evaluateParameterizable( parameterizable, evaluationContext );
                Iterator<Entry<String, DataSet>> iterator = results.getDataSets().entrySet().iterator();
                while ( iterator.hasNext() )
                {
                    dataSet = iterator.next().getValue();
                    if ( dataSet.iterator().hasNext() )
                    {
                        dsr = dataSet.iterator().next();
                        dsrlist = new ArrayList( dsr.getColumnValues().values() );
                        break;
                    }
                }
            }
            catch ( ParameterException e )
            {
                log.error( "unable to evaluate report: ", e );
            }
            catch ( MissingDependencyException ex )
            {
            }
        }

        int count = 0;
        for ( DataValueTemplate dvt : templates )
        {
            DataValue dataValue = new DataValue();
            dataValue.setDataElement( dvt.getDataelement().getCode() );
            //dataValue.setCategoryOptionCombo( dvt.getDisaggregation().getCode() );
            dataValue.setCategoryOptionCombo( dvt.getDisaggregation().getUid() );
            dataValue.setAttributeOptionCombo( dvt.getDisaggregation().getAttributeOptionCombo() );
            dataValue.setValue( dsrlist.get( count ).toString() );
            dataValues.add( dataValue );
            count++;
        }

        return dataValueSet;
    }

    @Override
    public void saveReportTemplates( ReportTemplates rt )
    {
        // throw new UnsupportedOperationException( "Not supported yet." );

        List<ReportDefinition> reportdef = rt.getReportDefinitions();

        for ( ReportDefinition rd : reportdef )
        {

            Set<DataValueTemplate> datavaluetemplate = rd.getDataValueTemplates();

            for ( DataValueTemplate dvt : datavaluetemplate )
            {

                saveDataValueTemplateTest( dvt );

            }

        }
    }

    public void unMarshallandSaveReportTemplates( InputStream is )
        throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance( ReportTemplates.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ReportTemplates reportTemplates = (ReportTemplates) jaxbUnmarshaller.unmarshal( is );

        //todo might need to add error handling
        if ( reportTemplates.getDataElements() != null )
        {
            for ( DataElement de : reportTemplates.getDataElements() )
            {
                saveDataElement( de );
            }
        }
        for ( Disaggregation disagg : reportTemplates.getDisaggregations() )
        {
            saveDisaggregation( disagg );
        }
        for ( ReportDefinition rd : reportTemplates.getReportDefinitions() )
        {
            if ( rd.getMetaDataValueTemplates() != null )
            {
                for ( MetaDataValueTemplate mdvt : rd.getMetaDataValueTemplates() )
                {
                    mdvt.setReportDefinition( rd );
                }
            }
            if ( rd.getDataValueTemplates() != null )
            {
                for ( DataValueTemplate dvt : rd.getDataValueTemplates() )
                {
                    dvt.setReportDefinition( rd );
                }
            }
            if ( reportTemplates.getDataElementQuerys() != null )
            {
                for ( DataElementQuery dq : reportTemplates.getDataElementQuerys() )
                {
                    rd.addQueries( dq );
                }
            }
            saveReportDefinition( rd );
        }
    }

    @Override
    public ReportTemplates getReportTemplates()
    {
        ReportTemplates rt = new ReportTemplates();

        rt.setDataElements( getAllDataElements() );
        rt.setDisaggregations( getAllDisaggregations() );
        rt.setReportDefinitions( getAllReportDefinitions() );

        return rt;
    }

    @Override
    public void marshallReportTemplates( OutputStream os, ReportTemplates rt )
        throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance( ReportTemplates.class );
        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.marshal( rt, os );
    }

    @Override
    public DataValueTemplate getDataValueTemplate( Integer id )
    {
        return dao.getDataValueTemplate( id );
    }

    @Override
    public void saveDataValueTemplate( DataValueTemplate dvt )
    {
        dao.saveDataValueTemplate( dvt );

    }

    @Override
    public void saveDataValueTemplateTest( DataValueTemplate dvt )
    {
        dao.saveDataValueTemplateTest( dvt );

    }

    @Override
    public Location getLocationByOU_Code( String OU_Code )
    {
        return dao.getLocationByOU_Code( OU_Code );
    }

    @Override
    public Location getLocationByOrgUnitCode( String orgUnitCode )
    {
        List<Location> locationList = new ArrayList<Location>();
        locationList.addAll( Context.getLocationService().getAllLocations() );
        for ( Location l : locationList )
        {
            for ( LocationAttribute la : l.getActiveAttributes() )
            {
                if ( la.getAttributeType().getName().equals( "CODE" ) )
                {
                    // System.out.println( la.getValue().toString() );
                    if ( (la.getValue().toString()).equals( orgUnitCode ) )
                    {
                        return l;
                    }
                }

            }
        }
        return null;
    }

    /***
     *
     ***/
    @Override
    public List<AggregatedResultSet> postBulkReportDefinition( String reportType, String destination, String freq,
        String dateStr, int consecutive, String mappingType )
        throws ParseException, LocationException, SendMetaDataException, SendReportException
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        //get a period from the date string
        Period period;
        period = getPeriodFromDateString( freq, dateStr );

        //get period list of consecutive dates after current period up to consecutive value limit
        List<Period> periodList = generatePeriodList( period, consecutive );

        // Get Location by OrgUnit Code
        List<Location> locationListFinal;
        locationListFinal = getValidLocationList();

        List<AggregatedResultSet> aggregatedList = new ArrayList<AggregatedResultSet>();

        //run reports and metadata
        List<ReportDefinition> definitionList = service.getReportDefinitionByPeriodType( reportType );
        for ( ReportDefinition r : definitionList )
        {
            for ( Location l : locationListFinal )
            {
                //send meta data
                try
                {
                    aggregatedList.add( sendMetadata( destination, r, l ) );
                }
                catch ( DHIS2ReportingException ex )
                {
                    ex.printStackTrace();
                    throw new SendMetaDataException();
                }

                for ( Period periodValue : periodList )
                {
                    //send report
                    try
                    {
                        aggregatedList.add( sendReport( mappingType, destination, r, periodValue, l ) );
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                        throw new SendReportException();
                    }

                }
            }
        }
        return aggregatedList;
    }

    @Override
    public List<AggregatedResultSet> postReportDefinition( int reportDefinition_id, String destination, String freq,
        String dateStr, int consecutive, String mappingType )
        throws ParseException, LocationException, SendMetaDataException, SendReportException
    {
        //get a period from the date string
        Period period;

        period = getPeriodFromDateString( freq, dateStr );

        //get period list of consecutive dates after current period up to consecutive value limit
        List<Period> periodList = generatePeriodList( period, consecutive );

        // Get Location by OrgUnit Code
        List<Location> locationListFinal;
        locationListFinal = getValidLocationList();

        //ImportSummaries importSummaries = null;
        List<AggregatedResultSet> aggregatedList = new ArrayList<AggregatedResultSet>();

        //set up and run report definition

        //for each location
        for ( Location l : locationListFinal )
        {
            ReportDefinition report = Context.getService( DHIS2ReportingService.class ).getReportDefinition(
                reportDefinition_id );

            //send meta data
            try
            {
                aggregatedList.add( sendMetadata( destination, report, l ) );
            }
            catch ( DHIS2ReportingException ex )
            {
                ex.printStackTrace();
                throw new SendMetaDataException();
            }

            //for each date period
            for ( Period periodValue : periodList )
            {

                //send report
                try
                {
                    System.out.println( "report start" );
                    aggregatedList.add( sendReport( mappingType, destination, report, periodValue, l ) );
                    System.out.println( "report end" );
                }
                catch ( Exception e )
                {
                    System.out.println( "report error" );
                    e.printStackTrace();
                    throw new SendReportException();
                }
            }
        }
        return aggregatedList;
    }

    public Period getPeriodFromDateString( String freq, String dateStr )
        throws ParseException
    {
        Period period = null;
        if ( freq.equalsIgnoreCase( "monthly" ) )
        {
            period = monthly( dateStr );
        }
        if ( freq.equalsIgnoreCase( "weekly" ) )
        {
            period = weekly( dateStr );
        }
        if ( freq.equalsIgnoreCase( "daily" ) )
        {
            period = daily( dateStr );
        }
        return period;
    }

    public List<Period> generatePeriodList( Period period, int consecutive )
    {
        List<Period> periodList = new ArrayList<Period>();
        periodList.add( period );
        for ( int i = 1; i < consecutive; i++ )
        {
            Period nextPeriod = period.getAsIsoStringNextValue( i );
            periodList.add( nextPeriod );
        }
        return periodList;
    }

    public List<Location> getValidLocationList()
        throws LocationException
    {
        List<Location> locationList = new ArrayList<Location>();
        List<Location> locationListFinal = new ArrayList<Location>();
        //use below line if not location restricting
        locationList.addAll( Context.getLocationService().getAllLocations() );
        //use below line if location restricting to current
        //locationList.add( LocationUtility.getDefaultLocation() );
        Location userLocation = LocationUtility.getDefaultLocation();
        //remove locations without Organization Unit Codes
        for ( Location l : locationList )
        {
            for ( LocationAttribute la : l.getActiveAttributes() )
            {
                if ( la.getAttributeType().getName().equals( "CODE" ) && (l.getId() == userLocation.getId()) )
                {
                    if ( !la.getValue().toString().isEmpty() && la.getValue().toString() != null )
                    {
                        locationListFinal.add( l );
                        break;
                    }
                }
                else if ( la.getAttributeType().getName().equals( "MASTERCODE" ) )
                {
                    if ( !la.getValue().toString().isEmpty() && la.getValue().toString() != null )
                    {
                        locationListFinal.add( l );
                        break;
                    }
                }
            }
        }

        if ( locationListFinal.isEmpty() && !locationList.isEmpty() )
        {
            throw new LocationException();
        }

        return locationListFinal;
    }

    public AggregatedResultSet sendMetadata( String destination, ReportDefinition report, Location l )
        throws DHIS2ReportingException
    {
        System.out.println( "send meta data" );
        AggregatedResultSet agrs = new AggregatedResultSet();

        DataValueSet dvs = new DataValueSet();
        dvs.setDataSet( "Meta Data export" );
        dvs.setOrgUnit( l.getName() );

        ImportSummaries importSummaries = null;
        Set<DataElementQuery> reportlist = report.getQueries();
        for ( DataElementQuery eq : reportlist )
        {
            List<Object[]> metadataelements = Context.getService( DHIS2ReportingService.class )
                .evaluateDataElementQueries( eq, l );
            if ( destination.equals( "post" ) )
            {
                importSummaries = postMetaData( metadataelements, eq );
                //need to get the uids from dhis2 for each element generated
                List<String[]> uids = getDhis2Metadata( metadataelements, eq.getCodeprefix() );

                for ( int i = 0; i < uids.size(); i++ )
                {
                    DataValue dv = new DataValue();
                    dv.setDataElement( uids.get( i )[0] );
                    dv.setDataElementName( uids.get( i )[0] );
                    dv.setDataElementCode( uids.get( i )[1] );
                    dv.setCategoryOptionComboName( "meta data" );
                    dv.setValue( "sent" );
                    dvs.addDataValue( dv );

                    for ( MetaDataValueTemplate mdt : report.getMetaDataValueTemplates() )
                    {
                        System.out.println( "run " + uids.get( i )[0] + " '"
                            + ((Integer) (metadataelements.get( i )[0])).intValue() + "' "
                            + mdt.getDisaggregation().getName() );
                        if ( !contains( report, uids.get( i )[0], mdt.getDisaggregation().getName() ) )
                        {
                            System.out.println( "ran" );
                            DataElement element = Context.getService( DHIS2ReportingService.class )
                                .getDataElementByCode( eq.getCodeprefix() + uids.get( i )[2] );
                            System.out.println( element );
                            if ( element == null )
                            {
                                element = new DataElement();
                                element.setName( uids.get( i )[0] );
                                element.setCode( eq.getCodeprefix() + uids.get( i )[2] );
                                element.setUid( uids.get( i )[1] );
                            }
                            Context.getService( DHIS2ReportingService.class ).saveDataElement( element );
                            DataValueTemplate data = new DataValueTemplate();
                            data.setDisaggregation( mdt.getDisaggregation() );
                            data.setDataelement( element );
                            String query = mdt.getQuery();
                            String newquery = query;
                            int idcode = Integer.parseInt( uids.get( i )[2] );
                            if ( query.contains( "#metaDataId" ) )
                            {
                                //(Integer) (metadataelements.get( i )[0])).intValue()
                                newquery = query.replaceAll( "#metaDataId", idcode + "" );
                            }
                            data.setQuery( newquery );
                            data.setReportDefinition( report );
                            report.addDataValueTemplate( data );
                        }

                    }
                }
            }

        }
        System.out.println( dvs.getDataValues().size() );
        System.out.println( importSummaries );
        agrs.setDataValueSet( dvs );
        agrs.setImportSummaries( importSummaries );
        return agrs;
    }

    public ImportSummaries postReport( AdxType adxType )
        throws DHIS2ReportingException
    {
        String standard = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2Standard" );
        ImportSummaries importSummaries = null;
        if ( standard.equals( "adx" ) )
        {
            importSummaries = Context.getService( DHIS2ReportingService.class ).postAdxReport( adxType );
        }
        else
        {
            importSummaries = Context.getService( DHIS2ReportingService.class ).postDxf2Report( adxType );
        }
        return importSummaries;
    }

    public AggregatedResultSet sendReport( String mappingType, String destination, ReportDefinition report,
        Period periodValue, Location l )
        throws Exception
    {
        AggregatedResultSet agrs = new AggregatedResultSet();
        // Set OrgUnit code into DataValueSet
        DataValueSet dvs = null;
        if ( mappingType.equalsIgnoreCase( "SQL" ) )
        {
            dvs = Context.getService( DHIS2ReportingService.class ).evaluateReportDefinition( report, periodValue, l );
        }
        else if ( mappingType.equalsIgnoreCase( "Reporting" ) )
        {
            dvs = Context.getService( DHIS2ReportingService.class ).generateReportingReportDefinition( report,
                periodValue, l );
        }

        if ( dvs == null )
        {
            return agrs;
        }

        for ( LocationAttribute la : l.getActiveAttributes() )
        {
            if ( la.getAttributeType().getName().equals( "CODE" ) )
                dvs.setOrgUnit( la.getValue().toString() );
        }

        //
        List<DataValue> datavalue = dvs.getDataValues();
        /*
        Map<DataElement, String> deset = new HashMap<DataElement, String>();
        for ( DataValue dv : datavalue )
        {
            DataElement detrmp = Context.getService( DHIS2ReportingService.class ).getDataElementByCode(
                dv.getDataElement() );
            deset.put( detrmp, dv.getValue() );
            System.out.println( "dv value: " + dv.getValue() );
        }
         */
        agrs.setDataValueSet( dvs );
        //agrs.setDataElementMap( deset );
        AdxType adxType = getAdxType( dvs, periodValue.getAsIsoString() );

        //todo
        //dvs.getDataValues().get(0).getCategoryOptionCombo();

        if ( destination.equals( "post" ) )
        {
            ImportSummaries importSummaries = postReport( adxType );
            if ( importSummaries != null )
            {
                agrs.setImportSummaries( importSummaries );
            }
        }
        return agrs;
    }

    AdxType getAdxType( DataValueSet dvs, String timeperiod )
    {
        AdxType adxType = new AdxType();
        adxType.setExported( dvs.getCompleteDate() );
        GroupType gt = new GroupType();
        List<DataValueType> dvTypeList = new ArrayList<DataValueType>();
        for ( DataValue dv : dvs.getDataValues() )
        {
            DataValueType dvtype = new DataValueType();
            dvtype.setDataElement( dv.getUid() );
            dvtype.setValue( new BigDecimal( dv.getValue() ) );
            dvtype.setCategoryOptionCombo( dv.getCategoryOptionCombo() );
            dvtype.setAttributeOptionCombo( dv.getAttributeOptionCombo() );
            dvTypeList.add( dvtype );
        }
        gt.getDataValue().addAll( dvTypeList );
        gt.setOrgUnit( dvs.getOrgUnit() );
        gt.setDataSet( dvs.getDataSet() );
        gt.setPeriod( timeperiod );
        adxType.getGroup().add( gt );
        return adxType;
    }

    ImportSummaries postMetaData( List<Object[]> metadataelements, DataElementQuery eq )
        throws DHIS2ReportingException
    {
        //todo generate metadata file
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        Date now = new Date();
        String metadatafile = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
        metadatafile += "<metaData xmlns=\"http://dhis2.org/schema/dxf/2.0\" created=\"" + dateFormat.format( now )
            + "\">";
        metadatafile += "<dataElements>";
        for ( int i = 0; i < metadataelements.size(); i++ )
        {
            //fixed via kmri 1005
            //turns out xml doesn't like ampersands in its string values
            String metaDataElementName = (String) (metadataelements.get( i )[1]);
            metaDataElementName = metaDataElementName.replaceAll( "&", "and" );

            metadatafile += "<dataElement ";
            metadatafile += "code=\"" + eq.getCodeprefix() + ((Integer) (metadataelements.get( i )[0])).intValue()
                + "\" ";
            metadatafile += "name=\"" + eq.getPrefix() + ": " + metaDataElementName + "\" ";
            metadatafile += "shortName=\"" + eq.getPrefix() + ": " + metaDataElementName + "\" ";
            metadatafile += ">";
            metadatafile += "<externalAccess>false</externalAccess>";
            metadatafile += "<aggregationType>SUM</aggregationType>";
            metadatafile += "<dataDimension>true</dataDimension>";
            metadatafile += "<valueType>INTEGER</valueType>";
            metadatafile += "<domainType>AGGREGATE</domainType>";
            metadatafile += "<url></url>";
            metadatafile += "<categoryCombo name=\"" + eq.getDisaggregation().getName() + "\" id =\""
                + eq.getDisaggregation().getUid() + "\" ></categoryCombo>";
            metadatafile += "<zeroIsSignificant>false</zeroIsSignificant>";
            metadatafile += "</dataElement>";
        }
        metadatafile += "</dataElements>";
        metadatafile += "</metaData>";
        //System.out.println( metadatafile );

        return Context.getService( DHIS2ReportingService.class ).postMetaData( metadatafile );
    }

    public List<String[]> getDhis2Metadata( List<Object[]> metadata, String prefix )
    {
        return Context.getService( DHIS2ReportingService.class ).getDataElements( metadata, prefix );
    }

    public boolean contains( ReportDefinition report, String elementName, String disaggrigationName )
    {
        Iterator i = report.getDataValueTemplates().iterator();
        while ( i.hasNext() )
        {
            DataValueTemplate value = (DataValueTemplate) i.next();
            String test = value.getDataelement().getName();
            String test2 = value.getDisaggregation().getName();
            if ( test.equals( elementName ) && test2.equals( disaggrigationName ) )
            {
                return true;
            }
        }
        return false;
    }

    public Period monthly( String dateStr )
        throws ParseException
    {
        Period period;
        if ( dateStr.length() > 7 )
            dateStr = replacedateStrMonth( dateStr );
        dateStr = dateStr.concat( "-01" );

        //System.out.println( "helloooooooooo1=====" + dateStr );
        period = new MonthlyPeriod( new SimpleDateFormat( "yyyy-MM-dd" ).parse( dateStr ) );
        // System.out.println( "helloooooooooo2=====" + period );

        return period;
    }

    public Period weekly( String dateStr )
        throws ParseException
    {
        Period period;
        String finalweek;
        String[] modify_week = dateStr.split( "W" );
        Integer weekvalue = Integer.parseInt( dateStr.substring( dateStr.indexOf( 'W' ) + 1 ) ) + 1;
        if ( weekvalue > 9 )
        {
            weekvalue = weekvalue == 54 ? 53 : weekvalue;
            finalweek = modify_week[0].concat( "W" + weekvalue.toString() );
        }
        else
        {
            finalweek = modify_week[0].concat( "W0" + weekvalue.toString() );
        }

        period = new WeeklyPeriod( new SimpleDateFormat( "yyyy-'W'ww" ).parse( finalweek ) );

        return period;
    }

    public Period daily( String dateStr )
        throws ParseException
    {
        Period period;

        period = new DailyPeriod( new SimpleDateFormat( "MM/dd/yyyy" ).parse( dateStr ) );

        return period;
    }

    private String replacedateStrMonth( String dateStr )
    {

        String str = "";
        // System.out.println( dateStr.substring( 5, 8 ) );

        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Jan" ) )
        {
            //System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Jan", "01" );
            // System.out.println( "converting date" + str );
        }
        else if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Feb" ) )
        {
            //  System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Feb", "02" );
            //  System.out.println( "converting date" + str );
        }
        else if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Mar" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Mar", "03" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Apr" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Apr", "04" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "May" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "May", "05" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Jun" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Jun", "06" );
            //  System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Jul" ) )
        {
            //  System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Jul", "07" );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Aug" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Aug", "08" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Sep" ) )
        {
            //  System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Sep", "09" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Oct" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Oct", "10" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Nov" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Nov", "11" );
            // System.out.println( "converting date" + str );
        }
        if ( dateStr.substring( 5, 8 ).equalsIgnoreCase( "Dec" ) )
        {
            // System.out.println( "converting date" );
            str = dateStr.replaceFirst( "Dec", "12" );
            //  System.out.println( "converting date" + str );
        }

        return str;
    }
}
