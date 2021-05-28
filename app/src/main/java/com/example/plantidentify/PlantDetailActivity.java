package com.example.plantidentify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PlantDetailActivity extends AppCompatActivity {
    private String plantName,userName;
    private boolean login;
    private SharedPreferences ifLogin;

    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    ArrayList<String> plantInformation = new ArrayList<>();

    TextView speciesName, chineseName, englishName, extraInfo;
    ImageView plantImg;
    Button collectBtn;
    //在消息队列中实现对控件的更改
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bitmap bmp = (Bitmap) msg.obj;
                    plantImg.setImageBitmap(bmp);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);

        Intent intent = getIntent();
        plantName = intent.getStringExtra("plantName");

        ifLogin =getSharedPreferences("if_login", MODE_PRIVATE);
        login = ifLogin.getBoolean("login", false);
        userName = ifLogin.getString("username", "");
        //初始化控件
        initView();
        showDetailPlantInfo(plantName);
        collectBtnClick();
    }

    private void collectBtnClick() {
        collectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (login){
                    collectPlant(userName,plantName);
                    //初始化Builder
                    builder = new AlertDialog.Builder(PlantDetailActivity.this);
                    builder.setTitle("提示")
                            .setMessage("收藏成功！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create().show();

                }else{
                    //初始化Builder
                    builder = new AlertDialog.Builder(PlantDetailActivity.this);
                    builder.setTitle("提示")
                            .setMessage("如需使用收藏功能请先登录！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create().show();
                  }
            }
        });
    }

    private void collectPlant(String userName, String plantName) {
        final String serverPath = "http://192.168.137.1:8888/add_collect";//本地测试

        new Thread(new Runnable() {
            @Override
            public void run() {
                String address =serverPath+ "?username=" + userName + "&plantname=" + plantName;
                HttpUtil.sendOkHttpReqest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PlantDetailActivity.this,"收藏成功！",Toast.LENGTH_LONG);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    private void showDetailPlantInfo(String plantName) {
        final String serverPath = "http://192.168.137.1:8888/select_plant";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                //http://192.168.137.1:8888/select_plant?plant_name=松
                String address = serverPath + "?plant_name=" + plantName;
                HttpUtil.sendOkHttpReqest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //TODO
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        parseEasyJson(responseMsg);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speciesName.setText(plantInformation.get(0));
                                chineseName.setText("中文名：" + plantInformation.get(0));
                                englishName.setText("英文名：" + plantInformation.get(1));
                                extraInfo.setText(plantInformation.get(2));
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap pic = getPic("http://192.168.137.1:8888/" + plantInformation.get(3));
                                        Message message = new Message();
                                        message.what = 0;
                                        message.obj = pic;
                                        handle.sendMessage(message);
                                    }
                                }).start();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    //初始化控件
    private void initView() {
        speciesName = findViewById(R.id.species_name);
        chineseName = findViewById(R.id.chinese_name);
        englishName = findViewById(R.id.english_name);
        extraInfo = findViewById(R.id.extra_info);
        plantImg = findViewById(R.id.plant_img);
        collectBtn =findViewById(R.id.btnCollect);
    }

    //解析JSON函数
    private void parseEasyJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            JSONArray jsonArray2 = (JSONArray) jsonArray.get(0);
            String plant_name = jsonArray2.getString(1);
            String english_plant_name = jsonArray2.getString(2);
            String plant_info = jsonArray2.getString(3);
            String plant_url = jsonArray2.getString(5);
            plantInformation.add(plant_name);
            plantInformation.add(english_plant_name);
            plantInformation.add(plant_info);
            plantInformation.add(plant_url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 传输网络图片
    public Bitmap getPic(String uriPic) {
        URL imageUrl = null;
        Bitmap bitmap = null;
        try {
            imageUrl = new URL(uriPic);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}