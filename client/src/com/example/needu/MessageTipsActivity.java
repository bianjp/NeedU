package com.example.needu;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class MessageTipsActivity extends Activity {
	private Button finishButton;
	private RadioButton ringButton;
	private RadioButton vibrateButton;
	private SharedPreferences cookies;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_tips);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    cookies = getSharedPreferences("cookies", MODE_PRIVATE);
	    initViews();
	}
	
	private void initViews() {
		finishButton = (Button)findViewById(R.id.finishButton);
		ringButton = (RadioButton)findViewById(R.id.modeRadioButton01);
		vibrateButton = (RadioButton)findViewById(R.id.modeRadioButton02);
		
		String mode = cookies.getString("mode", "");
		if (mode.equals("ring")) {
			ringButton.setChecked(true);
		} else if (mode.equals("vibrate")) {
			vibrateButton.setChecked(true);
		}
		
		finishButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editor editor = cookies.edit();
				if (ringButton.isChecked()) {
					editor.putString("mode", "ring");
					Toast.makeText(MessageTipsActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
				} else if (vibrateButton.isChecked()) {
					editor.putString("mode", "vibrate");
					Toast.makeText(MessageTipsActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
				}
				editor.commit();

				finish();
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}