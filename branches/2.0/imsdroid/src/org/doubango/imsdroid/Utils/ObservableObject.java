package org.doubango.imsdroid.Utils;

import java.util.Observable;

public abstract class ObservableObject extends Observable{
	protected void setChangedAndNotifyObservers(Object data){
		super.setChanged();
		super.notifyObservers(data);
	}
}
