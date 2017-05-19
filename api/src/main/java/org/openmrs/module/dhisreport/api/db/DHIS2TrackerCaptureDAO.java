package org.openmrs.module.dhisreport.api.db;

import org.openmrs.Location;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureAttribute;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ICCHANGE on 31/Mar/2017.
 */
public interface DHIS2TrackerCaptureDAO
{
    TrackerCaptureAttribute getTrackerCaptureAttribute( Integer id );

    TrackerCaptureAttribute saveTrackerCaptureAttribute( TrackerCaptureAttribute dq );

    TrackerCaptureEnrollment getTrackerCaptureEnrollment( Integer id );

    TrackerCaptureEnrollment saveTrackerCaptureEnrollment( TrackerCaptureEnrollment dq );

    TrackerCaptureTemplate getTrackerCaptureReport( Integer id );

    TrackerCaptureTemplate saveTrackerCaptureReport( TrackerCaptureTemplate tct );

    void deleteTrackerCaptureReport( TrackerCaptureTemplate tct );

    Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates();

    List<Map<String, Object>> evaluateTrackerCaptureQuery( String querystr )
        throws DHIS2ReportingException;

    List<Map<String, Object>> evaluateTrackerCaptureEnrollment( String querystr, Location location );
}
