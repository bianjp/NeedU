package com.example.needu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ModifyInfoActivity extends Activity {
	private String serverUrl = Network.SERVER + "/user";
	private String uploadUrl = Network.SERVER + "/user/photo";
	private static final int MSG_GET_INFO = 201;
	private static final int MSG_PUT_INFO = 202;
	private static final int MSG_PUT_PHOTO = 203;
	private String sessionId;
	private String studentId;
	private String sex;
	private String picPath;
	
	private EditText nameEditText;
	private EditText schoolYearEditText;
	private EditText schoolEditText;
	private EditText majorEditText;
	private EditText birthdayEditText;
	private EditText phoneEditText;
	private EditText qqEditText;
	private EditText wechatEditText;
	private EditText descriptionEditText;

	private ImageView photo;
	private Button addButton;
	private Button finishButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_info);
		
		SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
		sessionId = cookies.getString("sessionId", "");
		studentId = cookies.getString("studentId", "");
		
		initViews();
		getInfo();
	}
	
	private void initViews() {
		nameEditText = (EditText)findViewById(R.id.nicknameEditText);
		schoolYearEditText = (EditText)findViewById(R.id.schoolYearEditText);
		schoolEditText = (EditText)findViewById(R.id.schoolEditText);
		majorEditText = (EditText)findViewById(R.id.majorEditText);
		birthdayEditText = (EditText)findViewById(R.id.birthdayEditText);
		phoneEditText = (EditText)findViewById(R.id.phoneEditText);
		qqEditText = (EditText)findViewById(R.id.qqEditText);
		wechatEditText = (EditText)findViewById(R.id.wechatEditText);
		descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
		
		photo = (ImageView)findViewById(R.id.uploadPhoto);
		addButton = (Button)findViewById(R.id.uploadPhotoButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, 1);
			}
		});
		
		finishButton = (Button)findViewById(R.id.finishButton);
		finishButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				putInfo();
			}
		});
	}
	
	private void getInfo() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmpUrl = serverUrl + '/' + studentId + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.get(tmpUrl);
				sendMessage(MSG_GET_INFO, json);
			}
		}).start();
	}
	
	private void putInfo() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("school", schoolEditText.getText().toString()));
				params.add(new BasicNameValuePair("major", majorEditText.getText().toString()));
				params.add(new BasicNameValuePair("schoolYear", schoolYearEditText.getText().toString()));
				params.add(new BasicNameValuePair("name", nameEditText.getText().toString()));
				params.add(new BasicNameValuePair("gender", sex));
				params.add(new BasicNameValuePair("birthday", birthdayEditText.getText().toString()));
				params.add(new BasicNameValuePair("phone", phoneEditText.getText().toString()));
				params.add(new BasicNameValuePair("wechat", wechatEditText.getText().toString()));
				params.add(new BasicNameValuePair("QQ", qqEditText.getText().toString()));
				params.add(new BasicNameValuePair("description", descriptionEditText.getText().toString()));
				
				String tmpUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", tmpUrl);
				Network network = new Network();
				JSONObject json = network.put(tmpUrl, params);
				sendMessage(MSG_PUT_INFO, json);
			}
		}).start();
	}
	
	private void putPhoto() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				File file = new File(picPath); 

				if(file!=null)
				{ 
					String entityString = UploadUtil.uploadFile( file, uploadUrl + "?sid=" + sessionId); 
					try {
						JSONObject json = new JSONObject(entityString);
						sendMessage(MSG_PUT_PHOTO, json);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
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
			
			case MSG_PUT_INFO:
				JSONObject json2 = (JSONObject)msg.obj;
				handlePutResult(json2);
				break;

			case MSG_PUT_PHOTO:
				JSONObject json3 = (JSONObject)msg.obj;
				handlePhotoResult(json3);
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
	
	private void handlePutResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onPutSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void handlePhotoResult(JSONObject json) {
		int resultStatus = -3;
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onPhotoSuccess(json);
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
			sex = profile.getString("gender");
			nameEditText.setText(profile.getString("name").equals("null")?"":profile.getString("name"));
			schoolYearEditText.setText(profile.getString("schoolYear").equals("null")?"":profile.getString("schoolYear"));
			schoolEditText.setText(profile.getString("school").equals("null")?"":profile.getString("school"));
			majorEditText.setText(profile.getString("major").equals("null")?"":profile.getString("major"));
			birthdayEditText.setText(profile.getString("birthday").equals("null")?"":profile.getString("birthday"));
			phoneEditText.setText(profile.getString("phone").equals("null")?"":profile.getString("phone"));
			qqEditText.setText(profile.getString("QQ").equals("null")?"":profile.getString("QQ"));
			wechatEditText.setText(profile.getString("wechat").equals("null")?"":profile.getString("wechat"));
			descriptionEditText.setText(profile.getString("description").equals("null")?"":profile.getString("description"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onPutSuccess(JSONObject json) {
		putPhoto();
	}
	
	private void onPhotoSuccess(JSONObject json) {
		Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == 1 && data != null){
			setPicToView(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void setPicToView(Intent picdata){
		Uri uri = picdata.getData();
		
		try { 
			String[] pojo = {MediaStore.Images.Media.DATA}; 

			Cursor cursor = managedQuery(uri, pojo, null, null,null); 
			if(cursor!=null) 
			{ 
				ContentResolver cr = this.getContentResolver(); 
				int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA); 
				cursor.moveToFirst(); 
				String path = cursor.getString(colunm_index); 

				/*** 
				* 这里加这样一个判断主要是为了第三方的软件选择，比如：使用第三方的文件管理器的话，你选择的文件就不一定是图片了，这样的话，我们判断文件的后缀名 

				* 如果是图片格式的话，那么才可以 
				*/

				if(path.endsWith("jpg")||path.endsWith("png")) 
				{
					picPath = path; 
					Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri)); 
					photo.setImageBitmap(bitmap);
				}
			}
		} catch (Exception e) { 
		}
	}
}