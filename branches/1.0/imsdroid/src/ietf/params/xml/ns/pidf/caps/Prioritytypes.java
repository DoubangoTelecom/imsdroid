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
package ietf.params.xml.ns.pidf.caps;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root(name = "prioritytypes", strict=false)
@Namespace(reference = "urn:ietf:params:xml:ns:pidf:caps")
@Default(DefaultType.FIELD)
public class Prioritytypes {

    protected List<Equalstype> equals;
    protected List<Higherthantype> higherhan;
    protected List<Lowerthantype> lowerthan;
    protected List<Rangetype> range;
   
    public List<Equalstype> getEquals() {
        if (equals == null) {
            equals = new ArrayList<Equalstype>();
        }
        return this.equals;
    }
    
    public List<Higherthantype> getHigherhan() {
        if (higherhan == null) {
            higherhan = new ArrayList<Higherthantype>();
        }
        return this.higherhan;
    }
   
    public List<Lowerthantype> getLowerthan() {
        if (lowerthan == null) {
            lowerthan = new ArrayList<Lowerthantype>();
        }
        return this.lowerthan;
    }
   
    public List<Rangetype> getRange() {
        if (range == null) {
            range = new ArrayList<Rangetype>();
        }
        return this.range;
    }   
}
