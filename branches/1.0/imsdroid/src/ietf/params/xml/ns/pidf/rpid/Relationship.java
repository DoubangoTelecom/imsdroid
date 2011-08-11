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
package ietf.params.xml.ns.pidf.rpid;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "relationship", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
@Default(DefaultType.FIELD)
public class Relationship {

    protected List<NoteT> note;
    protected Empty assistant;
    protected Empty associate;
    protected Empty family;
    protected Empty friend;
    protected NoteT other;
    protected Empty self;
    protected Empty supervisor;
    protected Empty unknown;
    
    public List<NoteT> getNote() {
        if (note == null) {
            note = new ArrayList<NoteT>();
        }
        return this.note;
    }
   
    public Empty getAssistant() {
        return assistant;
    }
    
    public void setAssistant(Empty value) {
        this.assistant = value;
    }
    
    public Empty getAssociate() {
        return associate;
    }
   
    public void setAssociate(Empty value) {
        this.associate = value;
    }
   
    public Empty getFamily() {
        return family;
    }
    
    public void setFamily(Empty value) {
        this.family = value;
    }
    
    public Empty getFriend() {
        return friend;
    }
    
    public void setFriend(Empty value) {
        this.friend = value;
    }
    
    public NoteT getOther() {
        return other;
    }
    
    public void setOther(NoteT value) {
        this.other = value;
    }
    
    public Empty getSelf() {
        return self;
    }

    public void setSelf(Empty value) {
        this.self = value;
    }
    
    public Empty getSupervisor() {
        return supervisor;
    }
    
    public void setSupervisor(Empty value) {
        this.supervisor = value;
    }
   
    public Empty getUnknown() {
        return unknown;
    }
    
    public void setUnknown(Empty value) {
        this.unknown = value;
    }
}
