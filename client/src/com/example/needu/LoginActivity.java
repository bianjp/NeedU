package com.example.needu;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private String serverUrl = Network.SERVER + "/user/authentication";
	
	private EditText loginName;
	private EditText loginPassword;
	
	private Button loginButton;
	private Button forgetPasswordButton;
	private Button registerButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		initViews();
	}
	
	private void initViews() {
		loginName = (EditText)findViewById(R.id.loginName);
		loginPassword = (EditText)findViewById(R.id.loginPassword);
		
		loginButton = (Button)findViewById(R.id.loginButton);
		forgetPasswordButton = (Button)findViewById(R.id.forgetPasswordButton);
		registerButton = (Button)findViewById(R.id.registerButton);
		
		loginButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleLogin();
			}
		});
		forgetPasswordButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(LoginActivity.this, "我暂时也没有办法，谁叫你忘记密码的", Toast.LENGTH_SHORT).show();
			}
		});
		registerButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	private void handleLogin() {
		if (TextUtils.isEmpty(loginName.getText())) {
			Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(loginPassword.getText())) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
		} else {
			login();
		}
	}
	
	private void login() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", loginName.getText().toString()));
				params.add(new BasicNameValuePair("password", loginPassword.getText().toString()));
				
				Network network = new Network();
				JSONObject json = network.post(serverUrl, params);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Network.MSG_OK:
				JSONObject json = (JSONObject)msg.obj;
				handleLoginResult(json);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleLoginResult(JSONObject json) {
		int resultStatus = -3;
		
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onLoginSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onLoginSuccess(JSONObject json) {
		Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
		
		try {
			String sessionId = json.getString("sid");
			JSONObject user = json.getJSONObject("user");
			String studentId = user.getString("_id");
			JSONObject profile = user.getJSONObject("profile");
			String name = profile.getString("name");
			String school = profile.getString("school");
			
			SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
			Editor editor = cookies.edit();
			editor.putString("sessionId", sessionId);
			editor.putString("studentId", studentId);
			editor.putString("name", name);
			editor.putString("school", school);
			editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Intent intent = new Intent(this, GroundActivity.class);
		startActivity(intent);
		finish();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}