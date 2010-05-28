package org.doubango.imsdroid.events;

public class RegistrationEventArgs extends EventArgs {
	
	private final RegistrationEventTypes type;
	private final short sipCode;
	private final String phrase;
	
	public RegistrationEventArgs(RegistrationEventTypes type, short sipCode, String phrase)
	{
		super();
		
		this.type = type;
		this.sipCode = sipCode;
		this.phrase = phrase;
	}
	
	public RegistrationEventTypes getType()
	{
		return this.type;
	}
	
	public short getSipCode()
	{
		return this.sipCode;
	}
	
	public String getPhrase()
	{
		return this.phrase;
	}
}
