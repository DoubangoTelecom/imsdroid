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
package oma.xml.prs.pidf.oma_pres;

import java.text.ParseException;
import java.util.Date;

import org.doubango.imsdroid.utils.RFC3339Date;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;


@Root(name = "overriding-willingness", strict=false)
@Namespace(reference = "urn:oma:xml:prs:pidf:oma-pres")
/* @Default(DefaultType.FIELD) */
public class OverridingWillingness {

	@Element(required=false)
    protected BasicType basic;
    
    public BasicType getBasic() {
        return basic;
    }
    
    public void setBasic(BasicType value) {
        this.basic = value;
    }
    
    /* ====================== Extension =========================*/
    @Attribute(name="until", required=false)
    protected String _until;
    
    private Date until;
    
    @Commit
    public void commit() {
    	if(this._until != null){
    		try {
    			until = RFC3339Date.parseRFC3339Date(this._until);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			_until = null;
    	}
    }
    
    public Date getUntil(){
    	return this.until;
    }
}
