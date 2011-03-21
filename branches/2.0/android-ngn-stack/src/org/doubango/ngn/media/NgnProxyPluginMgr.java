package org.doubango.ngn.media;

import java.math.BigInteger;
import java.util.Hashtable;

import org.doubango.tinyWRAP.ProxyAudioConsumer;
import org.doubango.tinyWRAP.ProxyAudioProducer;
import org.doubango.tinyWRAP.ProxyPluginMgr;
import org.doubango.tinyWRAP.ProxyPluginMgrCallback;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.tmedia_chroma_t;
import org.doubango.tinyWRAP.twrap_proxy_plugin_type_t;

import android.util.Log;

/***
 * MyProxyPluginMgr
 */
public class NgnProxyPluginMgr {
	private static final String TAG = NgnProxyPluginMgr.class.getCanonicalName();
	private static final MyProxyPluginMgrCallback sMyProxyPluginMgrCallback  = new MyProxyPluginMgrCallback();
	private static final ProxyPluginMgr sPluginMgr = ProxyPluginMgr.createInstance(sMyProxyPluginMgrCallback);
	private static final Hashtable<BigInteger, NgnProxyPlugin>sPlugins = new Hashtable<BigInteger, NgnProxyPlugin>(); // HashTable is synchronized
	
	public static void Initialize() {
        ProxyVideoConsumer.setDefaultChroma(tmedia_chroma_t.tmedia_rgb565le);
        ProxyVideoProducer.setDefaultChroma(tmedia_chroma_t.tmedia_nv21);
	}
	
	private NgnProxyPluginMgr(){
		
	}
	
	public static NgnProxyPlugin findPlugin(BigInteger id){
		return sPlugins.get(id);
	}
	
	/**
	 * MyProxyPluginMgrCallback
	 */
	static class MyProxyPluginMgrCallback extends ProxyPluginMgrCallback
	{
		MyProxyPluginMgrCallback(){
			super();
		}

		@Override
		public int OnPluginCreated(BigInteger id, twrap_proxy_plugin_type_t type) {
			Log.d(TAG, "OnPluginCreated("+id+","+ type+")");
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				{	
					synchronized(this){
						ProxyAudioProducer producer = sPluginMgr.findAudioProducer(id);
						if(producer != null){
							NgnProxyAudioProducer myProducer = new NgnProxyAudioProducer(id, producer);
							sPlugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_producer:
				{
					synchronized(this){
						ProxyVideoProducer producer = sPluginMgr.findVideoProducer(id);
						if(producer != null){
							NgnProxyVideoProducer myProducer = new NgnProxyVideoProducer(id, producer);
							sPlugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_audio_consumer:
				{
					synchronized(this){
						ProxyAudioConsumer consumer = sPluginMgr.findAudioConsumer(id);
						if(consumer != null){
							NgnProxyAudioConsumer myConsumer = new NgnProxyAudioConsumer(id, consumer);
							sPlugins.put(id, myConsumer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						ProxyVideoConsumer consumer = sPluginMgr.findVideoConsumer(id);
						if(consumer != null){
							NgnProxyVideoConsumer myConsumer = new NgnProxyVideoConsumer(id, consumer);
							sPlugins.put(id, myConsumer);
						}
					}
					break;
				}
				default:
				{
					Log.e(TAG, "Invalid Plugin type");
					return -1;
				}
			}
			return 0;
		}

		@Override
		public int OnPluginDestroyed(BigInteger id, twrap_proxy_plugin_type_t type) {
			Log.d(TAG, "OnPluginDestroyed("+id+","+ type+")");
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				case twrap_proxy_plugin_video_producer:
				case twrap_proxy_plugin_audio_consumer:
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						NgnProxyPlugin plugin = sPlugins.get(id);
						if(plugin != null){
							plugin.invalidate();
							sPlugins.remove(id);
							return 0;
						}
						else{
							Log.e(TAG, "Failed to find plugin");
							return -1;
						}
					}
				}
				default:
				{
					Log.e(TAG, "Invalid Plugin type");
					return -1;
				}
			}
		}
	}
}
