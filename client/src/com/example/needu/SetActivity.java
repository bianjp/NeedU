package com.example.needu;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SetActivity extends Activity {
	private Button changePasswordButton;
	private Button versionUpdateButton;
	private Button messageTipsButton;
	private Button aboutNeedUButton;
	private Button exitButton;
	
	private Button squareButton;
	private Button personalButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set);
		
		initViews();
	}
	
	private void initViews() {
		changePasswordButton = (Button)findViewById(R.id.changePassword);
		versionUpdateButton = (Button)findViewById(R.id.versionUpdate);
		messageTipsButton = (Button)findViewById(R.id.messageTips);
		aboutNeedUButton = (Button)findViewById(R.id.aboutNeedU);
		exitButton = (Button)findViewById(R.id.exit);
		
		squareButton = (Button)findViewById(R.id.squareButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		
		changePasswordButton.setOnClickListener(listener);
		versionUpdateButton.setOnClickListener(listener);
		messageTipsButton.setOnClickListener(listener);
		aboutNeedUButton.setOnClickListener(listener);
		exitButton.setOnClickListener(listener);
		
		squareButton.setOnClickListener(listener);
		personalButton.setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.versionUpdate) {
				Toast.makeText(SetActivity.this, "暂时没有新版本", Toast.LENGTH_SHORT).show();
				return ;
			}
			
			Intent intent = new Intent();
			if (arg0.getId() == R.id.changePassword) {
				intent.setClass(SetActivity.this, ChangePasswordActivity.class);
				startActivity(intent);
			} else if (arg0.getId() == R.id.messageTips) {
				intent.setClass(SetActivity.this, MessageTipsActivity.class);
				startActivity(intent);
			} else if (arg0.getId() == R.id.aboutNeedU) {
				intent.setClass(SetActivity.this, AboutActivity.class);
				startActivity(intent);
			} else if (arg0.getId() == R.id.exit) {
				// TODO
				intent.setClass(SetActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(SetActivity.this, GroundActivity.class);
				startActivity(intent);
				finish();
			} else if (arg0.getId() == R.id.personalButton) {
				intent.setClass(SetActivity.this, PersonalDataActivity.class);
				startActivity(intent);
				finish();
			}
		}
	};

}