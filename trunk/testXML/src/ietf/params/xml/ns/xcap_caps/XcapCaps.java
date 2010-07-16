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
package ietf.params.xml.ns.xcap_caps;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "xcap-caps", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:xcap-caps")
public class XcapCaps {

    @Element(name="auids", required = false) // Should be required
    protected XcapCaps.Auids auids;
    @Element(name="extensions", required = false) // Should be required
    protected XcapCaps.Extensions extensions;
    @Element(name="namespaces", required = false) // Should be required
    protected XcapCaps.Namespaces namespaces;
    
    public XcapCaps.Auids getAuids() {
        return auids;
    }
    
    public void setAuids(XcapCaps.Auids value) {
        this.auids = value;
    }
    
    public XcapCaps.Extensions getExtensions() {
        return extensions;
    }
    
    public void setExtensions(XcapCaps.Extensions value) {
        this.extensions = value;
    }
    
    public XcapCaps.Namespaces getNamespaces() {
        return namespaces;
    }
    
    public void setNamespaces(XcapCaps.Namespaces value) {
        this.namespaces = value;
    }
    
    @Root(name="auids", strict=false)
    public static class Auids {

    	@ElementList(entry = "auid", inline=true, required=false)
        protected List<String> auid;       
        public List<String> getAuid() {
            if (auid == null) {
                auid = new ArrayList<String>();
            }
            return this.auid;
        }
    }
    
    @Root(name="extensions", strict=false)
    public static class Extensions {
    	
    	@ElementList(entry = "extension", inline=true, required=false)
        protected List<String> extension;
        
        public List<String> getExtension() {
            if (extension == null) {
                extension = new ArrayList<String>();
            }
            return this.extension;
        }
    }
    
    @Root(name="namespaces", strict=false)
    public static class Namespaces {
    	
    	@ElementList(entry = "namespace", inline=true, required=false)
        protected List<String> namespace;
       
        public List<String> getNamespace() {
            if (namespace == null) {
                namespace = new ArrayList<String>();
            }
            return this.namespace;
        }
    }
}
