package org.openmrs.module.dhisreport.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisreport.api.AggregatedResultSet;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.DHIS2TrackerCaptureService;
import org.openmrs.module.dhisreport.api.db.DHIS2TrackerCaptureDAO;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.trackercapture.*;
import org.openmrs.util.LocationUtility;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ICCHANGE on 31/Mar/2017.
 */
public class DHIS2TrackerCaptureServiceImpl
    extends BaseOpenmrsService
    implements DHIS2TrackerCaptureService
{
    protected final Log log = LogFactory.getLog( this.getClass() );

    private DHIS2TrackerCaptureDAO dao;

    private HttpDhis2Server dhis2Server;

    /**
     * @param dao the dao to set
     */
    public void setDao( DHIS2TrackerCaptureDAO dao )
    {
        this.dao = dao;
    }

    /**
     * @return the dao
     */
    public DHIS2TrackerCaptureDAO getDao()
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
    public Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates()
    {
        return dao.getAllTrackerCaptureTemplates();
    }

    @Override
    public List<TrackedEntity> getTrackedEntities( String ouCode )
    {
        return dhis2Server.getTrackedEntities( ouCode );
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
    public List<Map<String, Object>> evaluateTrackerCaptureEnrollment( TrackerCaptureEnrollment e, Location location )
        throws DHIS2ReportingException
    {
        return dao.evaluateTrackerCaptureEnrollment( e.getQuery(), location );
    }

    @Override
    public List<TrackerCaptureTemplateMessage> postTrackerCapture( String trackerCapture )
        throws DHIS2ReportingException
    {
        return dhis2Server.postTrackerCapture( trackerCapture );
    }

    public TrackerCaptureResultSet sendTrackerCapture( List<TrackedEntity> list, String destination,
        TrackerCaptureTemplate report, Location l )
        throws DHIS2ReportingException
    {
        TrackerCaptureResultSet agrs = new TrackerCaptureResultSet();

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
            List<TrackerCaptureTemplateMessage> messages = Context.getService( DHIS2TrackerCaptureService.class )
                .postTrackerCapture( jsonMessage );
            agrs.addMessageList( messages );
        }
        return agrs;
    }

    public AggregatedResultSet updateTrackerCapture( List<TrackedEntity> list, String destination,
        TrackerCaptureTemplate report, Location l )
        throws DHIS2ReportingException
    {
        AggregatedResultSet agrs = new AggregatedResultSet();

        if ( list != null && list.size() != 0 )
        {

            for ( TrackedEntity result : list )
            {
                String jsonMessage = "";
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
                jsonMessage += "}";
                dhis2Server.updateTrackerCapture( jsonMessage, result );
            }
        }
        return agrs;
    }

    @Override
    public List<TrackerCaptureResultSet> postTrackerCapture( int trackerCapture_id, String destination )
        throws ParseException, LocationException, SendMetaDataException, SendReportException//, JAXBException
    {
        List<TrackerCaptureResultSet> aggregatedList = new ArrayList<TrackerCaptureResultSet>();
        String ouCode;
        try
        {
            TrackerCaptureTemplate report = Context.getService( DHIS2TrackerCaptureService.class )
                .getTrackerCaptureReport( trackerCapture_id );
            /*
            get tracked entities from dhis2 that corospond to this reports tracked entity id
             */
            ouCode = dhis2Server.getOrgUnitCode();
            List<TrackedEntity> entityList = this.getTrackedEntities( ouCode );

            /*
            run query to get openmrs side tracked entity and check if there is differences
             */
            List<Map<String, Object>> resultMap = Context.getService( DHIS2TrackerCaptureService.class )
                .evaluateTrackerCaptureQuery( report.getQuery() );
            /*
            System.out.println( resultMap.size() );
            System.out.println( "things on dhis2 side: " + entityList.size() );
            for ( TrackedEntity et : entityList )
            {
                System.out.println( et.getAttributes().get( report.getKeyAttribute().getName() ) );
            }


            for ( Map<String, Object> e : resultMap )
            {
                System.out.println( e.get( report.getKeyAttribute().getQuery() ).toString() );
            }
             */
            List<TrackedEntity> deleteFromDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> sameAsDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> addToDhis2List = new ArrayList<TrackedEntity>();
            List<TrackedEntity> updateInDhis2List = new ArrayList<TrackedEntity>();

            //separate dhis2 information into corresponding lists
            List<String> enitiyMatchList = new ArrayList<String>();
            for ( int j = 0; j < entityList.size(); j++ )
            {
                //if the entity doesn't have a an id element its not an from openmrs so delete it
                if ( entityList.get( j ).getAttributes().get( report.getKeyAttribute().getName() ) == null )
                {
                    deleteFromDhis2List.add( entityList.get( j ) );
                    break;
                }
                else
                {
                    for ( int i = 0; i < resultMap.size(); i++ )
                    {
                        //check if the list has the same id as
                        if ( entityList.get( j ).getAttributes().get( report.getKeyAttribute().getName() ).equals(
                            resultMap.get( i ).get( report.getKeyAttribute().getQuery() ).toString() ) )
                        {
                            enitiyMatchList.add( entityList.get( j ).getAttributes().get(
                                report.getKeyAttribute().getName() ) );
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
                }
            }

            //if there was no match for the element in result map then it needs to be added to dhis2
            for ( int i = 0; i < resultMap.size(); i++ )
            {
                boolean match = false;
                for ( int j = 0; j < enitiyMatchList.size(); j++ )
                {
                    if ( resultMap.get( i ).get( report.getKeyAttribute().getQuery() ).toString().equals(
                        enitiyMatchList.get( j ) ) )
                    {
                        match = true;
                        break;
                    }
                }
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

            /*
            //go through entityList for all the elements with no element in enitiyMatchList and delete those
            //because they aren't in openmrs anymore
            for ( int i = 0; i < entityList.size(); i++ )
            {
                if ( enitiyMatchList[i] == false )
                {
                    deleteFromDhis2List.add( entityList.get( i ) );
                }
            }
             */
            //data output
            //todo note this code is superficial in its reporting of what information is being sent to dhsi2
            // these reports should be added to the below functions and should confirm if working
            TrackerCaptureResultSet tcrsDelete = new TrackerCaptureResultSet();
            System.out.println( "DELETE ELEMENTS " + deleteFromDhis2List.size() );
            for ( int i = 0; i < deleteFromDhis2List.size(); i++ )
            {
                TrackedEntity e = deleteFromDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
                TrackerCaptureTemplateMessage tctm = new TrackerCaptureTemplateMessage();
                tctm.setId( e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
                tctm.setDescription( "Delete" );
                tcrsDelete.addMessage( tctm );
            }
            aggregatedList.add( tcrsDelete );

            TrackerCaptureResultSet tcrsAdd = new TrackerCaptureResultSet();
            System.out.println( "ADD ELEMENTS " + addToDhis2List.size() );
            for ( int i = 0; i < addToDhis2List.size(); i++ )
            {
                TrackedEntity e = addToDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );

                TrackerCaptureTemplateMessage tctm = new TrackerCaptureTemplateMessage();
                tctm.setId( e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
                tctm.setDescription( "Add" );
                tcrsAdd.addMessage( tctm );
            }
            aggregatedList.add( tcrsAdd );

            System.out.println( "SAME ELEMENTS " + sameAsDhis2List.size() );
            for ( int i = 0; i < sameAsDhis2List.size(); i++ )
            {
                TrackedEntity e = sameAsDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
            }

            TrackerCaptureResultSet tcrsUpdate = new TrackerCaptureResultSet();
            System.out.println( "UPDATE ELEMENTS " + updateInDhis2List.size() );
            for ( int i = 0; i < updateInDhis2List.size(); i++ )
            {
                TrackedEntity e = updateInDhis2List.get( i );
                System.out.println( e.getUid() + " " + e.getAttributes().get( report.getKeyAttribute().getName() )
                    + " " + e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
                TrackerCaptureTemplateMessage tctm = new TrackerCaptureTemplateMessage();
                tctm.setId( e.getAttributes().get( report.getKeyAttribute().getQuery() ) );
                tctm.setDescription( "Update" );
                tcrsUpdate.addMessage( tctm );
            }
            aggregatedList.add( tcrsUpdate );

            //delete TrackedEntity elements that arent in openmrs
            for ( TrackedEntity entity : deleteFromDhis2List )
            {
                if ( entity.getUid() != null )
                {
                    dhis2Server.deleteTrackerCapture( entity );
                }
            }

            //create update data message to add new records that aren't on the dhis2 side
            sendTrackerCapture( addToDhis2List, destination, report, LocationUtility.getDefaultLocation() );

            //create update data messages to update records that have changed if required
            updateTrackerCapture( updateInDhis2List, destination, report, LocationUtility.getDefaultLocation() );

            //find all uids for TrackedEntitys added to dhis2
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
                List<Map<String, Object>> list = Context.getService( DHIS2TrackerCaptureService.class )
                    .evaluateTrackerCaptureEnrollment( t, LocationUtility.getDefaultLocation() );
                t.setQueryList( list );

                //for ( int i = 0; i < list.size(); i++ )
                //{
                //    System.out.println( list.get( i ).get( "id" ).toString() + " "
                //        + list.get( i ).get( "date" ).toString() );
                //}

            }

            //todo send program enrollment data
            System.out.println( "send enrollment data" );
            TrackerCaptureResultSet tcrsEnrollment = new TrackerCaptureResultSet();
            for ( TrackedEntity entity : newentityList )
            {
                for ( TrackerCaptureEnrollment enrollment : report.getTrackerCaptureEnrollmentList() )
                {

                    System.out.println( entity.getAttributes().get( report.getKeyAttribute().getName() )
                        + " "
                        + enrollment.checkQueryList( entity.getAttributes().get( report.getKeyAttribute().getName() ) )
                        + " "
                        + enrollment
                            .getQueryListDate( entity.getAttributes().get( report.getKeyAttribute().getName() ) ) );

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

                        TrackerCaptureTemplateMessage tctm = new TrackerCaptureTemplateMessage();
                        tctm.setId( enrollment.getName() + " "
                            + entity.getAttributes().get( report.getKeyAttribute().getName() ) );
                        tctm.setDescription( "Enrollment" );
                        tcrsAdd.addMessage( tctm );

                        dhis2Server.postEnrollment( message );
                    }
                }
            }
            aggregatedList.add( tcrsEnrollment );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return aggregatedList;
    }

    @Override
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

}
