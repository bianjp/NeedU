package com.example.needu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NeedDetailActivity extends Activity {
	private String getHelpUrl = Network.SERVER + "/help/";
	private String getUserUrl = Network.SERVER + "/user/";
	private String postCommentUrl = Network.SERVER + "/comment/help/";
	private static final int MSG_GET_HELP = 201;
	private static final int MSG_GET_USER = 202;
	private static final int MSG_POST_COMMENT = 203;
	private String helpId;
	private String sessionId;
	
	private TextView nameTextView;
	private TextView dateTextView;
	private TextView newsTextView;
	private LinearLayout commentLayout;
	private EditText commentEditText;
	private Button commentButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_need_detail);
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		helpId = getIntent().getStringExtra("helpId");
		
		initViews();
		
		getDetail(getHelpUrl + helpId, MSG_GET_HELP);
	}
	
	private void initViews()
	{
		nameTextView = (TextView)findViewById(R.id.nameText);
		dateTextView = (TextView)findViewById(R.id.dateText);
		newsTextView = (TextView)findViewById(R.id.newsText);
		commentLayout = (LinearLayout)findViewById(R.id.commentLayout);
		commentEditText = (EditText)findViewById(R.id.commentEditText);
		commentButton = (Button)findViewById(R.id.commentButton);
		
		commentButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postComment();
			}
		});
	}
	
	private void getDetail(final String serverUrl, final int msg) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				sendMessage(msg, json);
			}
		}).start();
	}
	
	private void postComment() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = postCommentUrl + helpId + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("content", commentEditText.getText().toString()));
				
				Network network = new Network();
				JSONObject json = network.post(tmpUrl, params);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_HELP:
			case MSG_GET_USER:
			case MSG_POST_COMMENT:
				JSONObject json = (JSONObject)msg.obj;
				handleResult(json, msg.what);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleResult(JSONObject json, int msg) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				switch (msg) {
				case MSG_GET_HELP:
					onNeedDetailSuccess(json);
					break;

				case MSG_GET_USER:
					onUserDetailSuccess(json);
					break;
				
				case MSG_POST_COMMENT:
					onCommentSuccess(json);
					break;
				
				default:
					break;
				}
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onNeedDetailSuccess(JSONObject json) {
		try {
			JSONObject help = json.getJSONObject("help");
			String createdAt = help.getString("createdAt");
			String createdBy = help.getString("createdBy");
			String title = help.getString("title");
			String content = help.getString("content");
			JSONArray tagsArray = help.getJSONArray("tags");
			String tags = tagsArray.optString(0);
			for (int i = 1; i < tagsArray.length(); i++) {
				tags = tags + ';' + tagsArray.optString(i);
			}
			
			getDetail(getUserUrl + createdBy, MSG_GET_USER);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			SimpleDateFormat output = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
			output.setTimeZone(TimeZone.getTimeZone("PRC"));
			String time = output.format(sdf.parse(createdAt));
			
			dateTextView.setText(time);
			newsTextView.setText("标题：" + title + "\n" + content + "\n" + "标签：" + tags);
			
			JSONArray commentArray = json.getJSONArray("comments");
			for (int i = 0; i < commentArray.length(); i++) {
				RelativeLayout commentBar = new RelativeLayout(this);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				commentBar.setLayoutParams(params);
				commentLayout.addView(commentBar);
				
				ImageView imageView = new ImageView(this);
				RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(DensityUtil.dip2px(this, 43), DensityUtil.dip2px(this, 43));
				params1.setMargins(DensityUtil.dip2px(this, 10), 0, 0, 0);
				imageView.setLayoutParams(params1);
				imageView.setImageResource(R.drawable.ic_launcher);
				commentBar.addView(imageView);
				
				
				
				JSONObject comment = commentArray.optJSONObject(i);
				String at = comment.getString("createdAt");
				String by = comment.getString("createdBy");
				String cont = comment.getString("content");
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onUserDetailSuccess(JSONObject json) {
		try {
			JSONObject profile = json.getJSONObject("profile");
			String name = profile.getString("name");
			nameTextView.setText(name);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onCommentSuccess(JSONObject json) {
		commentEditText.setText("");
		Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();	
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}