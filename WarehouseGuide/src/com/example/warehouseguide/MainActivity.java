package com.example.warehouseguide;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {

	//private static MainActivity instance;
	DialogFragment dlgExit, dlgClear, dlgLoad;
	DB db;
	SimpleCursorAdapter scAdapter;
	public ListView lvData;
	public EditText etSearch;
	ArrayList<String[]> result = new ArrayList<String[]>();
	final String LOG_TAG = "myLogs";
	
	private static final int ACT_GOODS = 2;
	private static final int RESULT_SPEECH_TO_TEXT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//instance = this;
		setContentView(R.layout.fragment_main);

		ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
		
        dlgExit = new DialogExit();
        
		//подключаемся к БД
		db = new DB(this);
		db.open();
				
		// формируем столбцы сопоставления
	    String[] from = new String[] { DB.GOODS_COLUMN_NAME, DB.GOODS_COLUMN_ROW, DB.GOODS_COLUMN_SECTION };
	    int[] to = new int[] { R.id.tvName, R.id.tvRow, R.id.tvSection };

	    // создааем адаптер и настраиваем список
	    scAdapter = new SimpleCursorAdapter(this, /*android.R.layout.simple_list_item_multiple_choice*/R.layout.item, null, from, to, 0);
	    lvData = (ListView) findViewById(R.id.lvFind);
	    lvData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    lvData.setAdapter(scAdapter);
	    etSearch = (EditText) findViewById(R.id.etSearch);
	    etSearch.addTextChangedListener(inputTW);
	    
	    // создаем лоадер для чтения данных
		getSupportLoaderManager().initLoader(0, null, this);
		
		if (savedInstanceState == null) {
			//getFragmentManager().beginTransaction()
					//.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	TextWatcher inputTW = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG, "Text Changed");
			scAdapter.swapCursor(db.searchGoods(etSearch.getText().toString()));
			getSupportLoaderManager().getLoader(0).forceLoad();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}};
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibSearch:
			scAdapter.swapCursor(db.searchGoods(etSearch.getText().toString()));
			getSupportLoaderManager().getLoader(0).forceLoad();
		break;
		case R.id.ibMicrophone:
			Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
			    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak please");
			startActivityForResult(speechIntent, RESULT_SPEECH_TO_TEXT);
		break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_add) {
			Intent intent = new Intent(this, EditGoods.class);
			intent.putExtra("new", true);
			startActivityForResult(intent, ACT_GOODS);
			return true;
		} else if (id == R.id.action_clear) {
			SparseBooleanArray sbArray = lvData.getCheckedItemPositions();
			for (int i = 0; i < sbArray.size(); i++) {
				int key = sbArray.keyAt(i);
				if (sbArray.get(key))
					Log.d(LOG_TAG, key + "");
				}
			dlgClear = new DialogClear();
			dlgClear.show(getFragmentManager(), "dlgClear");
		} else if (id == R.id.action_share) {
			//db.OutXML();
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/html");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, XML.OutXML(db.getGoodsData()));
			startActivity(Intent.createChooser(sharingIntent,"Share using"));
		} else if (id == R.id.action_download) {
			ClipboardManager cbm = (ClipboardManager)
			        getSystemService(Context.CLIPBOARD_SERVICE);
			if (cbm.hasPrimaryClip()) {
				String clipText = cbm.getPrimaryClip().getItemAt(0).getText().toString();
				//Toast.makeText(this, clipText, Toast.LENGTH_SHORT).show();
				if (XML.CheckXML(clipText)) {
					result = XML.LoadFromXML(clipText);
					if (result.isEmpty()) {
						Toast.makeText(this, "В файле нет данных", Toast.LENGTH_SHORT).show();
					} else {
						dlgLoad = new DialogLoadFromXML();
						dlgLoad.show(getFragmentManager(), "dlgLoad");
					}
				} else {
					Toast.makeText(this, "Неправильный формат файла", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "Нет данных в буфере обмена", Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			return rootView;
		}
	}
	
	protected void onDestroy() {
	    super.onDestroy();
	    // закрываем подключение при выходе
	    db.close();
	}
	
	public void closeDB() {
	    db.close();
	}
	
	public void clearDB() {
	    db.ClearGoods();
	    getSupportLoaderManager().getLoader(0).forceLoad();
	    Toast.makeText(this, "Все данные по товарам удалены", Toast.LENGTH_SHORT).show();
	}
	
	public void LoadFromXML() {
		if (db.FillFromXML(result)) {
			Toast.makeText(this, "Данные были успешно загружены", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO Auto-generated method stub
		return new MyCursorLoader(this, db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO Auto-generated method stub
		scAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	static class MyCursorLoader extends CursorLoader {

	    DB db;
	    
	    public MyCursorLoader(Context context, DB db) {
	      super(context);
	      this.db = db;
	    }
	    
	    @Override
	    public Cursor loadInBackground() {
	      //Cursor cursor = db.getGoodsData();
	      return null;
	    }
	    
	  }
	
	@Override
	public void onBackPressed() {
		dlgExit.show(getFragmentManager(), "dlgExit");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACT_GOODS) {
			if (resultCode == RESULT_OK) {
				String[] shop = { "", "", "", ""};
				shop[0] = data.getStringExtra("name");
				shop[1] = data.getStringExtra("name").toUpperCase();
				shop[2] = data.getStringExtra("row");
				shop[3] = data.getStringExtra("section");
				db.AddGoods(shop);
				Toast.makeText(this, R.string.goods_added, Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == RESULT_SPEECH_TO_TEXT) {
			if (resultCode == RESULT_OK) {
		        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		        etSearch.setText(matches.get(0));
		    }
		}
	}
	
	public class MyAdapter extends SimpleCursorAdapter {

		public MyAdapter(Context context, int layout, Cursor c, String[] from,
				int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
		}
				
	}
}
