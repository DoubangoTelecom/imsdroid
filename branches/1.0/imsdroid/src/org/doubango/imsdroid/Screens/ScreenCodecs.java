/*
* Copyright (C) 2010 Mamadou Diop.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango.org>
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
*
*/
package org.doubango.imsdroid.Screens;

import java.util.ArrayList;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.Model.Configuration;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_ENTRY;
import org.doubango.imsdroid.Model.Configuration.CONFIGURATION_SECTION;
import org.doubango.imsdroid.Services.IConfigurationService;
import org.doubango.imsdroid.Services.Impl.ServiceManager;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.tdav_codec_id_t;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenCodecs  extends Screen{

	private ArrayList<ScreenCodecsItem> items;
	private GridView gridView;
	private ScreenCodecsAdapter adapter;
	
	private final IConfigurationService configurationService;
	private int codecs;
	
	public ScreenCodecs() {
		super(SCREEN_TYPE.CODECS_T, ScreenCodecs.class.getCanonicalName());
		
		this.configurationService = ServiceManager.getConfigurationService();
		
		this.codecs = this.configurationService.getInt(CONFIGURATION_SECTION.MEDIA, 
        		CONFIGURATION_ENTRY.CODECS, Configuration.DEFAULT_MEDIA_CODECS);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_codecs);
        
        this.items = new ArrayList<ScreenCodecsItem>();
        
        //
        // Audio Codecs
        //
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_pcma.swigValue(), "PCMA", "PCMA (8 KHz)"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_pcmu.swigValue(), "PCMU", "PCMU (8 KHz)"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_gsm.swigValue(), "GSM", "GSM (8 KHz)"));
        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_oa)){
        	this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_amr_nb_oa.swigValue(), "AMR-NB-OA", "AMR Narrow Band Octet Aligned (8 KHz)"));
        }
        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_amr_nb_be)){
        	this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_amr_nb_be.swigValue(), "AMR-NB-BE", "AMR Narrow Band Bandwidth Efficient (8 KHz)"));
        }
        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_ilbc)){
        	this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_ilbc.swigValue(), "iLBC", "internet Low Bitrate Codec (8 KHz)"));
        }
        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_speex_nb)){
        	this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_speex_nb.swigValue(), "Speex-NB", "Speex Narrow Band (8 KHz)"));
        }
        if(SipStack.isCodecSupported(tdav_codec_id_t.tdav_codec_id_g729ab)){
        	this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_g729ab.swigValue(), "G.729", "G729 Annex A/B (8 KHz)"));
        }
        
        //
        // Video Codecs
        //
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_mp4ves_es.swigValue(), "MP4V-ES", "MPEG-4 Part 2"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_theora.swigValue(), "Theora", "Theora"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp10.swigValue(), "H264-BP10", "H.264 Base Profile 1.0"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp20.swigValue(), "H264-BP20", "H.264 Base Profile 2.0"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h264_bp30.swigValue(), "H264-BP30", "H.264 Base Profile 3.0"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263.swigValue(),"H.263", "H.263"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263p.swigValue(), "H.263+", "H.263-1998"));
        this.items.add(new ScreenCodecsItem(tdav_codec_id_t.tdav_codec_id_h263pp.swigValue(), "H.263++", "H.263-2000"));
        
        // gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_codecs_gridView);
		this.gridView.setAdapter((this.adapter = new ScreenCodecsAdapter()));
		this.gridView.setOnItemClickListener(this.gridView_OnItemClickListener);
	}
	
	protected void onPause() {
		if(this.computeConfiguration){			
			this.configurationService.setInt(CONFIGURATION_SECTION.MEDIA, CONFIGURATION_ENTRY.CODECS, codecs);
			
			// Compute
			if(!this.configurationService.compute()){
				Log.e(this.getClass().getCanonicalName(), "Failed to Compute() configuration");
			}
			else{
				SipStack.setCodecs_2(this.configurationService.getInt(CONFIGURATION_SECTION.MEDIA, 
		        		CONFIGURATION_ENTRY.CODECS, Configuration.DEFAULT_MEDIA_CODECS));
			}
			
			this.computeConfiguration = false;
		}
		super.onPause();
	}
	
	private OnItemClickListener gridView_OnItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final ScreenCodecsItem item;
			if ((ScreenCodecs.this.items.size() > position) && ((item = ScreenCodecs.this.items.get(position)) != null)) {
				if((ScreenCodecs.this.codecs & item.id) == item.id){
					ScreenCodecs.this.codecs &= ~item.id;
				}
				else{
					ScreenCodecs.this.codecs |= item.id;
				}
				ScreenCodecs.this.adapter.updateView();
				ScreenCodecs.this.computeConfiguration = true;
			}
		}
	};
	
	
	
	
	
	/* ===================== Adapter ======================== */

	private class ScreenCodecsItem {
		private final String name;
		private final String description;
		private final int id;

		private ScreenCodecsItem(int id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
	}
	
	private class ScreenCodecsAdapter extends BaseAdapter {
		
		private ScreenCodecsAdapter() {
		}

		public int getCount() {
			return ScreenCodecs.this.items.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		private void updateView(){
			this.notifyDataSetChanged();
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ScreenCodecsItem item;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_codecs_item, null);
			}
			
			if ((ScreenCodecs.this.items.size() <= position) || ((item = ScreenCodecs.this.items.get(position)) == null)) {
				return view;
			}
			
			ImageView iv = (ImageView) view .findViewById(R.id.screen_codecs_item_imageView_state);
			TextView tvName = (TextView) view .findViewById(R.id.screen_codecs_item_textView_name);
			TextView tvDescription = (TextView) view .findViewById(R.id.screen_codecs_item_textView_description);
			
			
			iv.setImageResource((item.id & ScreenCodecs.this.codecs) == item.id  ? R.drawable.check_on_38 : R.drawable.check_off_38);
			tvName.setText(item.name);
			tvDescription.setText(item.description);

			return view;
		}
	}
}
