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
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
	
	private ImageView protraitImageView;
	private TextView nameTextView;
	private TextView dateTextView;
	private TextView newsTextView;
	private TextView countTextView;
	private ListView commentListView;
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
		
		getHelp();
	}
	
	private void initViews()
	{
		protraitImageView = (ImageView)findViewById(R.id.protrait);
		nameTextView = (TextView)findViewById(R.id.nameText);
		dateTextView = (TextView)findViewById(R.id.dateText);
		newsTextView = (TextView)findViewById(R.id.newsText);
		countTextView = (TextView)findViewById(R.id.countText);
		commentListView = (ListView)findViewById(R.id.commentList);
		commentEditText = (EditText)findViewById(R.id.commentEditText);
		commentButton = (Button)findViewById(R.id.commentButton);
		
		commentButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postComment();
			}
		});
	}
	
	private void getHelp() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = getHelpUrl + helpId + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				Help help = handleHelpJSON(json);
				sendMessage(MSG_GET_HELP, help);
			}
		}).start();
	}
	
	private void getComment() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = getHelpUrl + helpId + "/comments?sid=" + sessionId;
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
				sendMessage(MSG_POST_COMMENT, json);
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
				JSONObject json = (JSONObject)msg.obj;
				handleCommentResult(json);
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
				getComment();
				
				help.status = true;
				JSONObject helpJson = json.getJSONObject("help");
				
				help.createdBy = helpJson.getString("createdBy");
				help.name = getUsername(help.createdBy);
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
				
				help.commentCount = helpJson.getInt("commentCount");
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
	
	private String handleUserJSON(JSONObject json) {
		int resultStatus = -3;
		String username = "";
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				JSONObject profile = json.getJSONObject("profile");
				username = profile.getString("name");
				break;

			default:
				// TODO
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return username;
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
	
	private void handleCommentResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onCommentSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void updateHelpUI(final Help help) {
		if (help.status) {
			nameTextView.setText(help.name);
			dateTextView.setText(help.time);
			newsTextView.setText("标题：" + help.title + "\n" + help.content + "\n" + "标签：" + help.tags);
			countTextView.setText("评论：" + help.commentCount);
			
			protraitImageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(NeedDetailActivity.this, OtherPersonActivity.class);
					intent.putExtra("userId", help.createdBy);
					startActivity(intent);
				}
			});
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
		SimpleAdapter adapter = new SimpleAdapter(this, commentList, R.layout.comment,
				new String[]{"name", "date", "content"}, new int[]{R.id.name, R.id.date, R.id.content});
		commentListView.setAdapter(adapter);
	}
	
	private String getUsername(String id) {
		String username = null;
		try {
			String tmpUrl = getUserUrl + id + "?sid=" + sessionId;
			Log.e("alen", tmpUrl);
			Network network = new Network();
			JSONObject userJson = network.get(tmpUrl);
			username = handleUserJSON(userJson);
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
	
	private void onCommentSuccess(JSONObject json) {
		commentEditText.setText("");
		Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();	
		
		getHelp();
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
		public String createdBy;
		public String name;
		public String time;
		public String title;
		public String content;
		public String tags;
		public int commentCount;
	}
}