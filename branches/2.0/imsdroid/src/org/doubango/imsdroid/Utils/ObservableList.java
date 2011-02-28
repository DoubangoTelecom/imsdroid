package org.doubango.imsdroid.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ObservableList<T> extends ObservableObject implements Observer {
	private final List<T> mList;
	private boolean mWatchValueChanged;
	
	public ObservableList(boolean watchValueChanged){
		super();
		mList = new ArrayList<T>();
		if((mWatchValueChanged = watchValueChanged)){
			
		}
	}
	
	public ObservableList(){
		this(false);
	}
	
	public List<T> getList(){
		return mList;
	}
	
	public List<T> filter(Predicate<T> predicate) {
		return ListUtils.filter(mList, predicate);
	}
	
	public boolean add(T object){
		int location = mList.size();
		this.add(location, object);
		return true;
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
