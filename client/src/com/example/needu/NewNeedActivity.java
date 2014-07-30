package com.example.needu;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class NewNeedActivity extends Activity {
	private String serverUrl = Network.SERVER + "/help";
	
	private EditText titleEditText;
	private EditText tagEditText;
	private EditText contentEditText;
	private Button addButton;
	private Button makesureButton;
	private Button deleteButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_need);
	
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		initViews();
	}
	
	private void initViews()
	{
		titleEditText = (EditText)findViewById(R.id.TitleEdit);
		tagEditText = (EditText)findViewById(R.id.TagEdit);
		contentEditText = (EditText)findViewById(R.id.ContentEdit);
		addButton = (Button)findViewById(R.id.AddButton);
		makesureButton = (Button)findViewById(R.id.MakesureButton);
		deleteButton = (Button)findViewById(R.id.DeleteButton);
		
		addButton.setOnClickListener(listener);
		makesureButton.setOnClickListener(listener);
		deleteButton.setOnClickListener(listener);
	}	
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() == R.id.AddButton) {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, 1);
			} else if (arg0.getId() == R.id.MakesureButton) {
				handleNewNeed();
			} else if (arg0.getId() == R.id.DeleteButton) {
				finish();
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == 1 && data != null){
			setPicToView(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//动态添加新的图片
	private void setPicToView(Intent picdata){
		Uri uri = picdata.getData();
		LinearLayout addImage = (LinearLayout)findViewById(R.id.addImageLayout);
		ImageView imag = new ImageView(this);
		imag.setImageURI(uri);
		imag.setScaleType(ScaleType.CENTER_CROP);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp1.width = 60;
		lp1.height = 60;
		lp1.leftMargin = 10;
		addImage.addView(imag, lp1);
	}
	
	private void handleNewNeed() {
		if (TextUtils.isEmpty(titleEditText.getText())) {
			Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(tagEditText.getText())) {
			Toast.makeText(this, "标签不能为空", Toast.LENGTH_SHORT).show();
		} else if (TextUtils.isEmpty(contentEditText.getText())) {
			Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
		} else {
			newNeed();
		}
	}
	
	private void newNeed() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("title", titleEditText.getText().toString()));
				params.add(new BasicNameValuePair("tags", tagEditText.getText().toString()));
				params.add(new BasicNameValuePair("content", contentEditText.getText().toString()));
				
				SharedPreferences cookies = getSharedPreferences("cookies", MODE_PRIVATE);
				String sessionId = cookies.getString("sessionId", "");
				
				serverUrl = serverUrl + "?sid=" + sessionId;
				Log.e("alen", serverUrl);
				Network network = new Network();
				JSONObject json = network.post(serverUrl, params);
				sendMessage(Network.MSG_OK, json);
			}
		}).start();
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Network.MSG_OK:
				JSONObject json = (JSONObject)msg.obj;
				handleNewNeedResult(json);
				break;

			default:
				break;
			}
		}
	};
	
	private void handleNewNeedResult(JSONObject json) {
		int resultStatus = -3;
		
		try {
			resultStatus = json.getInt("status");
			switch (resultStatus) {
			case 0:
				onNewNeedSuccess(json);
				break;

			default:
				Toast.makeText(this, new String(json.getString("message").getBytes("iso-8859-1"),"UTF-8"), Toast.LENGTH_SHORT).show();
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private void onNewNeedSuccess(JSONObject json) {
		Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void sendMessage(int what, Object obj) {
		Message msg = Message.obtain();
		msg.what = what;
		msg.obj = obj;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}