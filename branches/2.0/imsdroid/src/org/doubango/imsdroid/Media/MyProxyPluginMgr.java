package org.doubango.imsdroid.Media;

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
public class MyProxyPluginMgr {
	private static final String TAG = MyProxyPluginMgr.class.getCanonicalName();
	private static final MyProxyPluginMgrCallback myProxyPluginMgrCallback  = new MyProxyPluginMgrCallback();
	private static final ProxyPluginMgr pluginMgr = ProxyPluginMgr.createInstance(MyProxyPluginMgr.myProxyPluginMgrCallback);
	private static final Hashtable<BigInteger, MyProxyPlugin>plugins = new Hashtable<BigInteger, MyProxyPlugin>(); // HashTable is synchronized
	
	public static void Initialize() {
        ProxyVideoConsumer.setDefaultChroma(tmedia_chroma_t.tmedia_rgb565le);
        ProxyVideoProducer.setDefaultChroma(tmedia_chroma_t.tmedia_nv21);
	}
	
	private MyProxyPluginMgr(){
		
	}
	
	public static MyProxyPlugin findPlugin(BigInteger id){
		return MyProxyPluginMgr.plugins.get(id);
	}
	
	/**
	 * MyProxyPluginMgrCallback
	 * @author Mamadou Diop
	 *
	 */
	static class MyProxyPluginMgrCallback extends ProxyPluginMgrCallback
	{
		MyProxyPluginMgrCallback(){
			super();
		}

		@Override
		public int OnPluginCreated(BigInteger id, twrap_proxy_plugin_type_t type) {
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				{	
					synchronized(this){
						ProxyAudioProducer producer = MyProxyPluginMgr.pluginMgr.findAudioProducer(id);
						if(producer != null){
							MyProxyAudioProducer myProducer = new MyProxyAudioProducer(id, producer);
							MyProxyPluginMgr.plugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_producer:
				{
					synchronized(this){
						ProxyVideoProducer producer = MyProxyPluginMgr.pluginMgr.findVideoProducer(id);
						if(producer != null){
							MyProxyVideoProducer myProducer = new MyProxyVideoProducer(id, producer);
							MyProxyPluginMgr.plugins.put(id, myProducer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_audio_consumer:
				{
					synchronized(this){
						ProxyAudioConsumer consumer = MyProxyPluginMgr.pluginMgr.findAudioConsumer(id);
						if(consumer != null){
							MyProxyAudioConsumer myConsumer = new MyProxyAudioConsumer(id, consumer);
							MyProxyPluginMgr.plugins.put(id, myConsumer);
						}
					}
					break;
				}
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						ProxyVideoConsumer consumer = MyProxyPluginMgr.pluginMgr.findVideoConsumer(id);
						if(consumer != null){
							MyProxyVideoConsumer myConsumer = new MyProxyVideoConsumer(id, consumer);
							MyProxyPluginMgr.plugins.put(id, myConsumer);
						}
					}
					break;
				}
				default:
				{
					Log.e(MyProxyPluginMgr.TAG, "Invalid Plugin type");
					return -1;
				}
			}
			return 0;
		}

		@Override
		public int OnPluginDestroyed(BigInteger id, twrap_proxy_plugin_type_t type) {
			switch(type){
				case twrap_proxy_plugin_audio_producer:
				case twrap_proxy_plugin_video_producer:
				case twrap_proxy_plugin_audio_consumer:
				case twrap_proxy_plugin_video_consumer:
				{
					synchronized(this){
						MyProxyPlugin plugin = MyProxyPluginMgr.plugins.get(id);
						if(plugin != null){
							plugin.invalidate();
							MyProxyPluginMgr.plugins.remove(id);
							return 0;
						}
						else{
							Log.e(MyProxyPluginMgr.TAG, "Failed to find plugin");
							return -1;
						}
					}
				}
				default:
				{
					Log.e(MyProxyPluginMgr.TAG, "Invalid Plugin type");
					return -1;
				}
			}
		}
	}
}
