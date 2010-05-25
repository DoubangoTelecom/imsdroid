package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.INetworkService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.IStorageService;
import org.doubango.imsdroid.Services.IXcapService;

import android.app.ActivityGroup;

/**
 * Screen Manager. Entry point to retrieve all services (Singletons).
 * 
 * @author root
 * 
 */
public class ServiceManager {
	/* Singletons */
	private static final ConfigurationService configurationService = new ConfigurationService();
	private static final ContactService contactService = new ContactService();
	private static final NetworkService networkService = new NetworkService();
	private static final ScreenService screenService = new ScreenService();
	private static final SipService sipService = new SipService();
	private static final StorageService storageService = new StorageService();
	private static final XcapService xcapService = new XcapService();
	
	private static ActivityGroup mainActivity;

	/**
	 * Starts all services
	 * 
	 * @return true if succeed and false otherwise
	 */
	public static boolean start() {
		boolean success = true;

		success &= ServiceManager.configurationService.start();
		success &= ServiceManager.contactService.start();
		success &= ServiceManager.networkService.start();
		success &= ServiceManager.screenService.start();
		success &= ServiceManager.sipService.start();
		success &= ServiceManager.storageService.start();
		success &= ServiceManager.xcapService.start();

		return true;
		/*return success; */
	}

	/**
	 * Stops all services
	 * 
	 * @return true if succeed and false otherwise
	 */
	public static boolean stop() {
		boolean success = true;

		success &= ServiceManager.configurationService.stop();
		success &= ServiceManager.contactService.stop();
		success &= ServiceManager.networkService.stop();
		success &= ServiceManager.screenService.stop();
		success &= ServiceManager.sipService.stop();
		success &= ServiceManager.storageService.stop();
		success &= ServiceManager.xcapService.stop();

		return success;
	}

	public static void setMainActivity(ActivityGroup mainActivity){
		ServiceManager.mainActivity = mainActivity;
	}
	
	public static ActivityGroup getMainActivity(){
		return ServiceManager.mainActivity;
	}
	
	/**
	 * Gets the Configuration Service.
	 * 
	 * @return
	 */
	public static IConfigurationService getConfigurationService() {
		return ServiceManager.configurationService;
	}

	/**
	 * Gets the Configuration Service.
	 * 
	 * @return
	 */
	public static IContactService getContactService() {
		return (IContactService) ServiceManager.contactService;
	}
	
	/**
	 * Gets the Network Service.
	 * 
	 * @return
	 */
	public static INetworkService getNetworkService() {
		return (INetworkService) ServiceManager.networkService;
	}

	/**
	 * Gets the Screen Service.
	 * 
	 * @return
	 */
	public static IScreenService getScreenService() {
		return (IScreenService) ServiceManager.screenService;
	}

	/**
	 * Gets the Sip Service.
	 * 
	 * @return
	 */
	public static ISipService getSipService() {
		return (ISipService) ServiceManager.sipService;
	}

	/**
	 * Gets the Storage Service.
	 * 
	 * @return
	 */
	public static IStorageService getStorageService() {
		return (IStorageService) ServiceManager.storageService;
	}

	/**
	 * Gets the XCAP Service.
	 * 
	 * @return
	 */
	public static IXcapService getXcapService() {
		return ServiceManager.xcapService;
	}
}
