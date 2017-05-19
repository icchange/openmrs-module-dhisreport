package org.openmrs.module.dhisreport.api.db;

import org.openmrs.module.dhisreport.api.model.DataValueTemplate;
import org.openmrs.module.dhisreport.api.table.TableDhis2;

import java.util.Collection;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
public interface DHIS2TableDAO
{

    TableDhis2 saveTable( TableDhis2 dvt );

    TableDhis2 getTableById( Integer id );

    TableDhis2 getTableByUid( String uid );

    Collection<TableDhis2> getAllTables();
}
