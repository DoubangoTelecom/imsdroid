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
package org.doubango.ngn.utils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NgnDataBaseHelper {
	protected static final String TAG = NgnDataBaseHelper.class.getCanonicalName();
	
	private final Context mContext;
	private final String mDataBaseName;
	private final int mDataBaseVersion;
	private final NgnDataBaseOpenHelper mDataBaseOpenHelper;
	private SQLiteDatabase mSQLiteDatabase;
	
	public NgnDataBaseHelper(Context context, String dataBaseName, int dataBaseVersion, String[][] createTableSt){
		mContext = context;
		mDataBaseName = dataBaseName;
		mDataBaseVersion = dataBaseVersion;
		
		mDataBaseOpenHelper = new NgnDataBaseOpenHelper(mContext, mDataBaseName, mDataBaseVersion, createTableSt);
		mSQLiteDatabase = mDataBaseOpenHelper.getWritableDatabase();
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public boolean close(){
		try{
			if(mSQLiteDatabase != null){
				mSQLiteDatabase.close();
				mSQLiteDatabase = null;
			}
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean isFreshDataBase(){
		return mDataBaseOpenHelper.isFreshDataBase();
	}
	
	public SQLiteDatabase getSQLiteDatabase(){
		return mSQLiteDatabase;
	}
	
	public boolean deleteAll(String table, String whereClause, String[] whereArgs){
		try{
			mSQLiteDatabase.delete(table, whereClause, whereArgs);
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean deleteAll(String table){
		return deleteAll(table, null, null);
	}

	
	static class NgnDataBaseOpenHelper extends SQLiteOpenHelper {
		boolean mFreshDataBase;
		private final String[][] mCreateTableSt;
		
		NgnDataBaseOpenHelper(Context context, String dataBaseName, int dataBaseVersion, String[][] createTableSt) {
			super(context, dataBaseName, null, dataBaseVersion);
			mCreateTableSt = createTableSt;
		}

		private boolean isFreshDataBase(){
			return mFreshDataBase;
		}
		
		private boolean createDataBase(SQLiteDatabase db){
			mFreshDataBase = true;
			if(mCreateTableSt != null){
				for(String st[] : mCreateTableSt){
					try{
						db.execSQL(String.format("CREATE TABLE %s(%s)", st[0], st[1]));
					}
					catch(SQLException e){
						e.printStackTrace();
						return false;
					}
				}
			}
			return true;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "NgnDataBaseOpenHelper.onCreate()");
			createDataBase(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "NgnDataBaseOpenHelper.onUpgrade("+oldVersion+","+newVersion+")");
			if(mCreateTableSt != null){
				for(String st[] : mCreateTableSt){
					try{
						db.execSQL(String.format("DROP TABLE IF EXISTS %s", st[0]));
					}
					catch(SQLException e){
						e.printStackTrace();
					}
				}
			}
			createDataBase(db);
		}
	}
}
