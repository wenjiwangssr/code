package com.example.autoplayad;


import android.app.Activity;
import android.app.Application;
import android.os.Environment;

import com.example.autoplayad.activity.MainActivity;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

public class App extends Application {
    private static App instance;
    public static Activity context;

    public static App getInstance() {
        if(instance==null){
            synchronized (App.class){
                instance=new App();
                Bugly.init(App.instance,"02c221b6f0",false);
                Beta.autoInit=true;
                Beta.autoCheckUpgrade = true;
                Beta.upgradeCheckPeriod = 60 * 1000;
                Beta.initDelay=1*1000;
                Beta.storageDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                Beta.autoDownloadOnWifi=true;
                Beta.upgradeCheckPeriod = 60 * 1000;
                Beta.canShowUpgradeActs.add(MainActivity.class);
//                Beta.upgradeDialogLayoutId = R.layout.upgrade_dialog;//更新界面布局
                Beta.enableHotfix=true;//热更新？暂且开启
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
