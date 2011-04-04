package org.doubango.ngn.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.doubango.ngn.services.impl.NgnContactService;

import android.util.Log;

public class NgnTimer extends Timer{
	private final static String TAG = NgnTimer.class.getCanonicalName();
	
	public NgnTimer(){
		super();
	}

	@Override
	public void cancel() {
		try{
			super.cancel();
		}
		catch(IllegalStateException ise){
			Log.w(TAG, ise.toString());
		}
	}

	@Override
	public void schedule(TimerTask task, Date when, long period) {
		try{
			super.schedule(task, when, period);
		}
		catch(IllegalStateException ise){
			Log.w(TAG, ise.toString());
		}
	}

	@Override
	public void schedule(TimerTask task, Date when) {
		try{
			super.schedule(task, when);
		}
		catch(IllegalStateException ise){
			Log.w(TAG, ise.toString());
		}
	}

	@Override
	public void schedule(TimerTask task, long delay, long period) {
		try{
			super.schedule(task, delay, period);
		}
		catch(IllegalStateException ise){
			Log.w(TAG, ise.toString());
		}
	}

	@Override
	public void schedule(TimerTask task, long delay) {
		try{
			super.schedule(task, delay);
		}
		catch(IllegalStateException ise){
			Log.w(TAG, ise.toString());
		}
	}
}
