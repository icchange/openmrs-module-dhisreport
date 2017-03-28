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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.hisp.dhis.dxf2.Dxf2Exception;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.adx.AdxType;
import org.openmrs.module.dhisreport.api.dxf2.DataValueSet;
import org.openmrs.module.dhisreport.api.dxf2.OrganizationUnit;
import org.openmrs.module.dhisreport.api.importsummary.ImportSummaries;
import org.openmrs.module.dhisreport.api.model.ReportDefinition;
import org.openmrs.module.dhisreport.api.trackercapture.TrackedEntity;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.util.LocationUtility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author bobj
 */
public class HttpDhis2Server
    implements Dhis2Server
{

    private static Log log = LogFactory.getLog( HttpDhis2Server.class );

    public static final String REPORTS_METADATA_PATH = "/api/forms.xml";

    public static final String DATAVALUESET_PATH = "/api/dataValueSets?orgUnitIdScheme=CODE";

    public static final String TRACKERCAPTURE_PATH = "/api/trackedEntityInstances?orgUnitIdScheme=CODE";

    public static final String ORGUNIT_PATH = "/api/organisationUnits?paging=false";

    public static final String METADATA_PATH = "/api/metadata";

    public static final String DATAELEMENT_PATH = "/api/dataElements?paging=false";

    public static final String TRACKEDENTITY_PATH = "/api/trackedEntityInstances";

    // /api/trackedEntityInstances/<tracked-entity-instance-id> via PUT to update
    // /api/trackedEntityInstances/<tracked-entity-instance-id> via DELETE to delete

    public static final String ENROLLMENT_PATH = "/api/enrollments";

    // /api/enrollments/<enrollment-id>/cancelled via PUT to cancel
    // /api/enrollments/<enrollment-id>/completed via PUT to complete
    // /api/enrollments/<enrollment-id> via DELETE to delete

    private URL url;

    private String username;

    private String password;

    private String standard;

    public URL getUrl()
    {
        return url;
    }

    public void setUrl( URL url )
    {
        this.url = url;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getStandard()
    {
        return this.standard;
    }

    public void setStandard( String standard )
    {
        this.standard = standard;

    }

    public HttpDhis2Server()
    {
    }

    @Override
    public boolean isConfigured()
    {
        if ( username == null | password == null | url == null )
        {
            return false;
        }
        if ( username.isEmpty() | password.isEmpty() | url.getHost().isEmpty() )
        {
            return false;
        }

        return true;
    }

    @Override
    public ImportSummary postReport( DataValueSet report )
        throws DHIS2ReportingException
    {
        log.debug( "Posting datavalueset report" );
        ImportSummary summary = null;

        StringWriter xmlReport = new StringWriter();
        try
        {
            JAXBContext jaxbDataValueSetContext = JAXBContext.newInstance( DataValueSet.class );

            Marshaller dataValueSetMarshaller = jaxbDataValueSetContext.createMarshaller();
            // output pretty printed
            dataValueSetMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            dataValueSetMarshaller.marshal( report, xmlReport );
        }
        catch ( JAXBException ex )
        {
            throw new Dxf2Exception( "Problem marshalling dataValueSet", ex );
        }

        //System.out.print( "URL-" + url );

        String host = url.getHost();
        int port = url.getPort();

        //System.out.print( "URL-" + url + ":host-" + host + ":port-" );
        // System.out.println( port );

        HttpHost targetHost = new HttpHost( host, port, url.getProtocol() );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpContext localcontext = new BasicHttpContext();

        try
        {
            HttpPost httpPost = new HttpPost( url.getPath() + DATAVALUESET_PATH );
            Credentials creds = new UsernamePasswordCredentials( username, password );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/xml" );
            httpPost.addHeader( "Accept", "application/xml" );

            String reportString = xmlReport.toString();

            httpPost.setEntity( new StringEntity( reportString ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );
            HttpEntity entity = response.getEntity();

            if ( response.getStatusLine().getStatusCode() != 200 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                JAXBContext jaxbImportSummaryContext = JAXBContext.newInstance( ImportSummary.class );
                Unmarshaller importSummaryUnMarshaller = jaxbImportSummaryContext.createUnmarshaller();
                summary = (ImportSummary) importSummaryUnMarshaller.unmarshal( entity.getContent() );
            }
            else
            {
                summary = new ImportSummary();
                summary.setStatus( ImportStatus.ERROR );
            }
            // EntityUtils.consume( entity );

            // TODO: fix these catches ...
        }
        catch ( JAXBException ex )
        {
            throw new Dhis2Exception( this, "Problem unmarshalling ImportSummary", ex );
        }
        catch ( AuthenticationException ex )
        {
            throw new Dhis2Exception( this, "Problem authenticating to DHIS2 server", ex );
        }
        catch ( IOException ex )
        {
            throw new Dhis2Exception( this, "Problem accessing DHIS2 server", ex );
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
        return summary;
    }

    public String outputISteam( InputStream entity )
    {
        String inputLine;
        String outputLine = "";
        BufferedReader br = new BufferedReader( new InputStreamReader( entity ) );
        try
        {
            while ( (inputLine = br.readLine()) != null )
            {
                outputLine += inputLine;
            }
            br.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return outputLine;
    }

    @Override
    public ImportSummaries postAdxReport( AdxType report )
        throws DHIS2ReportingException
    {

        log.debug( "Posting A report" );
        ImportSummaries summaries = null;

        StringWriter xmlReport = new StringWriter();
        try
        {
            JAXBContext jaxbDataValueSetContext = JAXBContext.newInstance( AdxType.class );

            Marshaller adxTypeMarshaller = jaxbDataValueSetContext.createMarshaller();
            // output pretty printed
            adxTypeMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            adxTypeMarshaller.marshal( report, xmlReport );
        }
        catch ( JAXBException ex )
        {
            throw new Dxf2Exception( "Problem marshalling adxtype", ex );
        }

        //System.out.print( "URL-" + url );

        String host = url.getHost();
        int port = url.getPort();

        //System.out.print( "URL-" + url + ":host-" + host + ":port-" );
        // System.out.println( port );

        HttpHost targetHost = new HttpHost( host, port, url.getProtocol() );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpContext localcontext = new BasicHttpContext();

        try
        {
            HttpPost httpPost = new HttpPost( url.getPath() + DATAVALUESET_PATH );
            Credentials creds = new UsernamePasswordCredentials( username, password );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/xml" );
            httpPost.addHeader( "Accept", "application/xml" );

            httpPost.setEntity( new StringEntity( xmlReport.toString() ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ( (len = entity.getContent().read( buffer )) > -1 )
            {
                baos.write( buffer, 0, len );
            }
            baos.flush();
            InputStream entity1 = new ByteArrayInputStream( baos.toByteArray() );
            InputStream entity2 = new ByteArrayInputStream( baos.toByteArray() );

            if ( response.getStatusLine().getStatusCode() != 200 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                try
                {
                    JAXBContext jaxbImportSummaryContext = JAXBContext.newInstance( ImportSummaries.class );
                    Unmarshaller importSummaryUnMarshaller = jaxbImportSummaryContext.createUnmarshaller();
                    summaries = (ImportSummaries) importSummaryUnMarshaller.unmarshal( entity1 );
                }
                catch ( Exception e )
                {
                    String output = outputISteam( entity2 );
                    //System.out.println( output );
                    summaries = new ImportSummaries();
                    try
                    {
                        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        InputSource src = new InputSource();
                        src.setCharacterStream( new StringReader( output ) );

                        Document doc = builder.parse( src );
                        doc.getDocumentElement().normalize();
                        Element importSummary = (Element) (doc.getElementsByTagName( "importSummary" ).item( 0 ));
                        Element importCount = (Element) (importSummary.getElementsByTagName( "importCount" ).item( 0 ));
                        Element importStatus = (Element) (importSummary.getElementsByTagName( "status" ).item( 0 ));
                        Element importDescription = (Element) (importSummary.getElementsByTagName( "description" )
                            .item( 0 ));

                        summaries.setDeleted( Integer.parseInt( importCount.getAttribute( "deleted" ) ) );
                        summaries.setIgnored( Integer.parseInt( importCount.getAttribute( "ignored" ) ) );
                        summaries.setImported( Integer.parseInt( importCount.getAttribute( "imported" ) ) );
                        summaries.setUpdated( Integer.parseInt( importCount.getAttribute( "updated" ) ) );
                        org.openmrs.module.dhisreport.api.importsummary.ImportSummary s = new org.openmrs.module.dhisreport.api.importsummary.ImportSummary();
                        s.setStatus( importStatus.getTextContent() );
                        s.setDescription( importDescription.getTextContent() );
                        List<org.openmrs.module.dhisreport.api.importsummary.ImportSummary> l = new ArrayList<org.openmrs.module.dhisreport.api.importsummary.ImportSummary>();
                        l.add( s );
                        summaries.setImportSummaryList( l );
                    }
                    catch ( Exception e2 )
                    {
                        e2.printStackTrace();
                        summaries.setDeleted( -1 );
                        summaries.setIgnored( -1 );
                        summaries.setImported( -1 );
                        summaries.setUpdated( -1 );
                    }
                }
            }
            else
            {
                summaries = new ImportSummaries();
            }
            // EntityUtils.consume( entity );

            // TODO: fix these catches ...
        }/*
         catch ( JAXBException ex )
         {
            throw new Dhis2Exception( this, "Problem unmarshalling ImportSummary", ex );
         }*/
        catch ( AuthenticationException ex )
        {
            throw new Dhis2Exception( this, "Problem authenticating to DHIS2 server", ex );
        }
        catch ( IOException ex )
        {
            throw new Dhis2Exception( this, "Problem accessing DHIS2 server", ex );
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
        return summaries;
    }

    @Override
    public ImportSummaries postDxf2Report( AdxType report )
        throws DHIS2ReportingException
    {

        log.debug( "Posting A report" );
        ImportSummaries summaries = null;

        StringWriter xmlReport = new StringWriter();
        try
        {
            JAXBContext jaxbDataValueSetContext = JAXBContext.newInstance( AdxType.class );

            Marshaller adxTypeMarshaller = jaxbDataValueSetContext.createMarshaller();
            // output pretty printed
            adxTypeMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
            adxTypeMarshaller.marshal( report, xmlReport );
        }
        catch ( JAXBException ex )
        {
            throw new Dxf2Exception( "Problem marshalling adxtype", ex );
        }

        //System.out.print( "URL-" + url );

        String host = url.getHost();
        int port = url.getPort();

        //System.out.print( "URL-" + url + ":host-" + host + ":port-" );
        //System.out.println( port );

        HttpHost targetHost = new HttpHost( host, port, url.getProtocol() );
        DefaultHttpClient httpclient = new DefaultHttpClient();
        BasicHttpContext localcontext = new BasicHttpContext();

        try
        {
            HttpPost httpPost = new HttpPost( url.getPath() + DATAVALUESET_PATH );

            //String datavaluepath = "/api/metadata";
            //HttpPost httpPost = new HttpPost( url.getPath() + datavaluepath );

            Credentials creds = new UsernamePasswordCredentials( username, password );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/xml" );
            httpPost.addHeader( "Accept", "application/xml" );

            String reportString = xmlReport.toString();
            System.out.println( reportString );
            try
            {
                //use dom to get information
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream( new StringReader( reportString ) );
                Document doc = builder.parse( src );
                doc.getDocumentElement().normalize();
                Element dataValueSet = (Element) (doc.getElementsByTagName( "adx" ).item( 0 ));
                Element group = (Element) (dataValueSet.getElementsByTagName( "group" ).item( 0 ));
                NodeList dataValues = group.getElementsByTagName( "dataValue" );

                //get data
                String periodOutput = group.getAttribute( "period" );
                String orgUnitValue = group.getAttribute( "orgUnit" );

                //try to reassemble new document manually
                String newdocument = "<?xml version='1.0' encoding='UTF-8'?>\n"
                    + "<dataValueSet xmlns=\"http://dhis2.org/schema/dxf/2.0\">\n";
                for ( int i = 0; i < dataValues.getLength(); i++ )
                {
                    Element dataValue = (Element) dataValues.item( i );
                    String dataElementValue = dataValue.getAttribute( "dataElement" );
                    String valueValue = dataValue.getAttribute( "value" );
                    String categoryOptionCombo = dataValue.getAttribute( "CategoryOptionCombo" );
                    String attributeOptionCombo = dataValue.getAttribute( "AttributeOptionCombo" );
                    newdocument += "<dataValue ";
                    newdocument += "dataElement=\"" + dataElementValue + "\" ";
                    newdocument += "period=\"" + periodOutput + "\" ";
                    newdocument += "orgUnit=\"" + orgUnitValue + "\" ";
                    newdocument += "categoryOptionCombo=\"" + categoryOptionCombo + "\" ";
                    newdocument += "attributeOptionCombo=\"" + attributeOptionCombo + "\" ";
                    newdocument += "value=\"" + valueValue + "\" ";
                    newdocument += "/>\n";
                }
                newdocument += "</dataValueSet>";
                reportString = newdocument;

            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }

            System.out.println( reportString );

            httpPost.setEntity( new StringEntity( reportString ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );
            HttpEntity entity = response.getEntity();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ( (len = entity.getContent().read( buffer )) > -1 )
            {
                baos.write( buffer, 0, len );
            }
            baos.flush();
            InputStream entity1 = new ByteArrayInputStream( baos.toByteArray() );
            InputStream entity2 = new ByteArrayInputStream( baos.toByteArray() );

            if ( response.getStatusLine().getStatusCode() != 200 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                try
                {
                    JAXBContext jaxbImportSummaryContext = JAXBContext.newInstance( ImportSummaries.class );
                    Unmarshaller importSummaryUnMarshaller = jaxbImportSummaryContext.createUnmarshaller();
                    summaries = (ImportSummaries) importSummaryUnMarshaller.unmarshal( entity1 );
                }
                catch ( Exception e )
                {
                    String output = outputISteam( entity2 );
                    System.out.println( output );
                    summaries = new ImportSummaries();
                    try
                    {
                        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        InputSource src = new InputSource();
                        src.setCharacterStream( new StringReader( output ) );

                        Document doc = builder.parse( src );
                        doc.getDocumentElement().normalize();
                        Element importSummary = (Element) (doc.getElementsByTagName( "importSummary" ).item( 0 ));
                        Element importCount = (Element) (importSummary.getElementsByTagName( "importCount" ).item( 0 ));
                        Element importStatus = (Element) (importSummary.getElementsByTagName( "status" ).item( 0 ));
                        Element importDescription = (Element) (importSummary.getElementsByTagName( "description" )
                            .item( 0 ));
                        // Element importCount = doc.getElementById( "importCount" );
                        summaries.setDeleted( Integer.parseInt( importCount.getAttribute( "deleted" ) ) );
                        summaries.setIgnored( Integer.parseInt( importCount.getAttribute( "ignored" ) ) );
                        summaries.setImported( Integer.parseInt( importCount.getAttribute( "imported" ) ) );
                        summaries.setUpdated( Integer.parseInt( importCount.getAttribute( "updated" ) ) );
                        org.openmrs.module.dhisreport.api.importsummary.ImportSummary s = new org.openmrs.module.dhisreport.api.importsummary.ImportSummary();
                        s.setStatus( importStatus.getTextContent() );
                        s.setDescription( importDescription.getTextContent() );
                        List<org.openmrs.module.dhisreport.api.importsummary.ImportSummary> l = new ArrayList<org.openmrs.module.dhisreport.api.importsummary.ImportSummary>();
                        l.add( s );
                        summaries.setImportSummaryList( l );
                    }
                    catch ( Exception e2 )
                    {
                        e2.printStackTrace();
                        summaries.setDeleted( -1 );
                        summaries.setIgnored( -1 );
                        summaries.setImported( -1 );
                        summaries.setUpdated( -1 );
                    }
                }
            }
            else
            {
                summaries = new ImportSummaries();
            }
            // EntityUtils.consume( entity );

            // TODO: fix these catches ...
        }/*
         catch ( JAXBException ex )
         {
            throw new Dhis2Exception( this, "Problem unmarshalling ImportSummary", ex );
         }*/
        catch ( AuthenticationException ex )
        {
            throw new Dhis2Exception( this, "Problem authenticating to DHIS2 server", ex );
        }
        catch ( IOException ex )
        {
            throw new Dhis2Exception( this, "Problem accessing DHIS2 server", ex );
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
        return summaries;
    }

    @Override
    public ImportSummaries postMetaData( String metaData )
        throws DHIS2ReportingException
    {

        log.debug( "Posting A meta data report" );
        ImportSummaries summaries = null;

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
            HttpPost httpPost = new HttpPost( dhisurl + METADATA_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/xml" );
            httpPost.addHeader( "Accept", "application/xml" );

            httpPost.setEntity( new StringEntity( metaData ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );
            HttpEntity entity = response.getEntity();

            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );
            String line;
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                //System.out.println( line );
                output += line;
            }

            if ( response.getStatusLine().getStatusCode() != 200 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                try
                {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource src = new InputSource();
                    summaries = new ImportSummaries();
                    src.setCharacterStream( new StringReader( output ) );

                    Document doc = builder.parse( src );
                    doc.getDocumentElement().normalize();

                    Element importCount = (Element) (doc.getElementsByTagName( "importCount" ).item( 0 ));
                    summaries.setDeleted( Integer.parseInt( importCount.getAttribute( "deleted" ) ) );
                    summaries.setIgnored( Integer.parseInt( importCount.getAttribute( "ignored" ) ) );
                    summaries.setImported( Integer.parseInt( importCount.getAttribute( "imported" ) ) );
                    summaries.setUpdated( Integer.parseInt( importCount.getAttribute( "updated" ) ) );

                    org.openmrs.module.dhisreport.api.importsummary.ImportSummary s = new org.openmrs.module.dhisreport.api.importsummary.ImportSummary();
                    s.setStatus( "meta data upload" );
                    List<org.openmrs.module.dhisreport.api.importsummary.ImportSummary> l = new ArrayList<org.openmrs.module.dhisreport.api.importsummary.ImportSummary>();
                    l.add( s );
                    summaries.setImportSummaryList( l );

                    //summaries.setImportSummaryList();

                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
                /*
                try
                {
                    JAXBContext jaxbImportSummaryContext = JAXBContext.newInstance( ImportSummaries.class );
                    Unmarshaller importSummaryUnMarshaller = jaxbImportSummaryContext.createUnmarshaller();
                    summaries = (ImportSummaries) importSummaryUnMarshaller.unmarshal( entity.getContent() );
                }
                catch ( Exception e )
                {
                    try
                    {
                        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        InputSource src = new InputSource();
                        summaries = new ImportSummaries();
                        src.setCharacterStream( new StringReader( output ) );

                        Document doc = builder.parse( src );
                        doc.getDocumentElement().normalize();
                        Element importSummary = (Element) (doc.getElementsByTagName( "importSummary" ).item( 0 ));
                        Element importCount = (Element) (importSummary.getElementsByTagName( "importCount" ).item( 0 ));
                        Element importStatus = (Element) (importSummary.getElementsByTagName( "status" ).item( 0 ));
                        Element importDescription = (Element) (importSummary.getElementsByTagName( "description" )
                            .item( 0 ));

                        summaries.setDeleted( Integer.parseInt( importCount.getAttribute( "deleted" ) ) );
                        summaries.setIgnored( Integer.parseInt( importCount.getAttribute( "ignored" ) ) );
                        summaries.setImported( Integer.parseInt( importCount.getAttribute( "imported" ) ) );
                        summaries.setUpdated( Integer.parseInt( importCount.getAttribute( "updated" ) ) );
                        org.openmrs.module.dhisreport.api.importsummary.ImportSummary s = new org.openmrs.module.dhisreport.api.importsummary.ImportSummary();
                        s.setStatus( importStatus.getTextContent() );
                        //s.setDescription( importDescription.getTextContent() );
                        List<org.openmrs.module.dhisreport.api.importsummary.ImportSummary> l = new ArrayList<org.openmrs.module.dhisreport.api.importsummary.ImportSummary>();
                        l.add( s );
                        summaries.setImportSummaryList( l );
                    }
                    catch ( Exception e2 )
                    {
                        e2.printStackTrace();
                        summaries.setDeleted( -1 );
                        summaries.setIgnored( -1 );
                        summaries.setImported( -1 );
                        summaries.setUpdated( -1 );
                    }
                }
                 */
            }
            else
            {
                summaries = new ImportSummaries();
            }

            // EntityUtils.consume( entity );

            // TODO: fix these catches ...
        }/*
         catch ( JAXBException ex )
         {
            throw new Dhis2Exception( this, "Problem unmarshalling ImportSummary", ex );
         }*/
        catch ( AuthenticationException ex )
        {
            throw new Dhis2Exception( this, "Problem authenticating to DHIS2 server", ex );
        }
        catch ( IOException ex )
        {
            throw new Dhis2Exception( this, "Problem accessing DHIS2 server", ex );
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
        return summaries;
    }

    @Override
    public ImportSummaries postTrackerCapture( String trackerCapture )
        throws Dhis2Exception
    {
        log.debug( "Posting A Tracker Capture Report" );
        ImportSummaries summaries = null;

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
            HttpPost httpPost = new HttpPost( dhisurl + TRACKERCAPTURE_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/json" );
            httpPost.addHeader( "Accept", "application/xml" );

            System.out.println( trackerCapture );
            httpPost.setEntity( new StringEntity( trackerCapture ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );
            HttpEntity entity = response.getEntity();

            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );
            String line;
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                output += line;
            }

            System.out.println( output );
            if ( response.getStatusLine().getStatusCode() != 201 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                try
                {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource src = new InputSource();
                    summaries = new ImportSummaries();
                    src.setCharacterStream( new StringReader( output ) );
                    /*
                    Document doc = builder.parse( src );
                    doc.getDocumentElement().normalize();

                    Element importCount = (Element) (doc.getElementsByTagName( "importCount" ).item( 0 ));
                    summaries.setDeleted( Integer.parseInt( importCount.getAttribute( "deleted" ) ) );
                    summaries.setIgnored( Integer.parseInt( importCount.getAttribute( "ignored" ) ) );
                    summaries.setImported( Integer.parseInt( importCount.getAttribute( "imported" ) ) );
                    summaries.setUpdated( Integer.parseInt( importCount.getAttribute( "updated" ) ) );

                    org.openmrs.module.dhisreport.api.importsummary.ImportSummary s = new org.openmrs.module.dhisreport.api.importsummary.ImportSummary();
                    s.setStatus( "meta data upload" );
                    List<org.openmrs.module.dhisreport.api.importsummary.ImportSummary> l = new ArrayList<org.openmrs.module.dhisreport.api.importsummary.ImportSummary>();
                    l.add( s );
                    summaries.setImportSummaryList( l );

                    //summaries.setImportSummaryList();
                     */
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
            else
            {
                summaries = new ImportSummaries();
            }
        }
        catch ( AuthenticationException ex )
        {
            throw new Dhis2Exception( this, "Problem authenticating to DHIS2 server", ex );
        }
        catch ( IOException ex )
        {
            throw new Dhis2Exception( this, "Problem accessing DHIS2 server", ex );
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }

        return summaries;
    }

    @Override
    public ImportSummaries postEnrollment( String enrollmentMessage )
    {
        log.debug( "Posting A Tracker Capture Report" );
        ImportSummaries summaries = null;

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
            HttpPost httpPost = new HttpPost( dhisurl + ENROLLMENT_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpPost, localcontext );
            httpPost.addHeader( "Authorization", bs.getValue() );
            httpPost.addHeader( "Content-Type", "application/json" );
            httpPost.addHeader( "Accept", "application/json" );

            httpPost.setEntity( new StringEntity( enrollmentMessage ) );
            HttpResponse response = httpclient.execute( targetHost, httpPost, localcontext );

            HttpEntity entity = response.getEntity();

            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );
            String line;
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                output += line;
            }
            /*
            if ( response.getStatusLine().getStatusCode() != 200 )
            {
                throw new Dhis2Exception( this, response.getStatusLine().getReasonPhrase(), null );
            }

            if ( entity != null )
            {
                try
                {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource src = new InputSource();
                    summaries = new ImportSummaries();
                    src.setCharacterStream( new StringReader( output ) );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
             */
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return summaries;
    }

    @Override
    public List<String[]> getDataElements( List<Object[]> elements, String prefix )
    {
        List<String[]> outputlist = new ArrayList<String[]>();
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
            HttpGet httpGet = new HttpGet( dhisurl + DATAELEMENT_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpGet, localcontext );
            httpGet.addHeader( "Authorization", bs.getValue() );
            httpGet.addHeader( "Content-Type", "application/xml" );
            httpGet.addHeader( "Accept", "application/xml" );

            HttpResponse response = httpclient.execute( targetHost, httpGet, localcontext );

            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

            String line;
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
            Element dataElements = (Element) (doc.getElementsByTagName( "dataElements" ).item( 0 ));

            NodeList dataElementList = dataElements.getElementsByTagName( "dataElement" );
            for ( int i = 0; i < dataElementList.getLength(); i++ )
            {
                Element dataElement = (Element) dataElementList.item( i );
                String elementName = dataElement.getAttribute( "name" );
                Object elementDatabaseId = listcontains( elements, elementName, prefix );
                String elementId;
                if ( !elementDatabaseId.equals( "" ) )
                {
                    elementId = dataElement.getAttribute( "id" );
                    String[] g = new String[3];
                    g[0] = elementName;
                    g[1] = elementId;
                    g[2] = ((Integer) (elementDatabaseId)).intValue() + "";
                    outputlist.add( g );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return outputlist;
    }

    @Override
    public List<TrackedEntity> getTrackedEntities( String ouCode )
    {
        List<TrackedEntity> outputlist = new ArrayList<TrackedEntity>();
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

            HttpGet httpGet = new HttpGet( dhisurl + TRACKEDENTITY_PATH + "?ou=" + ouCode );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpGet, localcontext );
            httpGet.addHeader( "Authorization", bs.getValue() );
            httpGet.addHeader( "Content-Type", "application/xml" );
            httpGet.addHeader( "Accept", "application/xml" );

            HttpResponse response = httpclient.execute( targetHost, httpGet, localcontext );
            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

            String line;
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                output += line;
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream( new StringReader( output ) );

            Document doc = builder.parse( src );
            doc.getDocumentElement().normalize();
            Element trackedEntityInstances = (Element) (doc.getElementsByTagName( "trackedEntityInstances" ).item( 0 ));

            NodeList trackedEntityInstanceList = trackedEntityInstances.getElementsByTagName( "trackedEntityInstance" );
            for ( int i = 0; i < trackedEntityInstanceList.getLength(); i++ )
            {
                Element trackedEntityInstance = (Element) trackedEntityInstanceList.item( i );
                String elementName = trackedEntityInstance.getAttribute( "trackedEntityInstance" );
                Map<String, String> attributelist = new HashMap<String, String>();
                Element attributeList = (Element) (trackedEntityInstance.getElementsByTagName( "attributes" ).item( 0 ));
                NodeList attributes = attributeList.getElementsByTagName( "attribute" );
                for ( int j = 0; j < attributes.getLength(); j++ )
                {
                    Element attribute = (Element) (attributes.item( j ));
                    String displayName = attribute.getAttribute( "displayName" );
                    String value = attribute.getAttribute( "value" );
                    attributelist.put( displayName, value );
                }
                TrackedEntity entity = new TrackedEntity( elementName, attributelist );
                outputlist.add( entity );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return outputlist;
    }

    @Override
    public void deleteTrackerCapture( TrackedEntity entity )
    {
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

            HttpDelete httpDelete = new HttpDelete( dhisurl + TRACKEDENTITY_PATH + "/" + entity.getUid() );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpDelete, localcontext );
            httpDelete.addHeader( "Authorization", bs.getValue() );
            httpDelete.addHeader( "Content-Type", "application/xml" );
            httpDelete.addHeader( "Accept", "application/xml" );

            httpclient.execute( targetHost, httpDelete, localcontext );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<OrganizationUnit> getOrgUnits()
    {
        List<OrganizationUnit> outputlist = new ArrayList<OrganizationUnit>();
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
            HttpGet httpGet = new HttpGet( dhisurl + ORGUNIT_PATH );
            Credentials creds = new UsernamePasswordCredentials( dhisusername, dhispassword );
            Header bs = new BasicScheme().authenticate( creds, httpGet, localcontext );
            httpGet.addHeader( "Authorization", bs.getValue() );
            httpGet.addHeader( "Content-Type", "application/xml" );
            httpGet.addHeader( "Accept", "application/xml" );

            HttpResponse response = httpclient.execute( targetHost, httpGet, localcontext );
            BufferedReader rd = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );

            String line = "";
            String output = "";
            while ( (line = rd.readLine()) != null )
            {
                output += line;
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream( new StringReader( output ) );

            Document doc = builder.parse( src );
            doc.getDocumentElement().normalize();
            Element organisationUnits = (Element) (doc.getElementsByTagName( "organisationUnits" ).item( 0 ));

            NodeList organisationUnitList = organisationUnits.getElementsByTagName( "organisationUnit" );
            for ( int i = 0; i < organisationUnitList.getLength(); i++ )
            {
                Element trackedEntityInstance = (Element) organisationUnitList.item( i );
                String orgUnitName = trackedEntityInstance.getAttribute( "name" );
                String orgUnitCode = trackedEntityInstance.getAttribute( "code" );
                String orgUnitId = trackedEntityInstance.getAttribute( "id" );
                OrganizationUnit orgUnit = new OrganizationUnit();
                orgUnit.setName( orgUnitName );
                orgUnit.setCode( orgUnitCode );
                orgUnit.setId( orgUnitId );
                outputlist.add( orgUnit );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return outputlist;
    }

    @Override
    public String getOrgUnitCode()
        throws Exception
    {

        Location l = LocationUtility.getDefaultLocation();
        List<OrganizationUnit> orgUnitList = this.getOrgUnits();
        String lCode = "";
        for ( LocationAttribute la : l.getActiveAttributes() )
        {
            if ( la.getAttributeType().getName().equals( "CODE" ) )
            {
                lCode = la.getValue().toString();
                break;
            }

        }
        if ( lCode.equals( "" ) )
        {
            throw new Exception( "location doesn't have a code attribute and thus cannot be mapped to an org unit" );
        }
        String ouCode = "";
        for ( OrganizationUnit ou : orgUnitList )
        {
            if ( ou.getCode().equals( lCode ) )
            {
                ouCode = ou.getId();
                break;
            }
        }
        if ( ouCode.equals( "" ) )
        {
            throw new Exception( "no org unit found in dhis2 instance that matches your current location" );
        }
        return ouCode;
    }

    private Object listcontains( List<Object[]> list, String item, String prefix )
    {
        for ( int i = 0; i < list.size(); i++ )
        {
            if ( ((String) (prefix + ": " + list.get( i )[1])).equals( item ) )
            {
                return (list.get( i )[0]);
            }
        }
        return "";
    }

    @Override
    public ReportDefinition fetchReportTemplates()
        throws Dhis2Exception
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
