package org.openmrs.module.dhisreport.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisreport.api.DHIS2ReportingService;
import org.openmrs.module.dhisreport.api.DHIS2TableService;
import org.openmrs.module.dhisreport.api.db.DHIS2ReportingDAO;
import org.openmrs.module.dhisreport.api.db.DHIS2TableDAO;
import org.openmrs.module.dhisreport.api.table.TableDhis2;

import java.util.Collection;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
public class DHIS2TableServiceImpl
    extends BaseOpenmrsService
    implements DHIS2TableService
{

    protected final Log log = LogFactory.getLog( this.getClass() );

    private DHIS2TableDAO dao;

    /**
     * @param dao the dao to set
     */
    public void setDao( DHIS2TableDAO dao )
    {
        this.dao = dao;
    }

    /**
     * @return the dao
     */
    public DHIS2TableDAO getDao()
    {
        return dao;
    }

    @Override
    public TableDhis2 getTableById( Integer id )
    {
        return dao.getTableById( id );
    }

    @Override
    public TableDhis2 getTableByUid( String uid )
    {
        return dao.getTableByUid( uid );
    }

    @Override
    public Collection<TableDhis2> getAllTables()
    {
        return dao.getAllTables();
    }

    @Override
    public TableDhis2 saveTable( TableDhis2 td )
    {
        return dao.saveTable( td );
    }
}
