package com.example.needu;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.TextView;
import android.widget.Toast;

public class NeedDetailActivity extends Activity {
	private String getHelpUrl = Network.SERVER + "/help/";
	private String getUserUrl = Network.SERVER + "/user/";
	private String postCommentUrl = Network.SERVER + "/comment/help/";
	private static final int MSG_GET_HELP = 201;
	private static final int MSG_GET_COMMENT = 202;
	private static final int MSG_POST_COMMENT = 203;
	private String helpId;
	private String sessionId;
	
	private TextView nameTextView;
	private TextView dateTextView;
	private TextView newsTextView;
//	private LinearLayout commentLayout;
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
		
		getHelp(getHelpUrl + helpId);
	}
	
	private void initViews()
	{
		nameTextView = (TextView)findViewById(R.id.nameText);
		dateTextView = (TextView)findViewById(R.id.dateText);
		newsTextView = (TextView)findViewById(R.id.newsText);
//		commentLayout = (LinearLayout)findViewById(R.id.commentLayout);
		commentEditText = (EditText)findViewById(R.id.commentEditText);
		commentButton = (Button)findViewById(R.id.commentButton);
		
		commentButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postComment();
			}
		});
	}
	
	private void getHelp(final String serverUrl) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				Help help = handleHelpJSON(json);
				sendMessage(MSG_GET_HELP, help);
			}
		}).start();
	}
	
	private void getComment(final String serverUrl) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				ArrayList<HashMap<String, Object>> commentList = handleCommentJSON(json);
				sendMessage(MSG_GET_COMMENT, commentList);
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
				Help help = (Help)msg.obj;
				updateHelpUI(help);
				break;
				
			case MSG_GET_COMMENT:
				ArrayList<HashMap<String, Object>> commentList = (ArrayList<HashMap<String, Object>>)msg.obj;
				updateCommentUI(commentList);
				break;
				
			case MSG_POST_COMMENT:
				break;

			default:
				break;
			}
		}
	};
	
	private Help handleHelpJSON(JSONObject json) {
		int resultStatus = -3;
		Help help = new Help();
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				getComment(getHelpUrl + helpId + "/comments");
				
				help.status = true;
				JSONObject helpJson = json.getJSONObject("help");
				
				String createdBy = helpJson.getString("createdBy");
				help.name = getUsername(createdBy);
				String createdAt = helpJson.getString("createdAt");
				help.time = convertDate(createdAt);
				
				help.title = helpJson.getString("title");
				help.content = helpJson.getString("content");
				JSONArray tagsArray = helpJson.getJSONArray("tags");
				String tags = tagsArray.optString(0);
				for (int i = 1; i < tagsArray.length(); i++) {
					tags = tags + ';' + tagsArray.optString(i);
				}
				help.tags = tags;
				
				break;

			default:
				help.status = false;
				help.msg = json.getString("msg");
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return help;
	}
	
	private ArrayList<HashMap<String, Object>> handleCommentJSON(JSONObject json) {
		int resultStatus = -3;
		ArrayList<HashMap<String, Object>> commentList = new ArrayList<HashMap<String,Object>>();
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				JSONArray commentArray = json.getJSONArray("comments");
				for (int i = 0; i < commentArray.length(); i++) {
					JSONObject commentJson = commentArray.optJSONObject(i);
					HashMap<String, Object> comment = new HashMap<String, Object>();
					
					String createdBy = commentJson.getString("createdBy");
					comment.put("name", getUsername(createdBy));
					String createdAt = commentJson.getString("createdAt");
					comment.put("date", convertDate(createdAt));
					String content = commentJson.getString("content");
					comment.put("content", content);
					
					commentList.add(comment);
				}
				break;

			default:
				// TODO
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return commentList;
	}
/*	
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
*/
	
	private void updateHelpUI(Help help) {
		if (help.status) {
			nameTextView.setText(help.name);
			dateTextView.setText(help.time);
			newsTextView.setText("标题：" + help.title + "\n" + help.content + "\n" + "标签：" + help.tags);
		} else {
			try {
				Toast.makeText(this, new String(help.msg.getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void updateCommentUI(ArrayList<HashMap<String, Object>> commentList) {
		Log.e("alen", commentList.toString());
	}
	
/*	
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
			
			getHelp(getUserUrl + createdBy, MSG_GET_USER);
//			String tmpUrl = getUserUrl + createdBy + "?sid=" + sessionId;
//			Log.e("alen", tmpUrl);
//			Network network = new Network();
//			JSONObject json2 = network.get(tmpUrl);
//			Log.e("alen", json2.toString());
			
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
*/
	
	private String getUsername(String id) {
		String username = null;
		try {
			String tmpUrl = getUserUrl + id + "?sid=" + sessionId;
			Log.e("alen", tmpUrl);
			Network network = new Network();
			JSONObject userJson = network.get(tmpUrl);
			JSONObject profile = userJson.getJSONObject("profile");
			username = profile.getString("name");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return username;
	}
	
	private String convertDate(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat output = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);
		output.setTimeZone(TimeZone.getTimeZone("PRC"));
		String shortDate = null;
		try {
			shortDate = output.format(sdf.parse(date));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return shortDate;
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
	
	private class Help {
		public Boolean status;
		public String msg;
		public String name;
		public String time;
		public String title;
		public String content;
		public String tags;
		public String commentCount;
	}
	
	private class Comment {
		public Boolean status;
		public String msg;
		public String name;
		public String time;
		public String content;
	}
}