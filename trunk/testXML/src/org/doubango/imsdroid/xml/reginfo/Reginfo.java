package org.doubango.imsdroid.xml.reginfo;

import java.math.BigInteger;
import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "reginfo")
@Namespace(reference="urn:ietf:params:xml:ns:reginfo")
public class Reginfo {
	
	@ElementList(inline=true, required=false)
    protected ArrayList<Registration> registration;
    @Attribute(required = true)
    protected BigInteger version;
    @Attribute(required = true)
    protected String state;
    
    public ArrayList<Registration> getRegistration(){
    	return this.registration;
    }
    
    public BigInteger getVersion(){
    	return this.version;
    }
    
    public String getState(){
    	return this.state;
    }
}
