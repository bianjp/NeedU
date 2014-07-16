package com.example.needu;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends Activity {
	public String serverUrl = GlobalData.SERVER + "/user/password";

	private EditText oldcodeEditText;
	private EditText newcodeEditText;
	private EditText ensurecodeEditText;
	
	private Button completeButton;
	private Button cancelButton;

	private String sessionId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		initViews();
		
		sessionId = getIntent().getStringExtra("sessionId");
	}

	
	private void initViews()
	{
		oldcodeEditText = (EditText)findViewById(R.id.oldcodeEditText);
		newcodeEditText = (EditText)findViewById(R.id.newcodeEditText);
		ensurecodeEditText = (EditText)findViewById(R.id.ensurecodeEditText);
		
		completeButton = (Button)findViewById(R.id.completeButton);
		cancelButton = (Button)findViewById(R.id.cancelButton);
		
		completeButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleChange();
			}
		});
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	private void handleChange() {
		String oldCode = oldcodeEditText.getText().toString();
		String newCode = newcodeEditText.getText().toString();
		String ensureCode = ensurecodeEditText.getText().toString();
		if (oldCode.length() == 0) {
			Toast.makeText(this, "旧密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (newCode.length() == 0) {
			Toast.makeText(this, "新密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (ensureCode.length() == 0) {
			Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (!newCode.equals(ensureCode)) {
			Toast.makeText(this, "新密码和确认密码不相同", Toast.LENGTH_SHORT).show();
		} else {
			change();
		}
	}
	
	private void change() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("oldPassword", oldcodeEditText.getText().toString()));
				params.add(new BasicNameValuePair("password", newcodeEditText.getText().toString()));
				
				serverUrl = serverUrl + "?sid=" + sessionId;
				HttpClient client = new DefaultHttpClient();
				HttpPut put = new HttpPut(serverUrl);
				HttpResponse response = null;
				try {
					put.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					response = client.execute(put);
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
				handleChangeResult(json);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleChangeResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		switch (resultStatus) {
		case 0:
			onChangeSuccess(json);
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
	
	private void onChangeSuccess(JSONObject json) {
		Toast.makeText(this, "修改密码成功", Toast.LENGTH_SHORT).show();

		finish();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
