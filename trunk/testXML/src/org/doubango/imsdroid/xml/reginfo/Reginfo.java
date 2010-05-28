package org.doubango.imsdroid.xml.reginfo;

import java.math.BigInteger;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "reginfo")
@Namespace(reference="urn:ietf:params:xml:ns:reginfo")
public class Reginfo {
	
	@ElementList(inline=true, required=false)
    protected List<Registration> registration;
    @Attribute(required = true)
    protected BigInteger version;
    @Attribute(required = true)
    protected String state;
}
