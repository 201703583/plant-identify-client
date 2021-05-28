package com.example.plantidentify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    Button register,login,overbtn;
    EditText username,pwd;
    CheckBox remember;

    private SharedPreferences.Editor editor;
    private SharedPreferences sp;
    private SharedPreferences ifLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sp =getSharedPreferences("info1.txt", MODE_PRIVATE);
        ifLogin =getSharedPreferences("if_login", MODE_PRIVATE);
        editor=ifLogin.edit();
        editor.putBoolean("login",false);
        editor.commit();

        //初始化控件
        initView();

        //若勾选记住密码，填入记录在本地的用户信息
        boolean isRemember = sp.getBoolean("remember_password", false);
        if (isRemember) {
            //将账号密码都设置到文本框中
            String account = sp.getString("username", "");
            String password = sp.getString("password", "");
            username.setText(account);
            pwd.setText(password);
            remember.setChecked(true);
        }

        //监听登录按钮
        clickLoginBtn();
        clickRegisterBtn();
        clickOverBtn();
    }

    private void clickOverBtn() {
        overbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clickRegisterBtn() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }


    //初始化组件
    private void initView() {
        username=findViewById(R.id.username);
        pwd=findViewById(R.id.pwd);
        register=findViewById(R.id.register);
        login=findViewById(R.id.login);
        remember=findViewById(R.id.remember);
        overbtn=findViewById(R.id.over);
    }

    //点击login做的事
    private void clickLoginBtn() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = username.getText().toString();
                final String password = pwd.getText().toString();
                //服务端路径
                //final String serverPath = "http://121.4.170.240:80/signin";
                final String serverPath = "http://192.168.137.1:8888/signin";//本地测试


                if (TextUtils.isEmpty(name)||TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"用户名或密码不能为空！",Toast.LENGTH_SHORT).show();
                }else{
                    editor=sp.edit();
                    if (remember.isChecked()){
                        editor.putString("username",name );
                        editor.putString("password",password);
                        editor.putBoolean("remember_password", true);
                    }else{
                        editor.clear();
                    }
                    editor.commit();



                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String address =serverPath+ "?username=" + name + "&password=" + password;
                            HttpUtil.sendOkHttpReqest(address, new okhttp3.Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    Toast.makeText(LoginActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    final String responseMsg = response.body().string();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (responseMsg.equals("true")){
                                                //存储登录信息
                                                editor=ifLogin.edit();
                                                editor.putString("username",name);
                                                editor.putBoolean("login",true);
                                                editor.commit();
                                                //Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(LoginActivity.this, DrawerMainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Toast.makeText(LoginActivity.this, "登录失败！", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }

}