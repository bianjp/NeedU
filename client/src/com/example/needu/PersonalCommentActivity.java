package com.example.needu;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PersonalCommentActivity extends Activity {
	private String serverUrl = Network.SERVER + "/helps/commented";
	private static final int MSG_GET_PHOTO = 201;
	private String sessionId;
	private String name;
	private String college;
	private String photoUrl;
	
	private ImageView portrait;
	private TextView nameText;
	private TextView collegeText;
	private Button squareButton;
	private Button setButton;
	private Button personalDataButton;
	private Button announceButton;
	private Button interestButton;
	private ListView helpListView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_comment);
	
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		photoUrl = cookies.getString("photo", "");
		name = cookies.getString("name", "");
		college = cookies.getString("school", "");
		
		initViews();
		getPhoto(photoUrl);
		getCommentedHelps();
	}
	
	private void initViews()
	{
		portrait = (ImageView)findViewById(R.id.headpic);
		nameText = (TextView)findViewById(R.id.name);
		collegeText = (TextView)findViewById(R.id.college);
		nameText.append(name);
		collegeText.append(college);
		
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		announceButton = (Button)findViewById(R.id.announceButton);
		interestButton = (Button)findViewById(R.id.interestButton);
		helpListView = (ListView)findViewById(R.id.helpListView);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		announceButton.setOnClickListener(listener);
		interestButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.announceButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalAnnounceActivity.class);
			} else if (arg0.getId() == R.id.interestButton) {
				intent.setClass(PersonalCommentActivity.this, PersonalInterestActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalCommentActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalCommentActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getCommentedHelps() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				serverUrl = serverUrl + "?sid=" + sessionId;
				Network network = new Network();
				JSONObject json = network.get(serverUrl);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}
	
	private void getPhoto(final String photoUrl) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (!photoUrl.equals("null")) {
					try {
						URL url = new URL(Network.HOST + photoUrl);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setDoInput(true); 
					    conn.connect(); 
					    InputStream is = conn.getInputStream(); 
					    Bitmap bitmap = BitmapFactory.decodeStream(is); 
					    is.close();
					    sendMessage(MSG_GET_PHOTO, bitmap);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
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

			case MSG_GET_PHOTO:
				Bitmap bitmap = (Bitmap)msg.obj;
				portrait.setImageBitmap(bitmap);
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
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onGetSuccess(JSONObject json) {
		try {
			final JSONArray helpArray = json.getJSONArray("helps");
			if (helpArray.length() == 0) {
				helpListView.setAdapter(null);
				Toast.makeText(this, "你还未进行过评论", Toast.LENGTH_SHORT).show();
				return ;
			}
			ArrayList<HashMap<String, Object>> helpList = new ArrayList<HashMap<String,Object>>();
			for (int i = 0; i < helpArray.length(); i++) {
				JSONObject helpJson = helpArray.optJSONObject(i);
				HashMap<String, Object> help = new HashMap<String, Object>();
				
				String title = "标题：" + helpJson.getString("title");
				String content = helpJson.getString("content");
				JSONArray tagsArray = helpJson.getJSONArray("tags");
				String tags = "标签：" + tagsArray.optString(0);
				for (int j = 1; j < tagsArray.length(); j++) {
					tags = tags + ';' + tagsArray.optString(j);
				}
				String helpString = title + "\n" + content + "\n" + tags;
				help.put("content", helpString);
				helpList.add(help);
			}
			SimpleAdapter adapter = new SimpleAdapter(this, helpList, R.layout.help,
					new String[]{"content"}, new int[]{R.id.helpContent});
			helpListView.setAdapter(adapter);
			helpListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					JSONObject helpJson = helpArray.optJSONObject(position);
					try {
						String helpId = helpJson.getString("_id");
						Intent intent = new Intent(PersonalCommentActivity.this, NeedDetailActivity.class);
						intent.putExtra("helpId", helpId);
						startActivity(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}