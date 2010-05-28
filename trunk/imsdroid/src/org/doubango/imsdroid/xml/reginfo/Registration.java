/*	Mozilla Public License Version 1.1
 * 
 * Copyright (C) 2010 Mamadou Diop.
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/

 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.

 * The Original Code is imsdroid v1.0.0.

 * The Initial Developers of the Original Code is Mamadou Diop <diopmamadou (at) doubango.org>

 * Portions created by ., ., . and .; 

 * All Rights Reserved.

 * Contributor(s): 
 * Mamadou Diop <diopmamadou (at) doubango.org>
 * ... add your name and email here
 */
package org.doubango.imsdroid.xml.reginfo;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "registration")
public class Registration {

	@ElementList(required = false, inline=true)
    protected List<Contact> contact;
    @Attribute(required = true)
    protected String aor;
    @Attribute(required = true)
    protected String id;
    @Attribute(required = true)
    protected String state;
    
    public List<Contact> getContact() {
        return this.contact;
    }
    
    public String getAor() {
        return aor;
    }
    
    public String getId() {
        return id;
    }
    
    public String getState() {
        return state;
    }
}
