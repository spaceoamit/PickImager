package com.spaceo.pickimager.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spaceo.pickimager.R;

public class PickImager {

	public interface SendResultImage {
		public void setOnSendResultImage(Bitmap mBitmap, String sdCardPath);
	}

	public SendResultImage onSendResultImage;
	
	private String action=Intent.ACTION_PICK;
	
	private Context mContext;
	private String[] dialogArray = { "Camera", "Gallary" };
	private Uri mImageCaptureUri;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;

	private static final int GALLERY_PICTURE = 4;
	private static final int GALLERY_KITKAT_INTENT_CALLED = 5;

	boolean cropImage = false;
	private boolean isSizeAvailable=false;
	private boolean cropChooser=false;
	private double cropSize=0;
	
	private boolean multipleAction=false;

	public PickImager(Context mContext) {
		this.mContext = mContext;
	}
	
	public void setmultipleAction(Boolean multipleAction) {
		this.multipleAction = multipleAction;
		if(this.multipleAction){
			cropImage = false;
		}
	}
	
	public void setPickerAction(String action) {
		this.action = action;
	}
	
	public void setCropChooser(boolean cropChooser) {
		this.cropChooser = cropChooser;
	}
	
	public void setCropSize(Double cropSize) {
		
		this.isSizeAvailable=true;
		this.cropSize = cropSize;
	}

	public void pickImagerDialog(Context mContext, boolean cropImage, SendResultImage onSendResultImage) {
		
		this.cropImage = cropImage;
		this.mContext = mContext;
		this.onSendResultImage = onSendResultImage;

		showDialog();
		if(multipleAction){
			this.cropImage=false;
		}
	}

	public void pickImagerCamera(Context mContext, boolean cropImage, SendResultImage onSendResultImage) {

		this.cropImage = cropImage;
		this.mContext = mContext;
		this.onSendResultImage = onSendResultImage;
		if(multipleAction){
			this.cropImage=false;
		}
		pickFromCamera();

	}

	public void pickImagerGallary(Context mContext, boolean cropImage, SendResultImage onSendResultImage) {

		this.cropImage = cropImage;
		this.mContext = mContext;
		this.onSendResultImage = onSendResultImage;
		if(multipleAction){
			this.cropImage=false;
		}
		pickFromGallary();

	}

	private void showDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.app_name));
		alertDialogBuilder.setItems(dialogArray, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which == 0) {
					// get From camera
					pickFromCamera();

				} else if (which == 1) {
					// get From Gallary
					pickFromGallary();
				}
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();

	}

	public void pickFromCamera() {

		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "IMG_temp.jpg");
		values.put(MediaStore.Images.Media.ORIENTATION, android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung") ? 90
				: 0);
		mImageCaptureUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		((Activity) mContext).startActivityForResult(intent, PICK_FROM_CAMERA);

	}

	private void pickFromGallary() {
		// TODO Auto-generated method stub
		if (Build.VERSION.SDK_INT < 19) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(action);
			((Activity) mContext).startActivityForResult(Intent.createChooser(intent, "Select picture"),
					GALLERY_PICTURE);
		} else {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multipleAction);
			intent.setAction(action);
			((Activity) mContext).startActivityForResult(Intent.createChooser(intent, "Select picture"),
					GALLERY_KITKAT_INTENT_CALLED);
		}
	}

	@SuppressLint("NewApi")
	public void sendResultToLibrary(int requestCode, int resultCode, Intent data) {

		if (resultCode == ((Activity) mContext).RESULT_OK) {
			switch (requestCode) {
			case PICK_FROM_CAMERA:

				if (cropImage) {
					/**
					 * After taking a picture, do the crop
					 */
					doCrop();

				} else {
					getCaptureImageBitmapAndPath();
				}
				break;

			case GALLERY_PICTURE:
				/**
				 * After selecting image from files, save the selected path
				 */
				mImageCaptureUri = data.getData();
				if (cropImage) {
					doCrop();
				} else {
					getCaptureImageBitmapAndPath();
				}

				break;

			case GALLERY_KITKAT_INTENT_CALLED:
				/**
				 * After selecting image from files, save the selected path
				 */
				// mImageCaptureUri = data.getData();
				
				if(multipleAction){
					 //If uploaded with the new Android Photos gallery
					try {
						 ClipData clipData = data.getClipData();
			                String items="";
			                for(int i = 0; i < clipData.getItemCount(); i++){
			                	//items+=clipData.getItemAt(i)+",";
			                	
			                	//items+=clipData.getItemAt(i).getUri()+",";
			                	
			                	items += getPath(mContext, clipData.getItemAt(i).getUri())+",";
			                	
			                	if(i==10){
			                		Toast.makeText(mContext, "You can not select more then 10 images.", Toast.LENGTH_SHORT).show();
			                		break;
			                	}
			                	
			                    //What now?
			                }
			                if(items.length()>0){
			                	items=items.substring(0,items.length()-1);
			                }
			                onSendResultImage.setOnSendResultImage(null, items);
			                return;
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
	               
				}

				mImageCaptureUri = data.getData();
				/*File tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "temp_image.jpg");

				// Copy Uri contents into temp File.
				try {
					tempFile.createNewFile();
					copyAndClose(mContext.getContentResolver().openInputStream(data.getData()), new FileOutputStream(
							tempFile));
				} catch (IOException e) {
					// Log Error
					e.printStackTrace();
				}
				// Now fetch the new URI
				mImageCaptureUri = Uri.fromFile(tempFile);*/
				if (cropImage) {
					doCrop();
				} else {
					getCaptureImageBitmapAndPath();
				}

				break;
			case CROP_FROM_CAMERA:
				
				/**
				 * After cropping the image, get the bitmap of the cropped image
				 * and display it on imageview.
				 */
				
				
				getCaptureImageBitmapAndPath();
				
				
				
				break;

			}

		}

	}
	


	private void getCaptureImageBitmapAndPath() {

		String capturedImageFilePath = getPath(mContext, mImageCaptureUri);
		Bitmap bitmap = BitmapFactory.decodeFile(capturedImageFilePath);
		onSendResultImage.setOnSendResultImage(bitmap, capturedImageFilePath);

	}

	public class CropOption {
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}

	private ArrayList<Uri> uris;

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
		/**
		 * Open image crop app by starting an intent
		 * ‘com.android.camera.action.CROP‘.
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		/**
		 * Check if there is image cropper app installed.
		 */
		List<ResolveInfo> list = mContext.getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		/**
		 * If there is no image cropper app, display warning message
		 */
		if (size == 0) {

			Toast.makeText(mContext, "Can not find image crop app", Toast.LENGTH_SHORT).show();

			return;
		} else {
			/**
			 * Specify the image path, crop dimension and scale
			 */
			
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
			intent.setData(mImageCaptureUri);
			
			if(isSizeAvailable){
				
				intent.putExtra("outputX", cropSize);
				intent.putExtra("outputY", cropSize);
			}else {
				intent.putExtra("outputX", metrics.widthPixels);
				intent.putExtra("outputY", metrics.widthPixels);
			}
			
			
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("crop", true);
			intent.putExtra("return-data", true);

			/**
			 * There is posibility when more than one image cropper app exist,
			 * so we have to check for it first. If there is only one app, open
			 * then app.
			 */

			if(!cropChooser){
				
				ResolveInfo res = list.get(0);
				
				oneAppCrop(intent, res);
				
				
			}else {
				
				if (size == 1) {

					ResolveInfo res = list.get(0);
					
					oneAppCrop(intent, res);

				} else {
					
					/**
					 * If there are several app exist, create a custom chooser to
					 * 
					 * let user selects the app.
					 */
					uris = new ArrayList<Uri>();

					for (ResolveInfo res : list) {
						final CropOption co = new CropOption();

						co.title = mContext.getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
						co.icon = mContext.getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
						co.appIntent = new Intent(intent);

						ContentValues values = new ContentValues();
						values.put(MediaStore.Images.Media.TITLE, "IMG_temp_crop.jpg");
						mImageCaptureUri = mContext.getContentResolver().insert(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
						co.appIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
						uris.add(mImageCaptureUri);
						co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

						cropOptions.add(co);

					}

					CropOptionAdapter adapter = new CropOptionAdapter(mContext, cropOptions);

					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("Choose Crop App");
					builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							mImageCaptureUri = uris.get(item);
							((Activity) mContext).startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
						}
					});

					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {

							if (mImageCaptureUri != null) {
								mContext.getContentResolver().delete(mImageCaptureUri, null, null);
								mImageCaptureUri = null;
							}
						}
					});

					AlertDialog alert = builder.create();

					alert.show();
				}
			}
	
		}
	}
	
	private void oneAppCrop(Intent intent,ResolveInfo res){
		
		Intent i = new Intent(intent);
		
		i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, "IMG_temp_drop.jpg");
		mImageCaptureUri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				values);
		i.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

		((Activity) mContext).startActivityForResult(i, CROP_FROM_CAMERA);
	}

	public class CropOptionAdapter extends ArrayAdapter<CropOption> {
		private ArrayList<CropOption> mOptions;
		private LayoutInflater mInflater;

		public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
			super(context, R.layout.crop_selector, options);

			mOptions = options;

			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.crop_selector, null);

			CropOption item = mOptions.get(position);

			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
				((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);

				return convertView;
			}

			return null;
		}
	}

	private void copyAndClose(InputStream in, FileOutputStream out) {

		try {
			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	
	/**
	 * without passing uri we can...RND
	 * */
	public static Bitmap getBitmapFromCameraData(Intent data, Context context) {
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return BitmapFactory.decodeFile(picturePath);
	}
	
}
