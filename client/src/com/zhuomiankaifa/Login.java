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

public class Login extends ActionBarActivity {

	private EditText inputIdEditText;
	private EditText inputCodeEditText;
	private Button loginButton;
	private Button fogetButton;
	private Button registrationButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		
		initViews();

		}

		
		private void initViews()
		{

			inputIdEditText = (EditText)findViewById(R.id.inputIdEditText);
			inputCodeEditText = (EditText)findViewById(R.id.inputCodeEditText);
			loginButton = (Button)findViewById(R.id.loginButton);
			fogetButton = (Button)findViewById(R.id.fogetButton);
			registrationButton = (Button)findViewById(R.id.registrationButton);
		}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
