/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/
package oma.xml.prs.pidf.oma_pres;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


@Root(name = "service-description", strict=false)
@Namespace(reference = "urn:oma:xml:prs:pidf:oma-pres")
@Default(DefaultType.FIELD)
public class ServiceDescription {

    @Element(name = "service-id", required = true)
    protected String serviceId;
    @Element(required = true)
    protected String version;
    @Element(required = false)
    protected String description;
   
    public String getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(String value) {
        this.serviceId = value;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String value) {
        this.version = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String value) {
        this.description = value;
    }    
}
