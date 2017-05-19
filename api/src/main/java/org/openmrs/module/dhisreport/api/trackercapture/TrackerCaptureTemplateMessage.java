package org.openmrs.module.dhisreport.api.trackercapture;

import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ICCHANGE on 3/Apr/2017.
 */
public class TrackerCaptureTemplateMessage
{

    String id;

    String aditionalInformation;

    String description;

    String dhis2message;

    private ImportStatus summary;

    private List<ImportConflict> conflicts;

    int imported;

    int updated;

    int ignored;

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getDhis2message()
    {
        return dhis2message;
    }

    public void setDhis2message( String dhis2message )
    {
        this.dhis2message = dhis2message;
    }

    public void setConflicts( List<ImportConflict> list )
    {
        this.conflicts = list;
    }

    public List<ImportConflict> getConflicts()
    {
        return conflicts;
    }

    public void setSummary( ImportStatus summary )
    {
        this.summary = summary;
    }

    public ImportStatus getSummary()
    {
        return summary;
    }

    public static List<TrackerCaptureTemplateMessage> unMarshalJSON( String messages )
    {
        //todo create json unmarshiler

        List<TrackerCaptureTemplateMessage> messagelist = new ArrayList<TrackerCaptureTemplateMessage>();

        try
        {
            System.out.println( "message" );
            System.out.println( messages );
            JSONParser parser = new JSONParser();
            Object obj = parser.parse( messages );
            JSONObject jsonObject = (JSONObject) obj;

            Long imported = (Long) jsonObject.get( "imported" );
            Long updated = (Long) jsonObject.get( "updated" );
            Long ignored = (Long) jsonObject.get( "ignored" );

            JSONArray importsummaries = (JSONArray) jsonObject.get( "importSummaries" );
            for ( int i = 0; i < importsummaries.size(); i++ )
            {
                TrackerCaptureTemplateMessage tctm = new TrackerCaptureTemplateMessage();
                importsummaries.get( i );

                if ( ((JSONObject) importsummaries.get( i )).get( "status" ).equals( "SUCCESS" ) )
                {
                    tctm.setSummary( ImportStatus.SUCCESS );
                }
                else
                {
                    tctm.setSummary( ImportStatus.ERROR );
                    JSONArray conflicts = (JSONArray) ((JSONObject) importsummaries.get( i )).get( "conflicts" );
                    List<ImportConflict> conflictList = new ArrayList<ImportConflict>();
                    for ( int j = 0; j < conflicts.size(); j++ )
                    {
                        String conflictObject = (String) ((JSONObject) conflicts.get( j )).get( "object" );
                        String conflictValue = (String) ((JSONObject) conflicts.get( j )).get( "value" );
                        ImportConflict c = new ImportConflict();
                        c.setObject( conflictObject );
                        c.setValue( conflictValue );
                        conflictList.add( c );
                    }
                    tctm.setConflicts( conflictList );
                    messagelist.add( tctm );
                }
            }
        }
        catch ( ParseException e )
        {
            e.printStackTrace();
        }

        return messagelist;
    }

    public static List<TrackerCaptureTemplateMessage> unmarshalXML( String messages )
    {
        return null;
    }
}
