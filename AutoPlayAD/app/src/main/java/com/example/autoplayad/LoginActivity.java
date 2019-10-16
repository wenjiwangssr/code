package com.example.autoplayad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The type Login activity.
 */
public class LoginActivity extends AppCompatActivity {

   private Button bt_login;
   private Button bt_sign_in;
   private Button bt_send_verify_code;
   private EditText et_phoneText;
   private EditText et_verification_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();


        //手机号正则校验，否则无法发送验证码
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
                        Toast.makeText(LoginActivity.this,"请输入正确的手机号",Toast.LENGTH_LONG).show();
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
        bt_send_verify_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneText=et_phoneText.getText().toString();
                    getVerificationCode(phoneText);

            }
        });

        bt_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void initView() {
        getSupportActionBar().hide();

        bt_login=(Button)findViewById(R.id.login_button);
        bt_send_verify_code=(Button)findViewById(R.id.send_verify_code);
        bt_sign_in=(Button)findViewById(R.id.sign_in_button);

        bt_send_verify_code.setEnabled(false);
        et_phoneText=(EditText)findViewById(R.id.phoneText);
        et_verification_code=(EditText)findViewById(R.id.verification_code);
        et_phoneText.setInputType(InputType.TYPE_CLASS_PHONE);
        et_verification_code.setInputType(InputType.TYPE_CLASS_NUMBER);
    }


    private void getVerificationCode(String phoneNumber){
        
    }
}
