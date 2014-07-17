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

public class Registration extends ActionBarActivity {

	private EditText nicknameEditText;
	private EditText schoolEditText;
	private EditText nameEditText;
	private EditText collegeEditText;
	private EditText birthdayEditText;
	private EditText phoneEditText;
	private EditText qqEeditText;
	private EditText accountEditText;
	private EditText weixinEditText;
	private EditText codeEditText;
	private EditText ensureCodeEditText;
	private Button confirmButton;
	private Button cancelButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
		

		initViews();

		}

		
		private void initViews()
		{

			nicknameEditText = (EditText)findViewById(R.id.nicknameEditText);
			schoolEditText = (EditText)findViewById(R.id.schoolEditText);
			nameEditText = (EditText)findViewById(R.id.nameEditText);
			collegeEditText = (EditText)findViewById(R.id.collegeEditText);
			birthdayEditText = (EditText)findViewById(R.id.birthdayEditText);
			phoneEditText = (EditText)findViewById(R.id.phoneEditText);
			qqEeditText = (EditText)findViewById(R.id.qqEeditText);
			accountEditText = (EditText)findViewById(R.id.accountEditText);
			weixinEditText = (EditText)findViewById(R.id.weixinEditText);
			codeEditText = (EditText)findViewById(R.id.codeEditText);
			ensureCodeEditText = (EditText)findViewById(R.id.ensureCodeEditText);
			confirmButton = (Button)findViewById(R.id.confirmButton);
			cancelButton = (Button)findViewById(R.id.cancelButton);
		}	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
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
