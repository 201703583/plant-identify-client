package com.example.plantidentify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangeUserInfoActivity extends AppCompatActivity {
    private SharedPreferences ifLogin;
    private Button btn_update;
    private EditText user,password,password1;
    //用户名，密码，再次输入的密码的控件的获取值
    private String userName,psw,pswAgain,oldUsername;

    //判断是否用户名重复
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_info);
        initView();

        ifLogin =getSharedPreferences("if_login", MODE_PRIVATE);
        oldUsername = ifLogin.getString("username", "");
        btnUpadteClicked();
    }

    private void btnUpadteClicked() {
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取输入在相应控件中的字符串
                getEditString();
                //判断输入框内容
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(ChangeUserInfoActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(psw)){
                    Toast.makeText(ChangeUserInfoActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(TextUtils.isEmpty(pswAgain)){
                    Toast.makeText(ChangeUserInfoActivity.this,"请再次输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }else if(!psw.equals(pswAgain)){
                    Toast.makeText(ChangeUserInfoActivity.this, "输入两次的密码不一样", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    isExistUserName(userName);
                    if(flag){
                        Toast.makeText(ChangeUserInfoActivity.this, "用户名重复！", Toast.LENGTH_SHORT).show();
                        return;
                    } else{
                        //更新用户信息
                        updateUserInfo(userName,psw,oldUsername);
                    }
                }
            }
        });
    }

    private void updateUserInfo(String newUsername, String newPassword, String oldUsername) {
        final String serverPath = "http://192.168.137.1:8888/update_userinfo";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                //http://127.0.0.1:5000/update_userinfo?newUsername=luosiqi&newPassword=123456&oldUsername=1998
                String address =serverPath+ "?newUsername=" + newUsername+ "&newPassword=" + newPassword+"&oldUsername="+oldUsername;
                HttpUtil.sendOkHttpReqest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Toast.makeText(ChangeUserInfoActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseMsg.equals("true")){
                                    //Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = ifLogin.edit();
                                    editor.putString("username",newUsername);
                                    editor.putBoolean("login",true);
                                    editor.commit();
                                    Intent intent = new Intent(ChangeUserInfoActivity.this, DrawerMainActivity.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(ChangeUserInfoActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void initView() {
        btn_update=findViewById(R.id.btn_update);
        user=findViewById(R.id.user);
        password=findViewById(R.id.password);
        password1=findViewById(R.id.password1);
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
                        Toast.makeText(ChangeUserInfoActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (responseMsg.equals("true")){
                                    Toast.makeText(ChangeUserInfoActivity.this, "此账户名已经存在", Toast.LENGTH_SHORT).show();
                                    flag=true;
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

}