package com.example.warehouseguide;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class DB {
	
	final String LOG_TAG = "myLogs";
	
	private static final String DB_NAME = "mydb";
	private static final int DB_VERSION = 1;
	
	public static final String GOODS_COLUMN_ID = "_id";
	public static final String GOODS_COLUMN_NAME = "name";
	public static final String GOODS_COLUMN_NAME_UPCASE = "nameup";
	public static final String GOODS_COLUMN_ROW = "row";
	public static final String GOODS_COLUMN_SECTION = "section";
	public static final String GOODS_COLUMN_TIME = "time";
	
	private final Context mCtx;
	
	private DBHelper mDBHelper;
	private SQLiteDatabase mDB;
	
	private static final String CREATE_TABLE_GOODS = "create table goods (" 
			+ GOODS_COLUMN_ID + " integer primary key autoincrement,"
			+ GOODS_COLUMN_NAME + " text,"
			+ GOODS_COLUMN_NAME_UPCASE + " text,"
			+ GOODS_COLUMN_ROW + " text," 
			+ GOODS_COLUMN_SECTION + " text,"
			+ GOODS_COLUMN_TIME + " text"
			+ ");";
	
	Cursor c;
	ContentValues cv = new ContentValues();
	
	public DB(Context ctx) {
		mCtx = ctx;
	} 

	//открываем подключение
	public void open() {
		mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
		mDB = mDBHelper.getWritableDatabase();
	}
	
	//закрываем подключение
	public void close() {
		if (mDBHelper != null) mDBHelper.close();
	}
	
	//все данные по товарам
	public Cursor getGoodsData() {
		c = mDB.query("goods", null, null, null, null, null, null);
		logCursor(c);
		return c;
	}
	
	//поиск товара
	public Cursor searchGoods(String search) {
		c = mDB.query("goods", null, "nameup LIKE ?", new String[] {"%" + search.trim().toUpperCase() + "%"}, 
				null, null, GOODS_COLUMN_NAME_UPCASE);
		Log.d(LOG_TAG, "--- Search goods ---");
		logCursor(c);
		Log.d(LOG_TAG, "--- ---");
		return c;
	}
	
	//добавить товар
	public void AddGoods(String[] goods) {
		cv.clear();
		cv.put(GOODS_COLUMN_NAME, goods[0]);
		cv.put(GOODS_COLUMN_NAME_UPCASE, goods[1]);
		cv.put(GOODS_COLUMN_ROW, goods[2]);
		cv.put(GOODS_COLUMN_SECTION, goods[3]);
		Date d = new Date();
		cv.put(GOODS_COLUMN_TIME, d.getTime() + "");
		mDB.insert("goods", null, cv);
	}
	
	//удалить все товары
	public void ClearGoods() {
		mDB.delete("goods", null, null);
	}
	
	//заполнить базу из XML
		public boolean FillFromXML(ArrayList<String[]> goods) {
			boolean result = true;
			mDB.beginTransaction();
			try {
				mDB.delete("goods", null, null);
				mDB.execSQL("drop table if exists goods");
				mDB.execSQL(CREATE_TABLE_GOODS);
				
				//заполняем таблицу товаров
				for (int i = 0; i < goods.size(); i++) {
					String[] good = goods.get(i);
					cv.clear();
					cv.put(GOODS_COLUMN_NAME, good[0]);
					cv.put(GOODS_COLUMN_NAME_UPCASE, good[1]);
					cv.put(GOODS_COLUMN_ROW, good[2]);
					cv.put(GOODS_COLUMN_SECTION, good[3]);
					cv.put(GOODS_COLUMN_TIME, good[4]);
					mDB.insert("goods", null, cv);
				}
				Log.d(LOG_TAG, "--- Table goods ---");
				c = mDB.query("goods", null, null, null, null, null, null);
				logCursor(c);
				Log.d(LOG_TAG, "--- ---");
								
				mDB.setTransactionSuccessful();
			} catch(Exception ex) {
				Log.d(LOG_TAG, ex.getClass() + " error: " + ex.getMessage());
				result = false;
			} finally {
				mDB.endTransaction();
			}
			return result;
		}
	
	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			//данные для таблицы товаров
			String[] goods_name = { "Вино Молоко любимой женщины", "Вино Кагор", "Вино Портвейн", "Коньяк Старейшина", "Коньяк Камю",
					"Виски Белая Лошадь", "Виски Вильям Лоусонс", "Брэнди", "Ликер Ягермейстер", "Ром Бакарди" };
			String[] goods_row = { "11", "12", "13", "14", "15", "31", "32", "33", "34", "35" };
			String[] goods_section = { "A", "B", "C", "D", "A", "B", "C", "D", "A", "B" };
			
			//создаем таблицу товаров
			db.execSQL(CREATE_TABLE_GOODS);
			
			//заполняем ее
			for (int i = 0; i < goods_name.length; i++) {
				cv.clear();
				cv.put(GOODS_COLUMN_NAME, goods_name[i]);
				cv.put(GOODS_COLUMN_NAME_UPCASE, goods_name[i].toUpperCase());
				cv.put(GOODS_COLUMN_ROW, goods_row[i]);
				cv.put(GOODS_COLUMN_SECTION, goods_section[i]);
				Date d = new Date();
				cv.put(GOODS_COLUMN_TIME, d.getTime() + "");
				db.insert("goods", null, cv);
			}
			
			//выводим в лог данные по товарам
			Log.d(LOG_TAG, "--- Table goods ---");
			c = db.query("goods", null, null, null, null, null, null);
			logCursor(c);
			Log.d(LOG_TAG, "--- ---");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	//вывод в лог данных из курсора
	void logCursor(Cursor c) {
		if (c != null) {
			if (c.moveToFirst()) {
				String str;
				do {
					str = "";
					for (String cn : c.getColumnNames()) {
						str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
					}
					Log.d(LOG_TAG, str);
				} while (c.moveToNext());
			}
		} else
			Log.d(LOG_TAG, "Cursor is null");
	}
}
