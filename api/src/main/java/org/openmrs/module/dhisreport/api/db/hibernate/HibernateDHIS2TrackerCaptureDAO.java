package org.openmrs.module.dhisreport.api.db.hibernate;

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
import org.openmrs.module.dhisreport.api.db.DHIS2TrackerCaptureDAO;
import org.openmrs.module.dhisreport.api.model.Identifiable;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureAttribute;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by ICCHANGE on 31/Mar/2017.
 */
public class HibernateDHIS2TrackerCaptureDAO
    implements DHIS2TrackerCaptureDAO
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
    public Collection<TrackerCaptureTemplate> getAllTrackerCaptureTemplates()
    {
        Query query = sessionFactory.getCurrentSession().createQuery( "from TrackerCaptureTemplate order by name asc" );
        return (List<TrackerCaptureTemplate>) query.list();
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

    // --------------------------------------------------------------------------------------------------------------
    // Generic methods for DHIS2 identifiable objects
    // --------------------------------------------------------------------------------------------------------------
    public Identifiable getObjectByUid( String uid, Class<?> clazz )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( clazz );
        criteria.add( Restrictions.eq( "uid", uid ) );
        return (Identifiable) criteria.uniqueResult();
    }

}
