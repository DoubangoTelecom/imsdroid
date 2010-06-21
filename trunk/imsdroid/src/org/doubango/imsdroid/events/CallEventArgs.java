package org.doubango.imsdroid.events;

import java.util.HashMap;

public class CallEventArgs extends EventArgs {
	
	private final CallEventTypes type;
	private final long id;
	private final String phrase;
	private final HashMap<String, Object> extra;
	
	public CallEventArgs(long id, CallEventTypes type, String phrase){
		super();
		
		this.type = type;
		this.id = id;
		this.phrase = phrase;
		
		this.extra = new HashMap<String, Object>();
	}
	
	public CallEventTypes getType(){
		return this.type;
	}
	
	public long getSessionId(){
		return this.id;
	}
	
	public String getPhrase(){
		return this.phrase;
	}
	
	public Object getExtra(String key){
		return this.extra.get(key);
	}
	
	public void putExtra(String key, Object value){
		this.extra.put(key, value);
	}
}
