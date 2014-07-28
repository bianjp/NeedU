package com.example.needu;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

public class LogoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logo);
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		final String sessionId = cookies.getString("sessionId", "");
		
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				Intent intent = new Intent();
				if (!Network.isNetworkConnected(LogoActivity.this)) {
					Toast.makeText(LogoActivity.this, "网络没有连接，请查看网络设置", Toast.LENGTH_SHORT).show();
					intent.setClass(LogoActivity.this, LoginActivity.class);
				} else if (TextUtils.isEmpty(sessionId)) {
					intent.setClass(LogoActivity.this, LoginActivity.class);
				} else {
					intent.setClass(LogoActivity.this, GroundActivity.class);
				}
				startActivity(intent);
				finish();
			}
		};
		timer.schedule(task, 1500);
	}
}