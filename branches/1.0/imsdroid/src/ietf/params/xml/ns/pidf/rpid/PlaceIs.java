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
import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;


@Root(name = "place-is", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
@Default(DefaultType.FIELD)
public class PlaceIs {

    protected List<NoteT> note;
    protected PlaceIs.Audio audio;
    protected PlaceIs.Video video;
    protected PlaceIs.Text text;
    @Attribute
    protected String id;
    @Attribute
    protected String from; //FIXME: datetime
    @Attribute
    protected String until; //FIXME: datetime
    
    public List<NoteT> getNote() {
        if (note == null) {
            note = new ArrayList<NoteT>();
        }
        return this.note;
    }
   
    public PlaceIs.Audio getAudio() {
        return audio;
    }

    public void setAudio(PlaceIs.Audio value) {
        this.audio = value;
    }
    
    public PlaceIs.Video getVideo() {
        return video;
    }
   
    public void setVideo(PlaceIs.Video value) {
        this.video = value;
    }
   
    public PlaceIs.Text getText() {
        return text;
    }
    
    public void setText(PlaceIs.Text value) {
        this.text = value;
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
    
    
    @Root(strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
	@Default(DefaultType.FIELD)
    public static class Audio {

        protected Empty noisy;
        protected Empty ok;
        protected Empty quiet;
        protected Empty unknown;
        
        public Empty getNoisy() {
            return noisy;
        }

        public void setNoisy(Empty value) {
            this.noisy = value;
        }
       
        public Empty getOk() {
            return ok;
        }
        
        public void setOk(Empty value) {
            this.ok = value;
        }
       
        public Empty getQuiet() {
            return quiet;
        }
       
        public void setQuiet(Empty value) {
            this.quiet = value;
        }
      
        public Empty getUnknown() {
            return unknown;
        }
       
        public void setUnknown(Empty value) {
            this.unknown = value;
        }
    }

    
    @Root(strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
	@Default(DefaultType.FIELD)
    public static class Text {

        protected Empty uncomfortable;
        protected Empty inappropriate;
        protected Empty ok;
        protected Empty unknown;
       
        public Empty getUncomfortable() {
            return uncomfortable;
        }
        
        public void setUncomfortable(Empty value) {
            this.uncomfortable = value;
        }
        
        public Empty getInappropriate() {
            return inappropriate;
        }
       
        public void setInappropriate(Empty value) {
            this.inappropriate = value;
        }
       
        public Empty getOk() {
            return ok;
        }
        
        public void setOk(Empty value) {
            this.ok = value;
        }
       
        public Empty getUnknown() {
            return unknown;
        }
       
        public void setUnknown(Empty value) {
            this.unknown = value;
        }

    }
    
    @Root(strict=false)
    @Namespace(reference = "urn:ietf:params:xml:ns:pidf:rpid")
	@Default(DefaultType.FIELD)
    public static class Video {

        protected Empty toobright;
        protected Empty ok;
        protected Empty dark;
        protected Empty unknown;
        
        public Empty getToobright() {
            return toobright;
        }

        public void setToobright(Empty value) {
            this.toobright = value;
        }
        
        public Empty getOk() {
            return ok;
        }
        
        public void setOk(Empty value) {
            this.ok = value;
        }
        
        public Empty getDark() {
            return dark;
        }
       
        public void setDark(Empty value) {
            this.dark = value;
        }
       
        public Empty getUnknown() {
            return unknown;
        }
       
        public void setUnknown(Empty value) {
            this.unknown = value;
        }
    }
}
