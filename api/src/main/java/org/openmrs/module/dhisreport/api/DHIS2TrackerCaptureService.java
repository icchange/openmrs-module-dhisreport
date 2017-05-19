package org.openmrs.module.dhisreport.api;

import org.openmrs.Location;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.trackercapture.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ICCHANGE on 31/Mar/2017.
 */
@Transactional
public interface DHIS2TrackerCaptureService
    extends OpenmrsService
{
    HttpDhis2Server getDhis2Server();

    void setDhis2Server( HttpDhis2Server dhis2Server );

    TrackerCaptureEnrollment getTrackerCaptureEnrollment( Integer id );

    TrackerCaptureTemplate saveTrackerCaptureReport( TrackerCaptureTemplate tct );

    TrackerCaptureTemplate getTrackerCaptureReport( Integer id );

    void purgeTrackerCaptureReport( TrackerCaptureTemplate tct );

    TrackerCaptureAttribute saveTrackerCaptureAttribute( TrackerCaptureAttribute dq );

    TrackerCaptureAttribute getTrackerCaptureAttribute( Integer id );

    TrackerCaptureEnrollment saveTrackerCaptureEnrollment( TrackerCaptureEnrollment dq );

    Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates();

    List<TrackedEntity> getTrackedEntities( String ouCode );

    List<Map<String, Object>> evaluateTrackerCaptureQuery( String query );

    List<Map<String, Object>> evaluateTrackerCaptureEnrollment( TrackerCaptureEnrollment e, Location location )
        throws DHIS2ReportingException;

    List<TrackerCaptureTemplateMessage> postTrackerCapture( String trackerCapture )
        throws DHIS2ReportingException;

    List<TrackerCaptureResultSet> postTrackerCapture( int trackerCapture_id, String destination )
        throws ParseException, LocationException, SendMetaDataException, SendReportException;

    void unMarshallandSaveTrackerCaptureTemplate( InputStream is )
        throws Exception;
}
