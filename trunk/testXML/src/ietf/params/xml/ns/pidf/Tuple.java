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
package ietf.params.xml.ns.pidf;

import ietf.params.xml.ns.pidf.caps.Servcapstype;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oma.xml.prs.pidf.oma_pres.ServiceDescription;
import oma.xml.prs.pidf.oma_pres.Willingness;

import org.doubango.imsdroid.utils.RFC3339Date;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

@Root(name="tuple", strict = false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf")
public class Tuple {
	
    @Element(name="status", required = true)
    protected Status status;
    
    @Element(required = false)
    protected Contact contact;
    @ElementList(entry = "note", inline=true, required=false)
    protected List<Note> note;
    @Element(name="timestamp", required = false)
    protected String _timestamp;
    @Attribute(required = true)
    protected String id;
    
    protected Date timestamp;
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status value) {
        this.status = value;
    }
    
    public Contact getContact() {
        return contact;
    }
    
    public void setContact(Contact value) {
        this.contact = value;
    }
    
    public List<Note> getNote() {
        if (note == null) {
            note = new ArrayList<Note>();
        }
        return this.note;
    }
    
    @Commit
    public void commit() {
    	if(this._timestamp != null){
    		try {
				timestamp = RFC3339Date.parseRFC3339Date(this._timestamp);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		_timestamp = null;
    	}
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date value) {
        this.timestamp = value;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String value) {
        this.id = value;
    }
    
    /* ====================== Extension =========================*/
    @Element(name="willingness", required=false)
    protected Willingness willingness;
    @Element(name="servicedescription", required=false)
    protected ServiceDescription servicedescription;
    @Element(name="deviceID", required=false)
    protected String deviceID;
    //@Element(name="relationship", required=false)
    //protected relationship relationship;
    @Element(name="servcaps", required=false)
    Servcapstype servcapstype;
    
    public Willingness getWillingness(){
    	return this.willingness;
    }
    
    public ServiceDescription getServiceDescription(){
    	return this.servicedescription;
    }
    
    public String getDeviceID(){
    	return this.deviceID;
    }
    
    public Servcapstype getServcapstype(){
    	return this.servcapstype;
    }
}
