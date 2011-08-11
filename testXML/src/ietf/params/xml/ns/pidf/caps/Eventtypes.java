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

@Root(name = "eventtypes", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Eventtypes {

    protected String conference;
    protected String dialog;
    protected String kpml;
    @Element(name = "message-summary")
    protected String messageSummary;
    @Element(name = "poc-settings")
    protected String pocSettings;
    protected String presence;
    protected String reg;
    protected String refer;
    @Element(name = "Siemens-RTP-Stats")
    protected String siemensRTPStats;
    @Element(name = "spirits-INDPs")
    protected String spiritsINDPs;
    @Element(name = "spirits-user-prof")
    protected String spiritsUserProf;
    protected String winfo;
   
    public String getConference() {
        return conference;
    }
    
    public void setConference(String value) {
        this.conference = value;
    }
    
    public String getDialog() {
        return dialog;
    }
    
    public void setDialog(String value) {
        this.dialog = value;
    }
   
    public String getKpml() {
        return kpml;
    }
    
    public void setKpml(String value) {
        this.kpml = value;
    }
   
    public String getMessageSummary() {
        return messageSummary;
    }
   
    public void setMessageSummary(String value) {
        this.messageSummary = value;
    }
    
    public String getPocSettings() {
        return pocSettings;
    }
    
    public void setPocSettings(String value) {
        this.pocSettings = value;
    }
    
    public String getPresence() {
        return presence;
    }
 
    public void setPresence(String value) {
        this.presence = value;
    }
   
    public String getReg() {
        return reg;
    }
    
    public void setReg(String value) {
        this.reg = value;
    }
    
    public String getRefer() {
        return refer;
    }
    
    public void setRefer(String value) {
        this.refer = value;
    }
   
    public String getSiemensRTPStats() {
        return siemensRTPStats;
    }
    
    public void setSiemensRTPStats(String value) {
        this.siemensRTPStats = value;
    }
   
    public String getSpiritsINDPs() {
        return spiritsINDPs;
    }
   
    public void setSpiritsINDPs(String value) {
        this.spiritsINDPs = value;
    }
    
    public String getSpiritsUserProf() {
        return spiritsUserProf;
    }
    
    public void setSpiritsUserProf(String value) {
        this.spiritsUserProf = value;
    }
    
    public String getWinfo() {
        return winfo;
    }
   
    public void setWinfo(String value) {
        this.winfo = value;
    }
}
