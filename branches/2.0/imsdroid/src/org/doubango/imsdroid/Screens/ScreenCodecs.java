/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.imsdroid.Screens;

import java.util.ArrayList;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.tdav_codec_id_t;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ScreenCodecs  extends BaseScreen{
	private static String TAG = ScreenCodecs.class.getCanonicalName();
	
	private final INgnConfigurationService mConfigurationService;
	
	private GridView mGridView;
	private ScreenCodecsAdapter mAdapter;
	private int mCodecs;
	
	public ScreenCodecs() {
		super(SCREEN_TYPE.CODECS_T, ScreenCodecs.class.getCanonicalName());
		
		mConfigurationService = getEngine().getConfigurationService();
		mCodecs = mConfigurationService.getInt(NgnConfigurationEntry.MEDIA_CODECS, NgnConfigurationEntry.DEFAULT_MEDIA_CODECS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_codecs);
        
		mGridView = (GridView) findViewById(R.id.screen_codecs_gridView);
		mGridView.setAdapter((mAdapter = new ScreenCodecsAdapter(this)));
		mGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final ScreenCodecsItem item = (ScreenCodecsItem)parent.getItemAtPosition(position);
				if (item != null) {
					if((mCodecs & item.mId) == item.mId){
						mCodecs &= ~item.mId;
					}
					else{
						mCodecs |= item.mId;
					}
					mAdapter.updateView();
					mComputeConfiguration = true;
				}
			}
		});
	}
	
	protected void onPause() {
		if(super.mComputeConfiguration){			
			mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS, mCodecs);
			SipStack.setCodecs_2(mCodecs);
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	
	
	/**
	 * ScreenCodecsItem
	 */
	static class ScreenCodecsItem {
		private final String mName;
		private final String mDescription;
		private final int mId;

		private ScreenCodecsItem(int id, String name, String description) {
			mId = id;
			mName = name;
			mDescription = description;
		}
	}
	
	/**
	 * ScreenCodecsAdapter
	 */
	static class ScreenCodecsAdapter extends BaseAdapter {
		private static final ArrayList<ScreenCodecsItem> sScreenCodecsItems = new ArrayList<ScreenCodecsItem>();
		
		static{
	        // Audio Codecs
			sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_pcma.swigValue(), "PCMA", "PCMA (8 KHz)"));
			sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_pcmu.swigValue(), "PCMU", "PCMU (8 KHz)"));
			if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_gsm))
				sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_gsm.swigValue(), "GSM", "GSM (8 KHz)"));
			if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_oa))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_amr_nb_oa.swigValue(), "AMR-NB-OA", "AMR Narrow Band Octet Aligned (8 KHz)"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_be))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_amr_nb_be.swigValue(), "AMR-NB-BE", "AMR Narrow Band Bandwidth Efficient (8 KHz)"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_ilbc))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_ilbc.swigValue(), "iLBC", "internet Low Bitrate Codec (8 KHz)"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_speex_nb)){
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_speex_nb.swigValue(), "Speex-NB", "Speex Narrow-Band (8 KHz)"));
	        	// WebRTC AEC doesn't support WB and UWB codecs => bas voice quality
	        	// these codecs will be re-enabled when WebRTC gives better quality.
	        	// You have two choices: 
	        	// 	1. Disable AEC
	        	//	2. Use Speex-DSP AEC
	        	// sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_speex_wb.swigValue(), "Speex-WB", "Speex Wide-Band (16 KHz)"));
	        	// sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_speex_uwb.swigValue(), "Speex-UWB", "Speex Ultra Wide-Band (32 KHz)"));
	        }
	        //if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_g722))
	        //	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_g722.swigValue(), "G.722", "G722 HD Voice (16 KHz)"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_g729ab))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_g729ab.swigValue(), "G.729", "G729 Annex A/B (8 KHz)"));
	        // Video Codecs
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_vp8))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_vp8.swigValue(), "VP8", "Google's VP8"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_mp4ves_es))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_mp4ves_es.swigValue(), "MP4V-ES", "MPEG-4 Part 2"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_theora))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_theora.swigValue(), "Theora", "Theora"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h264_bp10))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp10.swigValue(), "H264-BP10", "H.264 Base Profile 1.0"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h264_bp20))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp20.swigValue(), "H264-BP20", "H.264 Base Profile 2.0"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h264_bp30))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp30.swigValue(), "H264-BP30", "H.264 Base Profile 3.0"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263.swigValue(),"H.263", "H.263"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263p))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263p.swigValue(), "H.263+", "H.263-1998"));
	        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_h263pp))
	        	sScreenCodecsItems.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263pp.swigValue(), "H.263++", "H.263-2000"));
		}
		
		private final LayoutInflater mInflater;
		private final ScreenCodecs mBaseScreen;
		
		ScreenCodecsAdapter(ScreenCodecs baseScreen) {
			mBaseScreen = baseScreen;
			mInflater = LayoutInflater.from(mBaseScreen);
		}

		public int getCount() {
			return sScreenCodecsItems.size();
		}

		public Object getItem(int position) {
			return sScreenCodecsItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		private void updateView(){
			notifyDataSetChanged();
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ScreenCodecsItem item = (ScreenCodecsItem)getItem(position);

			if (view == null) {
				view = mInflater.inflate(R.layout.screen_codecs_item, null);
			}
			
			if (item == null) {
				return view;
			}
			
			((ImageView) view.findViewById(R.id.screen_codecs_item_imageView_state)).
				setImageResource((item.mId & mBaseScreen.mCodecs) == item.mId  ? R.drawable.check_on_38 : R.drawable.check_off_38);
			((TextView) view .findViewById(R.id.screen_codecs_item_textView_name)).setText(item.mName);
			((TextView) view .findViewById(R.id.screen_codecs_item_textView_description)).setText(item.mDescription);

			return view;
		}
	}
}
