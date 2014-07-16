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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public String serverUrl = GlobalData.SERVER + "/user/authentication";
	
	private EditText loginName;
	private EditText loginPassword;
	
	private Button loginButton;
	private Button forgetPasswordButton;
	private Button registerButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		if (!isNetworkConnected(this)) {
			Toast.makeText(LoginActivity.this, "网络没有连接，请查看网络设置", Toast.LENGTH_SHORT).show();
		}
		
		initViews();
	}
	
	public boolean isNetworkConnected(Context context) { 
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
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
		String username = loginName.getText().toString();
		String password = loginPassword.getText().toString();
		if (username.length() == 0) {
			Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
		} else if (password.length() == 0) {
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
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		switch (resultStatus) {
		case 0:
			onLoginSuccess(json);
			break;

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
	
	private void onLoginSuccess(JSONObject json) {
		Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
		
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
