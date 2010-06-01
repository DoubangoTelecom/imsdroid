package org.doubango.imsdroid.Model;

import org.doubango.imsdroid.sip.PresenceStatus;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import android.graphics.Bitmap;

@Root(name = "contact")
public class Contact implements Comparable<Contact> {
	
	@Attribute(name = "uri", required = true)
	private String uri;
	@Element(name = "group", required = false)
	private String group;
	@Element(name = "displayName", required = false)
	private String displayName;
	@Element(name = "firstName", required = false)
	private String firstName;
	@Element(name = "lastName", required = false)
	private String lastName;
	@Element(name = "phoneNumber", required = false)
	private String phoneNumber;
	@Element(name = "freeText", required = false)
	private String freeText;
	
	private PresenceStatus status;
	private Bitmap avatarImage;
	
	public Contact(){
		
	}
	
	public String getUri(){
		return this.uri;
	}
	
	public void setUri(String  uri){
		this.uri = uri;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}
	
	public String getGroup(){
		return this.group;
	}
	
	public void setGroup(String group){
		this.group = group;
	}
	
	public String getFirstName(){
		return this.firstName;
	}
	
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public String getLastName(){
		return this.lastName;
	}
	
	public void setPhoneNumber(String phoneNumber){
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber(){
		return this.phoneNumber;
	}
	
	public void setFreeText(String freeText){
		this.freeText = freeText;
	}
	
	public String getFreeText(){
		return this.freeText;
	}
	
	public void setStatus(PresenceStatus status){
		this.status = status;
	}
	
	public PresenceStatus getStatus(){
		return this.status;
	}
	
	public Bitmap getAvatar(){
		return this.avatarImage;
	}
	
	public void setAvatar(String base64String){
		this.avatarImage = null;
	}
	
	public int compareTo(Contact another) {
		return this.uri.compareTo(another.uri);
	}
}
