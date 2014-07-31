package com.example.needu;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends Activity {
	private String serverUrl = Network.SERVER + "/user/password";

	private EditText oldcodeEditText;
	private EditText newcodeEditText;
	private EditText ensurecodeEditText;
	
	private Button completeButton;
	private Button cancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

		initViews();
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
		if (TextUtils.isEmpty(oldcodeEditText.getText())) {
			Toast.makeText(this, "旧密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(newcodeEditText.getText())) {
			Toast.makeText(this, "新密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(ensurecodeEditText.getText())) {
			Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
		} else if (!TextUtils.equals(newcodeEditText.getText(), ensurecodeEditText.getText())) {
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
				
				SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
				String sessionId = cookies.getString("sessionId", "");
				
				serverUrl = serverUrl + "?sid=" + sessionId;
				Network network = new Network();
				JSONObject json = network.put(serverUrl, params);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Network.MSG_OK:
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
			switch (resultStatus) {
			case 0:
				onChangeSuccess(json);
				break;

			default:
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
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