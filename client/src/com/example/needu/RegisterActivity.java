package com.example.needu;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	public String serverUrl = GlobalData.SERVER + "/user";
	
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
		String username = registerUsername.getText().toString();
		String password = registerPassword.getText().toString();
		String passwordAgain = registerPasswordConfirm.getText().toString();
		if (username.length() == 0) {
			Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
		} else if (password.length() == 0) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (passwordAgain.length() == 0) {
			Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (!password.equals(passwordAgain)) {
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
				/* for test
				params.add(new BasicNameValuePair("username", "31331281"));
				params.add(new BasicNameValuePair("password", "zhoujielun"));
				params.add(new BasicNameValuePair("school", "software"));
				params.add(new BasicNameValuePair("major", "software"));
				params.add(new BasicNameValuePair("schoolYear", "2011"));
				params.add(new BasicNameValuePair("name", "alen"));
				params.add(new BasicNameValuePair("birthday", "1992"));
				params.add(new BasicNameValuePair("gender", "male"));
				params.add(new BasicNameValuePair("phone", "668"));
				params.add(new BasicNameValuePair("wechat", "alen"));
				params.add(new BasicNameValuePair("QQ", "304"));
				*/
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
				
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(serverUrl);
				HttpResponse response = null;
				try {
					post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					response = client.execute(post);
					Log.e("alen", "code:" + response.getStatusLine().getStatusCode());
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						String entityString = EntityUtils.toString(entity);
						Log.e("alen", new String(entityString.getBytes("iso-8859-1"),"UTF-8"));
				//		String jsonString = entityString.substring(entityString.indexOf("{"));
				//		Log.e("alen", jsonString);
						JSONObject json = new JSONObject(entityString);
						sendMessage(GlobalData.MSG_OK, json);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalData.MSG_OK:
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
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		switch (resultStatus) {
		case 0:
			onRegisterSuccess(json);
			break;

		// TODO
		
		default:
			try {
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
			} catch (JSONException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			break;
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
		
		// TODO
		Intent intent = new Intent(this, GroundActivity.class);
		intent.putExtra("sessionId", sessionId);
		intent.putExtra("studentId", studentId);
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