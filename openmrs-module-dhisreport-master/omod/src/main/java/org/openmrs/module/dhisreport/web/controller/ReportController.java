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
package org.openmrs.module.dhisreport.web.controller;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.AggregatedResultSet;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.adx.AdxType;
import org.openmrs.module.dhisreport.api.adx.DataValueType;
import org.openmrs.module.dhisreport.api.adx.GroupType;
import org.openmrs.module.dhisreport.api.dhis.Dhis2Server;
import org.openmrs.module.dhisreport.api.dhis.HttpDhis2Server;
import org.openmrs.module.dhisreport.api.dxf2.DataValue;
import org.openmrs.module.dhisreport.api.dxf2.DataValueSet;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.model.*;
import org.openmrs.module.dhisreport.api.utils.DailyPeriod;
import org.openmrs.module.dhisreport.api.utils.MonthlyPeriod;
import org.openmrs.module.dhisreport.api.utils.Period;
import org.openmrs.module.dhisreport.api.utils.WeeklyPeriod;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.util.LocationUtility;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The main controller.
 */
@Controller
public class ReportController
{

    protected final Log log = LogFactory.getLog( getClass() );

    @RequestMapping( value = "/module/dhisreport/manage", method = RequestMethod.GET )
    public void manage( ModelMap model )
    {
        model.addAttribute( "user", Context.getAuthenticatedUser() );
    }

    @RequestMapping( value = "/module/dhisreport/listDhis2Reports", method = RequestMethod.GET )
    public void listReports( ModelMap model )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinitions", service.getAllReportDefinitions() );
        model.addAttribute( "TrackerCaptureTemplates", service.getAllTrackerCaptureTemplates() );
    }

    @RequestMapping( value = "/module/dhisreport/setupReport", method = RequestMethod.GET )
    public void setupReport( ModelMap model, @RequestParam( value = "reportDefinition_id", required = false )
    Integer reportDefinition_id, HttpSession session )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );
        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinition", service.getReportDefinition( reportDefinition_id ) );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );

        HttpDhis2Server server = setupServer( service );
        if ( (server != null) & (server.isConfigured()) )
        {
            model.addAttribute( "dhis2Server", server );
        }
    }

    @RequestMapping( value = "/module/dhisreport/setupTrackerCapture", method = RequestMethod.GET )
    public void setupTrackerCapture( ModelMap model, @RequestParam( value = "trackerCapture_id", required = false )
    Integer trackerCapture_id, HttpSession session )
    {

        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );
        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "trackerCapture", service.getTrackerCaptureReport( trackerCapture_id ) );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );

        HttpDhis2Server server = setupServer( service );
        if ( (server != null) & (server.isConfigured()) )
        {
            model.addAttribute( "dhis2Server", server );
        }
    }

    @RequestMapping( value = "/module/dhisreport/setupBulkReport", method = RequestMethod.GET )
    public void setupBulkReport( ModelMap model, @RequestParam( value = "type", required = false )
    String reportType, HttpSession session )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );
        String errormsg = (String) session.getAttribute( "errorMessage" );
        session.removeAttribute( "errorMessage" );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinitions", service.getReportDefinitionByPeriodType( reportType ) );
        model.addAttribute( "reportType", reportType );
        model.addAttribute( "locations", Context.getLocationService().getAllLocations() );
        model.addAttribute( "errorMessage", errormsg );

        HttpDhis2Server server = setupServer( service );
        if ( (server != null) & (server.isConfigured()) )
        {
            model.addAttribute( "dhis2Server", server );
        }
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

    @RequestMapping( value = "/module/dhisreport/executeReport", method = RequestMethod.POST )
    public String executeReport( ModelMap model, @RequestParam( value = "reportDefinition_id", required = true )
    Integer reportDefinition_id, @RequestParam( value = "location", required = false )
    String OU_Code, @RequestParam( value = "resultDestination", required = true )
    String destination, @RequestParam( value = "date", required = true )
    String dateStr, @RequestParam( value = "frequency", required = true )
    String freq, @RequestParam( value = "consecutive", required = true )
    Integer consecutive, @RequestParam( value = "mappingType", required = true )
    String mappingType, WebRequest webRequest, HttpServletRequest request )
        throws Exception
    {
        List<AggregatedResultSet> aggregatedList = null;
        try
        {
            aggregatedList = Context.getService( DHIS2ReportingService.class ).postReportDefinition(
                reportDefinition_id, destination, freq, dateStr, consecutive, mappingType );
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

        //sort aggregatedList values
        for ( AggregatedResultSet agrs : aggregatedList )
        {
            Collections.sort( agrs.getDataValueSet().getDataValues(), new Comparator<DataValue>()
            {
                public int compare( DataValue d1, DataValue d2 )
                {
                    int order = d1.getDataElement().compareTo( d2.getDataElement() );
                    if ( order == 0 )
                    {
                        return d1.getCategoryOptionComboName().compareTo( d2.getCategoryOptionComboName() );
                    }
                    else
                    {
                        return order;
                    }
                }
            } );
        }

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "aggregatedList", aggregatedList );
        return null;
    }

    @RequestMapping( value = "/module/dhisreport/executeBulkReport", method = RequestMethod.POST )
    public String executeBulkReport( ModelMap model, @RequestParam( value = "reportType", required = true )
    String reportType, @RequestParam( value = "location", required = false )
    String OU_Code, @RequestParam( value = "resultDestination", required = true )
    String destination, @RequestParam( value = "date", required = true )
    String dateStr, @RequestParam( value = "frequency", required = true )
    String freq, @RequestParam( value = "consecutive", required = true )
    Integer consecutive, @RequestParam( value = "mappingType", required = true )
    String mappingType, WebRequest webRequest, HttpServletRequest request )
        throws Exception
    {
        List<AggregatedResultSet> aggregatedList = null;
        try
        {
            aggregatedList = Context.getService( DHIS2ReportingService.class ).postBulkReportDefinition( reportType,
                destination, freq, dateStr, consecutive, mappingType );
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

        /*
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        //get a period from the date string
        Period period = null;
        try
        {
            period = getPeriodFromDateString( freq, dateStr );
        }
        catch ( ParseException pex )
        {
            return "redirect:" + logDateError( webRequest, pex );
        }

        //get period list of consecutive dates after current period up to consecutive value limit
        List<Period> periodList = generatePeriodList( period, consecutive );

        // Get Location by OrgUnit Code
        List<Location> locationListFinal = null;
        try
        {
            locationListFinal = getValidLocationList();
        }
        catch ( Exception e )
        {
            return "redirect:" + logLocationError( webRequest, request );
        }

        List<AggregatedResultSet> aggregatedList = new ArrayList<AggregatedResultSet>();
        //run reports and metadata
        for ( Location l : locationListFinal )
        {
            for ( Period periodValue : periodList )
            {
                List<ReportDefinition> definitionList = service.getReportDefinitionByPeriodType( reportType );
                for ( ReportDefinition r : definitionList )
                {
                    //send meta data
                    aggregatedList.add( sendMetadata( destination, r, l ) );
                    //send report
                    aggregatedList.add( sendReport( mappingType, destination, r, periodValue, l ) );
                }
            }
        }
         */
        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "aggregatedList", aggregatedList );
        return null;
    }

    @RequestMapping( value = "/module/dhisreport/executeTrackerCapture", method = RequestMethod.POST )
    public String executeTrackerCapture( ModelMap model, @RequestParam( value = "trackerCapture_id", required = true )
    Integer trackerCapture_id, @RequestParam( value = "location", required = false )
    String OU_Code, @RequestParam( value = "resultDestination", required = true )
    String destination, WebRequest webRequest, HttpServletRequest request )
        throws Exception
    {
        List<AggregatedResultSet> aggregatedList = null;
        try
        {
            aggregatedList = Context.getService( DHIS2ReportingService.class ).postTrackerCapture( trackerCapture_id,
                destination );
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

        /*
        //sort aggregatedList values
        for ( AggregatedResultSet agrs : aggregatedList )
        {
            Collections.sort( agrs.getDataValueSet().getDataValues(), new Comparator<DataValue>()
            {
                public int compare( DataValue d1, DataValue d2 )
                {
                    int order = d1.getDataElement().compareTo( d2.getDataElement() );
                    if ( order == 0 )
                    {
                        return d1.getCategoryOptionComboName().compareTo( d2.getCategoryOptionComboName() );
                    }
                    else
                    {
                        return order;
                    }
                }
            } );
        }
         */
        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "aggregatedList", aggregatedList );
        return null;
    }

    // @RequestMapping(value = "/module/dhisreport/executeReport", method =
    // RequestMethod.POST)
    // public void saveReport( ModelMap model,
    // @RequestParam(value = "reportDefinition_id", required = true) Integer
    // reportDefinition_id,
    // @RequestParam(value = "location", required = true) Integer location_id,
    // @RequestParam(value = "resultDestination", required = true) String
    // destination,
    // @RequestParam(value = "date", required = true) String dateStr,
    // HttpServletResponse response )
    // throws ParseException, IOException, JAXBException,
    // DHIS2ReportingException
    // {
    // DHIS2ReportingService service = Context.getService(
    // DHIS2ReportingService.class );
    //
    // MonthlyPeriod period = new MonthlyPeriod( new SimpleDateFormat(
    // "yyyy-MM-dd" ).parse( dateStr ) );
    // Location location = Context.getLocationService().getLocation( location_id
    // );
    //
    // DataValueSet dvs = service.evaluateReportDefinition(
    // service.getReportDefinition( reportDefinition_id ), period, location );
    //
    // response.setContentType( "application/xml" );
    // response.setCharacterEncoding( "UTF-8" );
    // response.addHeader( "Content-Disposition",
    // "attachment; filename=report.xml" );
    //
    // dvs.marshall( response.getOutputStream());
    // }

    @RequestMapping( value = "/module/dhisreport/syncReports", method = RequestMethod.GET )
    public void syncReports( ModelMap model )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinitions", service.getAllReportDefinitions() );
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
}
