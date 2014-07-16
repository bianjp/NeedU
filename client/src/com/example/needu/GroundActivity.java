package com.example.needu;

import java.io.UnsupportedEncodingException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GroundActivity extends Activity {
	public String serverUrl = GlobalData.SERVER + "/helps/latest";
	
	private EditText inputEditText;
	private Button searchButton;
	private Button rankButton;
	private Button personalButton;
	private Button setButton;
	
	private String sessionId;
	private String studentId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ground);
		
		initViews();
		
		sessionId = getIntent().getStringExtra("sessionId");
		studentId = getIntent().getStringExtra("studentId");
		
		getLatestNeeds();
	}
	
	private void initViews()
	{
		inputEditText = (EditText)findViewById(R.id.inputEditText);
		searchButton = (Button)findViewById(R.id.searchButton);
		rankButton = (Button)findViewById(R.id.rankButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
		
		searchButton.setOnClickListener(listener);
		rankButton.setOnClickListener(listener);
		personalButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.searchButton) {
				return ;
			} else if (arg0.getId() == R.id.rankButton) {
				Intent intent = new Intent(GroundActivity.this, RankActivity.class);
				startActivity(intent);
				return ;
			}
			
			Intent intent = new Intent();
			intent.putExtra("sessionId", sessionId);
			intent.putExtra("studentId", studentId);
			if (arg0.getId() == R.id.personalButton) {
				intent.setClass(GroundActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(GroundActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getLatestNeeds() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				serverUrl = serverUrl + "?sid=" + sessionId + "&limit=6&offset=0&tag=t";
				Log.e("alen", serverUrl);
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(serverUrl);
				HttpResponse response = null;
				try {
					response = client.execute(get);
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
				handleLatestNeedsResult(json);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleLatestNeedsResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		switch (resultStatus) {
		case 0:
			onLatestNeedsSuccess(json);
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
	
	private void onLatestNeedsSuccess(JSONObject json) {
		
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}
