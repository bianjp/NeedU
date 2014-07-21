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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	public String serverUrl = Network.SERVER + "/user";
	
	private EditText registerName;
	private EditText registerSchoolYear;
	private EditText registerSchool;
	private EditText registerMajor;
	private EditText registerBirthday;
	private EditText registerPhone;
	private EditText registerQQ;
	private EditText registerUsername;
	private EditText registerWechat;
	private EditText registerPassword;
	private EditText registerPasswordConfirm;
	
	private RadioButton maleButton;
	private RadioButton femaleButton;
	
	private CheckBox registerCheckBox;
	private Button registerButton;
	private Button loginButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		initViews();
	}
	
	private void initViews() {
		registerName = (EditText)findViewById(R.id.editText1);
		registerSchoolYear = (EditText)findViewById(R.id.editText2);
		registerSchool = (EditText)findViewById(R.id.editText3);
		registerMajor = (EditText)findViewById(R.id.editText5);
		registerBirthday = (EditText)findViewById(R.id.editText7);
		registerPhone = (EditText)findViewById(R.id.editText6);
		registerQQ = (EditText)findViewById(R.id.editText8);
		registerUsername = (EditText)findViewById(R.id.editText9);
		registerWechat = (EditText)findViewById(R.id.editText11);
		registerPassword = (EditText)findViewById(R.id.editText10);
		registerPasswordConfirm = (EditText)findViewById(R.id.editText12);
		
		maleButton = (RadioButton)findViewById(R.id.radioButton1);
		femaleButton = (RadioButton)findViewById(R.id.radioButton2);
		
		registerCheckBox = (CheckBox)findViewById(R.id.checkBox1);
		registerButton = (Button)findViewById(R.id.button1);
		loginButton = (Button)findViewById(R.id.button2);
		
		registerButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleRegister();
			}
		});
		loginButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}
	
	private void handleRegister() {
		if (TextUtils.isEmpty(registerUsername.getText())) {
			Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(registerPassword.getText())) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(registerPasswordConfirm.getText())) {
			Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (!TextUtils.equals(registerPassword.getText(), registerPasswordConfirm.getText())) {
			Toast.makeText(this, "密码和确认密码不相同", Toast.LENGTH_SHORT).show();
		} else if (!registerCheckBox.isChecked()) {
			Toast.makeText(this, "请确认服务条款", Toast.LENGTH_SHORT).show();
		} else {
			register();
		}
	}
	
	private void register() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("username", registerUsername.getText().toString()));
				params.add(new BasicNameValuePair("password", registerPassword.getText().toString()));
				params.add(new BasicNameValuePair("school", registerSchool.getText().toString()));
				params.add(new BasicNameValuePair("major", registerMajor.getText().toString()));
				params.add(new BasicNameValuePair("schoolYear", registerSchoolYear.getText().toString()));
				params.add(new BasicNameValuePair("name", registerName.getText().toString()));
				params.add(new BasicNameValuePair("birthday", registerBirthday.getText().toString()));
				params.add(new BasicNameValuePair("phone", registerPhone.getText().toString()));
				params.add(new BasicNameValuePair("wechat", registerWechat.getText().toString()));
				params.add(new BasicNameValuePair("QQ", registerQQ.getText().toString()));
				
				if (maleButton.isChecked()) {
					params.add(new BasicNameValuePair("gender", "male"));
				} else if (femaleButton.isChecked()) {
					params.add(new BasicNameValuePair("gender", "female"));
				}
				
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
				handleRegisterResult(json);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleRegisterResult(JSONObject json) {
		int resultStatus = -3;
		
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onRegisterSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onRegisterSuccess(JSONObject json) {
		Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
		
		String sessionId = null;
		String studentId = null;
		try {
			sessionId = json.getString("sid");
			JSONObject user = json.getJSONObject("user");
			studentId = user.getString("_id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		Editor editor = cookies.edit();
		editor.putString("sessionId", sessionId);
		editor.putString("studentId", studentId);
		editor.commit();
		
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