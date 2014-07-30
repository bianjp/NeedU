package com.example.needu;

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
import android.widget.TextView;
import android.widget.Toast;

public class OtherPersonActivity extends Activity {
	private String getInfoUrl = Network.SERVER + "/user/";
	private String followUrl = Network.SERVER + "/concern/";
	private String userId;
	private String sessionId;
	private static final int MSG_GET_INFO = 201;
	private static final int MSG_FOLLOW = 202;
	private static final int MSG_CANCEL = 203;
	private boolean concerned;
	
	private TextView nameText;
	private TextView collegeText;
	private Button followButton;
	
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_person);
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		userId = getIntent().getStringExtra("userId");
		initViews();
		getPersonalData();
	}
	
	private void initViews()
	{
		nameText = (TextView)findViewById(R.id.name);
		collegeText = (TextView)findViewById(R.id.college);
		followButton = (Button)findViewById(R.id.follow);
		
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
	}
	
	private void getPersonalData() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String url = getInfoUrl + userId + "?sid=" + sessionId;
				Log.e("alen", url);
				Network network = new Network();
				JSONObject json = network.get(url);
				sendMessage(MSG_GET_INFO, json);
			}
		}).start();
	}
	
	private void follow() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String url = followUrl + userId + "?sid=" + sessionId;
				Log.e("alen", url);
				Network network = new Network();
				JSONObject json = network.post(url, null);
				sendMessage(MSG_FOLLOW, json);
			}
		}).start();
	}
	
	private void cancel() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String url = followUrl + userId + "?sid=" + sessionId;
				Log.e("alen", url);
				Network network = new Network();
				JSONObject json = network.delete(url);
				sendMessage(MSG_CANCEL, json);
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

			case MSG_FOLLOW:
				JSONObject json2 = (JSONObject)msg.obj;
				handleFollowResult(json2);
				break;
			
			case MSG_CANCEL:
				JSONObject json3 = (JSONObject)msg.obj;
				handleCancelResult(json3);
			
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
	
	private void handleFollowResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onFollowSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void handleCancelResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onCancelSuccess(json);
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
			
			concerned = json.getBoolean("concerned");
			if (concerned) {
				followButton.setText("取消关注");
			}
			followButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!concerned) {
						follow();
					} else {
						cancel();
					}	
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onFollowSuccess(JSONObject json) {
		followButton.setText("取消关注");
		Toast.makeText(this, "关注成功", Toast.LENGTH_SHORT).show();
	}
	
	private void onCancelSuccess(JSONObject json) {
		followButton.setText("+关注");
		Toast.makeText(this, "已取消关注", Toast.LENGTH_SHORT).show();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}