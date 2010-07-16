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

@Root(name = "extensiontypes", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Extensiontypes {

    protected String rel100;
    @Element(name = "early-session")
    protected String earlySession;
    protected String eventlist;
    @Element(name = "from-change")
    protected String fromChange;
    protected String gruu;
    @Element(name = "hist-info")
    protected String histInfo;
    protected String join;
    protected String norefersub;
    protected String path;
    protected String precondition;
    protected String pref;
    protected String privacy;
    protected String replaces;
    @Element(name = "resource-priority")
    protected String resourcePriority;
    @Element(name = "sdp-anat")
    protected String sdpAnat;
    @Element(name = "sec-agree")
    protected String secAgree;
    protected String tdialog;
    protected String timer;
    
    public String getRel100() {
        return rel100;
    }

    public void setRel100(String value) {
        this.rel100 = value;
    }
    
    public String getEarlySession() {
        return earlySession;
    }
   
    public void setEarlySession(String value) {
        this.earlySession = value;
    }
   
    public String getEventlist() {
        return eventlist;
    }

    public void setEventlist(String value) {
        this.eventlist = value;
    }
   
    public String getFromChange() {
        return fromChange;
    }
    
    public void setFromChange(String value) {
        this.fromChange = value;
    }
    
    public String getGruu() {
        return gruu;
    }

    public void setGruu(String value) {
        this.gruu = value;
    }

    public String getHistInfo() {
        return histInfo;
    }
    
    public void setHistInfo(String value) {
        this.histInfo = value;
    }
   
    public String getJoin() {
        return join;
    }
   
    public void setJoin(String value) {
        this.join = value;
    }
    
    public String getNorefersub() {
        return norefersub;
    }
    
    public void setNorefersub(String value) {
        this.norefersub = value;
    }
   
    public String getPath() {
        return path;
    }
   
    public void setPath(String value) {
        this.path = value;
    }
   
    public String getPrecondition() {
        return precondition;
    }
    
    public void setPrecondition(String value) {
        this.precondition = value;
    }
    
    public String getPref() {
        return pref;
    }
   
    public void setPref(String value) {
        this.pref = value;
    }
    
    public String getPrivacy() {
        return privacy;
    }
   
    public void setPrivacy(String value) {
        this.privacy = value;
    }
   
    public String getReplaces() {
        return replaces;
    }
   
    public void setReplaces(String value) {
        this.replaces = value;
    }
    
    public String getResourcePriority() {
        return resourcePriority;
    }
   
    public void setResourcePriority(String value) {
        this.resourcePriority = value;
    }
   
    public String getSdpAnat() {
        return sdpAnat;
    }
   
    public void setSdpAnat(String value) {
        this.sdpAnat = value;
    }
  
    public String getSecAgree() {
        return secAgree;
    }
  
    public void setSecAgree(String value) {
        this.secAgree = value;
    }
    
    public String getTdialog() {
        return tdialog;
    }
   
    public void setTdialog(String value) {
        this.tdialog = value;
    }
   
    public String getTimer() {
        return timer;
    }
    
    public void setTimer(String value) {
        this.timer = value;
    }
}
