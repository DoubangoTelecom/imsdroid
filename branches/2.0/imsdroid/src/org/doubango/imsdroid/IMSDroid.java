package org.doubango.imsdroid;

import org.doubango.imsdroid.Utils.StringUtils;

import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IMSDroid extends Application{
	private final static String TAG = IMSDroid.class.getCanonicalName();
	
	private static IMSDroid sInstance;
	private static PackageManager sPackageManager;
    private static String sPackageName;
    private static String sDeviceURN;
    private static int sSdkVersion;
    private static int sVersionCode;
    private static AudioManager sAudioManager;
    private static SensorManager sSensorManager;
    private static KeyguardManager sKeyguardManager;
    
    public IMSDroid() {
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
	}
    
    public static int getSDKVersion(){
    	if(sSdkVersion == 0){
    		sSdkVersion = Integer.parseInt(Build.VERSION.SDK);
    	}
    	return sSdkVersion;
    }
    
    public static boolean useSetModeToHackSpeaker(){
    	//http://stackoverflow.com/questions/4278471/trouble-with-loud-speaker-off-on-galaxy-s
    	String model = Build.MODEL;
        return  model.equalsIgnoreCase("GT-I9000") ||       // base model
        		model.equalsIgnoreCase("GT-I5500") ||		// Galaxy Europa
                model.equalsIgnoreCase("SPH-D700") ||       // Epic         (Sprint)
                model.equalsIgnoreCase("SGH-I897") ||       // Captivate    (AT&T)
                model.equalsIgnoreCase("SGH-T959") ||       // Vibrant      (T-Mobile)
                model.equalsIgnoreCase("SCH-I500") ||       // Fascinate    (Verizon)
                model.equalsIgnoreCase("SCH-I400") ||       // Continuum    (T-Mobile) 
                
                model.equalsIgnoreCase("blade")    ||		// ZTE Blade
                
                model.equalsIgnoreCase("htc_supersonic") || //HTC EVO
                
                model.equalsIgnoreCase("U8110") || // Huawei U8110
                model.equalsIgnoreCase("U8150")  // Huawei U8110
                
                ;
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
    	if(StringUtils.isNullOrEmpty(sDeviceURN)){
	    	try{
		    	TelephonyManager telephonyMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		        String msisdn = telephonyMgr.getLine1Number();
		        if(msisdn == null){
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
}
