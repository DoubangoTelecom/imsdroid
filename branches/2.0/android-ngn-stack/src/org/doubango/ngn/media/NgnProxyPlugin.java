package org.doubango.ngn.media;

import java.math.BigInteger;

import org.doubango.tinyWRAP.ProxyPlugin;

/**
 * MyProxyPlugin
 */
public class NgnProxyPlugin implements Comparable<NgnProxyPlugin>{
	
	protected boolean mValid;
	protected boolean mStarted;
	protected boolean mPaused;
	protected boolean mPrepared;
	protected final BigInteger mId;
	protected final ProxyPlugin mPlugin;
	
	public NgnProxyPlugin(BigInteger id, ProxyPlugin plugin){
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
	public int compareTo(NgnProxyPlugin another) {
		return (mId.intValue() - another.mId.intValue());
	}
}
