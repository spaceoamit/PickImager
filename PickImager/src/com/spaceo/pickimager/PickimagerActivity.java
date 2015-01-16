package com.spaceo.pickimager;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.spaceo.pickimager.utility.PickImager;
import com.spaceo.pickimager.utility.PickImager.SendResultImage;

public class PickimagerActivity extends Activity implements OnClickListener {

	Button buttonPickImage;
	Button buttonCamera;
	Button buttonGallary;
	
	Button buttonMulGallary;
	
	

	ImageView imageViewImage;

	PickImager mPikImager;

	TextView textViewSdCardPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// init Object of picker Class
		mPikImager = new PickImager(this);
		
		iniControls();  
		
		//getSettings();
		//galleryAddPic();
	}
	
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    File f = new File("");
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    this.sendBroadcast(mediaScanIntent);
	}

	private void getSettings() {
		// TODO Auto-generated method stub
		/**
		 * for croping dialog chooser
		 * default chooser is false.
		 * */
		mPikImager.setCropChooser(true);
		
		//default action is  Intent.ACTION_PICK
		/**
		 * for all types of app that contain images
		 * 
		 * */
		mPikImager.setPickerAction(Intent.ACTION_GET_CONTENT);
		
		/**
		 * For Gallery and google photo
		 * */
		mPikImager.setPickerAction(Intent.ACTION_PICK);
		
		/**
		 * For croping ratio
		 * bu default its same as device width
		 * */
		mPikImager.setCropSize(200.00); //i.e 300.00, 350.00
		
	}

	private void iniControls() {

		buttonPickImage = (Button) findViewById(R.id.buttonPickImage);
		buttonCamera = (Button) findViewById(R.id.buttonCamera);
		buttonGallary = (Button) findViewById(R.id.buttonGallary);
		
		buttonMulGallary= (Button) findViewById(R.id.buttonMulGallary);

		buttonPickImage.setOnClickListener(this);
		buttonCamera.setOnClickListener(this);
		buttonGallary.setOnClickListener(this);
		
		buttonMulGallary.setOnClickListener(this);

		imageViewImage = (ImageView) findViewById(R.id.imageViewImage);

		textViewSdCardPath = (TextView) findViewById(R.id.textViewSdCardPath);

	}

	@Override
	public void onClick(View v) {

		if (v == buttonPickImage) {
			// dialog aprear for Camera and gallary
			
			mPikImager.pickImagerDialog(PickimagerActivity.this, false, new SendResultImage() {

				@Override
				public void setOnSendResultImage(Bitmap mBitmap, String sdCardPath) {

					if (mBitmap != null)
						imageViewImage.setImageBitmap(mBitmap);
					if (sdCardPath != null)
						textViewSdCardPath.setText(sdCardPath);

				}
			});

		} else if (v == buttonCamera) {

			mPikImager.pickImagerCamera(PickimagerActivity.this, true, new SendResultImage() {

				@Override
				public void setOnSendResultImage(Bitmap mBitmap, String sdCardPath) {

					if (mBitmap != null)
						imageViewImage.setImageBitmap(mBitmap);
					if (sdCardPath != null)
						textViewSdCardPath.setText(sdCardPath);

				}
			});

		} else if (v == buttonGallary) {
		//	mPikImager.setmultipleAction(false);
			mPikImager.pickImagerGallary(PickimagerActivity.this, false, new SendResultImage() {

				@Override
				public void setOnSendResultImage(Bitmap mBitmap, String sdCardPath) {

					if (mBitmap != null)
						imageViewImage.setImageBitmap(mBitmap);
					if (sdCardPath != null)
						textViewSdCardPath.setText(sdCardPath);

				}
			});
		}else if (v == buttonMulGallary) {

			mPikImager.setmultipleAction(true);
			
			mPikImager.pickImagerGallary(PickimagerActivity.this, false, new SendResultImage() {

				@Override
				public void setOnSendResultImage(Bitmap mBitmap, String sdCardPath) {

					if (mBitmap != null)
						imageViewImage.setImageBitmap(mBitmap);
					if (sdCardPath != null){
						
						String[]myselectedImages=sdCardPath.split(",");
						String selected="";
						for (int i = 0; i < myselectedImages.length; i++) {
							selected+=myselectedImages[i]+"\n";
						}
						textViewSdCardPath.setText(selected);
					}
						
						
						
						

				}
			});
		}
	}

	/**
	 * override this mathod is necessory for pikImage its pass data to lib class
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		mPikImager.sendResultToLibrary(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);

	}

}
