package com.example.autoplayad.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.autoplayad.App;
import com.example.autoplayad.DataCallBack;
import com.example.autoplayad.utils.Config;
import com.example.autoplayad.utils.OkHttpUtils;
import com.example.autoplayad.R;
import com.example.autoplayad.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Login activity.
 */
public class LaunchActivity extends AppCompatActivity {

   private Button bt_login;
   private Button btChangePassword;
   private Button btSignUp;
   private EditText et_phoneText;
   private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        App.context=this;
//        startService()
        initView();
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }

    private void initView() {
        getSupportActionBar().hide();

        bt_login=(Button)findViewById(R.id.login_button);
        btChangePassword=(Button)findViewById(R.id.bt_change_password);
        btSignUp=(Button)findViewById(R.id.sign_in_button);
        et_phoneText=(EditText)findViewById(R.id.phoneText);
        et_phoneText.setInputType(InputType.TYPE_CLASS_PHONE);

        password=(EditText)findViewById(R.id.password);
        password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LaunchActivity.this,SignUpActivity.class));
            }
        });

        btChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LaunchActivity.this, ChangePasswordActivity.class));
            }
        });

        //输入手机号校验
        bt_login.setEnabled(false);
        et_phoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phoneText=charSequence.toString();
                if (phoneText.length()==11){

                    boolean isPhone=phoneText.matches(Config.telRegex);
                    if (!isPhone){
                        bt_login.setEnabled(false);//好像多余了
                        Toast.makeText(LaunchActivity.this,"请输入正确的手机号",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        bt_login.setEnabled(true);
                    }

                }else {
                    bt_login.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    //登录
    private void login() {
        if((!TextUtils.isEmpty(password.getText().toString()))&&password.getText().length()>5) {
            String url = "http://api.7958.com/public/index.php/api/login/userlogin";
            Map<String, String> map = new HashMap<>();
            map.put("phone", et_phoneText.getText().toString());
            map.put("password", password.getText().toString());
            OkHttpUtils.getInstance().doPost(url, map, new DataCallBack() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.optString("code") == "200") {
                            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            ToastUtils.show(LaunchActivity.this,"登录成功");

                        }
                        else {
                            ToastUtils.show(LaunchActivity.this,jsonObject.optString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailed() {

                }
            });
        }else {
            ToastUtils.show(LaunchActivity.this,"密码不能为空或小于6位");
        }
    }



}
