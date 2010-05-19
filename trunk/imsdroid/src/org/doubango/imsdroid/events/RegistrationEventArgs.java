package org.doubango.imsdroid.events;

public class RegistrationEventArgs extends EventArgs {
	
	private final RegistrationEventTypes type;
	private final short SipCode;
	private final String phrase;
	
	public RegistrationEventArgs(RegistrationEventTypes type, short SipCode, String phrase)
	{
		super();
		
		this.type = type;
		this.SipCode = SipCode;
		this.phrase = phrase;
	}
	
	public RegistrationEventTypes getType()
	{
		return this.type;
	}
	
	public short getSipCode()
	{
		return this.SipCode;
	}
	
	public String getPhrase()
	{
		return this.phrase;
	}
}
