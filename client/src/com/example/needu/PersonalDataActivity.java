package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;

public class PersonalDataActivity extends Activity 
{
	private Button squareButton;
	private Button personalButton;
	private Button setButton;
	private Button personalDataButton;
	private Button announceButton;
	private Button commentButton;
	private Button interestButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personal_data);
	
		initViews();
		}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		announceButton = (Button)findViewById(R.id.announceButton);
		commentButton = (Button)findViewById(R.id.commentButton);
		interestButton = (Button)findViewById(R.id.interestButton);
	}	
}