package com.example.autoplayad.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtils {
    public static void show(Context context, String message){
        Toast toast=Toast.makeText(context,message,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP,0,30);
        toast.show();
    }
    public static void show(Context context, String message,int xOffset,int yOffset){
        Toast toast=Toast.makeText(context,message,Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP,xOffset,yOffset);
        toast.show();
    }
}
