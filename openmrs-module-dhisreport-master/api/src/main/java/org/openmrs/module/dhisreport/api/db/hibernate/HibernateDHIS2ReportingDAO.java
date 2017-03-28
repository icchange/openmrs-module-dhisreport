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
package org.openmrs.module.dhisreport.api.db.hibernate;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.openmrs.Location;
import org.openmrs.module.dhisreport.api.DHIS2ReportingException;
import org.openmrs.module.dhisreport.api.db.DHIS2ReportingDAO;
import org.openmrs.module.dhisreport.api.model.*;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureAttribute;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;
import org.openmrs.module.dhisreport.api.utils.Period;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link DHIS2ReportingDAO}.
 */

public class HibernateDHIS2ReportingDAO
    implements DHIS2ReportingDAO
{

    // query parameters

    private static final String LOCATION = "locationId";

    private static final String START = "startDate";

    private static final String END = "endDate";

    protected final Log log = LogFactory.getLog( this.getClass() );

    private SessionFactory sessionFactory;

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory( SessionFactory sessionFactory )
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    @Override
    public DataElementQuery getDataElementQuery( Integer id )
    {
        return (DataElementQuery) sessionFactory.getCurrentSession().get( DataElementQuery.class, id );
    }

    @Override
    public DataElementQuery saveDataElementQuery( DataElementQuery de )
    {
        return (DataElementQuery) saveObject( de );
    }

    @Override
    public void deleteDataElementQuery( DataElementQuery de )
    {
        sessionFactory.getCurrentSession().delete( de );
    }

    @Override
    public DataElement getDataElement( Integer id )
    {
        return (DataElement) sessionFactory.getCurrentSession().get( DataElement.class, id );
    }

    @Override
    public DataElement saveDataElement( DataElement de )
    {
        return (DataElement) saveObject( de );
    }

    @Override
    public void deleteDataElement( DataElement de )
    {
        sessionFactory.getCurrentSession().delete( de );
    }

    @Override
    public Disaggregation getDisaggregation( Integer id )
    {
        return (Disaggregation) sessionFactory.getCurrentSession().get( Disaggregation.class, id );
    }

    @Override
    public Disaggregation saveDisaggregation( Disaggregation disagg )
    {
        return (Disaggregation) saveObject( disagg );
    }

    @Override
    public ReportDefinition getReportDefinition( Integer id )
    {
        return (ReportDefinition) sessionFactory.getCurrentSession().get( ReportDefinition.class, id );
    }

    @Override
    public ReportDefinition saveReportDefinition( ReportDefinition rd )
    {
        //        System.out.println( "sending the report definition for being saved which has period :------"
        //            + rd.getPeriodType() );
        return (ReportDefinition) saveObject( rd );
    }

    @Override
    public TrackerCaptureAttribute getTrackerCaptureAttribute( Integer id )
    {
        return (TrackerCaptureAttribute) sessionFactory.getCurrentSession().get( TrackerCaptureAttribute.class, id );
    }

    @Override
    public TrackerCaptureAttribute saveTrackerCaptureAttribute( TrackerCaptureAttribute dq )
    {
        sessionFactory.getCurrentSession().saveOrUpdate( dq );
        return dq;
    }

    @Override
    public TrackerCaptureEnrollment getTrackerCaptureEnrollment( Integer id )
    {
        return (TrackerCaptureEnrollment) sessionFactory.getCurrentSession().get( TrackerCaptureEnrollment.class, id );
    }

    @Override
    public TrackerCaptureEnrollment saveTrackerCaptureEnrollment( TrackerCaptureEnrollment dq )
    {
        sessionFactory.getCurrentSession().saveOrUpdate( dq );
        return dq;
    }

    @Override
    public TrackerCaptureTemplate getTrackerCaptureReport( Integer id )
    {
        return (TrackerCaptureTemplate) sessionFactory.getCurrentSession().get( TrackerCaptureTemplate.class, id );
    }

    @Override
    public TrackerCaptureTemplate saveTrackerCaptureReport( TrackerCaptureTemplate tct )
    {
        return (TrackerCaptureTemplate) saveObject( tct );
    }

    @Override
    public void deleteTrackerCaptureReport( TrackerCaptureTemplate tct )
    {
        sessionFactory.getCurrentSession().delete( tct );
    }

    @Override
    public Collection<DataElement> getAllDataElements()
    {
        Query query = sessionFactory.getCurrentSession().createQuery( "from DataElement order by name asc" );
        return (List<DataElement>) query.list();
    }

    @Override
    public Collection<Disaggregation> getAllDisaggregations()
    {
        Query query = sessionFactory.getCurrentSession().createQuery( "from Disaggregation" );
        return (List<Disaggregation>) query.list();
    }

    @Override
    public void deleteDisaggregation( Disaggregation disagg )
    {
        sessionFactory.getCurrentSession().delete( disagg );
    }

    @Override
    public Collection<ReportDefinition> getAllReportDefinitions()
    {
        Query query = sessionFactory.getCurrentSession().createQuery( "from ReportDefinition order by name asc" );
        return (List<ReportDefinition>) query.list();
    }

    @Override
    public Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates()
    {
        Query query = sessionFactory.getCurrentSession().createQuery( "from TrackerCaptureTemplate order by name asc" );
        return (List<TrackerCaptureTemplate>) query.list();
    }

    @Override
    public void deleteReportDefinition( ReportDefinition rd )
    {
        sessionFactory.getCurrentSession().delete( rd );
    }

    /*
     * Should evaluateDataValueTemplates
     * return String
     * */
    @Override
    public String evaluateDataValueTemplate( DataValueTemplate dvt, Period period, Location location )
        throws DHIS2ReportingException
    {
        String queryString = dvt.getQuery();
        queryString = queryString.replaceAll( "\t", " " );
        queryString = queryString.replaceAll( "\n", " " );
        queryString = queryString.trim();
        if ( queryString == null || queryString.isEmpty() )
        {
            log.debug( "Empty query for " + dvt.getDataelement().getName() + " : " + dvt.getDisaggregation().getName() );
            return null;
        }

        if ( dvt.potentialUpdateDelete() )
        {
            throw new DHIS2ReportingException( "Attempt to execute potential update/delete query for "
                + dvt.getDataelement().getName() + " : " + dvt.getDisaggregation().getName() );
        }

        Query query = sessionFactory.getCurrentSession().createSQLQuery( queryString );

        List<String> parameters = new ArrayList<String>( Arrays.asList( query.getNamedParameters() ) );
        // loactionId is optional
        if ( parameters.contains( "locationId" ) )
        {
            query.setParameter( "locationId", location.getId().toString() );
        }
        query.setParameter( "startOfPeriod", period.getStartDate() );
        query.setParameter( "endOfPeriod", period.getEndDate() );

        Object result = query.uniqueResult();
        if ( result != null )
        {
            return result.toString();
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<Object[]> evaluateDataElementQuery( DataElementQuery dq, Location location )
        throws DHIS2ReportingException
    {
        String queryString = dq.getQuery();
        queryString = queryString.replaceAll( "\t", " " );
        queryString = queryString.replaceAll( "\n", " " );
        queryString = queryString.trim();

        if ( queryString == null || queryString.isEmpty() )
        {
            log.debug( "Empty query for Data Element Query" );
            return null;
        }

        if ( dq.potentialUpdateDelete() )
        {
            throw new DHIS2ReportingException(
                "Attempt to execute potential update/delete query for Data Element Query" );
        }

        Query query = sessionFactory.getCurrentSession().createSQLQuery( queryString );

        List<String> parameters = new ArrayList<String>( Arrays.asList( query.getNamedParameters() ) );
        // loactionId is optional
        if ( parameters.contains( "locationId" ) )
        {
            query.setParameter( "locationId", location.getId().toString() );
        }
        return query.list();
    }

    @Override
    public List<Map<String, Object>> evaluateTrackerCaptureQuery( String querystr )
        throws DHIS2ReportingException
    {

        String queryString = querystr;
        queryString = queryString.replaceAll( "\t", " " );
        queryString = queryString.replaceAll( "\n", " " );
        queryString = queryString.trim();

        if ( queryString == null || queryString.isEmpty() )
        {
            log.debug( "Empty query for Data Element Query" );
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        Query query = sessionFactory.getCurrentSession().createSQLQuery( queryString ).setResultTransformer(
            AliasToEntityMapResultTransformer.INSTANCE );
        List<Map<String, Object>> rows = query.list();

        return rows;
    }

    @Override
    public List<Map<String, Object>> evaluateTrackerCaptureEnrollment( String querystr, Location location )
    {
        String queryString = querystr;
        queryString = queryString.replaceAll( "\t", " " );
        queryString = queryString.replaceAll( "\n", " " );
        queryString = queryString.trim();

        if ( queryString == null || queryString.isEmpty() )
        {
            log.debug( "Empty query for Data Element Query" );
            return null;
        }

        Query query = sessionFactory.getCurrentSession().createSQLQuery( queryString ).setResultTransformer(
            AliasToEntityMapResultTransformer.INSTANCE );
        List<String> parameters = new ArrayList<String>( Arrays.asList( query.getNamedParameters() ) );
        if ( parameters.contains( "locationId" ) )
        {
            query.setParameter( "locationId", location.getId().toString() );
        }
        List<Map<String, Object>> map = query.list();

        return map;
    }

    // --------------------------------------------------------------------------------------------------------------
    // Generic methods for DHIS2 identifiable objects
    // --------------------------------------------------------------------------------------------------------------
    public Identifiable getObjectByUid( String uid, Class<?> clazz )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( clazz );
        criteria.add( Restrictions.eq( "uid", uid ) );
        return (Identifiable) criteria.uniqueResult();
    }

    @Transactional
    public Identifiable saveReportDefinitionObject( ReportDefinition object )
    {
        Session session = sessionFactory.getCurrentSession();

        // force merge if uid already exists
        Criteria criteria = session.createCriteria( object.getClass() );
        criteria.add( Restrictions.eq( "uid", object.getUid() ) );
        ReportDefinition existingObject = (ReportDefinition) criteria.uniqueResult();

        if ( existingObject != null )
        {
            session.evict( existingObject );
            existingObject.setPeriodType( object.getPeriodType() );
            existingObject.setName( object.getName() );
            existingObject.setCode( object.getCode() );
            existingObject.setDataValueTemplates( object.getDataValueTemplates() );

            session.update( existingObject );
            return existingObject;

        }
        session.saveOrUpdate( object );
        return object;
    }

    @Transactional
    public Identifiable saveDataElementObject( DataElement object )
    {
        Session session = sessionFactory.getCurrentSession();
        // force merge if uid already exists

        DataElement existingObject = (DataElement) getObjectByUid( object.getUid(), object.getClass() );

        if ( existingObject != null )
        {
            existingObject.setName( object.getName() );
            existingObject.setCode( object.getCode() );

            session.update( existingObject );
            return existingObject;

        }
        // sessionFactory.getCurrentSession().saveOrUpdate( object );
        session.save( object );
        return object;
    }

    @Transactional
    public Identifiable saveObject( Identifiable object )
    {
        Session session = sessionFactory.getCurrentSession();
        // force merge if uid already exists
        Identifiable existingObject = getObjectByUid( object.getUid(), object.getClass() );
        if ( existingObject != null )
        {
            session.evict( existingObject );
            object.setId( existingObject.getId() );
            session.load( object, object.getId() );
        }
        sessionFactory.getCurrentSession().saveOrUpdate( object );
        return object;
    }

    @Override
    public DataElementQuery getDataElementQueryByUid( String uid )
    {
        return (DataElementQuery) getObjectByUid( uid, DataElementQuery.class );
    }

    @Override
    public DataElement getDataElementByUid( String uid )
    {
        return (DataElement) getObjectByUid( uid, DataElement.class );
    }

    @Override
    public Disaggregation getDisaggregationByUid( String uid )
    {
        return (Disaggregation) getObjectByUid( uid, Disaggregation.class );
    }

    @Override
    public ReportDefinition getReportDefinitionByUid( String uid )
    {
        return (ReportDefinition) getObjectByUid( uid, ReportDefinition.class );
    }

    @Override
    public ReportDefinition getReportDefinitionByCode( String code )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( ReportDefinition.class );
        criteria.add( Restrictions.eq( "code", code ) );
        return (ReportDefinition) criteria.uniqueResult();
    }

    public List<ReportDefinition> getReportDefinitionByPeriodType( String periodType )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( ReportDefinition.class );
        criteria.add( Restrictions.eq( "periodType", periodType ) );
        return (List<ReportDefinition>) criteria.list();
    }

    @Override
    public DataValueTemplate getDataValueTemplate( Integer id )
    {
        return (DataValueTemplate) sessionFactory.getCurrentSession().get( DataValueTemplate.class, id );
    }

    @Override
    public DataValueTemplate saveDataValueTemplate( DataValueTemplate dvt )
    {
        sessionFactory.getCurrentSession().saveOrUpdate( dvt );
        return dvt;
    }

    @Override
    public DataValueTemplate saveDataValueTemplateTest( DataValueTemplate dvt )
    {

        ReportDefinition rd = getReportDefinitionByUid( dvt.getReportDefinition().getUid() );
        DataElement de = getDataElementByUid( dvt.getDataelement().getUid() );
        Disaggregation dis = getDisaggregationByUid( dvt.getDisaggregation().getUid() );
        dvt.setDataelement( de );
        dvt.setDisaggregation( dis );
        dvt.setReportDefinition( rd );

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( DataValueTemplate.class );
        criteria.add( Restrictions.eq( "reportDefinition", rd ) ).add( Restrictions.eq( "dataelement", de ) ).add(
            Restrictions.eq( "disaggregation", dis ) );

        DataValueTemplate dvt_db = (DataValueTemplate) criteria.uniqueResult();

        if ( dvt_db == null )
        {
            sessionFactory.getCurrentSession().save( dvt );
            return dvt;
        }
        else
        {
            sessionFactory.getCurrentSession().saveOrUpdate( dvt );
            return dvt_db;
        }

    }

    @Override
    public Location getLocationByOU_Code( String OU_Code )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( Location.class );
        criteria.add( Restrictions.like( "name", OU_Code ) );
        return (Location) criteria.uniqueResult();
    }

    @Override
    public DataElement getDataElementByCode( String code )
    {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( DataElement.class );
        criteria.add( Restrictions.eq( "code", code ) );
        return (DataElement) criteria.uniqueResult();

    }
}
