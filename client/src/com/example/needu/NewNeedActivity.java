package com.example.needu;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class NewNeedActivity extends Activity 
{
	private Button squareButton;
	private Button personalButton;
	private Button setButton;
	private EditText targetEditText;
	private EditText contentEditText;
	private Button addButton;
	private Button makesureButton;
	private Button detectButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_need);
	
		initViews();
		}
	
	private void initViews()
	{
		squareButton = (Button)findViewById(R.id.squareButton);
		personalButton = (Button)findViewById(R.id.personalButton);
		setButton = (Button)findViewById(R.id.setButton);
		targetEditText = (EditText)findViewById(R.id.TargetEdit);
		contentEditText = (EditText)findViewById(R.id.ContentEdit);
		addButton = (Button)findViewById(R.id.AddButton);
		makesureButton = (Button)findViewById(R.id.MakesureButton);
		detectButton = (Button)findViewById(R.id.DetectButton);
		addButton.setOnClickListener(new addButtonClickListener());
	}	
	
	private final class addButtonClickListener implements View.OnClickListener{
		public void onClick(View v){
			Intent intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			startActivityForResult(intent, 1);
		}
	}
	
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
}