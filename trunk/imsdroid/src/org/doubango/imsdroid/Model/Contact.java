package org.doubango.imsdroid.Model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="contact")
public class Contact {
	
	@Element(name="uri")
	private String uriString;
	
	public Contact(){
		this.uriString = "sip:bob@open-ims.test";
	}
}
