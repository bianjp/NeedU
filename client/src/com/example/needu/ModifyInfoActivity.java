package com.example.needu;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyInfoActivity extends Activity {
	private String serverUrl = Network.SERVER + "/user";
	private static final int MSG_GET_INFO = 201;
	private static final int MSG_PUT_INFO = 202;
	private String sessionId;
	private String studentId;
	private String sex;
	
	private EditText nameEditText;
	private EditText schoolYearEditText;
	private EditText schoolEditText;
	private EditText majorEditText;
	private EditText birthdayEditText;
	private EditText phoneEditText;
	private EditText qqEditText;
	private EditText wechatEditText;
	private EditText descriptionEditText;

	private Button finishButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_info);
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		studentId = cookies.getString("studentId", "");
		
		initViews();
		
		getInfo();
	}
	
	private void initViews() {
		nameEditText = (EditText)findViewById(R.id.nicknameEditText);
		schoolYearEditText = (EditText)findViewById(R.id.schoolYearEditText);
		schoolEditText = (EditText)findViewById(R.id.schoolEditText);
		majorEditText = (EditText)findViewById(R.id.majorEditText);
		birthdayEditText = (EditText)findViewById(R.id.birthdayEditText);
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		qqEditText = (EditText)findViewById(R.id.qqEditText);
		wechatEditText = (EditText)findViewById(R.id.wechatEditText);
		descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
		
		finishButton = (Button)findViewById(R.id.finishButton);
		finishButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				putInfo();
			}
		});
	}
	
	private void getInfo() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = serverUrl + '/' + studentId + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				sendMessage(MSG_GET_INFO, json);
			}
		}).start();
	}
	
	private void putInfo() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("school", schoolEditText.getText().toString()));
				params.add(new BasicNameValuePair("major", majorEditText.getText().toString()));
				params.add(new BasicNameValuePair("schoolYear", schoolYearEditText.getText().toString()));
				params.add(new BasicNameValuePair("name", nameEditText.getText().toString()));
				params.add(new BasicNameValuePair("gender", sex));
				params.add(new BasicNameValuePair("birthday", birthdayEditText.getText().toString()));
				params.add(new BasicNameValuePair("phone", phoneEditText.getText().toString()));
				params.add(new BasicNameValuePair("wechat", wechatEditText.getText().toString()));
				params.add(new BasicNameValuePair("QQ", qqEditText.getText().toString()));
				params.add(new BasicNameValuePair("description", descriptionEditText.getText().toString()));
				
				String tmpUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.put(tmpUrl, params);
				sendMessage(MSG_PUT_INFO, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_INFO:
				JSONObject json = (JSONObject)msg.obj;
				handleGetResult(json);
				break;
			
			case MSG_PUT_INFO:
				JSONObject json2 = (JSONObject)msg.obj;
				handlePutResult(json2);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleGetResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onGetSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void handlePutResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onPutSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onGetSuccess(JSONObject json) {
		try {
			JSONObject profile = json.getJSONObject("profile");
			sex = profile.getString("gender");
			nameEditText.setText(profile.getString("name").equals("null")?"":profile.getString("name"));
			schoolYearEditText.setText(profile.getString("schoolYear").equals("null")?"":profile.getString("schoolYear"));
			schoolEditText.setText(profile.getString("school").equals("null")?"":profile.getString("school"));
			majorEditText.setText(profile.getString("major").equals("null")?"":profile.getString("major"));
			birthdayEditText.setText(profile.getString("birthday").equals("null")?"":profile.getString("birthday"));
			phoneEditText.setText(profile.getString("phone").equals("null")?"":profile.getString("phone"));
			qqEditText.setText(profile.getString("QQ").equals("null")?"":profile.getString("QQ"));
			wechatEditText.setText(profile.getString("wechat").equals("null")?"":profile.getString("wechat"));
			descriptionEditText.setText(profile.getString("description").equals("null")?"":profile.getString("description"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onPutSuccess(JSONObject json) {
		Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}