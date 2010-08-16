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
package org.doubango.imsdroid.Model;

import org.doubango.imsdroid.media.MediaType;
import org.simpleframework.xml.Element;

public class HistoryMsrpEvent extends HistoryEvent{

	@Element(required = false)
	protected String filePath;
	
	HistoryMsrpEvent(){
		this(null, false);
	}
	
	public HistoryMsrpEvent(String remoteParty, boolean fileTransfer) {
		super(fileTransfer?MediaType.FileTransfer:MediaType.Chat, remoteParty);
	}
	
	public void setFilePath(String filePath){
		this.filePath = filePath;
	}
	
	public String getFilePath(){
		return this.filePath;
	}
}
