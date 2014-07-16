package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PersonalCommentActivity extends Activity {
	private Button squareButton;
	private Button setButton;
	private Button personalDataButton;
	private Button announceButton;
	private Button interestButton;
	
	private String sessionId;
	private String studentId;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_comment);
	
		initViews();
		
		sessionId = getIntent().getStringExtra("sessionId");
		studentId = getIntent().getStringExtra("studentId");
	}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		announceButton = (Button)findViewById(R.id.announceButton);
		interestButton = (Button)findViewById(R.id.interestButton);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		announceButton.setOnClickListener(listener);
		interestButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			intent.putExtra("sessionId", sessionId);
			intent.putExtra("studentId", studentId);
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.announceButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalAnnounceActivity.class);
			} else if (arg0.getId() == R.id.interestButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalInterestActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalCommentActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalCommentActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
}