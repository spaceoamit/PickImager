package com.mobstar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mobstar.utils.Constant;
import com.mobstar.utils.Utility;

public class EditProfileActivity extends Activity {

	Context mContext;
	TextView textEditProfile;

	String[] arrayChangePicture = { "Take From camera", "Choose from Library" };
	Uri tempUri;

	ImageView imgProfilePic, imgCoverImage;

	String ProfilePicPath;
	String CoverImagePath;

	boolean isProfilePicClicked = false;
	SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		mContext = EditProfileActivity.this;
		preferences = getSharedPreferences("mobstar_pref", Activity.MODE_PRIVATE);

		ProfilePicPath = preferences.getString("profile_image", "");
		CoverImagePath = preferences.getString("cover_image", "");

		InitControls();
	}

	void InitControls() {

		textEditProfile = (TextView) findViewById(R.id.textEditProfile);
		textEditProfile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});

		imgProfilePic = (ImageView) findViewById(R.id.imgProfilePic);
		imgProfilePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				isProfilePicClicked = true;
				onProfilePic();
			}
		});

		imgCoverImage = (ImageView) findViewById(R.id.imgCoverImage);
		imgCoverImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				isProfilePicClicked = false;
				onProfilePic();
			}
		});

		if (ProfilePicPath.equals("")) {
			imgProfilePic.setImageResource(R.drawable.profile_pic);
		} else {
			imgProfilePic.setImageResource(R.drawable.profile_pic);
			Ion.with(mContext).load(ProfilePicPath).withBitmap().placeholder(R.drawable.profile_pic).error(R.drawable.profile_pic).resize(Utility.dpToPx(mContext, 126), Utility.dpToPx(mContext, 126)).centerCrop().asBitmap().setCallback(new FutureCallback<Bitmap>() {

				@Override
				public void onCompleted(Exception exception, Bitmap bitmap) {
					// TODO Auto-generated method stub
					if (exception == null) {
						imgProfilePic.setImageBitmap(bitmap);
					}
				}
			});
		}

		if (CoverImagePath.equals("")) {
			imgCoverImage.setImageResource(R.drawable.cover_image);
		} else {
			imgCoverImage.setImageResource(R.drawable.cover_image);
			Ion.with(mContext).load(CoverImagePath).withBitmap().placeholder(R.drawable.small_pic).error(R.drawable.small_pic).resize(Utility.dpToPx(mContext, 126), Utility.dpToPx(mContext, 126)).centerCrop().asBitmap().setCallback(new FutureCallback<Bitmap>() {

				@Override
				public void onCompleted(Exception exception, Bitmap bitmap) {
					// TODO Auto-generated method stub
					if (exception == null) {
						imgCoverImage.setImageBitmap(bitmap);
					}
				}
			});
		}

	}

	void onProfilePic() {

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Change Picture").setItems(arrayChangePicture, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
					ContentValues values = new ContentValues();
					values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");
					values.put(MediaStore.Images.Media.ORIENTATION, android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung") ? 90 : 0);
					tempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
					startActivityForResult(intent, 25);
				} else if (which == 1) {

					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), 26);

				}
			}
		});
		builder.create().show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			/**
			 * From Camera here...
			 * */

			if (requestCode == 25) {
				doCrop(tempUri, data);
			}

			/**
			 * From Gallery here...
			 * */
			else if (requestCode == 26) {

				Uri selectedImageUri = data.getData();
				tempUri = selectedImageUri;
				doCrop(tempUri, data);
			}

			/**
			 * From Crop here...
			 * */
			else if (requestCode == 27) {

				try {
					String capturedImageFilePath = getPath(mContext, tempUri);

					if (isProfilePicClicked) {
						ProfilePicPath = capturedImageFilePath;
					} else {
						CoverImagePath = capturedImageFilePath;
					}

					Bitmap bitmap = BitmapFactory.decodeFile(capturedImageFilePath);

					if (bitmap != null) {
						bitmap = Bitmap.createScaledBitmap(bitmap, Utility.dpToPx(mContext, 126), Utility.dpToPx(mContext, 126), false);
						if (isProfilePicClicked) {
							imgProfilePic.setImageBitmap(bitmap);
							imgProfilePic.invalidate();

							Utility.ShowProgressDialog(mContext, "Uploading...");

							if (Utility.isNetworkAvailable(mContext)) {

								new UploadImage().execute(Constant.SERVER_URL + Constant.UPLOAD_PROFILE_IMAGE);
							} else {

								Toast.makeText(mContext, "No, Internet Access!", Toast.LENGTH_SHORT).show();
								Utility.HideDialog(mContext);
							}
						} else {
							imgCoverImage.setImageBitmap(bitmap);
							imgCoverImage.invalidate();

							Utility.ShowProgressDialog(mContext, "Uploading...");

							if (Utility.isNetworkAvailable(mContext)) {
								new UploadImage().execute(Constant.SERVER_URL + Constant.UPLOAD_COVER_IMAGE);
							} else {

								Toast.makeText(mContext, "No, Internet Access!", Toast.LENGTH_SHORT).show();
								Utility.HideDialog(mContext);
							}
						}
					}

				} catch (Exception e) {
					Toast.makeText(EditProfileActivity.this, "Unknown Error, Please Retake Photo!", Toast.LENGTH_SHORT).show();
				}

			}

		}
	};

	private void doCrop(Uri mImageCaptureUri, Intent data) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
		int size = list.size();
		if (size == 0) {
			Toast.makeText(this, "Can not find image crop application", Toast.LENGTH_SHORT).show();
		} else {
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			intent.setData(mImageCaptureUri);
			intent.putExtra("crop", true);
			intent.putExtra("outputX", metrics.widthPixels);
			intent.putExtra("outputY", metrics.widthPixels);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			Intent i = new Intent(intent);
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");
			tempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			i.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
			ResolveInfo res = list.get(0);
			i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			startActivityForResult(i, 27);
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
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

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

	private class UploadImage extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(String... urls) {
			InputStream is = null;
			String json = "";

			// Making HTTP request
			try {
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(urls[0]);
				httpPost.addHeader("X-API-KEY", Constant.API_KEY);

				httpPost.addHeader("X-API-TOKEN", preferences.getString("token", null));

				MultipartEntity multipartContent = new MultipartEntity();

				String path;

				if (isProfilePicClicked) {
					path = ProfilePicPath;
				} else {
					path = CoverImagePath;
				}

				File myFile = new File(path);
				FileBody fileBody = new FileBody(myFile, "image/png");
				if (isProfilePicClicked) {
					multipartContent.addPart("profileImage", fileBody);
				} else {
					multipartContent.addPart("coverImage", fileBody);
				}

				httpPost.setEntity(multipartContent);

				HttpResponse httpResponse = httpClient.execute(httpPost);

				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				json = sb.toString();
			} catch (Exception e) {
				Log.e("Buffer Error", "Error converting result " + e.toString());
			}

			return json;

		}

		@Override
		protected void onPostExecute(String jsonString) {
			Log.v(Constant.TAG, "Upload Response " + jsonString);
			Utility.HideDialog(mContext);

			Intent intent = new Intent("profile_image_changed");
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
			
			try {

				JSONObject jsonObject = new JSONObject(jsonString);

				JSONObject jsonUserObj = jsonObject.getJSONObject("user");

				preferences.edit().putString("profile_image", jsonUserObj.getString("profileImage")).commit();
				preferences.edit().putString("cover_image", jsonUserObj.getString("profileCover")).commit();

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}
}
