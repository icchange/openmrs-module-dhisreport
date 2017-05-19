package org.openmrs.module.dhisreport.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ICCHANGE on 30/Jan/2017.
 */
@Controller
public class ScheduleControler
{

    protected final Log log = LogFactory.getLog( getClass() );

    @RequestMapping( value = "/module/dhisreport/scheduler", method = RequestMethod.GET )
    public void schedulerPage( ModelMap model, @RequestParam( value = "reportDefinition_id", required = false )
    Integer reportDefinition_id, HttpSession session )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );
        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinition", service.getReportDefinition( reportDefinition_id ) );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );
    }

    @RequestMapping( value = "/module/dhisreport/scheduler", method = RequestMethod.POST )
    public void schedulerPagePost( ModelMap model, @RequestParam( value = "reportDefinition_id", required = false )
    Integer reportDefinition_id, @RequestParam( value = "schedule", required = false )
    String schedule, HttpSession session )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );
        ReportDefinition report = Context.getService( DHIS2ReportingService.class ).getReportDefinition(
            reportDefinition_id );

        if ( schedule.equals( "true" ) )
        {
            report.setScheduled( true );
        }
        else
        {
            report.setScheduled( false );
        }
        Context.getService( DHIS2ReportingService.class ).saveReportDefinition( report );

        System.out.println( "this works " + report.getScheduled() );

        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinition", report );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );
    }

}
