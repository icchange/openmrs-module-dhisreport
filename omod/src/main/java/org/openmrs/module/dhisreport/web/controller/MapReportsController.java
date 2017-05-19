package org.openmrs.module.dhisreport.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.module.reporting.definition.DefinitionSummary;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.util.PeriodIndicatorReportUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MapReportsController
{

    protected final Log log = LogFactory.getLog( getClass() );

    @RequestMapping( value = "/module/dhisreport/mapReports", method = RequestMethod.GET )
    public void mapReports( ModelMap model, @RequestParam( value = "reportDefinition_id", required = true )
    Integer reportDefinition_id )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        List<DefinitionSummary> definitionSummaries = Context.getService( ReportDefinitionService.class )
            .getAllDefinitionSummaries( false );
        List<DefinitionSummary> periodicIndicatorReportDefinitionSummaries = new ArrayList<DefinitionSummary>();
        for ( DefinitionSummary ds : definitionSummaries )
        {
            if ( ds.getType().equals( "org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition" ) )
                periodicIndicatorReportDefinitionSummaries.add( ds );
        }
        ReportDefinition rd = service.getReportDefinition( reportDefinition_id );
        SerializedObject so = Context.getService( SerializedDefinitionService.class ).getSerializedDefinitionByUuid(
            rd.getReportingReportId() );

        org.openmrs.module.reporting.report.definition.ReportDefinition rrd = Context.getService(
            ReportDefinitionService.class ).getDefinitionByUuid( rd.getReportingReportId() );

        model.put( "definitionSummaries", periodicIndicatorReportDefinitionSummaries );
        model.addAttribute( "user", Context.getAuthenticatedUser() );
        model.addAttribute( "reportDefinition", rd );
        if ( rd.getReportingReportId() != null && rrd instanceof PeriodIndicatorReportDefinition )
        {
            model.addAttribute( "correspondingReportDefinition", so );
            Parameterizable parameterizable = ParameterizableUtil.getParameterizable( rd.getReportingReportId(),
                PeriodIndicatorReportDefinition.class );
            model.addAttribute( "reportingReportDefinitionReport", (PeriodIndicatorReportDefinition) rrd );
        }

    }

    @RequestMapping( value = "/module/dhisreport/mapReports", method = RequestMethod.POST )
    public void mapReportsSubmit( ModelMap model, @RequestParam( value = "reportDefinition_id", required = true )
    Integer reportDefinition_id, @RequestParam( value = "reportmoduledefinitions", required = true )
    String reportmoduleDefinitionID )
    {
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        ReportDefinition reportDefinition = service.getReportDefinition( reportDefinition_id );
        reportDefinition.setReportingReportId( reportmoduleDefinitionID );
        service.saveReportDefinition( reportDefinition );
    }

}
