package org.doubango.imsdroid.xml.reginfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "contact")
public class Contact {
	
    @Element(required = true)
    protected String uri;
    @Element(name = "display-name", required = false)
    protected Contact.DisplayName displayName;
    @ElementList(entry="unknown-param", inline = true, required = false)
    protected List<Contact.UnknownParam> unknownParam;
    @Attribute(required = true)
    protected String state;
    @Attribute(required = true)
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
    
    public void setState(String state) {
        this.state = state;
    }
   
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public BigInteger getDurationRegistered() {
        return durationRegistered;
    }

    public void setExpires(BigInteger expires) {
        this.expires = expires;
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

        @Text(required=true)
        protected String value;
        @Attribute(required=true)
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

    	@Text(required=false)
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
