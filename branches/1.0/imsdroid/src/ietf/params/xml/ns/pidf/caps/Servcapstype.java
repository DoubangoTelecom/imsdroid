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

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "servcapstype", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Servcapstype {

    protected Actortype actor;
    protected Boolean application;
    protected Boolean audio;
    protected Boolean automata;
    @Element(name = "class")
    protected Classtype clazz;
    protected Boolean control;
    protected Boolean data;
    protected List<Descriptiontype> description;
    protected Duplextype duplex;
    @Element(name = "event-packages")
    protected EventPackagestype eventPackages;
    protected Extensionstype extensions;
    protected Boolean isfocus;
    protected Boolean message;
    protected Methodstype methods;
    protected Languagestype languages;
    protected Prioritytype priority;
    protected Schemestype schemes;
    protected Boolean text;
    protected List<String> type;
    protected Boolean video;
    
    public Actortype getActor() {
        return actor;
    }
    
    public void setActor(Actortype value) {
        this.actor = value;
    }
   
    public Boolean isApplication() {
        return application;
    }
    
    public void setApplication(Boolean value) {
        this.application = value;
    }
    
    public Boolean isAudio() {
        return audio;
    }
    
    public void setAudio(Boolean value) {
        this.audio = value;
    }
    
    public Boolean isAutomata() {
        return automata;
    }
   
    public void setAutomata(Boolean value) {
        this.automata = value;
    }
    
    public Classtype getClazz() {
        return clazz;
    }
   
    public void setClazz(Classtype value) {
        this.clazz = value;
    }
   
    public Boolean isControl() {
        return control;
    }
    
    public void setControl(Boolean value) {
        this.control = value;
    }
   
    public Boolean isData() {
        return data;
    }
    
    public void setData(Boolean value) {
        this.data = value;
    }
    
    public List<Descriptiontype> getDescription() {
        if (description == null) {
            description = new ArrayList<Descriptiontype>();
        }
        return this.description;
    }
    
    public Duplextype getDuplex() {
        return duplex;
    }
    
    public void setDuplex(Duplextype value) {
        this.duplex = value;
    }
    
    public EventPackagestype getEventPackages() {
        return eventPackages;
    }
    
    public void setEventPackages(EventPackagestype value) {
        this.eventPackages = value;
    }
   
    public Extensionstype getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Extensionstype value) {
        this.extensions = value;
    }
    
    public Boolean isIsfocus() {
        return isfocus;
    }
   
    public void setIsfocus(Boolean value) {
        this.isfocus = value;
    }
    
    public Boolean isMessage() {
        return message;
    }
   
    public void setMessage(Boolean value) {
        this.message = value;
    }
    
    public Methodstype getMethods() {
        return methods;
    }
    
    public void setMethods(Methodstype value) {
        this.methods = value;
    }
    
    public Languagestype getLanguages() {
        return languages;
    }
   
    public void setLanguages(Languagestype value) {
        this.languages = value;
    }
   
    public Prioritytype getPriority() {
        return priority;
    }
    
    public void setPriority(Prioritytype value) {
        this.priority = value;
    }
    
    public Schemestype getSchemes() {
        return schemes;
    }
    
    public void setSchemes(Schemestype value) {
        this.schemes = value;
    }
    
    public Boolean isText() {
        return text;
    }
    
    public void setText(Boolean value) {
        this.text = value;
    }
   
    public List<String> getType() {
        if (type == null) {
            type = new ArrayList<String>();
        }
        return this.type;
    }
   
    public Boolean isVideo() {
        return video;
    }
    
    public void setVideo(Boolean value) {
        this.video = value;
    }
}
