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
package org.openmrs.module.dhisreport.api.dxf2;

import javax.xml.bind.annotation.*;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "" )
@XmlRootElement( name = "dataValue" )
public class DataValue
{

    @XmlAttribute( required = true )
    protected String dataElement;

    @XmlAttribute
    protected String dataElementName;

    @XmlAttribute
    protected String dataElementCode;

    @XmlAttribute
    protected String uid;

    @XmlAttribute
    protected String categoryOptionCombo;

    @XmlAttribute
    protected String categoryOptionComboName;

    @XmlAttribute
    protected String categoryOptionComboCode;

    @XmlAttribute
    protected String attributeOptionCombo;

    @XmlAttribute( required = true )
    protected String value;

    public DataValue()
    {
    }

    public DataValue( String dataElement, String value )
    {
        this.dataElement = dataElement;
        this.value = value;
    }

    public DataValue( String dataElement, String categoryOptionCombo, String value )
    {
        this.dataElement = dataElement;
        this.categoryOptionCombo = categoryOptionCombo;
        this.value = value;
    }

    /**
     * Gets the value of the dataElement property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataElement()
    {
        return dataElement;
    }

    /**
     * Sets the value of the dataElement property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDataElement( String value )
    {
        this.dataElement = value;
    }

    public String getDataElementName()
    {
        return dataElementName;
    }

    public void setDataElementName( String value )
    {
        this.dataElementName = value;
    }

    public String getDataElementCode()
    {
        return dataElementCode;
    }

    public void setDataElementCode( String value )
    {
        this.dataElementCode = value;
    }

    /**
     * Gets the value of the categoryOptionCombo property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCategoryOptionCombo()
    {
        return categoryOptionCombo;
    }

    /**
     * Sets the value of the categoryOptionCombo property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCategoryOptionCombo( String value )
    {
        this.categoryOptionCombo = value;
    }

    public String getCategoryOptionComboName()
    {
        return categoryOptionComboName;
    }

    public void setCategoryOptionComboName( String value )
    {
        this.categoryOptionComboName = value;
    }

    public String getCategoryOptionComboCode()
    {
        return categoryOptionComboCode;
    }

    public void setCategoryOptionComboCode( String value )
    {
        this.categoryOptionComboCode = value;
    }

    /**
     * Gets the value of the categoryOptionCombo property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAttributeOptionCombo()
    {
        return attributeOptionCombo;
    }

    /**
     * Sets the value of the categoryOptionCombo property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAttributeOptionCombo( String value )
    {
        this.attributeOptionCombo = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setValue( String value )
    {
        this.value = value;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }

}
