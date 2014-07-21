package com.example.needu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GroundActivity extends Activity {
	public String serverUrl = Network.SERVER + "/helps/latest";
	private static final int NUM_PER_PAGE = 20;
	
	private EditText inputEditText;
	private Button searchButton;
	private Button rankButton;
	private Button personalButton;
	private Button setButton;
	private Button moreButton;
	private LinearLayout contentLayout;
	
	private int offset = 0;
	private String tags = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ground);
		
		initViews();
		
		getLatestNeeds(NUM_PER_PAGE, offset, tags);
	}
	
	private void initViews()
	{
		inputEditText = (EditText)findViewById(R.id.inputEditText);
		searchButton = (Button)findViewById(R.id.searchButton);
		rankButton = (Button)findViewById(R.id.rankButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
		moreButton = (Button)findViewById(R.id.moreButton);
		contentLayout = (LinearLayout)findViewById(R.id.content);
		
		searchButton.setOnClickListener(listener);
		rankButton.setOnClickListener(listener);
		personalButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		moreButton.setOnClickListener(listener);
		
		inputEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() == 0) {
					offset = 0;
					tags = "";
					contentLayout.removeAllViews();
					moreButton.setVisibility(View.GONE);
					getLatestNeeds(NUM_PER_PAGE, offset, tags);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.searchButton) {
				if (!TextUtils.isEmpty(inputEditText.getText())) {
					offset = 0;
					tags = inputEditText.getText().toString();
					contentLayout.removeAllViews();
					moreButton.setVisibility(View.GONE);
					getLatestNeeds(NUM_PER_PAGE, offset, tags);
				}
				return ;
			} else if (arg0.getId() == R.id.rankButton) {
				Intent intent = new Intent(GroundActivity.this, RankActivity.class);
				startActivity(intent);
				return ;
			} else if (arg0.getId() == R.id.moreButton) {
				getLatestNeeds(NUM_PER_PAGE, offset, tags);
				return ;
			}
			
			Intent intent = new Intent();
			if (arg0.getId() == R.id.personalButton) {
				intent.setClass(GroundActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(GroundActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getLatestNeeds(final int limit, final int offset, final String tags) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
				String sessionId = cookies.getString("sessionId", "");
				
				String tmpServerUrl = serverUrl + "?sid=" + sessionId
						+ "&limit=" + Integer.toString(limit + 1) + "&offset=" + Integer.toString(offset) + "&tags=" + tags;
				Log.e("alen", tmpServerUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpServerUrl);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Network.MSG_OK:
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
			switch (resultStatus) {
			case 0:
				onLatestNeedsSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onLatestNeedsSuccess(JSONObject json) {
		try {
			JSONArray jsonArray = json.getJSONArray("helps");
			if (jsonArray.length() == 0) {
				Toast.makeText(this, "未找到内容", Toast.LENGTH_SHORT).show();
				return ;
			}
			for (int i = 0; i < jsonArray.length(); i += 2) {
				LinearLayout helpBar = new LinearLayout(this);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMargins(0, DensityUtil.dip2px(this, 10), 0, 0);
				helpBar.setLayoutParams(params);
				helpBar.setOrientation(LinearLayout.HORIZONTAL);
				contentLayout.addView(helpBar);
				
				TextView textView1 = new TextView(this);
				LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(this, 100), 1f);
				params1.setMargins(0, 0, DensityUtil.dip2px(this, 5), 0);
				textView1.setLayoutParams(params1);
				textView1.setPadding(DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5));
				textView1.setBackgroundResource(R.drawable.bg_edittext_normal);
				textView1.setTextSize(15f);
				
				JSONObject help1 = jsonArray.optJSONObject(i);
				final String helpId1 = help1.getString("_id");
				String title1 = help1.getString("title");
				JSONArray tagsArray1 = help1.getJSONArray("tags");
				String tags1 = tagsArray1.optString(0);
				for (int j = 1; j < tagsArray1.length(); j++) {
					tags1 = tags1 + ';' + tagsArray1.optString(j);
				}
				textView1.setText(title1 + "\n" + "标签：" + tags1);
				textView1.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(GroundActivity.this, NeedDetailActivity.class);
						intent.putExtra("helpId", helpId1);
						startActivity(intent);
					}
				});
				helpBar.addView(textView1);
				
				if (i + 1 >= jsonArray.length())
					break;
				
				TextView textView2 = new TextView(this);
				LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(this, 100), 1f);
				params2.setMargins(DensityUtil.dip2px(this, 5), 0, 0, 0);
				textView2.setLayoutParams(params2);
				textView2.setPadding(DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5), DensityUtil.dip2px(this, 5));
				textView2.setBackgroundResource(R.drawable.bg_edittext_normal);
				textView2.setTextSize(15f);
				
				JSONObject help2 = jsonArray.optJSONObject(i + 1);
				final String helpId2 = help2.getString("_id");
				String title2 = help2.getString("title");
				JSONArray tagsArray2 = help1.getJSONArray("tags");
				String tags2 = tagsArray2.optString(0);
				for (int j = 1; j < tagsArray2.length(); j++) {
					tags2 = tags2 + ';' + tagsArray2.optString(j);
				}
				textView2.setText(title2 + "\n" + "标签：" + tags2);
				textView2.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(GroundActivity.this, NeedDetailActivity.class);
						intent.putExtra("helpId", helpId2);
						startActivity(intent);
					}
				});
				helpBar.addView(textView2);
				
				if (i + 2 >= NUM_PER_PAGE) {
					break;
				}
			}
			if (jsonArray.length() > NUM_PER_PAGE) {
				offset += NUM_PER_PAGE;
				moreButton.setVisibility(View.VISIBLE);
			} else {
				moreButton.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}