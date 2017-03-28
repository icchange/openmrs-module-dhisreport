package org.openmrs.module.dhisreport.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.DHIS2TableService;
import org.openmrs.module.dhisreport.api.dhis.Dhis2Exception;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummary;
import org.openmrs.module.dhisreport.api.table.TableDhis2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
@Controller
public class TableController
{

    protected final Log log = LogFactory.getLog( getClass() );

    public static final String TABLE_PATH = "/api/reportTables?paging=false";

    @RequestMapping( value = "/module/dhisreport/tableAdmin", method = RequestMethod.GET )
    public void tableadmin( ModelMap model )
    {
        List<TableDhis2> tableList;
        try
        {
            tableList = getTablesDhis2();
            model.addAttribute( "tableList", tableList );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @RequestMapping( value = "/module/dhisreport/tablePermissions", method = RequestMethod.GET )
    public void tablepermissions( ModelMap model, @RequestParam( value = "id", required = false )
    String tableId )
    {
        TableDhis2 table = Context.getService( DHIS2TableService.class ).getTableByUid( tableId );
        List<Role> roleList = Context.getUserService().getAllRoles();
        List<Role> roleTable = new ArrayList<Role>( table.getRoles() );
        for ( int i = roleList.size() - 1; i >= 0; i-- )
        {
            boolean saved = false;
            for ( int j = 0; j < roleTable.size(); j++ )
            {
                if ( roleList.get( i ).getName().equals( roleTable.get( j ).getName() ) )
                {
                    saved = true;
                }
            }
            if ( saved == true )
            {
                roleList.remove( i );
            }
        }

        model.addAttribute( "table", table );
        model.addAttribute( "roleList", roleList );
        model.addAttribute( "roleTable", roleTable );
    }

    @RequestMapping( value = "/module/dhisreport/tablePermissions", method = RequestMethod.POST )
    public void tablepermissionssubmit( ModelMap model, @RequestParam( value = "id", required = false )
    String tableId, @RequestParam( value = "deSelected", required = false )
    String[] deSelected )
    {
        List<Role> roleTable = Context.getUserService().getAllRoles();
        List<Role> roleList = new ArrayList<Role>();
        if ( deSelected != null )
        {
            for ( int i = roleTable.size() - 1; i >= 0; i-- )
            {
                boolean saved = false;
                for ( int j = 0; j < deSelected.length; j++ )
                {
                    if ( roleTable.get( i ).getName().equals( deSelected[j] ) )
                    {
                        saved = true;
                    }
                }
                if ( saved != true )
                {
                    Role r = roleTable.remove( i );
                    roleList.add( r );
                }
            }
        }
        TableDhis2 table = Context.getService( DHIS2TableService.class ).getTableByUid( tableId );
        table.getRoles().clear();
        table.getRoles().addAll( roleTable );
        Context.getService( DHIS2TableService.class ).saveTable( table );
        model.addAttribute( "table", table );
        model.addAttribute( "roleList", roleList );
        model.addAttribute( "roleTable", roleTable );
    }

    @RequestMapping( value = "/module/dhisreport/tableView", method = RequestMethod.GET )
    public void tableview( ModelMap model )
    {
        User currentUser = Context.getAuthenticatedUser();

        if ( currentUser.isSuperUser() )
        {
            try
            {
                List<TableDhis2> tableList = getTablesDhis2();
                model.addAttribute( "tableList", tableList );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                //todo check if table has role associated with it before display
                List<TableDhis2> tableList = getTablesDhis2();
                List<Role> roleList = new ArrayList<Role>( currentUser.getAllRoles() );

                for ( int i = tableList.size() - 1; i >= 0; i-- )
                {

                    boolean saved = false;
                    for ( int m = 0; m < roleList.size(); m++ )
                    {
                        List<Role> roleTable = new ArrayList<Role>( tableList.get( i ).getRoles() );
                        for ( int n = 0; n < roleTable.size(); n++ )
                        {
                            if ( roleList.get( m ).getName().equals( roleTable.get( n ).getName() ) )
                            {
                                saved = true;
                            }
                        }
                    }
                    if ( !saved )
                    {
                        tableList.remove( i );
                    }
                }
                model.addAttribute( "tableList", tableList );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping( value = "/module/dhisreport/tableEmbed", method = RequestMethod.GET )
    public void tableembed( ModelMap model, @RequestParam( value = "id", required = false )
    String tableId )
    {
        TableDhis2 table = Context.getService( DHIS2TableService.class ).getTableByUid( tableId );
        model.addAttribute( "table", table );
    }

    public List<TableDhis2> getTablesDhis2()
        throws DHIS2ReportingException, AuthenticationException, IOException
    {
        DHIS2TableService tableServ = Context.getService( DHIS2TableService.class );
        log.debug( "Posting A meta data report" );
        ImportSummaries summaries = null;

        List<TableDhis2> tableList = new ArrayList<TableDhis2>();
        String dhisurl = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2URL" );
        URL url = null;
        try
        {
            url = new URL( dhisurl );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        String dhisusername = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2UserName" );
        String dhispassword = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2Password" );

        String host = url.getHost();
        int port = url.getPort();

        HttpHost targetHost = new HttpHost( host, port, url.getProtocol() );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpContext localcontext = new BasicHttpContext();

        try
        {
            HttpGet httpGet = new HttpGet( dhisurl + TABLE_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpGet, localcontext );
            httpGet.addHeader( "Authorization", bs.getValue() );
            httpGet.addHeader( "Content-Type", "application/xml" );
            httpGet.addHeader( "Accept", "application/xml" );

            HttpResponse response = httpclient.execute( targetHost, httpGet, localcontext );
            HttpEntity entity = response.getEntity();

            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

            String line = "";
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                //System.out.println( line );
                output += line;
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream( new StringReader( output ) );

            Document doc = builder.parse( src );
            doc.getDocumentElement().normalize();
            Element reportTables = (Element) (doc.getElementsByTagName( "reportTables" ).item( 0 ));

            NodeList reportTableList = reportTables.getElementsByTagName( "reportTable" );
            for ( int i = 0; i < reportTableList.getLength(); i++ )
            {
                Element reportTable = (Element) reportTableList.item( i );
                String tableName = reportTable.getAttribute( "name" );
                String tableId = reportTable.getAttribute( "id" );
                String tableHref = reportTable.getAttribute( "href" );

                TableDhis2 table = new TableDhis2( tableName, tableId, tableHref );
                TableDhis2 test = tableServ.getTableByUid( tableId );
                if ( test == null )
                {
                    tableServ.saveTable( table );
                }
                tableList.add( table );
            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return tableList;
    }

    public void getTableDhis2()
    {
        DHIS2TableService tableServ = Context.getService( DHIS2TableService.class );
        log.debug( "Posting A meta data report" );
        ImportSummaries summaries = null;

        List<TableDhis2> tableList = new ArrayList<TableDhis2>();
        String dhisurl = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2URL" );
        URL url = null;
        try
        {
            url = new URL( dhisurl );
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
        }
        String dhisusername = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2UserName" );
        String dhispassword = Context.getAdministrationService().getGlobalProperty( "dhisreport.dhis2Password" );

        String host = url.getHost();
        int port = url.getPort();

        HttpHost targetHost = new HttpHost( host, port, url.getProtocol() );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpContext localcontext = new BasicHttpContext();

        try
        {
            HttpGet httpGet = new HttpGet( dhisurl + TABLE_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpGet, localcontext );
            httpGet.addHeader( "Authorization", bs.getValue() );
            httpGet.addHeader( "Content-Type", "application/xml" );
            httpGet.addHeader( "Accept", "application/xml" );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

}
