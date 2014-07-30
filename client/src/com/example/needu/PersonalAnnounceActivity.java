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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalAnnounceActivity extends Activity {
	private String getHelpUrl = Network.SERVER + "/helps/user/";
	private String deleteHelpUrl = Network.SERVER + "/help/";
	private static final int MSG_GET_HELP = 201;
	private static final int MSG_GET_PHOTO = 202;
	private static final int MSG_DELETE_HELP = 203;
	private String sessionId;
	private String studentId;
	private String name;
	private String college;
	private String photoUrl;
	
	private ImageView headpic;
	private TextView nameText;
	private TextView collegeText;
	private Button squareButton;
	private Button setButton;
	private Button personalDataButton;
	private Button commentButton;
	private Button interestButton;
	private Button newNeedButton;
	private ListView helpListView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_announce);
	
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		studentId = cookies.getString("studentId", "");
		photoUrl = cookies.getString("photo", "");
		name = cookies.getString("name", "");
		college = cookies.getString("school", "");
		
		initViews();
		getPhoto(photoUrl);
		getOwnHelps();
	}
	
	private void initViews()
	{
		headpic = (ImageView)findViewById(R.id.headpic);
		nameText = (TextView)findViewById(R.id.name);
		collegeText = (TextView)findViewById(R.id.college);
		nameText.append(name);
		collegeText.append(college);
		
		squareButton = (Button)findViewById(R.id.squareButton);
		setButton = (Button)findViewById(R.id.setButton);
		personalDataButton = (Button)findViewById(R.id.personalDataButton);
		commentButton = (Button)findViewById(R.id.commentButton);
		interestButton = (Button)findViewById(R.id.interestButton);
		newNeedButton = (Button)findViewById(R.id.newNeedButton);
		helpListView = (ListView)findViewById(R.id.helpListView);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		commentButton.setOnClickListener(listener);
		interestButton.setOnClickListener(listener);
		newNeedButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.newNeedButton) {
				Intent intent = new Intent(PersonalAnnounceActivity.this, NewNeedActivity.class);
				startActivity(intent);
				return ;
			}
			
			Intent intent = new Intent();
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.commentButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalCommentActivity.class);
			} else if (arg0.getId() == R.id.interestButton) {
				intent.setClass(PersonalAnnounceActivity.this, PersonalInterestActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalAnnounceActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalAnnounceActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getOwnHelps() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String serverUrl = getHelpUrl + studentId + "?sid=" + sessionId;
				Network network = new Network();
				JSONObject json = network.get(serverUrl);
				sendMessage(MSG_GET_HELP, json);
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
	
	private void deleteOwnHelp(final String helpId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String serverUrl = deleteHelpUrl + helpId + "?sid=" + sessionId;
				Network network = new Network();
				JSONObject json = network.delete(serverUrl);
				sendMessage(MSG_DELETE_HELP, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_HELP:
				JSONObject json = (JSONObject)msg.obj;
				handleGetResult(json);
				break;
				
			case MSG_GET_PHOTO:
				Bitmap bitmap = (Bitmap)msg.obj;
				headpic.setImageBitmap(bitmap);
				break;
			
			case MSG_DELETE_HELP:
				JSONObject json2 = (JSONObject)msg.obj;
				handleDeleteResult(json2);
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
	
	private void handleDeleteResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onDeleteSuccess(json);
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
			final JSONArray helpArray = json.getJSONArray("helps");
			if (helpArray.length() == 0) {
				helpListView.setAdapter(null);
				Toast.makeText(this, "你还未发布过求助信息", Toast.LENGTH_SHORT).show();
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
						Intent intent = new Intent(PersonalAnnounceActivity.this, NeedDetailActivity.class);
						intent.putExtra("helpId", helpId);
						startActivity(intent);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			helpListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
					Dialog dialog = new AlertDialog.Builder(PersonalAnnounceActivity.this)
						.setMessage("删除该求助信息？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										JSONObject helpJson = helpArray.optJSONObject(position);
										try {
											String helpId = helpJson.getString("_id");
											deleteOwnHelp(helpId);
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										dialog.dismiss();
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								})
						.create();
					dialog.show();
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onDeleteSuccess(JSONObject json) {
		Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
		getOwnHelps();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}