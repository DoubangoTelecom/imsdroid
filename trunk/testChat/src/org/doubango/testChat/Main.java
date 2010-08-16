package org.doubango.testChat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class Main extends Activity {
    
	private ChatAdapterItem[] items;
	
	private GridView gridView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.items =  new ChatAdapterItem[]{
        		new ChatAdapterItem(true, "Bonjour, Comment tu vas?"),
        		new ChatAdapterItem(true, "Ca va bien, et toi? Tu raconte quoi?"),
        		new ChatAdapterItem(false, "Rien de 9"),
        		new ChatAdapterItem(false, "OK. A 2min"),
        		new ChatAdapterItem(true, "A+")
        };
        
     // gridView
		this.gridView = (GridView) this.findViewById(R.id.screen_chat_gridView);
		this.gridView.setAdapter((new ChatAdapter()));
    }
    
    
    /* ===================== Adapter ======================== */

	private class ChatAdapterItem {
		private final boolean in;
		private final String content;

		private ChatAdapterItem(boolean in, String content) {
			this.in = in;
			this.content = content;
		}
	}
	
	private class ChatAdapter extends BaseAdapter {
		
		private ChatAdapter() {
		}

		public int getCount() {
			return Main.this.items.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ChatAdapterItem item;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.screen_chat_item, null);
			}
			
			if ((Main.this.items.length <= position) || ((item = Main.this.items[position]) == null)) {
				return view;
			}
			
			TextView textView1 = (TextView) view .findViewById(R.id.TextView01);		
			
			textView1.setText(item.content);
			view.setBackgroundResource(item.in ? R.drawable.chat_in : R.drawable.chat_out);		

			return view;
		}
	}
}