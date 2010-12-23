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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

public class ObservableHashMap<K, V>  extends Observable implements Observer{
	private final HashMap<K, V> hashMap;
	private final boolean watchValueChanged;
	
	public ObservableHashMap(boolean watchValueChanged){
		this.hashMap = new HashMap<K, V>();
		if((this.watchValueChanged = watchValueChanged)){
			
		}
	}
	
	public V put(K key, V value){
		if(value == null){
			return null;
		}
		V result = this.hashMap.put(key, value);
		if(this.watchValueChanged && value instanceof Observable){
			((Observable)value).addObserver(this);
		}
		this.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public V get(K key){
		return this.hashMap.get(key);
	}
	
	public V getAt(int position){
		int index = 0;
		for(Map.Entry<K, V> entry : this.hashMap.entrySet()) {
			if(index == position){
				return entry.getValue();
			}
		    index++;
		}
		return null;
	}
	
	public Collection<V> values(){
		return this.hashMap.values();
	}
	
	public V remove(K key){
		V result = this.hashMap.remove(key);
		if(this.watchValueChanged && result != null && result instanceof Observable){
			((Observable)result).deleteObserver(this);
		}
		this.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public boolean isEmpty(){
		return this.hashMap.isEmpty();
	}
	
	public boolean containsKey(K key){
		return this.hashMap.containsKey(key);
	}
	
	public Set<Entry<K, V>> entrySet(){
		return this.hashMap.entrySet();
	}
	
	public int size(){
		return this.hashMap.size();
	}

	private void setChangedAndNotifyObservers(Object data){
		super.setChanged();
		super.notifyObservers(data);
	}
	
	@Override
	public void update(Observable observable, Object data) {
		this.setChangedAndNotifyObservers(data);
	}
}
