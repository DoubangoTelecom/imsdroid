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
package ietf.params.xml.ns.pidf.data_model;

import ietf.params.xml.ns.pidf.rpid.Activities;
import ietf.params.xml.ns.pidf.rpid.Mood;
import ietf.params.xml.ns.pidf.rpid.StatusIcon;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oma.xml.prs.pidf.oma_pres.OverridingWillingness;

import org.doubango.imsdroid.utils.RFC3339Date;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

@Root(name = "person", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:data-model")
/* @Default(DefaultType.FIELD) */
public class Person {
    
	@ElementList(entry="note", inline=true, required = false)
    protected List<NoteT> note;
    @Element(name = "timestamp", required = false)
    protected String _timestamp;
    @Attribute(required = true)
    protected String id;    
   
    private Date timestamp;
    
    public List<NoteT> getNote() {
        if (note == null) {
            note = new ArrayList<NoteT>();
        }
        return this.note;
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
    
    /* ====================== Extension =========================*/
    @Element(name="activities", required=false)
    protected Activities activities;
    @Element(name="mood", required=false)
    protected Mood mood;
    @Element(name="overriding-willingness", required=false)
    protected OverridingWillingness overridingWillingness;
    @Element(name="status-icon", required=false)
    protected StatusIcon statusIcon;
    
    public Activities getActivities(){
    	return this.activities;
    }
    
    public OverridingWillingness getOverridingWillingness(){
    	return this.overridingWillingness;
    }
    
    public Mood getMood(){
    	return this.mood;
    }
    
    public StatusIcon getStatusIcon(){
    	return this.statusIcon;
    }
}
