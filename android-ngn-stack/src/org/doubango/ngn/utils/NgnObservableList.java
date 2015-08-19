/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
*/
package org.doubango.ngn.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NgnObservableList<T> extends NgnObservableObject implements Observer {
	private final List<T> mList;
	private boolean mWatchValueChanged;
	
	public NgnObservableList(boolean watchValueChanged){
		super();
		mList = new ArrayList<T>();
		if((mWatchValueChanged = watchValueChanged)){
			
		}
	}
	
	public NgnObservableList(){
		this(false);
	}
	
	public List<T> getList(){
		return mList;
	}
	
	public List<T> filter(NgnPredicate<T> predicate) {
		return NgnListUtils.filter(mList, predicate);
	}
	
	public boolean add(T object){
		int location = mList.size();
		this.add(location, object);
		return true;
	}
	
	// FIXME: refactor
	public void add(T objects[]){
		for(T object : objects){
			mList.add(object);
			if(mWatchValueChanged && object instanceof Observable){
				((Observable)object).addObserver(this);
			}
		}
		super.setChangedAndNotifyObservers(null);
	}
	
	// FIXME: refactor
	public void add(Collection<T> list){
		for(T object : list){
			mList.add(object);
			if(mWatchValueChanged && object instanceof Observable){
				((Observable)object).addObserver(this);
			}
		}
		super.setChangedAndNotifyObservers(null);
	}
	
	public void add(int location, T object){
		mList.add(location, object);
		if(mWatchValueChanged && object instanceof Observable){
			((Observable)object).addObserver(this);
		}
		super.setChangedAndNotifyObservers(null);
	}
	
	public T remove(int location){
		T result = mList.remove(location);
		if(result != null && result instanceof Observable){
			((Observable)result).deleteObserver(this);
		}
		super.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public boolean remove(T object){
		if(object == null){
			return false;
		}
		boolean result = mList.remove(object);
		if(result && object instanceof Observable){
			((Observable)object).deleteObserver(this);
		}
		super.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public boolean removeAll(Collection<T> objects){
		if(objects == null){
			return false;
		}
		for(T object : objects){
			if(object instanceof Observable){
				((Observable)object).deleteObserver(this);
			}
		}
		boolean result = mList.removeAll(objects);
		super.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public void clear(){
		for(T object : mList){
			if(object instanceof Observable){
				((Observable)object).deleteObserver(this);
			}
		}
		mList.clear();
		super.setChangedAndNotifyObservers(null);
	}
	
	public void setWatchValueChanged(boolean watchValueChanged){
		mWatchValueChanged = watchValueChanged;
	}

	@Override
	public void update(Observable observable, Object data) {
		super.setChangedAndNotifyObservers(data);
	}
}
