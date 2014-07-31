package com.example.needu;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Network {
//	public static final String HOST = "http://172.29.173.1:3000";
	public static final String HOST = "http://199.231.215.144:3000";
	public static final String SERVER = HOST + "/api";
	public static final int MSG_OK = 200;
	public static final int MSG_FAILED = 0;
	
	private HttpClient client = new DefaultHttpClient();
	
	public JSONObject get(String serverUrl) {
		HttpGet get = new HttpGet(serverUrl);
		HttpResponse response = null;
		JSONObject json = null;
		try {
			response = client.execute(get);
			json = handleResponse(response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return json;
	}
	
	public JSONObject post(String serverUrl, List<NameValuePair> params) {
		HttpPost post = new HttpPost(serverUrl);
		HttpResponse response = null;
		JSONObject json = null;
		try {
			if (params != null) {
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}
			response = client.execute(post);
			json = handleResponse(response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return json;
	}
	
	public JSONObject put(String serverUrl, List<NameValuePair> params) {
		HttpPut put = new HttpPut(serverUrl);
		HttpResponse response = null;
		JSONObject json = null;
		try {
			put.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			response = client.execute(put);
			json = handleResponse(response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return json;
	}
	
	public JSONObject delete(String serverUrl) {
		HttpDelete delete = new HttpDelete(serverUrl);
		HttpResponse response = null;
		JSONObject json = null;
		try {
			response = client.execute(delete);
			json = handleResponse(response);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return json;
	}
	
	private JSONObject handleResponse(HttpResponse response) {
		JSONObject json = null;
		
		Log.e("alen", "code:" + response.getStatusLine().getStatusCode());
		try {
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				String entityString = EntityUtils.toString(entity);
				entityString = new String(entityString.getBytes("iso-8859-1"),"UTF-8");
				Log.e("alen", entityString);
		//		String jsonString = entityString.substring(entityString.indexOf("{"));
		//		Log.e("alen", jsonString);
				json = new JSONObject(entityString);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return json;
	}
	
	public static boolean isNetworkConnected(Context context) { 
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
}