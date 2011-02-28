package org.doubango.imsdroid.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

public class ObservableHashMap<K, V>  extends ObservableObject implements Observer{
	private final HashMap<K, V> mHashMap;
	private final boolean mWatchValueChanged;
	
	public ObservableHashMap(boolean watchValueChanged){
		super();
		mHashMap = new HashMap<K, V>();
		if((mWatchValueChanged = watchValueChanged)){
			
		}
	}
	
	public V put(K key, V value){
		if(value == null){
			return null;
		}
		V result = mHashMap.put(key, value);
		if(mWatchValueChanged && value instanceof Observable){
			((Observable)value).addObserver(this);
		}
		super.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public V get(K key){
		return mHashMap.get(key);
	}
	
	public V getAt(int position){
		int index = 0;
		for(Map.Entry<K, V> entry : mHashMap.entrySet()) {
			if(index == position){
				return entry.getValue();
			}
		    index++;
		}
		return null;
	}
	
	public Collection<V> values(){
		return this.mHashMap.values();
	}
	
	public V remove(K key){
		V result = this.mHashMap.remove(key);
		if(this.mWatchValueChanged && result != null && result instanceof Observable){
			((Observable)result).deleteObserver(this);
		}
		super.setChangedAndNotifyObservers(result);
		return result;
	}
	
	public boolean isEmpty(){
		return this.mHashMap.isEmpty();
	}
	
	public boolean containsKey(K key){
		return this.mHashMap.containsKey(key);
	}
	
	public Set<Entry<K, V>> entrySet(){
		return this.mHashMap.entrySet();
	}
	
	public int size(){
		return this.mHashMap.size();
	}
	
	@Override
	public void update(Observable observable, Object data) {
		super.setChangedAndNotifyObservers(data);
	}
}
