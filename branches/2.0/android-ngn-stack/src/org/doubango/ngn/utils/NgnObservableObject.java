package org.doubango.ngn.utils;

import java.util.Observable;

public abstract class NgnObservableObject extends Observable{
	protected void setChangedAndNotifyObservers(Object data){
		super.setChanged();
		super.notifyObservers(data);
	}
}
