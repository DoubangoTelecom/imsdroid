/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*
*/

package ietf.params.xml.ns.pidf.rpid;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;


@Root(name = "activities", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
/* @Default(DefaultType.FIELD) => see appointmentOrAwayOrBreakfast*/
public class Activities {
	
	@Element(required=false)
    protected List<NoteT> note;
	@Element(required=false)
    protected Empty unknown;
    @Attribute(required=false)
    protected String id;
    @Attribute(required=false)
    protected String from; // FIXME:date
    @Attribute(required=false)
    protected String until; // FIXME:date
    
    protected final List<activity> appointmentOrAwayOrBreakfast;
    
    @Element(name="worship", required=false)
    private worship worship;
    @Element(name="worslooking-for-workhip", required=false)
    private looking_for_work looking_for_work;
    @Element(name="meal", required=false)
    private meal meal;
    @Element(name="travel", required=false)
    private travel travel;
    @Element(name="steering", required=false)
    private steering steering;
    @Element(name="away", required=false)
    private away away;
    @Element(name="in-transit", required=false)
    private in_transit in_transit;
    @Element(name="spectator", required=false)
    private spectator spectator;
    @Element(name="breakfast", required=false)
    private breakfast breakfast;
    @Element(name="working", required=false)
    private working working;
    @Element(name="on-the-phone", required=false)
    private on_the_phone on_the_phone;
    @Element(name="permanent-absence", required=false)
    private permanent_absence permanent_absence;
    @Element(name="holiday", required=false)
    private holiday holiday;
    @Element(name="dinner", required=false)
    private dinner dinner;
    @Element(name="busy", required=false)
    private busy busy;
    @Element(name="playing", required=false)
    private playing playing;
    @Element(name="other", required=false)
    private other other;
    @Element(name="tv", required=false)
    private tv tv;
    @Element(name="vacation", required=false)
    private vacation vacation;
    @Element(name="performance", required=false)
    private performance performance;
    @Element(name="sleeping", required=false)
    private sleeping sleeping;
    @Element(name="appointment", required=false)
    private appointment appointment;
    @Element(name="presentation", required=false)
    private presentation presentation;
    @Element(name="meeting", required=false)
    private meeting meeting;
    @Element(name="shopping", required=false)
    private shopping shopping;
   
    public Activities(){
    	this.appointmentOrAwayOrBreakfast = new ArrayList<activity>();
    }
    
    public List<NoteT> getNote() {
        if (note == null) {
            note = new ArrayList<NoteT>();
        }
        return this.note;
    }
  
    public Empty getUnknown() {
        return unknown;
    }
    
    public void setUnknown(Empty value) {
        this.unknown = value;
    }
   
    public List<activity> getAppointmentOrAwayOrBreakfast() {
        return this.appointmentOrAwayOrBreakfast;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
    
    public String getFrom() {
        return from;
    }
   
    public void setFrom(String value) {
        this.from = value;
    }
    
    public String getUntil() {
        return until;
    }
    
    public void setUntil(String value) {
        this.until = value;
    }
    
    @Commit
	public void commit(){
    	if(this.worship != null){ this.appointmentOrAwayOrBreakfast.add(this.worship); }
    	if(this.looking_for_work != null){ this.appointmentOrAwayOrBreakfast.add(this.looking_for_work); }
    	if(this.meal != null){ this.appointmentOrAwayOrBreakfast.add(this.travel); }
    	if(this.travel != null){ this.appointmentOrAwayOrBreakfast.add(this.worship); }
    	if(this.steering != null){ this.appointmentOrAwayOrBreakfast.add(this.steering); }
    	if(this.away != null){ this.appointmentOrAwayOrBreakfast.add(this.away); }
    	if(this.in_transit != null){ this.appointmentOrAwayOrBreakfast.add(this.in_transit); }
    	if(this.spectator != null){ this.appointmentOrAwayOrBreakfast.add(this.spectator); }
    	if(this.breakfast != null){ this.appointmentOrAwayOrBreakfast.add(this.breakfast); }
    	if(this.working != null){ this.appointmentOrAwayOrBreakfast.add(this.working); }
    	if(this.on_the_phone != null){ this.appointmentOrAwayOrBreakfast.add(this.on_the_phone); }
    	if(this.permanent_absence != null){ this.appointmentOrAwayOrBreakfast.add(this.permanent_absence); }
    	if(this.holiday != null){ this.appointmentOrAwayOrBreakfast.add(this.holiday); }
    	if(this.dinner != null){ this.appointmentOrAwayOrBreakfast.add(this.dinner); }
    	if(this.busy != null){ this.appointmentOrAwayOrBreakfast.add(this.busy); }
    	if(this.playing != null){ this.appointmentOrAwayOrBreakfast.add(this.playing); }
    	if(this.other != null){ this.appointmentOrAwayOrBreakfast.add(this.other); }
    	if(this.tv != null){ this.appointmentOrAwayOrBreakfast.add(this.tv); }
    	if(this.vacation != null){ this.appointmentOrAwayOrBreakfast.add(this.vacation); }
    	if(this.performance != null){ this.appointmentOrAwayOrBreakfast.add(this.performance); }
    	if(this.sleeping != null){ this.appointmentOrAwayOrBreakfast.add(this.sleeping); }
    	if(this.appointment != null){ this.appointmentOrAwayOrBreakfast.add(this.appointment); }
    	if(this.presentation != null){ this.appointmentOrAwayOrBreakfast.add(this.presentation); }
    	if(this.meeting != null){ this.appointmentOrAwayOrBreakfast.add(this.meeting); }
    	if(this.shopping != null){ this.appointmentOrAwayOrBreakfast.add(this.shopping); }
	}
    
    public enum ACTIVITY_TYPE{
    	worship,
    	looking_for_work,
    	meal,
    	travel,
    	steering,
    	away,
    	in_transit,
    	spectator,
    	breakfast,
    	working,
    	on_the_phone,
    	permanent_absence,
    	holiday,
    	dinner,
    	busy,
    	playing,
    	other,
    	tv,
    	vacation,
    	performance,
    	sleeping,
    	appointment,
    	presentation,
    	meeting,
    	shopping
    }
    
    
    public static abstract class activity{
    	private ACTIVITY_TYPE type;
    	protected activity(ACTIVITY_TYPE type){
    		this.type = type;
    	}
    	
    	public ACTIVITY_TYPE getType(){
    		return this.type;
    	}
    }
    
    @Root(name = "worship", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class worship extends activity{
    	public worship(){
    		super(ACTIVITY_TYPE.worship);
    	}
    }
    
    @Root(name = "looking-for-work", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class looking_for_work extends activity{
    	public looking_for_work(){
    		super(ACTIVITY_TYPE.looking_for_work);
    	}
    }
    
    @Root(name = "meal", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class meal extends activity{
    	public meal(){
    		super(ACTIVITY_TYPE.meal);
    	}
    }
    
    @Root(name = "travel", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class travel extends activity{
    	public travel(){
    		super(ACTIVITY_TYPE.travel);
    	}
    }
    
    @Root(name = "steering", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class steering extends activity{
    	public steering(){
    		super(ACTIVITY_TYPE.travel);
    	}
    }
    
    @Root(name = "away", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class away extends activity{
    	public away(){
    		super(ACTIVITY_TYPE.away);
    	}
    }
    
    @Root(name = "in-transit", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class in_transit extends activity{
    	public in_transit(){
    		super(ACTIVITY_TYPE.in_transit);
    	}
    }
    
    @Root(name = "spectator", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class spectator extends activity{
    	public spectator(){
    		super(ACTIVITY_TYPE.spectator);
    	}
    }
    
    @Root(name = "breakfast", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class breakfast extends activity{
    	public breakfast(){
    		super(ACTIVITY_TYPE.breakfast);
    	}
    }
    
    @Root(name = "working", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class working extends activity{
    	public working(){
    		super(ACTIVITY_TYPE.working);
    	}
    }
    
    @Root(name = "on-the-phone", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class on_the_phone extends activity{
    	public on_the_phone(){
    		super(ACTIVITY_TYPE.on_the_phone);
    	}
    }
    
    @Root(name = "permanent-absence", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class permanent_absence extends activity{
    	public permanent_absence(){
    		super(ACTIVITY_TYPE.permanent_absence);
    	}
    }
    
    @Root(name = "holiday", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class holiday extends activity{
    	public holiday(){
    		super(ACTIVITY_TYPE.holiday);
    	}
    }
    
    @Root(name = "dinner", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class dinner extends activity{
    	public dinner(){
    		super(ACTIVITY_TYPE.dinner);
    	}
    }
    
    @Root(name = "busy", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class busy extends activity{
    	public busy(){
    		super(ACTIVITY_TYPE.busy);
    	}
    }
    
    @Root(name = "playing", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class playing extends activity{
    	public playing(){
    		super(ACTIVITY_TYPE.playing);
    	}
    }
    
    @Root(name = "other", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class other extends activity{
    	public other(){
    		super(ACTIVITY_TYPE.other);
    	}
    }
    
    @Root(name = "tv", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class tv extends activity{
    	public tv(){
    		super(ACTIVITY_TYPE.tv);
    	}
    }
    
    @Root(name = "vacation", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class vacation extends activity{
    	public vacation(){
    		super(ACTIVITY_TYPE.vacation);
    	}
    }
    
    @Root(name = "performance", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class performance extends activity{
    	public performance(){
    		super(ACTIVITY_TYPE.performance);
    	}
    }
    
    @Root(name = "sleeping", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class sleeping extends activity{
    	public sleeping(){
    		super(ACTIVITY_TYPE.sleeping);
    	}
    }
    
    @Root(name = "appointment", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class appointment extends activity{
    	public appointment(){
    		super(ACTIVITY_TYPE.appointment);
    	}
    }
    
    @Root(name = "presentation", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class presentation extends activity{
    	public presentation(){
    		super(ACTIVITY_TYPE.presentation);
    	}
    }
    
    @Root(name = "meeting", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class meeting extends activity{
    	public meeting(){
    		super(ACTIVITY_TYPE.meeting);
    	}
    }
    
    @Root(name = "shopping", strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
    public static class shopping extends activity{
    	public shopping(){
    		super(ACTIVITY_TYPE.shopping);
    	}
    }
}
