package com.example.plantidentify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private Button btn_register;//注册按钮
    //用户名，密码，再次输入的密码的控件
    private EditText user,password,password1;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw,pswAgain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面布局 ,注册界面
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {

        btn_register=findViewById(R.id.btn_register);
        user=findViewById(R.id.user);
        password=findViewById(R.id.password);
        password1=findViewById(R.id.password1);
        //注册按钮
        btn_register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容

                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(RegisterActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    isExistUserName(userName);
                    saveRegisterInfo(userName, psw);
                }
            }
        });
    }


    /**
     * 获取控件中的字符串
     */
    private void getEditString(){
        //trim() 方法用于删除字符串的头尾空白符
        userName=user.getText().toString().trim();
        psw=password.getText().toString().trim();
        pswAgain=password1.getText().toString().trim();
    }


    /**
     * 从Database中读取输入的用户名，判断Database中是否有此用户名
     */
    private void isExistUserName(String userName){
        final String serverPath = "http://192.168.137.1:8888/ifExistUserName";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                String address =serverPath+ "?username=" + userName;
                HttpUtil.sendOkHttpReqest(address, new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(RegisterActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseMsg.equals("true")){
                                    Toast.makeText(RegisterActivity.this, "此账户名已经存在", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }


    /**
     * 保存账号和密码到database
     */
    private void saveRegisterInfo(String userName,String password){
        final String serverPath = "http://192.168.137.1:8888/new_user";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                String address =serverPath+ "?username=" + userName+ "&password=" + password;
                HttpUtil.sendOkHttpReqest(address, new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(RegisterActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseMsg.equals("true")){
                                    //Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }
}
