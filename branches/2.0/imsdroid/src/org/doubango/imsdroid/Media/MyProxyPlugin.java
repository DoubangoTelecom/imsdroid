package org.doubango.imsdroid.Media;

import java.math.BigInteger;

import org.doubango.tinyWRAP.ProxyPlugin;

/**
 * MyProxyPlugin
 */
public class MyProxyPlugin implements Comparable<MyProxyPlugin>{
	
	protected boolean mValid;
	protected boolean mStarted;
	protected boolean mPaused;
	protected boolean mPrepared;
	protected final BigInteger mId;
	protected final ProxyPlugin mPlugin;
	
	public MyProxyPlugin(BigInteger id, ProxyPlugin plugin){
		mId = id;
		mPlugin = plugin;
		mValid = true;
	}
	
	public boolean isValid(){
		return mValid;
	}
	
	public boolean isStarted(){
		return mStarted;
	}
	
	public boolean isPaused(){
		return mPaused;
	}
	
	public boolean isPrepared(){
		return mPrepared;
	}
	
	public void invalidate(){
		mValid = false;
	}
	
	@Override
	public int compareTo(MyProxyPlugin another) {
		return (mId.intValue() - another.mId.intValue());
	}
}
