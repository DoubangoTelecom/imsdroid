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
package ietf.params.xml.ns.pidf.caps;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "actortypes", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Actortypes {

    protected String attendant;
    protected String information;
    @Element(name = "msg-taker", required=false)
    protected String msgTaker;
    protected String principal;
    
    public String getAttendant() {
        return attendant;
    }
    
    public void setAttendant(String value) {
        this.attendant = value;
    }
    
    public String getInformation() {
        return information;
    }
    
    public void setInformation(String value) {
        this.information = value;
    }
   
    public String getMsgTaker() {
        return msgTaker;
    }
    
    public void setMsgTaker(String value) {
        this.msgTaker = value;
    }
    
    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String value) {
        this.principal = value;
    }
}
