package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PersonalInterestActivity extends Activity {
	private Button squareButton;
	private Button setButton;
	private Button personalDataButton;
	private Button announceButton;
	private Button commentButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_interest);
	
		initViews();
	}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		announceButton = (Button)findViewById(R.id.announceButton);
		commentButton = (Button)findViewById(R.id.commentButton);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		announceButton.setOnClickListener(listener);
		commentButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.announceButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalAnnounceActivity.class);
			} else if (arg0.getId() == R.id.commentButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalCommentActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalInterestActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalInterestActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
}