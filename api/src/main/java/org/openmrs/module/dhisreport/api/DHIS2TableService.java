package org.openmrs.module.dhisreport.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.dhisreport.api.table.TableDhis2;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureEnrollment;
import org.openmrs.module.dhisreport.api.trackercapture.TrackerCaptureTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Created by ICCHANGE on 7/Mar/2017.
 */
@Transactional
public interface DHIS2TableService
    extends OpenmrsService
{

    TableDhis2 saveTable( TableDhis2 td );

    TableDhis2 getTableById( Integer id );

    TableDhis2 getTableByUid( String uid );

    Collection<TableDhis2> getAllTables();

}
