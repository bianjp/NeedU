package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;

public class AboutNeeduActivity extends Activity 
{
	private Button squareButton;
	private Button personalButton;
	private Button setButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_needu);
	
		initViews();
		}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
	}	
}