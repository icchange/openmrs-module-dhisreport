package org.openmrs.module.dhisreport.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.dhisreport.api.db.DHIS2TableDAO;
import org.openmrs.module.dhisreport.api.model.DataElement;
import org.openmrs.module.dhisreport.api.model.DataElementQuery;
import org.openmrs.module.dhisreport.api.model.DataValueTemplate;
import org.openmrs.module.dhisreport.api.model.Identifiable;
import org.openmrs.module.dhisreport.api.table.TableDhis2;

import java.util.Collection;
import java.util.List;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
public class HibernateDHIS2TableDAO
    implements DHIS2TableDAO
{

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
    public TableDhis2 saveTable( TableDhis2 td )
    {
        sessionFactory.getCurrentSession().saveOrUpdate( td );
        return td;
    }

    public Object getObjectByUid( String uid, Class<?> clazz )
    {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria( clazz );
        criteria.add( Restrictions.eq( "uid", uid ) );
        return criteria.uniqueResult();
    }

    @Override
    public TableDhis2 getTableById( Integer id )
    {
        return (TableDhis2) sessionFactory.getCurrentSession().get( TableDhis2.class, id );
    }

    @Override
    public TableDhis2 getTableByUid( String uid )
    {
        return (TableDhis2) getObjectByUid( uid, TableDhis2.class );
    }

    @Override
    public Collection<TableDhis2> getAllTables()
    {
        //todo
        Query query = sessionFactory.getCurrentSession().createQuery( "from TableDhis2 order by name asc" );
        return (List<TableDhis2>) query.list();
    }

}
