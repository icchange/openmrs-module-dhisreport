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
import java.text.DateFormat;
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
import org.openmrs.module.dhisreport.api.dhis.Dhis2Server;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.model.*;
import org.openmrs.module.dhisreport.api.dxf2.DataValue;
import org.openmrs.module.dhisreport.api.dxf2.DataValueSet;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.module.dhisreport.api.trackercapture.TrackedEntity;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureAttribute;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;
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
    public ImportSummaries postTrackerCapture( String trackerCapture )
        throws DHIS2ReportingException
    {
        //todo
        return dhis2Server.postTrackerCapture( trackerCapture );
    }

    @Override
    public List<String[]> getDataElements( List<Object[]> elements, String prefix )
    {
        return dhis2Server.getDataElements( elements, prefix );
    }

    @Override
    public List<TrackedEntity> getTrackedEntities( String ouCode )
    {
        return dhis2Server.getTrackedEntities( ouCode );
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

    @Override
    public TrackerCaptureAttribute saveTrackerCaptureAttribute( TrackerCaptureAttribute dq )
    {
        return dao.saveTrackerCaptureAttribute( dq );
    }

    @Override
    public TrackerCaptureAttribute getTrackerCaptureAttribute( Integer id )
    {
        return dao.getTrackerCaptureAttribute( id );
    }

    @Override
    public TrackerCaptureEnrollment saveTrackerCaptureEnrollment( TrackerCaptureEnrollment dq )
    {
        return dao.saveTrackerCaptureEnrollment( dq );
    }

    @Override
    public TrackerCaptureEnrollment getTrackerCaptureEnrollment( Integer id )
    {
        return dao.getTrackerCaptureEnrollment( id );
    }

    @Override
    public TrackerCaptureTemplate saveTrackerCaptureReport( TrackerCaptureTemplate tct )
    {
        return dao.saveTrackerCaptureReport( tct );
    }

    @Override
    public TrackerCaptureTemplate getTrackerCaptureReport( Integer id )
    {
        return dao.getTrackerCaptureReport( id );
    }

    @Override
    public void purgeTrackerCaptureReport( TrackerCaptureTemplate tct )
    {
        dao.deleteTrackerCaptureReport( tct );
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
    public Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates()
    {
        return dao.getAllTrackerCaptureTemplates();
    }

    @Override
    public String evaluateDataValueTemplate( DataValueTemplate dv, Period period, Location location )
        throws DHIS2ReportingException
    {
        return dao.evaluateDataValueTemplate( dv, period, location );
    }

    @Override
    public List<Map<String, Object>> evaluateTrackerCaptureEnrollment( TrackerCaptureEnrollment e, Location location )
        throws DHIS2ReportingException
    {
        return dao.evaluateTrackerCaptureEnrollment( e.getQuery(), location );
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
    public List<Map<String, Object>> evaluateTrackerCaptureQuery( String query )
    {
        try
        {
            return dao.evaluateTrackerCaptureQuery( query );
        }
        catch ( DHIS2ReportingException ex )
        {
            // TODO: percolate this through to UI
            log.warn( ex.getMessage() );
        }
        return null;
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

    public void unMarshallandSaveTrackerCaptureTemplate( InputStream is )
        throws Exception
    {
        JAXBContext jaxbContext = JAXBContext.newInstance( TrackerCaptureTemplate.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        TrackerCaptureTemplate trackerCaptureTemplates = (TrackerCaptureTemplate) jaxbUnmarshaller.unmarshal( is );
        saveTrackerCaptureReport( trackerCaptureTemplates );

        if ( trackerCaptureTemplates.getTrackerCaptureAttributeList() != null )
        {
            for ( TrackerCaptureAttribute attr : trackerCaptureTemplates.getTrackerCaptureAttributeList() )
            {
                attr.setTemplate( trackerCaptureTemplates );
                saveTrackerCaptureAttribute( attr );
            }
        }

        if ( trackerCaptureTemplates.getTrackerCaptureEnrollmentList() != null )
        {
            for ( TrackerCaptureEnrollment enroll : trackerCaptureTemplates.getTrackerCaptureEnrollmentList() )
            {
                enroll.setTemplate( trackerCaptureTemplates );
                saveTrackerCaptureEnrollment( enroll );
            }
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

    @Override
    public List<AggregatedResultSet> postTrackerCapture( int trackerCapture_id, String destination )
        throws ParseException, LocationException, SendMetaDataException, SendReportException
    {
        List<AggregatedResultSet> aggregatedList = new ArrayList<AggregatedResultSet>();
        String ouCode;
        try
        {
            TrackerCaptureTemplate report = Context.getService( DHIS2ReportingService.class ).getTrackerCaptureReport(
                trackerCapture_id );

            /*
            get tracked entities from dhis2 that corospond to this reports tracked entity id
             */
            ouCode = dhis2Server.getOrgUnitCode();
            List<TrackedEntity> entityList = this.getTrackedEntities( ouCode );
            System.out.println( entityList.size() );
            for ( TrackedEntity e : entityList )
            {
                System.out.println( e.getAttributes().get( report.getKeyAttribute().getName() ).toString() );
            }

            /*
            run query to get openmrs side tracked entity and check if there is differences
             */
            List<Map<String, Object>> resultMap = Context.getService( DHIS2ReportingService.class )
                .evaluateTrackerCaptureQuery( report.getQuery() );
            System.out.println( resultMap.size() );
            for ( Map<String, Object> e : resultMap )
            {
                System.out.println( e.get( report.getKeyAttribute().getQuery() ).toString() );
            }

            List<TrackedEntity> deleteFromDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> sameAsDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> addToDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> updateInDhis2List = new ArrayList<TrackedEntity>();

            //separate dhis2 information into corresponding lists
            boolean[] enitiyMatchList = new boolean[entityList.size()];
            for ( int i = 0; i < resultMap.size(); i++ )
            {
                boolean match = false;
                for ( int j = 0; j < entityList.size(); j++ )
                {
                    //if the entity doesn't have a an id element its not an from openmrs so delete it
                    if ( entityList.get( j ).getAttributes().get( report.getKeyAttribute().getName() ) == null )
                    {
                        enitiyMatchList[j] = true;
                        break;
                    }
                    //check if the list has the same id as
                    if ( entityList.get( j ).getAttributes().get( report.getKeyAttribute().getName() ).equals(
                        resultMap.get( i ).get( report.getKeyAttribute().getQuery() ).toString() ) )
                    {
                        match = true;
                        enitiyMatchList[j] = true;
                        //check to see if all the attributes are the same
                        int samenum = 0;
                        for ( TrackerCaptureAttribute t : report.getTrackerCaptureAttributeList() )
                        {
                            if ( entityList.get( j ).getAttributes().get( t.getName() ).equals(
                                resultMap.get( i ).get( t.getQuery() ).toString() ) )
                            {
                                samenum++;
                            }
                        }
                        if ( samenum == report.getTrackerCaptureAttributeList().size() )
                        {
                            sameAsDhis2List.add( entityList.get( j ) );
                        }
                        else
                        {
                            updateInDhis2List.add( entityList.get( j ) );
                        }
                        break;
                    }
                }
                //if there was no match for the element in result map then it needs to be added to dhis2
                if ( !match )
                {
                    String uid = null;
                    Map<String, String> attrs = new HashMap<String, String>();
                    for ( TrackerCaptureAttribute t : report.getTrackerCaptureAttributeList() )
                    {
                        attrs.put( t.getQuery(), resultMap.get( i ).get( t.getQuery() ).toString() );
                    }
                    TrackedEntity newTrackedEntity = new TrackedEntity( uid, attrs );
                    addToDhis2List.add( newTrackedEntity );
                }
            }
            //go through entityList for all the elements with no element in enitiyMatchList and delete those
            //because they aren't in openmrs anymore
            for ( int i = 0; i < entityList.size(); i++ )
            {
                if ( enitiyMatchList[i] == false )
                {
                    deleteFromDhis2List.add( entityList.get( i ) );
                }
            }

            //data output
            System.out.println( "DELETE ELEMENTS" );
            for ( int i = 0; i < deleteFromDhis2List.size(); i++ )
            {
                TrackedEntity e = deleteFromDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
            }
            System.out.println( "ADD ELEMENTS" );
            for ( int i = 0; i < addToDhis2List.size(); i++ )
            {
                TrackedEntity e = addToDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
            }
            System.out.println( "SAME ELEMENTS" );
            for ( int i = 0; i < sameAsDhis2List.size(); i++ )
            {
                TrackedEntity e = sameAsDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
            }
            System.out.println( "UPDATE ELEMENTS" );
            for ( int i = 0; i < updateInDhis2List.size(); i++ )
            {
                TrackedEntity e = updateInDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
            }

            /*
             delete TrackedEntity elements that arent in openmrs
             */
            for ( TrackedEntity entity : deleteFromDhis2List )
            {
                if ( entity.getUid() != null )
                {
                    dhis2Server.deleteTrackerCapture( entity );
                }
            }

            //todo create update data messages to update records that have changed if required

            /*
            create update data message to add new records that aren't on the dhis2 side
             */
            sendTrackerCapture( addToDhis2List, destination, report, LocationUtility.getDefaultLocation(), ouCode );

            /*
            find all uids for TrackedEntitys added to dhis2
             */
            System.out.println( "new entity list" );
            List<TrackedEntity> newentityList = this.getTrackedEntities( ouCode );
            for ( TrackedEntity entity : newentityList )
            {
                System.out.println( entity.getUid() + " "
                    + entity.getAttributes().get( report.getKeyAttribute().getName() ) );
            }

            //todo get all program data
            System.out.println( "program info" );
            for ( TrackerCaptureEnrollment t : report.getTrackerCaptureEnrollmentList() )
            {
                System.out.println( t.getName() );
                List<Map<String, Object>> list = Context.getService( DHIS2ReportingService.class )
                    .evaluateTrackerCaptureEnrollment( t, LocationUtility.getDefaultLocation() );
                t.setQueryList( list );
                /*
                for ( int i = 0; i < list.size(); i++ )
                {

                    System.out.println( list.get( i ).get( "id" ).toString() + " "
                        + list.get( i ).get( "date" ).toString() );
                }
                 */
            }

            //todo send program enrollment data
            System.out.println( "send enrollment data" );
            for ( TrackedEntity entity : newentityList )
            {
                for ( TrackerCaptureEnrollment enrollment : report.getTrackerCaptureEnrollmentList() )
                {
                    /*
                    System.out.println( entity.getAttributes().get( report.getKeyAttribute().getName() )
                        + " "
                        + enrollment.checkQueryList( entity.getAttributes().get( report.getKeyAttribute().getName() ) )
                        + " "
                        + enrollment
                            .getQueryListDate( entity.getAttributes().get( report.getKeyAttribute().getName() ) ) );
                     */
                    if ( enrollment.checkQueryList( entity.getAttributes().get( report.getKeyAttribute().getName() ) ) )
                    {
                        //send enrollment
                        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
                        Date date = new Date();
                        String message = "";
                        message += "{";
                        message += "\"trackedEntityInstance\": \"" + entity.getUid() + "\",";
                        message += "\"orgUnit\": \"" + ouCode + "\",";
                        message += "\"program\": \"" + enrollment.getUid() + "\",";
                        message += "\"enrollmentDate\": \"" + dateFormat.format( date ) + "\",";
                        message += "\"incidentDate\": \""
                            + enrollment.getQueryListDate( entity.getAttributes().get(
                                report.getKeyAttribute().getName() ) ) + "\"";
                        message += "}";

                        dhis2Server.postEnrollment( message );
                    }
                }

            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
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
        AggregatedResultSet agrs = new AggregatedResultSet();

        DataValueSet dvs = new DataValueSet();
        dvs.setDataSet( "Meta Data export" );
        dvs.setOrgUnit( l.getName() );
        agrs.setDataValueSet( dvs );

        ImportSummaries importSummaries = null;
        System.out.println( "test1" );
        for ( DataElementQuery eq : report.getQueries() )
        {
            System.out.println( "test2" );
            List<Object[]> metadataelements = Context.getService( DHIS2ReportingService.class )
                .evaluateDataElementQueries( eq, l );
            if ( destination.equals( "post" ) )
            {
                System.out.println( "test3" );
                importSummaries = postMetaData( metadataelements, eq );
                //need to get the uids from dhis2 for each element generated
                List<String[]> uids = getDhis2Metadata( metadataelements, eq.getPrefix() );

                for ( MetaDataValueTemplate mdt : report.getMetaDataValueTemplates() )
                {
                    for ( int i = 0; i < uids.size(); i++ )
                    {
                        if ( !contains( report, uids.get( i )[0] ) )
                        {
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
                            if ( query.contains( "#metaDataId" ) )
                            {
                                newquery = query.replaceAll( "#metaDataId", ((Integer) (metadataelements.get( i )[0]))
                                    .intValue()
                                    + "" );
                            }
                            data.setQuery( newquery );
                            data.setReportDefinition( report );
                            report.addDataValueTemplate( data );
                        }
                    }
                }
            }
        }
        agrs.setImportSummaries( importSummaries );
        return agrs;
    }

    public AggregatedResultSet sendTrackerCapture( List<TrackedEntity> list, String destination,
        TrackerCaptureTemplate report, Location l, String ouCode )
        throws DHIS2ReportingException
    {
        AggregatedResultSet agrs = new AggregatedResultSet();

        String jsonMessage = "";
        jsonMessage += "{\"trackedEntityInstances\": [";
        if ( list != null && list.size() != 0 )
        {

            for ( TrackedEntity result : list )
            {
                jsonMessage += "{";
                jsonMessage += " \"trackedEntity\":\"" + report.getUid() + "\",";
                jsonMessage += "\"orgUnit\": \"" + l.getName() + "\",";
                jsonMessage += "\"attributes\": [";
                if ( report.getTrackerCaptureAttributeList() != null
                    && report.getTrackerCaptureAttributeList().size() != 0 )
                {

                    for ( TrackerCaptureAttribute attribute : report.getTrackerCaptureAttributeList() )
                    {
                        jsonMessage += "{";
                        String output = result.getAttributes().get( attribute.getQuery() ).toString();
                        jsonMessage += "\"attribute\": \"" + attribute.getUid() + "\",";
                        jsonMessage += " \"value\": \"" + output + "\"";
                        jsonMessage += "},";
                    }
                    jsonMessage = jsonMessage.substring( 0, jsonMessage.length() - 1 );
                }
                jsonMessage += "]";
                jsonMessage += "},";
            }
            jsonMessage = jsonMessage.substring( 0, jsonMessage.length() - 1 );
        }
        jsonMessage += "]}";

        if ( destination.equals( "post" ) )
        {
            agrs
                .setImportSummaries( Context.getService( DHIS2ReportingService.class ).postTrackerCapture( jsonMessage ) );
        }
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

            metadatafile += "<dataElement ";
            metadatafile += "code=\"" + eq.getCodeprefix() + ((Integer) (metadataelements.get( i )[0])).intValue()
                + "\" ";
            metadatafile += "name=\"" + eq.getPrefix() + ": " + (metadataelements.get( i )[1]) + "\" ";
            metadatafile += "shortName=\"" + eq.getPrefix() + ": " + (metadataelements.get( i )[1]) + "\" ";
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

    public boolean contains( ReportDefinition report, String elementName )
    {
        Iterator i = report.getDataValueTemplates().iterator();
        while ( i.hasNext() )
        {
            String test = ((DataValueTemplate) i.next()).getDataelement().getName();
            if ( test.equals( elementName ) )
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
