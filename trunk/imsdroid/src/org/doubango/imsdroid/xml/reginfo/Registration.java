package org.doubango.imsdroid.xml.reginfo;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "registration")
public class Registration {

	@ElementList(required = false, inline=true)
    protected List<Contact> contact;
    @Attribute(required = true)
    protected String aor;
    @Attribute(required = true)
    protected String id;
    @Attribute(required = true)
    protected String state;
    
    public List<Contact> getContact() {
        return this.contact;
    }
    
    public String getAor() {
        return aor;
    }
    
    public String getId() {
        return id;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
}
