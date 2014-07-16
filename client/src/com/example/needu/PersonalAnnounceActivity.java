package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PersonalAnnounceActivity extends Activity {
	private Button squareButton;
	private Button setButton;
	private Button personalDataButton;
	private Button commentButton;
	private Button interestButton;
	private Button newNeedButton;
	
	private String sessionId;
	private String studentId;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_announce);
	
		initViews();
		
		sessionId = getIntent().getStringExtra("sessionId");
		studentId = getIntent().getStringExtra("studentId");
	}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		commentButton = (Button)findViewById(R.id.commentButton);
		interestButton = (Button)findViewById(R.id.interestButton);
		newNeedButton = (Button)findViewById(R.id.newNeedButton);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		commentButton.setOnClickListener(listener);
		interestButton.setOnClickListener(listener);
		newNeedButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.newNeedButton) {
				Intent intent = new Intent(PersonalAnnounceActivity.this, NewNeedActivity.class);
				intent.putExtra("sessionId", sessionId);
				startActivity(intent);
				return ;
			}
			
			Intent intent = new Intent();
			intent.putExtra("sessionId", sessionId);
			intent.putExtra("studentId", studentId);
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.commentButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalCommentActivity.class);
			} else if (arg0.getId() == R.id.interestButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalInterestActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalAnnounceActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalAnnounceActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
}