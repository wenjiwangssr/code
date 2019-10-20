package com.example.autoplayad.activity;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.autoplayad.App;
import com.example.autoplayad.DataCallBack;
import com.example.autoplayad.utils.Config;
import com.example.autoplayad.utils.OkHttpUtils;
import com.example.autoplayad.R;
import com.stx.xhb.xbanner.XBanner;
import com.stx.xhb.xbanner.transformers.Transformer;
import com.tencent.bugly.Bugly;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author wenjiwang
 *
 *  使用Okhttp3网络请求图片数据在banner框架中实现轮播图
 */
public class MainActivity extends AppCompatActivity  {

    private MyHandler myHandler;
    private XBanner mXBanner;
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
    public void onBackPressed() {
        super.onBackPressed();
//        Toast.makeText(this,"再按一次推出程序",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bugly.init(this, Config.appId,false);

        setContentView(R.layout.activity_main);

        App.context=this;
        myHandler=new MyHandler(this);

//        View main = getLayoutInflater().inflate(R.layout.activity_main, null);
//        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        mXBanner = findViewById(R.id.banner);
        mXBanner.setAllowUserScrollable(true);
        mXBanner.setAutoPalyTime(Config.autoPlayTime);
        mXBanner.setPageTransformer(Transformer.Accordion);
        mXBanner.setData(list_path, list_title);
        mXBanner.loadImage(new XBanner.XBannerAdapter() {
            @Override
            public void loadBanner(XBanner banner, Object model, View view, int position) {
//                SimpleDraweeView simpleDraweeView=(SimpleDraweeView)view;
//                simpleDraweeView.setImageURI(Uri.parse((String)model));
                Glide.with(MainActivity.this).load((String) model)
                        .into((ImageView) view);
            }
        });
        mXBanner.startAutoPlay();
    }
    //数据加载
    private void initData() {



        if (!isDataExist)
        {
            getDataFromInternet2();
            Log.d("INIT_DATA","get data from internet");
//            Toast.makeText(this,"get data from internet",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //若无网络连接则从本地调用数据，优先网络实时请求数据
            ConnectivityManager connectivityManager = (ConnectivityManager)App.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo!=null&&activeNetworkInfo.isConnected()){
                getDataFromInternet2();
                Log.d("INIT_DATA","get data from internet");
            }else {
                getDataFromSP();
                Log.d("INIT_DATA","get data from SP");
            }

//            Toast.makeText(this,"get data from SP",Toast.LENGTH_SHORT).show();
        }
    }


    //本地提取数据
    private void getDataFromSP() {
        clearData();
        for (int i = 0; i <mSharedPreferences.getInt("len",1) ; i++) {
            list_title.add(mSharedPreferences.getString(("imgName"+i),""));
            list_path.add(mSharedPreferences.getString(("path"+i),""));
        }
        initView();
    }



//    //网络请求数据
//    private void getDataFromInternet() {
//        clearData();
//        String url="http://api.7958.com/index.php/api/image/getimg";
//        OkHttpUtils.getInstance().doGet(url, new DataCallBack() {
//            @Override
//            public void onSuccess(String result) {
//
//                try {
//                    JSONObject object=new JSONObject(result);
//                    JSONArray array=object.optJSONArray("data");
//                    mEditor.putInt("len",array.length());
//                    for (int i = 0; i <array.length() ; i++) {
//                        list_title.add(array.getJSONObject(i).optString("imgname")+" ");
//                        list_path.add(array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
//                        mEditor.putString("path"+i,array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
//                        mEditor.putString("imgName"+i,array.getJSONObject(i).optString("imgname")+" ");
//                    }
//                    mEditor.putInt("len",array.length());
//                    mEditor.putBoolean("isDataExist",true);
//                    mEditor.commit();
//                    initView();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailed() {
//
//            }
//        });
//    }

    //用handler传递数据
    private void getDataFromInternet2() {
        clearData();
        String url="http://api.7958.com/index.php/api/image/getimg";
        OkHttpUtils.getInstance().doGet2(url, new DataCallBack() {
            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject object=new JSONObject(result);
                    JSONArray array=object.optJSONArray("data");
                    mEditor.putInt("len",array.length());
                    for (int i = 0; i <array.length() ; i++) {
                        list_title.add(array.getJSONObject(i).optString("imgname")+" ");
//                        list_path.add(array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        list_path.add(array.getJSONObject(i).optString("imgurl"));
                        mEditor.putString("path"+i,array.getJSONObject(i).optString("imgurl").replace('\\','\0'));
                        mEditor.putString("imgName"+i,array.getJSONObject(i).optString("imgname")+" ");
                    }
                    mEditor.putInt("len",array.length());
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

            @Override
            public void onFailed() {

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
                        getDataFromInternet2();
                        Log.d("REFRESH_DATA","12整点刷新");

//                        Toast.makeText(MainActivity.this,"请更新加载今日图集",Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    }
}
