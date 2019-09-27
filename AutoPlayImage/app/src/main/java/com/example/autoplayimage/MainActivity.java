package com.example.autoplayimage;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author wenjiwang
 *
 *  使用Okhttp3网络请求图片数据在banner框架中实现轮播图
 */
public class MainActivity extends AppCompatActivity implements OnBannerListener {

    private MyHandler myHandler;
    private Banner mBanner;
    private ArrayList<String> list_path;//图片地址
    private ArrayList<String> list_title;//图片标题
    //SP实现数据持久化
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    //判断是否已存在本地图片数据
    boolean isDataExist;

    //时间变化监听
    private IntentFilter mIntentFilter;
    private TimeChangeReceiver mTimeChangeReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.context=this;
        myHandler=new MyHandler(this);


        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        mSharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        mEditor=mSharedPreferences.edit();
        list_path=new ArrayList<>();
        list_title=new ArrayList<>();
        isDataExist=mSharedPreferences.getBoolean("isDataExist",false);

        initReceiver();
        initData();
//        initView();

    }

    private  static class MyHandler extends Handler{
        private WeakReference<MainActivity> mWeakReference;

        public MyHandler(MainActivity activity){
            mWeakReference=new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mWeakReference != null && mWeakReference.get() != null) {
                mWeakReference.get().handleMassage(msg);
            }

        }
    };
    private void handleMassage(Message msg){
        switch (msg.what){
            case 1:
                list_path=msg.getData().getStringArrayList("list_path");
                list_title=msg.getData().getStringArrayList("list_title");
                initView();
                break;
            default:
                break;
        }
    }
    //注册时间变化监听
    private void initReceiver() {
        mIntentFilter=new IntentFilter(Intent.ACTION_TIME_TICK);
        mTimeChangeReceiver =new TimeChangeReceiver();
        registerReceiver(mTimeChangeReceiver,mIntentFilter);

    }

    //视图加载
    private void initView() {
        mBanner=findViewById(R.id.banner);

        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
        mBanner.setImageLoader(new MyLoader());
        mBanner.setBannerAnimation(Transformer.Default);
        mBanner.setBannerTitles(list_title);
        mBanner.setDelayTime(3500);
        mBanner.isAutoPlay(true);
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mBanner.setImages(list_path)
                .setOnBannerListener(this)
                .start();
    }
    //数据加载
    private void initData() {



        if (!isDataExist)
        {
            getDataFromInternet2();
            Log.d("INIT_DATA","get data from internet");
            Toast.makeText(this,"get data from internet",Toast.LENGTH_SHORT).show();
        }
        else
        {

            getDataFromSP();
            Log.d("INIT_DATA","get data from SP");
            Toast.makeText(this,"get data from SP",Toast.LENGTH_SHORT).show();
        }
    }
    //本地提取数据
    private void getDataFromSP() {
        clearData();
        for (int i = 0; i <6 ; i++) {
            list_title.add(mSharedPreferences.getString(("imgName"+i),""));
            list_path.add(mSharedPreferences.getString(("path"+i),""));
        }
        initView();
        

    }



    //网络请求数据
    private void getDataFromInternet() {
        clearData();
        String url="http://api.7958.com/public/index.php/api/image/getimg";
        OkHttpUtils.getInstance().doGet(url, new DataCallBack() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject object=new JSONObject(result);
                    JSONArray array=object.optJSONArray("data");
                    for (int i = 0; i <array.length() ; i++) {
                        list_title.add(array.getJSONObject(i).optString("id"));
                        list_path.add(array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        mEditor.putString("path"+i,array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        mEditor.putString("imgName"+i,array.getJSONObject(i).optString("id"));
                    }
                    mEditor.putBoolean("isDataExist",true);
                    mEditor.commit();
                    initView();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    //用handler传递数据
    private void getDataFromInternet2() {
        clearData();
        String url="http://api.7958.com/public/index.php/api/image/getimg";
        OkHttpUtils.getInstance().doGet(url, new DataCallBack() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject object=new JSONObject(result);
                    JSONArray array=object.optJSONArray("data");
                    for (int i = 0; i <array.length() ; i++) {
                        list_title.add(array.getJSONObject(i).optString("id"));
                        list_path.add(array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        mEditor.putString("path"+i,array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        mEditor.putString("imgName"+i,array.getJSONObject(i).optString("id"));
                    }
                    mEditor.putBoolean("isDataExist",true);
                    mEditor.commit();
                    Bundle bundle=new Bundle();
                    bundle.putStringArrayList("list_path",list_path);
                    bundle.putStringArrayList("list_title",list_title);
                    Message message=new Message();
                    message.what=1;
                    message.setData(bundle);
                    myHandler.sendMessage(message);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void clearData() {
        list_path.clear();
        list_title.clear();

    }
    private void clearSpData(){
        mEditor.clear().commit();
    }


    //轮播监听
    @Override
    public void OnBannerClick(int position) {
        Toast.makeText(this,"You click photo:"+(position+1),Toast.LENGTH_SHORT).show();
    }




    //网络加载图片,使用Glide图片加载框架
    private class MyLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context.getApplicationContext())
                    .load((String)path)
                    .into(imageView);
        }
    }

    /**
     * 监听日期变化
     */
    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction()==Intent.ACTION_TIME_TICK){
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                if (min == 0) {
                    //整点请求新数据
                    if ((hour)%12==0)
                    {
                        clearSpData();
                        getDataFromInternet();
                        mBanner.setBannerTitles(list_title);
                        mBanner.setImages(list_path);
                        mBanner.start();
                        Toast.makeText(MainActivity.this,"请更新加载今日图集",Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    }
}
