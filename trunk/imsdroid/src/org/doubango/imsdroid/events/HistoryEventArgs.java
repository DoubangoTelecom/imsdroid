package org.doubango.imsdroid.events;

public class HistoryEventArgs {
	private final HistoryEventTypes type;
	private final int index;
	
	public HistoryEventArgs(HistoryEventTypes type, int index){
		this.type = type;
		this.index = index;
	}
	
	public HistoryEventTypes getType(){
		return this.type;
	}
	
	public int getIndex(){
		return this.index;
	}
}