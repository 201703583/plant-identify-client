package com.example.plantidentify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {
    Button btnBack;
    EditText key_words;
    ListView listView;
    private String search_words;
    private List<Plant> mData = null;
    private Context mContext;
    private PlantAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = SearchActivity.this;

        Intent intent = getIntent();
        search_words = intent.getStringExtra("search_words");
        //初始化控件
        initView();

        //获取搜索结果
        showSearchResult(search_words);

        //一些控件动作函数
        clickBtnBack();
        searchClick();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Plant plant = mData.get(position);
                String plantName = plant.getPlantName();
                //点击item后跳转详情页
                Intent intent = new Intent(SearchActivity.this, PlantDetailActivity.class);
                intent.putExtra("plantName",plantName);
                startActivity(intent);
            }
        });
    }

    private void showSearchResult(String key_word) {
        final String serverPath = "http://192.168.137.1:8888/select_plant";//本地测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                //http://192.168.137.1:8888/select_plant?plant_name=松
                String address =serverPath+ "?plant_name=" + key_word;
                HttpUtil.sendOkHttpReqest(address, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //Toast.makeText(SearchActivity.this, "网络连接失败！请检查网络", Toast.LENGTH_LONG).show();
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

    private void searchClick() {
        key_words.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /**
             * 参数说明
             * @param v 被监听的对象
             * @param actionId  动作标识符,如果值等于EditorInfo.IME_NULL，则回车键被按下。
             * @param keyEvent    如果由输入键触发，这是事件；否则，这是空的(比如非输入键触发是空的)。
             * @return 返回你的动作
             */
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    String search_words = key_words.getText().toString();
                    Intent intent = new Intent(SearchActivity.this, SearchActivity.class);
                    intent.putExtra("search_words",search_words);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    private void clickBtnBack() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        btnBack=findViewById(R.id.btnBack);
        key_words=findViewById(R.id.key_words);
        listView=findViewById(R.id.listView);

        //写在这里免得onCreate函数难看
        key_words.setText(search_words);
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

}