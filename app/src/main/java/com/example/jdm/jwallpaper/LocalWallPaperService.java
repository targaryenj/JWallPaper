package com.example.jdm.jwallpaper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * 更换壁纸Service
 * @author JDM
 * @date 2016年3月18日
 *
 */
public class LocalWallPaperService extends Service {


	WallpaperManager wManager;
	static Bitmap img;
	ImageLoader imageLoader;
	Calendar cal = Calendar.getInstance();
	Calendar current = Calendar.getInstance();


	@Override
	public void onCreate() {
		super.onCreate();
		wManager = WallpaperManager.getInstance(this);
		// 加载默认背景图片
		try {
			wManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.home_bg));
		} catch (IOException e) {
			e.printStackTrace();
		}
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(false).build();
		ImageLoaderConfiguration imgConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions).build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(imgConfig);

		cal.setTime(new Date());


	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String imagePosition = intent.getStringExtra(Constants.Extra.IMAGE_POSITION);
		DownImgTask task = new DownImgTask(this,imagePosition);
		task.execute("");
		return Service.START_STICKY;
	}


	class DownImgTask extends AsyncTask<String, Integer, Bitmap> {

		Context mContext;
		String imagePosition;

		public DownImgTask(Context ctx,String imagePosition) {
			mContext = ctx;
			this.imagePosition = imagePosition;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			if (imagePosition.startsWith("http")) {
				final RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
				StringRequest req = new StringRequest(imagePosition,
						new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								Log.d("VolleyTest", response);
								String imgUrl = response.substring(response.indexOf("http"));
								ImageRequest imageReq = new ImageRequest(imgUrl,
										new Response.Listener<Bitmap>() {
											@Override
											public void onResponse(Bitmap response) {
												img = response;
											}
										}, 540, 960, Config.RGB_565,
										new Response.ErrorListener() {
											@Override
											public void onErrorResponse(VolleyError error) {

											}
										});
								mQueue.add(imageReq);
							}
						}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("VolleyTest", error.getMessage(),error);
					}
				}
				);
				mQueue.add(req);
			}else{
				img = imageLoader.loadImageSync(imagePosition);
			}
			return img;
		}

		@Override
		protected void onPostExecute(Bitmap img) {
			try {
				wManager.setBitmap(img);
				Toast.makeText(LocalWallPaperService.this,"壁纸设置成功",Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

}
