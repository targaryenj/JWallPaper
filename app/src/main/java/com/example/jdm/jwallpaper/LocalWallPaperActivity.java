package com.example.jdm.jwallpaper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LocalWallPaperActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.gridView)
    AbsListView listView;
    Cursor cursor;
    Handler handler = new Handler();
    Runnable changeBingWallPaper = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(LocalWallPaperActivity.this,
                    LocalWallPaperService.class);
            intent.putExtra(Constants.Extra.IMAGE_POSITION, Constants.BING_IMG);
            startService(intent);
            handler.postDelayed(this,1000*60*60*10);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_wall_paper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.local_wall_paper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(LocalWallPaperActivity.this,
                    LocalWallPaperService.class);
            intent.putExtra(Constants.Extra.IMAGE_POSITION, Constants.BING_IMG);
            startService(intent);
            handler.postDelayed(changeBingWallPaper,1000*60*60*10);

        } else if (id == R.id.nav_gallery) {
            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Images.Media._ID,
                            MediaStore.Images.Media.DATA},MediaStore.Images.Media.MIME_TYPE + "=? OR " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg","image/png"},MediaStore.Images.Media.DATE_ADDED + " DESC");
            ((GridView)listView).setAdapter(new ImageAdapter(this,cursor));
            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("设为").setIcon(R.drawable.ic_settings_applications_black_18dp)
                    .setMessage("壁纸");

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                               int position, long id) {
                    cursor.moveToPosition(position);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(){
                                public void run() {
                                    Intent intent = new Intent(LocalWallPaperActivity.this,
                                            LocalWallPaperService.class);
                                    intent.putExtra(Constants.Extra.IMAGE_POSITION,"file://" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                                    startService(intent);
                                };
                            }.start();

                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
                    return true;
                }
            });


        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private static class ImageAdapter extends BaseAdapter {
        private static final String[] IMAGE_URLS = Constants.IMAGES;
        private LayoutInflater inflater;
        private DisplayImageOptions options;
        Cursor cursor;
        ImageLoaderConfiguration imgConfig;
        ImageLoader imageLoader = ImageLoader.getInstance();


        public ImageAdapter(Context ctx, Cursor cursor) {
            inflater = LayoutInflater.from(ctx);
            this.cursor = cursor;

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            imgConfig = new ImageLoaderConfiguration.Builder(ctx)
                    .defaultDisplayImageOptions(options).build();
            imageLoader.init(imgConfig);
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_grid_image, parent,false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else{
                holder = (ViewHolder) view.getTag();
            }
            cursor.moveToPosition(position);

            imageLoader.displayImage("file://" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)), holder.imageView,
                    options);
            return view;
        }

        static class ViewHolder{
            ImageView imageView;
        }

    }
}
