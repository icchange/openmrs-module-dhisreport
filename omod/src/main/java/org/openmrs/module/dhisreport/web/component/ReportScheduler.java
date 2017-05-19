package org.openmrs.module.dhisreport.web.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.exceptions.LocationException;
import org.openmrs.module.dhisreport.api.exceptions.SendMetaDataException;
import org.openmrs.module.dhisreport.api.exceptions.SendReportException;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ICCHANGE on 2/Feb/2017.
 */
public class ReportScheduler
    extends AbstractTask
{

    private static final Log log = LogFactory.getLog( ReportScheduler.class );

    public void runAutomatedReports()
    {
        System.out.println( "running automated reports" );
        DHIS2ReportingService service = Context.getService( DHIS2ReportingService.class );

        //run monthly scheduled reports
        if ( Calendar.getInstance().get( Calendar.DAY_OF_MONTH ) == 1 )
        {
            List<ReportDefinition> reportList = service.getReportDefinitionByPeriodType( "Monthly" );
            for ( int i = 0; i < reportList.size(); i++ )
            {
                if ( reportList.get( i ).getScheduled() )
                {
                    Calendar cal = Calendar.getInstance();
                    String dateStr = cal.get( Calendar.YEAR ) + "-"
                        + (new SimpleDateFormat( "MMM" )).format( cal.getTime() );
                    try
                    {
                        service
                            .postReportDefinition( reportList.get( i ).getId(), "post", "monthly", dateStr, 1, "SQL" );
                    }
                    catch ( ParseException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( LocationException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendMetaDataException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendReportException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        //run weekly scheduled reports
        if ( Calendar.getInstance().get( Calendar.DAY_OF_WEEK ) == 1 )
        {
            List<ReportDefinition> reportList = service.getReportDefinitionByPeriodType( "Weekly" );
            for ( int i = 0; i < reportList.size(); i++ )
            {
                if ( reportList.get( i ).getScheduled() )
                {
                    Calendar cal = Calendar.getInstance();
                    String dateStr = cal.get( Calendar.YEAR ) + "-W" + cal.get( Calendar.WEEK_OF_YEAR );
                    try
                    {
                        service.postReportDefinition( reportList.get( i ).getId(), "post", "Weekly", dateStr, 1, "SQL" );
                    }
                    catch ( ParseException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( LocationException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendMetaDataException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendReportException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        //run daily scheduled reports
        {
            List<ReportDefinition> reportList = service.getReportDefinitionByPeriodType( "Daily" );
            for ( int i = 0; i < reportList.size(); i++ )
            {
                if ( reportList.get( i ).getScheduled() )
                {
                    Calendar cal = Calendar.getInstance();
                    String dateStr = cal.get( Calendar.MONTH ) + "/" + cal.get( Calendar.DAY_OF_MONTH ) + "/"
                        + cal.get( Calendar.YEAR );
                    try
                    {
                        service.postReportDefinition( reportList.get( i ).getId(), "post", "Daily", dateStr, 1, "SQL" );
                    }
                    catch ( ParseException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( LocationException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendMetaDataException e )
                    {
                        e.printStackTrace();
                    }
                    catch ( SendReportException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println( "finished automated reports" );
    }

    @Override
    public void execute()
    {
        if ( !isExecuting )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "Starting Auto Close Visits Task..." );
            }

            startExecuting();
            try
            {
                runAutomatedReports();
            }
            catch ( Exception e )
            {
                log.error( "Error while auto closing visits:", e );
            }
            finally
            {
                stopExecuting();
            }
        }
    }
}
