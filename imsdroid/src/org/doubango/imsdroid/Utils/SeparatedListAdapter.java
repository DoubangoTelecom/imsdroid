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
// Adapted from 
// http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
package org.doubango.imsdroid.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

public abstract class SeparatedListAdapter extends BaseAdapter {

	public final Map<String,Adapter> mSections = new LinkedHashMap<String,Adapter>();
	public final static int TYPE_SECTION_HEADER = 0;

	public SeparatedListAdapter(Context context) {
	}

	protected abstract View getHeaderView(int position, View convertView, ViewGroup parent, final Adapter adapter);
	
	public void addSection(String section, Adapter adapter) {
		synchronized (mSections) {
			mSections.put(section, adapter);
		}
	}

	public void clearSections(){
		synchronized (mSections) {
			mSections.clear();
		}
	}
	
	@Override
	public Object getItem(int position) {
		synchronized (mSections) {
			for(Object section : this.mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;

				if(position == 0){
					return section;
				}
				if(position < size){
					return adapter.getItem(position - 1);
				}
				position -= size;
			}
			return null;	
		}
	}

	@Override
	public int getCount() {
		synchronized (mSections) {
			int total = 0;
			for(Adapter adapter : mSections.values()){
				total += adapter.getCount() + 1;
			}
			return total;
		}
	}

	@Override
	public int getViewTypeCount() {
		synchronized (mSections) {
			int total = 1;
			for(Adapter adapter : mSections.values()){
				total += adapter.getViewTypeCount();
			}
			return total;
		}
	}

	@Override
	public int getItemViewType(int position) {
		synchronized (mSections) {
			int type = 1;
			for(Object section : mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;
				
				if(position == 0){
					return TYPE_SECTION_HEADER;
				}
				if(position < size){
					return type + adapter.getItemViewType(position - 1);
				}
				
				position -= size;
				type += adapter.getViewTypeCount();
			}
			return -1;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		synchronized (mSections) {
			int sectionNum = 0;
			for(Object section : mSections.keySet()) {
				final Adapter adapter = mSections.get(section);
				final int size = adapter.getCount() + 1;

				if(position == 0){
					return getHeaderView(sectionNum, convertView, parent, adapter);
				}
				if(position < size){
					return adapter.getView(position - 1, convertView, parent);
				}

				// otherwise jump into next section
				position -= size;
				sectionNum++;
			}
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}

