package com.example.autoplayad.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.autoplayad.App;
import com.example.autoplayad.DataCallBack;
import com.example.autoplayad.utils.OkHttpUtils;
import com.example.autoplayad.R;
import com.example.autoplayad.utils.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignUpActivity extends AppCompatActivity {
    private Button bt_sign_in;
    private Button bt_send_verify_code;
    private EditText et_phoneText;
    private EditText et_verification_code;
    private EditText password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        App.context=this;
        initView();
        et_phoneText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String phoneText=charSequence.toString();
                if (phoneText.length()==11){
                    String telRegex = "^((13[0-9])|(15[^4])|(18[0-9])|(17[0-8])|(147,145))\\d{8}$";

                    boolean isPhone=phoneText.matches(telRegex);
                    if (!isPhone){
                        bt_send_verify_code.setEnabled(false);//好像多余了
                        ToastUtils.show(SignUpActivity.this,"请输入正确的手机号码");
                    }
                    else
                    {
                        bt_send_verify_code.setEnabled(true);
                    }

                }else {
                    bt_send_verify_code.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        //发送验证码
        bt_send_verify_code.setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick() {
                String phoneText=et_phoneText.getText().toString();
                getVerificationCode(phoneText);
            }

            @Override
            protected void onFastClick() {

            }
        });
        bt_sign_in.setOnClickListener(new CustomClickListener() {
            @Override
            protected void onSingleClick() {
                if (et_verification_code.getText().length()!=6){
                    ToastUtils.show(SignUpActivity.this,"请输入完整验证码");
                }
                if (password.getText().length()<6){
                    ToastUtils.show(SignUpActivity.this,"密码不得少于6位");
                }
                if (et_verification_code.getText().length()==6&&password.getText().length()>=6){
                    sign_up();
                }
            }

            @Override
            protected void onFastClick() {

            }
        });

    }

    private void initView() {
        getSupportActionBar().hide();


        bt_sign_in=(Button)findViewById(R.id.sign_in_button);
        bt_send_verify_code=(Button)findViewById(R.id.send_verify_code);
        bt_send_verify_code.setEnabled(false);
        et_phoneText=(EditText)findViewById(R.id.phoneText);
        et_verification_code=(EditText)findViewById(R.id.verification_code);
        et_phoneText.setInputType(InputType.TYPE_CLASS_PHONE);
        et_verification_code.setInputType(InputType.TYPE_CLASS_NUMBER);
        password=(EditText)findViewById(R.id.password);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }


    //注册
    private void sign_up() {

        String url="http://api.7958.com/index.php/api/login/registermember";
        Map<String,String> map=new HashMap<>();
        map.put("phone",et_phoneText.getText().toString());
        map.put("password",password.getText().toString());
        map.put("phonecode",et_verification_code.getText().toString());
        OkHttpUtils.getInstance().doPost(url, map, new DataCallBack() {
            @Override
            public void onSuccess(String result) {
                try {


                    JSONObject jsonObject=new JSONObject(result);
                    if (jsonObject.optString("code").equals("200")){
                        ToastUtils.show(SignUpActivity.this,"注册成功");
                        Intent intent=new Intent(SignUpActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }else {
                        ToastUtils.show(SignUpActivity.this,jsonObject.optString("msg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }

    private void getVerificationCode(String phoneNumber){
        String url="http://api.7958.com/index.php/api/login/getcode";
        Map<String,String> map=new HashMap<>();
        map.put("phone",phoneNumber);
        OkHttpUtils.getInstance().doPost(url, map, new DataCallBack() {
            @Override
            public void onSuccess(String result) {
                try {
                    bt_send_verify_code.setEnabled(false);
                    new CountDownTimer(5100, 1000) {
                        @Override
                        public void onTick(long l) {
                            bt_send_verify_code.setText(l/1000+"s后重发");
                        }

                        @Override
                        public void onFinish() {
                            bt_send_verify_code.setText("发送验证码");
                            bt_send_verify_code.setEnabled(true);
                        }
                    }.start();
                    JSONObject jsonObject=new JSONObject(result);
                    if (jsonObject.optString("code").equals("200")){
                        ToastUtils.show(SignUpActivity.this,"成功发送验证码");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed() {

            }
        });
    }
}
