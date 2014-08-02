package com.example.needu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GroundActivity extends Activity {
	private String getNotificationsUrl = Network.SERVER + "/notifications";
	private String deleteNotificationsUrl = Network.SERVER + "/notifications/all";
	private String getLatestUrl = Network.SERVER + "/helps/latest";
	private String getConcernsUrl = Network.SERVER + "/helps/concerns";
	private static final int MSG_GET_NOTIFICATIONS = 201;
	private static final int MSG_DELETE_NOTIFICATIONS = 202;
	private static final int MSG_GET_HELPS = 203;
	private static final int NUM_PER_PAGE = 20;
	private String sessionId;
	private String mode;
	private boolean viewAll = true;
	
	private EditText inputEditText;
	private Button searchButton;
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
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		mode = cookies.getString("mode", "ring");
		
		initViews();
		getNotifications();
		getLatestNeeds(NUM_PER_PAGE, 0, "");
	}

	private void initViews()
	{
		inputEditText = (EditText)findViewById(R.id.inputEditText);
		searchButton = (Button)findViewById(R.id.searchButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
		moreButton = (Button)findViewById(R.id.moreButton);
		contentLayout = (LinearLayout)findViewById(R.id.content);
		
		searchButton.setOnClickListener(listener);
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
					getLatestNeeds(NUM_PER_PAGE, 0, "");
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
					getLatestNeeds(NUM_PER_PAGE, 0, tags);
				}
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
	
	private void getNotifications() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpServerUrl = getNotificationsUrl + "?sid=" + sessionId;
				Log.e("alen", tmpServerUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpServerUrl);
				sendMessage(MSG_GET_NOTIFICATIONS, json);
			}
		}).start();
	}
	
	private void deleteAllNotifications() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpServerUrl = deleteNotificationsUrl + "?sid=" + sessionId;
				Log.e("alen", tmpServerUrl);
				Network network = new Network();
				JSONObject json = network.delete(tmpServerUrl);
				sendMessage(MSG_DELETE_NOTIFICATIONS, json);
			}
		}).start();
	}
	
	private void getLatestNeeds(final int limit, final int offset, final String tags) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpServerUrl = getLatestUrl + "?sid=" + sessionId
						+ "&limit=" + Integer.toString(limit + 1) + "&offset=" + Integer.toString(offset) + "&tags=" + tags;
				Log.e("alen", tmpServerUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpServerUrl);
				sendMessage(MSG_GET_HELPS, json);
			}
		}).start();
	}
	
	private void getConcernNeeds() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpServerUrl = getConcernsUrl + "?sid=" + sessionId;
				Log.e("alen", tmpServerUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpServerUrl);
				sendMessage(MSG_GET_HELPS, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_NOTIFICATIONS:
				JSONObject json = (JSONObject)msg.obj;
				handleNotificationResult(json);
				break;
				
			case MSG_DELETE_NOTIFICATIONS:
				JSONObject json2 = (JSONObject)msg.obj;
				handleDeleteNotificationResult(json2);
				break;
			
			case MSG_GET_HELPS:
				JSONObject json3 = (JSONObject)msg.obj;
				handleLatestNeedsResult(json3);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleNotificationResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onNotificationSuccess(json);
				break;

			default:
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void handleDeleteNotificationResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				// TODO
				break;

			default:
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void handleLatestNeedsResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onLatestNeedsSuccess(json);
				break;

			default:
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onNotificationSuccess(JSONObject json) {
		try {
			JSONArray noteArray = json.getJSONArray("notifications");
			NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			for (int i = 0; i < noteArray.length(); i++) {
				JSONObject noteJSON = noteArray.optJSONObject(i);
				if (noteJSON.has("followerId")) {
					String followerId = noteJSON.getString("followerId");
					String followerName = noteJSON.getString("followerName");
					Intent intent = new Intent(this, OtherPersonActivity.class);
					intent.putExtra("userId", followerId);
					PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
					Notification notification = new Notification();
					notification.icon = R.drawable.icon;
					notification.tickerText = followerName + "关注了你";
					if (mode.equals("ring")) {
						notification.defaults = Notification.DEFAULT_SOUND;
					} else if (mode.equals("vibrate")) {
						notification.defaults = Notification.DEFAULT_VIBRATE;
					}
					notification.setLatestEventInfo(this, "NeedU", followerName + "关注了你", pendingIntent);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notificationManager.notify(i, notification);
				} else if (noteJSON.has("type")) {
					String helpId = noteJSON.getString("helpId");
					Intent intent = new Intent(this, NeedDetailActivity.class);
					intent.putExtra("helpId", helpId);
					PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
					Notification notification = new Notification();
					notification.icon = R.drawable.icon;
					notification.tickerText = "有新评论";
					if (mode.equals("ring")) {
						notification.defaults = Notification.DEFAULT_SOUND;
					} else if (mode.equals("vibrate")) {
						notification.defaults = Notification.DEFAULT_VIBRATE;
					}
					notification.setLatestEventInfo(this, "NeedU", "有新评论", pendingIntent);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					notificationManager.notify(i, notification);
				}
			}
			if (noteArray.length() > 0) {
				deleteAllNotifications();
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("alen", e.toString());
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
				JSONArray tagsArray2 = help2.getJSONArray("tags");
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ground, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.menu_new) {
			Intent intent = new Intent(this, NewNeedActivity.class);
			startActivity(intent);
		} else if (id == R.id.menu_refresh) {
			contentLayout.removeAllViews();
			moreButton.setVisibility(View.GONE);
			if (viewAll) {
				offset = 0;
				getLatestNeeds(NUM_PER_PAGE, 0, "");
			} else {
				getConcernNeeds();
			}
		} else if (id == R.id.action_all && !viewAll) {
			viewAll = true;
			searchButton.setClickable(true);
			offset = 0;
			tags = "";
			contentLayout.removeAllViews();
			moreButton.setVisibility(View.GONE);
			getLatestNeeds(NUM_PER_PAGE, 0, "");
		} else if (id == R.id.action_concern && viewAll) {
			viewAll = false;
			searchButton.setClickable(false);
			contentLayout.removeAllViews();
			moreButton.setVisibility(View.GONE);
			getConcernNeeds();
		}
		return super.onOptionsItemSelected(item);
	}
}