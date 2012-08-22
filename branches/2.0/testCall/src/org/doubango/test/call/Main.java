package org.doubango.test.call;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.AndroidUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Main extends Activity {
	private static String TAG = Main.class.getCanonicalName();
	private ListView mListView;
	private TextView mTvLog;
	private MainAdapter mAdapter;
	
	private BroadcastReceiver mSipBroadCastRecv;
	
	private final NgnEngine mEngine;
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	
	private final static String SIP_DOMAIN = "doubango.org";
	private final static String SIP_USERNAME = "005";
	private final static String SIP_PASSWORD = "mysecret";
	private final static String SIP_SERVER_HOST = "192.168.0.12";
	private final static int SIP_SERVER_PORT = 5060;
	
	public final static String EXTRAT_SIP_SESSION_ID = "SipSession";
	
	static final MainListViewItem[] sMainListViewItems = new MainListViewItem[]{
		new MainListViewItem("My First Item", "001"),
		new MainListViewItem("My Second Item", "002"),
		new MainListViewItem("My Third Item", "003"),
		new MainListViewItem("My Fourth Item", "200006395544399062"),
	};
	
	public Main(){
		mEngine = NgnEngine.getInstance();
		mConfigurationService = mEngine.getConfigurationService();
		mSipService = mEngine.getSipService();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
 
        mTvLog = (TextView)findViewById(R.id.main_textView_log);
        
        mAdapter = new MainAdapter(this);
		mListView = (ListView) findViewById(R.id.main_listView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				return false;
			}
		});
		
		mTvLog.setText("onCreate()");
		
		// Listen for registration events
		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				
				// Registration Event
				if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
					NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
					if(args == null){
						mTvLog.setText("Invalid event args");
						return;
					}
					switch(args.getEventType()){
						case REGISTRATION_NOK:
							mTvLog.setText("Failed to register :(");
							break;
						case UNREGISTRATION_OK:
							mTvLog.setText("You are now unregistered :)");
							break;
						case REGISTRATION_OK:
							mTvLog.setText("You are now registered :)");
							break;
						case REGISTRATION_INPROGRESS:
							mTvLog.setText("Trying to register...");
							break;
						case UNREGISTRATION_INPROGRESS:
							mTvLog.setText("Trying to unregister...");
							break;
						case UNREGISTRATION_NOK:
							mTvLog.setText("Failed to unregister :(");
							break;
					}
					mAdapter.refresh();
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
	    registerReceiver(mSipBroadCastRecv, intentFilter);
    }
    
    
    @Override
	protected void onDestroy() {
		// Stops the engine
		if(mEngine.isStarted()){
			mEngine.stop();
		}
		// release the listener
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Starts the engine
		if(!mEngine.isStarted()){
			if(mEngine.start()){
				mTvLog.setText("Engine started :)");
			}
			else{
				mTvLog.setText("Failed to start the engine :(");
			}
		}
		// Register
		if(mEngine.isStarted()){
			if(!mSipService.isRegistered()){
				// Set credentials
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, SIP_USERNAME);
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, String.format("sip:%s@%s", SIP_USERNAME, SIP_DOMAIN));
				mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, SIP_PASSWORD);
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, SIP_SERVER_HOST);
				mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, SIP_SERVER_PORT);
				mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, SIP_DOMAIN);
				// VERY IMPORTANT: Commit changes
				mConfigurationService.commit();
				// register (log in)
				mSipService.register(this);
			}
		}
	}
	
	boolean makeVoiceCall(String phoneNumber){
		final String validUri = NgnUriUtils.makeValidSipUri(String.format("sip:%s@%s", phoneNumber, SIP_DOMAIN));
		if(validUri == null){
			mTvLog.setText("failed to normalize sip uri '" + phoneNumber + "'");
			return false;
		}
		NgnAVSession avSession = NgnAVSession.createOutgoingSession(mSipService.getSipStack(), NgnMediaType.Audio);
		
		Intent i = new Intent();
		i.setClass(this, CallScreen.class);
		i.putExtra(EXTRAT_SIP_SESSION_ID, avSession.getId());
		startActivity(i);
		
		return avSession.makeCall(validUri);
	}
	
    //
    //	MainListViewItem
    //
    static class MainListViewItem{
    	private final String mDescription;
    	private final String mPhoneNumber;
    	
    	MainListViewItem(String description, String phoneNumer){
    		mDescription = description;
    		mPhoneNumber = phoneNumer;
    	}
    	
    	String getDescription(){
    		return mDescription;
    	}
    	
    	String getPhoneNumber(){
    		return mPhoneNumber;
    	}
    }
    
    //
    //
    //
    static class MainAdapter extends BaseAdapter{
    	final LayoutInflater mInflater;
    	final Main mMain;
    	MainAdapter(Main main){
    		super();
    		mMain = main;
    		mInflater = LayoutInflater.from(main);
    	}

    	void refresh(){
    		notifyDataSetChanged();
    	}
    	
		@Override
		public int getCount() {
			return sMainListViewItems.length;
		}

		@Override
		public Object getItem(int position) {
			return sMainListViewItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final MainListViewItem item = (MainListViewItem)getItem(position);
			if(view == null){
				view = mInflater.inflate(R.layout.main_item, null);
			}
			((TextView)view.findViewById(R.id.main_item_textView1_description)).setText(item.getDescription());
			final Button button = (Button)view.findViewById(R.id.main_item_button_call);
			button.setEnabled(mMain.mSipService.isRegistered());
			button.setTag(item.getPhoneNumber());
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mMain.makeVoiceCall(v.getTag().toString());
				}
			});
			return view;
		}
    }
    
    private static final String DATA_FOLDER = String.format("/data/data/%s", Main.class.getPackage().getName());
	private static final String LIBS_FOLDER = String.format("%s/lib", Main.DATA_FOLDER);
    // Load native libraries (the shared libraries are from 'android-ngn-stack' project)
	static {
		// See 'http://code.google.com/p/imsdroid/issues/detail?id=197' for more information
		// Load Android utils library (required to detect CPU features)
		System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libutils_armv5te.so"));
		Log.d(TAG,"CPU_Feature=" + AndroidUtils.getCpuFeatures());
		if(NgnApplication.isCpuNeon()){
			Log.d(TAG,"isCpuNeon()=YES");
			System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libtinyWRAP_armv7-a.so"));
		}
		else{
			Log.d(TAG,"isCpuNeon()=NO");
			System.load(String.format("%s/%s", Main.LIBS_FOLDER, "libtinyWRAP_armv5te.so"));
		}
		// Initialize the engine
		NgnEngine.initialize();
	}
}