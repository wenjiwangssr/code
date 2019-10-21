package com.example.autoplayad;


import android.app.Activity;
import android.app.Application;
import android.os.Environment;

import com.example.autoplayad.activity.LaunchActivity;
import com.example.autoplayad.activity.MainActivity;
import com.example.autoplayad.utils.Config;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

public class App extends Application {
    private static App instance;
    public static Activity context;

    public static App getInstance() {
        if(instance==null){
            synchronized (App.class){
                instance=new App();
                Bugly.init(App.instance, Config.appId,false);
                Beta.autoInit=true;
                Beta.autoCheckUpgrade = true;
                Beta.upgradeCheckPeriod =3 * 1000;
                Beta.initDelay=500;
                Beta.storageDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                Beta.autoDownloadOnWifi=true;
                Beta.canShowUpgradeActs.add(LaunchActivity.class);
                Beta.canShowUpgradeActs.add(MainActivity.class);
//                Beta.upgradeDialogLayoutId = R.layout.upgrade_dialog;//更新界面布局
//                Beta.enableHotfix=true;//热更新？暂且开启
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
