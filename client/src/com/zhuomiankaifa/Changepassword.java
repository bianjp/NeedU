package com.zhuomiankaifa;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.os.Build;

public class Changepassword extends ActionBarActivity {

	private EditText oldcodeEditText;
	private EditText newcodeEditText;
	private EditText ensurecodeEditText;
	private Button completeButton;
	private Button cancelButton;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changepassword);

		initViews();
	}

	
	private void initViews()
	{
		oldcodeEditText = (EditText)findViewById(R.id.oldcodeEditText);
		newcodeEditText = (EditText)findViewById(R.id.newcodeEditText);
		ensurecodeEditText = (EditText)findViewById(R.id.ensurecodeEditText);
		completeButton = (Button)findViewById(R.id.completeButton);
		cancelButton = (Button)findViewById(R.id.cancelButton);
		
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.changepassword, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */


}
