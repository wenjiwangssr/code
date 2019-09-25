package com.example.autoplayimage;



import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity implements OnBannerListener {

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
        initView();
        clearData();
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
            getDataFromInternet();
            Toast.makeText(this,"get data from internet",Toast.LENGTH_SHORT).show();
        }
        else
        {
            getDataFromInternet();
//            getDataFromSP();
            Toast.makeText(this,"get data from SP",Toast.LENGTH_SHORT).show();
        }
    }
    //本地提取数据
    private void getDataFromSP() {
        list_title.clear();
        list_path.clear();
        for (int i = 0; i <4 ; i++) {
            list_title.add(mSharedPreferences.getString(("title"+i),""));
            list_path.add(mSharedPreferences.getString(("path"+i),""));
        }
    }
    //网络请求数据

    private void getDataFromInternet() {
        String url="http://api.7958.com/public/index.php/admin/Image/getimg";
        OkHttpUtils.getInstance().doGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body()!=null){
                    String myBody=response.body().string();
                    try {
                        JSONObject object=new JSONObject(myBody);
                        JSONArray array=object.getJSONArray("data");
                        for (int i = 0; i <array.length() ; i++) {
                            JSONObject imageData=array.getJSONObject(i);
                            list_path.add(imageData.optString("imgurl"));
                            list_title.add(imageData.optString("imgname"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
//        list_path.add("http://image.ngchina.com.cn/2019/0624/20190624051835895.jpg");
//        list_path.add("http://image.ngchina.com.cn/2019/0614/thumb_469_352_20190614100520711.jpg");
//        list_path.add("http://image.ngchina.com.cn/2017/1024/20171024023609391.jpg");
//        list_path.add("https://cdn.pixabay.com/photo/2019/09/07/20/35/way-4459666_960_720.jpg");
//        list_title.add("2第二次加载");
//        list_title.add("2我爱科比布莱恩特");
//        list_title.add("2我爱??");
//        list_title.add("2我爱我也不知道啥");
        for (int i = 0; i <list_title.size() ; i++) {
            mEditor.putString(("title"+i),list_title.get(i));
            mEditor.putString(("path"+i),list_path.get(i));
        }
        mEditor.putBoolean("isDataExist",true);
        mEditor.apply();

    }

    private void clearData() {
        list_path.clear();
        list_title.clear();
        mEditor.clear();
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
                        clearData();
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
