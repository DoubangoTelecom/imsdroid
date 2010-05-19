package org.doubango.imsdroid.Sevices.Impl;

import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.IContactService;
import org.doubango.imsdroid.Services.IScreenService;
import org.doubango.imsdroid.Services.ISipService;
import org.doubango.imsdroid.Services.IStorageService;
import org.doubango.imsdroid.Services.IXcapService;

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
	private static final ScreenService screenService = new ScreenService();
	private static final SipService sipService = new SipService();
	private static final StorageService storageService = new StorageService();
	private static final XcapService xcapService = new XcapService();

	/**
	 * Starts all services
	 * 
	 * @return true if succeed and false otherwise
	 */
	public static boolean start() {
		boolean success = true;

		success &= ServiceManager.configurationService.start();
		success &= ServiceManager.contactService.start();
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
		success &= ServiceManager.screenService.stop();
		success &= ServiceManager.sipService.stop();
		success &= ServiceManager.storageService.stop();
		success &= ServiceManager.xcapService.stop();

		return success;
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
