package com.example.plantidentify;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.IpSecManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CollectionActivity extends AppCompatActivity {
    ListView listView;
    private Context mContext;
    private SharedPreferences ifLogin;
    private String userName;
    private List<Plant> mData = null;
    private PlantAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        mContext = CollectionActivity.this;
        ifLogin =getSharedPreferences("if_login", MODE_PRIVATE);
        userName=ifLogin.getString("username","");
        //初始化控件
        initView();
        mData = new LinkedList<Plant>();
        //显示收藏
        showCollect(userName);
        //设置每一项的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Plant plant = mData.get(position);
                String plantName = plant.getPlantName();
                //点击item后跳转详情页
                Intent intent = new Intent(mContext, PlantDetailActivity.class);
                intent.putExtra("plantName",plantName);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                //定义AlertDialog.Builder对象，当长按列表项的时候弹出确认删除对话框
                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                builder.setMessage("确定删除?");
                builder.setTitle("提示");

                //添加AlertDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mData.remove(position)!=null){
                            Plant plant = mData.get(position);
                            String plantName = plant.getPlantName();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final String serverPath = "http://192.168.137.1:8888/delete_collect";//本地测试
                                    //http://127.0.0.1:5000/select_collect?username=siyu
                                    String address =serverPath+ "?username=" + userName+"&plantname="+plantName;
                                    HttpUtil.sendOkHttpReqest(address, new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            //TODO
                                        }
                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                        }
                                    });
                                }
                            }).start();
                        }else {
                            System.out.println("failed");
                        }
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(getBaseContext(), "删除列表项", Toast.LENGTH_SHORT).show();
                    }
                });

                //添加AlertDialog.Builder对象的setNegativeButton()方法
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
                return false;
            }
        });
    }

    private void showCollect(String userName) {
        final String serverPath = "http://192.168.137.1:8888/select_collect";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                //http://127.0.0.1:5000/select_collect?username=siyu
                String address =serverPath+ "?username=" + userName;
                HttpUtil.sendOkHttpReqest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //TODO
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String responseMsg = response.body().string();
                        parseEasyJson(responseMsg);

                        mAdapter = new PlantAdapter((LinkedList<Plant>)mData, mContext);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = mAdapter;
                        handle.sendMessage(msg);
                    }
                });
            }
        }).start();
    }

    private void initView() {
        listView=findViewById(R.id.listView);
    }

    //解析JSON函数
    private void parseEasyJson(String json){
        mData = new LinkedList<>();
        try{
            JSONArray jsonArray = new JSONArray(json);
            for(int i = 0;i < jsonArray.length();i++){
                JSONArray jsonArray2 = (JSONArray) jsonArray.get(i);
                String plant_name = jsonArray2.getString(1);
                String plantEnglishName=jsonArray2.getString(2);
                String plant_url = jsonArray2.getString(4);
                Plant plant = new Plant();
                plant.setPlantName(plant_name);
                plant.setPlantEnglishName(plantEnglishName);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getPic("http://192.168.137.1:8888/"+plant_url);
                        plant.setPlantBitmap(bitmap);
                    }
                }).start();
                mData.add(plant);
            }
        }catch (Exception e){e.printStackTrace();}
    }

    //在消息队列中实现对控件的更改
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    listView.setAdapter((ListAdapter) msg.obj);
                    break;
            }
        };
    };

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