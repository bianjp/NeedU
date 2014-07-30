package com.example.needu;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalDataActivity extends Activity {
	private String serverUrl = Network.SERVER + "/user/";
	
	private TextView nameText;
	private TextView collegeText;
	private TextView descriptionText;
	private TextView schoolText;
	private TextView majorText;
	private TextView schoolYearText;
	private TextView name2Text;
	private TextView genderText;
	private TextView birthdayText;
	private TextView phoneText;
	private TextView wechatText;
	private TextView qqText;
	
	private Button squareButton;
	private Button setButton;
	private Button announceButton;
	private Button commentButton;
	private Button interestButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_data);
		
		initViews();
		
		getPersonalData();
	}
	
	private void initViews()
	{
		nameText = (TextView)findViewById(R.id.name);
		collegeText = (TextView)findViewById(R.id.college);
		descriptionText = (TextView)findViewById(R.id.personal_description);
		name2Text = (TextView)findViewById(R.id.name2);
		schoolText = (TextView)findViewById(R.id.school);
		majorText = (TextView)findViewById(R.id.major);
		schoolYearText = (TextView)findViewById(R.id.schoolYear);
		genderText = (TextView)findViewById(R.id.sexual);
		birthdayText = (TextView)findViewById(R.id.birthday);
		phoneText = (TextView)findViewById(R.id.telephone);
		qqText = (TextView)findViewById(R.id.QQ);
		wechatText = (TextView)findViewById(R.id.wechat);
		
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		announceButton = (Button)findViewById(R.id.announceButton);
		commentButton = (Button)findViewById(R.id.commentButton);
		interestButton = (Button)findViewById(R.id.interestButton);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		announceButton.setOnClickListener(listener);
		commentButton.setOnClickListener(listener);
		interestButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			if (arg0.getId() == R.id.announceButton) {
				intent.setClass(PersonalDataActivity.this, PersonalAnnounceActivity.class);
			} else if (arg0.getId() == R.id.commentButton) {
				intent.setClass(PersonalDataActivity.this, PersonalCommentActivity.class);
			} else if (arg0.getId() == R.id.interestButton) {
				intent.setClass(PersonalDataActivity.this, PersonalInterestActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalDataActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalDataActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getPersonalData() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
				String sessionId = cookies.getString("sessionId", "");
				String studentId = cookies.getString("studentId", "");
				
				serverUrl = serverUrl + studentId + "?sid=" + sessionId;
				Log.e("alen", serverUrl);
				Network network = new Network();
				JSONObject json = network.get(serverUrl);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Network.MSG_OK:
				JSONObject json = (JSONObject)msg.obj;
				handleGetResult(json);
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
	
	private void onGetSuccess(JSONObject json) {
		try {
			JSONObject profile = json.getJSONObject("profile");
			nameText.append(profile.getString("name").equals("null")?"未填写":profile.getString("name"));
			collegeText.append(profile.getString("school").equals("null")?"未填写":profile.getString("school"));
			descriptionText.append(profile.getString("description").equals("null")?"未填写":profile.getString("description"));
			name2Text.append(profile.getString("name").equals("null")?"未填写":profile.getString("name"));
			schoolText.append(profile.getString("school").equals("null")?"未填写":profile.getString("school"));
			majorText.append(profile.getString("major").equals("null")?"未填写":profile.getString("major"));
			schoolYearText.append(profile.getString("schoolYear").equals("null")?"未填写":profile.getString("schoolYear"));
			genderText.append(profile.getString("gender").equals("male")?"男":"女");
			birthdayText.append(profile.getString("birthday").equals("null")?"未填写":profile.getString("birthday"));
			phoneText.append(profile.getString("phone").equals("null")?"未填写":profile.getString("phone"));
			qqText.append(profile.getString("QQ").equals("null")?"未填写":profile.getString("QQ"));
			wechatText.append(profile.getString("wechat").equals("null")?"未填写":profile.getString("wechat"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}