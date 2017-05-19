package org.openmrs.module.dhisreport.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.AggregatedResultSet;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.DHIS2TrackerCaptureService;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureResultSet;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

/**
 * Created by ICCHANGE on 3/Apr/2017.
 */
@Controller
public class TrackerCaptureController
{

    protected final Log log = LogFactory.getLog( getClass() );

    @RequestMapping( value = "/module/dhisreport/setupTrackerCapture", method = RequestMethod.GET )
    public void setupTrackerCapture( ModelMap model, @RequestParam( value = "trackerCapture_id", required = false )
    Integer trackerCapture_id, HttpSession session )
    {

        DHIS2TrackerCaptureService service = Context.getService( DHIS2TrackerCaptureService.class );
        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "trackerCapture", service.getTrackerCaptureReport( trackerCapture_id ) );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );

        HttpDhis2Server server = setupServer( Context.getService( DHIS2ReportingService.class ) );
        if ( (server != null) & (server.isConfigured()) )
        {
            model.addAttribute( "dhis2Server", server );
        }
    }

    @RequestMapping( value = "/module/dhisreport/deleteTrackerCapture", method = RequestMethod.GET )
    public String deleteTrackerCapture( ModelMap model, @RequestParam( value = "trackerCapture_id", required = false )
    Integer trackerCapture_id, WebRequest webRequest )
    {
        DHIS2TrackerCaptureService service = Context.getService( DHIS2TrackerCaptureService.class );

        model.addAttribute( "user", Context.getAuthenticatedUser() );

        TrackerCaptureTemplate rd = service.getTrackerCaptureReport( trackerCapture_id );

        service.purgeTrackerCaptureReport( rd );
        webRequest.setAttribute( WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
            "dhisreport.deleteSuccess" ), WebRequest.SCOPE_SESSION );
        return "redirect:/module/dhisreport/listDhis2Reports.form";
    }

    @RequestMapping( value = "/module/dhisreport/executeTrackerCapture", method = RequestMethod.POST )
    public String executeTrackerCapture( ModelMap model, @RequestParam( value = "trackerCapture_id", required = true )
    Integer trackerCapture_id, @RequestParam( value = "location", required = false )
    String OU_Code, @RequestParam( value = "resultDestination", required = true )
    String destination, WebRequest webRequest, HttpServletRequest request )
        throws Exception
    {
        List<TrackerCaptureResultSet> aggregatedList = null;
        try
        {
            aggregatedList = Context.getService( DHIS2TrackerCaptureService.class ).postTrackerCapture(
                trackerCapture_id, destination );
        }
        catch ( ParseException pex )
        {
            return "redirect:" + logDateError( webRequest, pex );
        }
        catch ( LocationException e )
        {
            return "redirect:" + logLocationError( webRequest, request );
        }
        catch ( SendMetaDataException e )
        {
            e.printStackTrace();
        }
        catch ( SendReportException e )
        {
            e.printStackTrace();
        }

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "aggregatedList", aggregatedList );
        return null;
    }

    public String logDateError( WebRequest webRequest, ParseException pex )
    {
        log.error( "Cannot convert passed string to date... Please check dateFormat", pex );
        webRequest.setAttribute( WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
            "Date Parsing Error" ), WebRequest.SCOPE_SESSION );
        String referer = webRequest.getHeader( "Referer" );
        return referer;
    }

    public String logLocationError( WebRequest webRequest, HttpServletRequest request )
    {
        log.error( "Location attribute CODE not set" );
        request.getSession().setAttribute( "errorMessage", "Please set location attribute CODE to generate results." );
        String referer = webRequest.getHeader( "Referer" );
        return referer;
    }

    public String logSendMetaDataError( WebRequest webRequest, HttpServletRequest request )
    {
        log.error( "Meta Data Error" );
        request.getSession().setAttribute( "errorMessage", "Meta Data sending had an Error." );
        String referer = webRequest.getHeader( "Referer" );
        return referer;
    }

    public String logReportError( WebRequest webRequest, HttpServletRequest request )
    {
        log.error( "Report Error" );
        request.getSession().setAttribute( "errorMessage", "Report sending had an Error." );
        String referer = webRequest.getHeader( "Referer" );
        return referer;
    }

    public HttpDhis2Server setupServer( DHIS2ReportingService service )
    {

        String dhisurl = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2URL" );
        String dhisusername = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2UserName" );
        String dhispassword = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2Password" );

        HttpDhis2Server server = service.getDhis2Server();

        URL url = null;
        try
        {
            url = new URL( dhisurl );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }

        server.setUrl( url );
        server.setUsername( dhisusername );
        server.setPassword( dhispassword );

        return server;
    }

}
