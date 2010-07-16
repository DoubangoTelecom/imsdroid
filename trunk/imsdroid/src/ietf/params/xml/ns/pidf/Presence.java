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

import ietf.params.xml.ns.pidf.data_model.Device;
import ietf.params.xml.ns.pidf.data_model.Person;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;



@Root(name="presence", strict = false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf")
public class Presence {

	@ElementList(entry = "tuple", inline=true, required=false)
    protected List<Tuple> tuple;
	@ElementList(entry = "note", inline=true, required=false)
    protected List<Note> note;
    @Attribute(required = true)
    protected String entity;

    
    public List<Tuple> getTuple() {
        if (tuple == null) {
            tuple = new ArrayList<Tuple>();
        }
        return this.tuple;
    }
    
    public List<Note> getNote() {
        if (note == null) {
            note = new ArrayList<Note>();
        }
        return this.note;
    }
    
    public String getEntity() {
        return entity;
    }
    
    public void setEntity(String value) {
        this.entity = value;
    }
    
    
    
    /* ====================== Extension =========================*/
    @ElementList(entry = "person", inline=true, required = false)
    protected List<Person> persons;
    @Element(name="device", required=false)
    protected Device device;
    
    
    public List<Person> getPersons() {
        if (persons == null) {
        	persons = new ArrayList<Person>();
        }
        return this.persons;
    }
    
    public Device getDevice(){
    	return this.device;
    }
}
