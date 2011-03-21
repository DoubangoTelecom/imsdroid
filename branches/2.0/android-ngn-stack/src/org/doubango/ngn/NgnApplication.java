package org.doubango.ngn;
import org.doubango.ngn.utils.NgnStringUtils;

import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NgnApplication extends Application{
	private final static String TAG = NgnApplication.class.getCanonicalName();
	
	private static NgnApplication sInstance;
	private static PackageManager sPackageManager;
    private static String sPackageName;
    private static String sDeviceURN;
    private static String sDeviceIMEI;
    private static int sSdkVersion;
    private static int sVersionCode;
    private static AudioManager sAudioManager;
    private static SensorManager sSensorManager;
    private static KeyguardManager sKeyguardManager;
    private static ConnectivityManager sConnectivityManager;
    private static PowerManager sPowerManager;
    private static PowerManager.WakeLock sPowerManagerLock;
    
    public NgnApplication() {
    	sInstance = this;
    }

    public static Context getContext() {
        return sInstance;
    }
    
    @Override
	public void onCreate() {
		super.onCreate();
		
		sPackageManager = sInstance.getPackageManager();    		
		sPackageName = sInstance.getPackageName();
		
		Log.d(TAG,"Build.MODEL="+Build.MODEL);
		Log.d(TAG,"Build.VERSION.SDK="+Build.VERSION.SDK);
	}
    
    public static int getSDKVersion(){
    	if(sSdkVersion == 0){
    		sSdkVersion = Integer.parseInt(Build.VERSION.SDK);
    	}
    	return sSdkVersion;
    }
    
    public static boolean useSetModeToHackSpeaker(){
    	final String model = Build.MODEL;
        return  // isSamsung() ||
                
                model.equalsIgnoreCase("blade")    ||		// ZTE Blade
                
                model.equalsIgnoreCase("htc_supersonic") || //HTC EVO
                
                model.equalsIgnoreCase("U8110") || // Huawei U8110
                model.equalsIgnoreCase("U8150")  // Huawei U8110
                
                ;
    }
    
    public static boolean isSamsung(){
    	final String model = Build.MODEL.toLowerCase();
    	return model.startsWith("gt-") 
		|| model.contains("samsung") 
		|| model.startsWith("sgh-") 
		|| model.startsWith("sph-") 
		|| model.startsWith("sch-");
    }
    
    public static boolean isHTC(){
    	final String model = Build.MODEL.toLowerCase();
    	return model.startsWith("htc");
    }
    
    public static boolean isAudioRecreateRequired(){
    	return false;
    }
    
    public static boolean isSetModeAllowed(){
    	final String model = Build.MODEL;
    	return model.equalsIgnoreCase("ZTE-U V880");
    }
    
    public static boolean isBuggyProximitySensor(){
    	final String model = Build.MODEL;
    	return model.equalsIgnoreCase("ZTE-U V880");
    }
    
    public static boolean isAGCSupported(){
    	return isSamsung() || isHTC();
    }
    
    public static int getVersionCode(){
    	if(sVersionCode == 0 && sPackageManager != null){
    		try {
    			sVersionCode = sPackageManager.getPackageInfo(sPackageName, 0).versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return sVersionCode;
    }
    
    public static String getVersionName(){
    	if(sPackageManager != null){
    		try {
				return sPackageManager.getPackageInfo(sPackageName, 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return "0.0";
    }
    
    public static String getDeviceURN(){
    	if(NgnStringUtils.isNullOrEmpty(sDeviceURN)){
	    	try{
		    	final TelephonyManager telephonyMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		        final String msisdn = telephonyMgr.getLine1Number();
		        if(NgnStringUtils.isNullOrEmpty(msisdn)){
		        	sDeviceURN = String.format("urn:imei:%s", telephonyMgr.getDeviceId());
		        }
		        else{
		        	sDeviceURN = String.format("urn:tel:%s", msisdn);
		        }
	    	}
	    	catch(Exception e){
	    		Log.d(TAG, e.toString());
	    		sDeviceURN = "urn:uuid:3ca50bcb-7a67-44f1-afd0-994a55f930f4";
	    	}
    	}
    	return sDeviceURN;
    }
    
    public static String getDeviceIMEI(){
    	if(NgnStringUtils.isNullOrEmpty(sDeviceIMEI)){
    		final TelephonyManager telephonyMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
    		sDeviceIMEI = telephonyMgr.getDeviceId();
    	}
    	return sDeviceIMEI;
    }
    
    public static AudioManager getAudioManager(){
    	if(sAudioManager == null){
    		sAudioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
    	}
    	return sAudioManager;
    }
    
    public static SensorManager getSensorManager(){
    	if(sSensorManager == null){
    		sSensorManager = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
    	}
    	return sSensorManager;
    }
    
    public static KeyguardManager getKeyguardManager(){
    	if(sKeyguardManager == null){
    		sKeyguardManager = (KeyguardManager)getContext().getSystemService(Context.KEYGUARD_SERVICE);
    	}
    	return sKeyguardManager;
    }
    
    public static ConnectivityManager getConnectivityManager(){
    	if(sConnectivityManager == null){
    		sConnectivityManager = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
    	}
    	return sConnectivityManager;
    }
    
    public static PowerManager getPowerManager(){
    	if(sPowerManager == null){
    		sPowerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
    	}
    	return sPowerManager;
    }
    
    public static boolean acquirePowerLock(){
    	if(sPowerManagerLock == null){
    		final PowerManager powerManager = getPowerManager();
    		if(powerManager == null){
    			Log.e(TAG, "Null Power manager from the system");
    			return false;
    		}
    		
			if((sPowerManagerLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)) == null){
				Log.e(TAG, "Null Power manager lock from the system");
				return false;
			}
			sPowerManagerLock.setReferenceCounted(false);
    	}
    	
    	synchronized(sPowerManagerLock){
	    	if(!sPowerManagerLock.isHeld()){
	    		Log.d(TAG,"acquirePowerLock()");
				sPowerManagerLock.acquire();	
			}
    	}
    	return true;
    }
    
    public static boolean releasePowerLock(){
    	if(sPowerManagerLock != null){
    		synchronized(sPowerManagerLock){
    	    	if(sPowerManagerLock.isHeld()){
    	    		Log.d(TAG,"releasePowerLock()");
    				sPowerManagerLock.release();
    			}
        	}
    	}
    	return true;
    }
}
