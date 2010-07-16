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

@Root(name = "methodtypes", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Methodtypes {

    @Element(name = "ACK")
    protected String ack;
    @Element(name = "BYE")
    protected String bye;
    @Element(name = "CANCEL")
    protected String cancel;
    @Element(name = "INFO")
    protected String info;
    @Element(name = "INVITE")
    protected String invite;
    @Element(name = "MESSAGE")
    protected String message;
    @Element(name = "NOTIFY")
    protected String notify;
    @Element(name = "OPTIONS")
    protected String options;
    @Element(name = "PRACK")
    protected String prack;
    @Element(name = "PUBLISH")
    protected String publish;
    @Element(name = "REFER")
    protected String refer;
    @Element(name = "REGISTER")
    protected String register;
    @Element(name = "SUBSCRIBE")
    protected String subscribe;
    @Element(name = "UPDATE")
    protected String update;
    
    public String getACK() {
        return ack;
    }
    
    public void setACK(String value) {
        this.ack = value;
    }
    
    public String getBYE() {
        return bye;
    }
    
    public void setBYE(String value) {
        this.bye = value;
    }
    
    public String getCANCEL() {
        return cancel;
    }
    
    public void setCANCEL(String value) {
        this.cancel = value;
    }
   
    public String getINFO() {
        return info;
    }
    
    public void setINFO(String value) {
        this.info = value;
    }
    
    public String getINVITE() {
        return invite;
    }
    
    public void setINVITE(String value) {
        this.invite = value;
    }
    
    public String getMESSAGE() {
        return message;
    }
   
    public void setMESSAGE(String value) {
        this.message = value;
    }

    public String getNOTIFY() {
        return notify;
    }
   
    public void setNOTIFY(String value) {
        this.notify = value;
    }
    
    public String getOPTIONS() {
        return options;
    }
   
    public void setOPTIONS(String value) {
        this.options = value;
    }
    
    public String getPRACK() {
        return prack;
    }
   
    public void setPRACK(String value) {
        this.prack = value;
    }
    
    public String getPUBLISH() {
        return publish;
    }
    
    public void setPUBLISH(String value) {
        this.publish = value;
    }
    
    public String getREFER() {
        return refer;
    }
   
    public void setREFER(String value) {
        this.refer = value;
    }
    
    public String getREGISTER() {
        return register;
    }
    
    public void setREGISTER(String value) {
        this.register = value;
    }
    
    public String getSUBSCRIBE() {
        return subscribe;
    }
   
    public void setSUBSCRIBE(String value) {
        this.subscribe = value;
    }
    
    public String getUPDATE() {
        return update;
    }
    
    public void setUPDATE(String value) {
        this.update = value;
    }
}
