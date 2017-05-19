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
package org.openmrs.module.dhisreport.api.dhis;

import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.adx.AdxType;
import org.openmrs.module.dhisreport.api.dxf2.OrganizationUnit;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.module.dhisreport.api.dxf2.DataValueSet;
import org.openmrs.module.dhisreport.api.trackercapture.TrackedEntity;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplateMessage;

import java.util.List;

/**
 * 
 * @author bobj
 */
public interface Dhis2Server
{

    /**
     * low level method to access dhis2 resources directly
     * 
     * @param path
     * @return
     * @throws Dhis2Exception
     */
    // public InputStream fetchDhisResource( String path ) throws
    // Dhis2Exception;

    boolean isConfigured();

    ImportSummaries postMetaData( String metaData )
        throws DHIS2ReportingException;

    List<TrackerCaptureTemplateMessage> postTrackerCapture( String trackerCapture )
        throws Dhis2Exception;

    ImportSummaries postEnrollment( String enrollmentMessage );

    List<String[]> getDataElements( List<Object[]> elements, String prefix );

    List<TrackedEntity> getTrackedEntities( String ouCode );

    void deleteTrackerCapture( TrackedEntity entity );

    void updateTrackerCapture( String entityMessage, TrackedEntity entity );

    List<OrganizationUnit> getOrgUnits();

    String getJson( String pageId );

    String getOrgUnitCode()
        throws Exception;

    ReportDefinition fetchReportTemplates()
        throws DHIS2ReportingException;

    ImportSummary postReport( DataValueSet report )
        throws DHIS2ReportingException;

    ImportSummaries postAdxReport( AdxType report )
        throws DHIS2ReportingException;

    ImportSummaries postDxf2Report( AdxType report )
        throws DHIS2ReportingException;
}
