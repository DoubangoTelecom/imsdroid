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

		boolean isFreshDataBase(){
			return mFreshDataBase;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "NgnDataBaseOpenHelper.onCreate()");
			mFreshDataBase = true;
			if(mCreateTableSt != null){
				for(String st[] : mCreateTableSt){
					try{
						db.execSQL(String.format("CREATE TABLE %s(%s)", st[0], st[1]));
					}
					catch(SQLException e){
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "NgnDataBaseOpenHelper.onUpgrade("+oldVersion+","+newVersion+")");
			if(mCreateTableSt != null){
				for(String st[] : mCreateTableSt){
					try{
						db.execSQL(String.format("DROP TABLE IF EXISTS ", st[0]));
					}
					catch(SQLException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
}
