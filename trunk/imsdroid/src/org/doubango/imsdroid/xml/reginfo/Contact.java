/*	Mozilla Public License Version 1.1
 * 
 * Copyright (C) 2010 Mamadou Diop.
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/

 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.

 * The Original Code is imsdroid v1.0.0.

 * The Initial Developers of the Original Code is Mamadou Diop <diopmamadou (at) doubango.org>

 * Portions created by ., ., . and .; 

 * All Rights Reserved.

 * Contributor(s): 
 * Mamadou Diop <diopmamadou (at) doubango.org>
 * ... add your name and email here
 */

package org.doubango.imsdroid.xml.reginfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "contact")
public class Contact {
	
    @Element(required = true)
    protected String uri;
    @Element(name = "display-name", required = false)
    protected Contact.DisplayName displayName;
    @Element(name = "unknown-param", required = false)
    protected List<Contact.UnknownParam> unknownParam;
    @Attribute(required = true)
    protected String state;
    @Attribute(required =true)
    protected String event;
    @Attribute(name = "duration-registered", required = false)
    protected BigInteger durationRegistered;
    @Attribute(required = false)
    protected BigInteger expires;
    @Attribute(name = "retry-after", required = false)
    protected BigInteger retryAfter;
    @Attribute(required = true)
    protected String id;
    @Attribute(required = false)
    protected String q;
    @Attribute(required = false)
    protected String callid;
    @Attribute(required = false)
    protected BigInteger cseq;
    
    
    public String getUri() {
        return uri;
    }

    public Contact.DisplayName getDisplayName() {
        return displayName;
    }

    public List<Contact.UnknownParam> getUnknownParam() {
        if (unknownParam == null) {
            unknownParam = new ArrayList<Contact.UnknownParam>();
        }
        return this.unknownParam;
    }
    
    public String getState() {
        return state;
    }
   
    public String getEvent() {
        return event;
    }
    
    public BigInteger getDurationRegistered() {
        return durationRegistered;
    }

    public BigInteger getExpires() {
        return expires;
    }

    public BigInteger getRetryAfter() {
        return retryAfter;
    }
    
    public String getId() {
        return id;
    }

    public String getQ() {
        return q;
    }
    
    public String getCallid() {
        return callid;
    }

    public BigInteger getCseq() {
        return cseq;
    }

    
    
    
    public static class DisplayName {

        @Text
        protected String value;
        @Attribute
        @Namespace(reference="http://www.w3.org/XML/1998/namespace")
        protected String lang;
        
        public String getValue() {
            return value;
        }
        public String getLang() {
            return lang;
        }
    }
    
    
    
    
    public static class UnknownParam {

    	@Text
        protected String value;
        @Attribute(required = true)
        protected String name;

        
        public String getValue() {
            return value;
        }
        
        public String getName() {
            return name;
        }

    }
}
