package com.example.needu;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PersonalInterestActivity extends Activity {
	private String getConcernUrl = Network.SERVER + "/concerns";
	private String deleteConcernUrl = Network.SERVER + "/concern/";
	private static final int MSG_GET_CONCERN = 201;
	private static final int MSG_GET_PHOTO = 202;
	private static final int MSG_DELETE_CONCERN = 203;
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
	private Button commentButton;
	private ListView userListView;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_interest);
	
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		photoUrl = cookies.getString("photo", "");
		name = cookies.getString("name", "");
		college = cookies.getString("school", "");
		
		initViews();
		getPhoto(photoUrl);
		getOwnConcerns();
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
		commentButton = (Button)findViewById(R.id.commentButton);
		userListView = (ListView)findViewById(R.id.userListView);
		
		squareButton.setOnClickListener(listener);
		setButton.setOnClickListener(listener);
		personalDataButton.setOnClickListener(listener);
		announceButton.setOnClickListener(listener);
		commentButton.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			if (arg0.getId() == R.id.personalDataButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalDataActivity.class);
			} else if (arg0.getId() == R.id.announceButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalAnnounceActivity.class);
			} else if (arg0.getId() == R.id.commentButton) {
				intent.setClass(PersonalInterestActivity.this, PersonalCommentActivity.class);
			} else if (arg0.getId() == R.id.squareButton) {
				intent.setClass(PersonalInterestActivity.this, GroundActivity.class);
			} else if (arg0.getId() == R.id.setButton) {
				intent.setClass(PersonalInterestActivity.this, SetActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};
	
	private void getOwnConcerns() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String serverUrl = getConcernUrl + "?sid=" + sessionId;
				Log.e("alen", serverUrl);
				Network network = new Network();
				JSONObject json = network.get(serverUrl);
				ArrayList<HashMap<String, Object>> userList = handleConcernJSON(json);
				sendMessage(MSG_GET_CONCERN, userList);
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
	
	private void deleteOwnConcern(final String userId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String serverUrl = deleteConcernUrl + userId + "?sid=" + sessionId;
				Log.e("alen", serverUrl);
				Network network = new Network();
				JSONObject json = network.delete(serverUrl);
				sendMessage(MSG_DELETE_CONCERN, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_CONCERN:
				ArrayList<HashMap<String, Object>> userList = (ArrayList<HashMap<String, Object>>)msg.obj;
				updateConcernUI(userList);
				break;
				
			case MSG_GET_PHOTO:
				Bitmap bitmap = (Bitmap)msg.obj;
				portrait.setImageBitmap(bitmap);
				break;

			case MSG_DELETE_CONCERN:
				JSONObject json2 = (JSONObject)msg.obj;
				handleDeleteResult(json2);
				break;
			
			default:
				break;
			}
		}
	};
	
	private ArrayList<HashMap<String, Object>> handleConcernJSON(JSONObject json) {
		int resultStatus = -3;
		ArrayList<HashMap<String, Object>> userList = new ArrayList<HashMap<String,Object>>();
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				JSONArray userArray = json.getJSONArray("users");
				for (int i = 0; i < userArray.length(); i++) {
					JSONObject profileJson = userArray.optJSONObject(i);
					HashMap<String, Object> user = new HashMap<String, Object>();
					
					user.put("id", profileJson.getString("_id"));
					user.put("nickname", profileJson.getString("name"));
					String photoUrl = profileJson.getString("photo");
					if (!photoUrl.equals("null")) {
						user.put("image", getPhotoByUrl(photoUrl));
					} else {
						user.put("image", R.drawable.ic_launcher);
					}
					userList.add(user);
				}
				break;

			default:
				// TODO
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return userList;
	}
	
	private Bitmap getPhotoByUrl(String photoUrl) {
		Bitmap bitmap = null;
		if (!photoUrl.equals("")) {
			try {
				URL url = new URL(Network.HOST + photoUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); 
			    conn.connect(); 
			    InputStream is = conn.getInputStream(); 
			    bitmap = BitmapFactory.decodeStream(is); 
			    is.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return bitmap;
	}
	
	private void updateConcernUI(final ArrayList<HashMap<String, Object>> userList) {
		if (userList.isEmpty()) {
			userListView.setAdapter(null);
			Toast.makeText(this, "你还未关注其他用户", Toast.LENGTH_SHORT).show();
			return ;
		}
		SimpleAdapter adapter = new SimpleAdapter(this, userList, R.layout.user,
				new String[]{"nickname", "image"}, new int[]{R.id.nickname, R.id.portrait});
		adapter.setViewBinder(new ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Bitmap) {
                    ImageView image = (ImageView) view;
                    image.setImageBitmap((Bitmap) data);
                    return true;
                }
                return false;
            }
        });
		userListView.setAdapter(adapter);
		userListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String userId = (String) userList.get(position).get("id");
				Intent intent = new Intent(PersonalInterestActivity.this, OtherPersonActivity.class);
				intent.putExtra("userId", userId);
				startActivity(intent);
			}
		});
		userListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				Dialog dialog = new AlertDialog.Builder(PersonalInterestActivity.this)
					.setMessage("取消关注该用户？")
					.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String userId = (String) userList.get(position).get("id");
								deleteOwnConcern(userId);
								
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
				Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onDeleteSuccess(JSONObject json) {
		Toast.makeText(this, "已取消关注", Toast.LENGTH_SHORT).show();
		getOwnConcerns();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
}