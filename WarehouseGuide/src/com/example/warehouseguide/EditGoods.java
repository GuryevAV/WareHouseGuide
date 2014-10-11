package com.example.warehouseguide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditGoods extends Activity{
	
	EditText etName, etRow, etSection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_goods);
		
		etName = (EditText) findViewById(R.id.etName);
		etRow = (EditText) findViewById(R.id.etRow);
		etSection = (EditText) findViewById(R.id.etSection);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_goods, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_save) {
			if (CheckField()) {
				Intent intent = new Intent();
				intent.putExtra("name", etName.getText().toString());
				intent.putExtra("row", etRow.getText().toString());
				intent.putExtra("section", etSection.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			} 
		}
		return super.onOptionsItemSelected(item);
	}
	
	private boolean CheckField() {
		
		boolean result = false;
		String toastString = "";
		
		if (TextUtils.isEmpty(etName.getText().toString())) {
			toastString = "¬ведите название" + "\n";
		} else result = true;
		if (TextUtils.isEmpty(etRow.getText().toString())) {
			result = false;
			toastString += "¬ведите р€д" + "\n";
		} else result = true;
		if (TextUtils.isEmpty(etSection.getText().toString())) {
			result = false;
			toastString += "¬ведите секцию" + "\n";
		} else result = true;
		
		
		if (toastString.length() > 0) Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
		return result;
	}
}
