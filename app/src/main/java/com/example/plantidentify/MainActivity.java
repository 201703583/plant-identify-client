 package com.example.plantidentify;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    //调试变量
    //private static final String TAG = "luo";
    private ImageView predict_image;
    private TextView predict_type;

    private Button btnPhoto, btnSelect;
    private EditText search;
    private Intent intent;
    private final int CAMERA = 1;//事件枚举(可以自定义)
    private final int CHOOSE = 2;//事件枚举(可以自定义)
    //private final String postUrl = "http://121.4.170.240:80/upload_photo";//接收上传图片的地址
    private final String postUrl = "http://192.168.137.1:8888/upload_photo";//本地调式
    String photoPath = "";//要上传的图片路径
    private final int permissionCode = 100;//权限请求码
    private ProgressDialog progressDialog;//加载框
    private Context mContext;

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET
    };
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext=MainActivity.this;

        //6.0才用动态权限
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }

        btnPhoto = findViewById(R.id.btnPhoto);
        btnSelect = findViewById(R.id.btnSelect);
        search = findViewById(R.id.search);
        predict_image = findViewById(R.id.predict_image);
        predict_type = findViewById(R.id.predict_type);
        //监听拍照按钮
        btnCameraClick();
        //监听相册按钮
        btnPhotoClick();
        //监听搜索按钮
        searchClick();
    }

    private void searchClick() {
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                    String search_words = search.getText().toString();
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("search_words",search_words);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    //检查权限
    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permissions[i]);
            }
        }
        if (permissionList.size() <= 0) {
            //说明权限都已经通过，可以做你想做的事情去
        } else {
            //存在未允许的权限
            ActivityCompat.requestPermissions(this, permissions, permissionCode);
        }
    }

    //授权后回调函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean haspermission = false;
        if (permissionCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    haspermission = true;
                }
            }
            if (haspermission) {
                //跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                permissionDialog();
            } else {
                //全部权限通过，可以进行下一步操作
            }
        }

    }

    //打开手动设置应用权限
    private void permissionDialog() {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("提示信息")
                    .setMessage("当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                    .setPositiveButton("设置", (dialog, which) -> {
                        cancelPermissionDialog();
                        Uri packageURI = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        alertDialog.show();
    }

    //用户取消授权
    private void cancelPermissionDialog() {
        alertDialog.cancel();
    }

    //监听拍照按钮事件
    private void btnCameraClick() {
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                方法一：这样拍照只能取到缩略图（不清晰）
                intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA);
                */

                //方法二：指定加载路径图片路径（保存原图，清晰）
                //返回一个文件对象toString后形如/storage/sdcard0
                String SD_PATH = Environment.getExternalStorageDirectory().getPath() + "/拍照上传示例/";
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                String fileName = format.format(new Date(System.currentTimeMillis())) + ".JPEG";
                photoPath = SD_PATH + fileName;     //形如20210409163258.JPEG
                File file = new File(photoPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                //兼容7.0以上的版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        ContentValues values = new ContentValues(1);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                        values.put(MediaStore.Images.Media.DATA, photoPath);
                        Uri tempuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        if (tempuri != null) {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempuri);
                            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        }
                        startActivityForResult(intent, CAMERA);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Standard Intent action that can be sent to have the camera application capture an image and return it
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri); //指定拍照后的存储路径，保存原图
                    startActivityForResult(intent, CAMERA);
                }
            }
        });
    }

    //监听相册按钮事件
    private void btnPhotoClick() {
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //选择按钮事件
                intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHOOSE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // 调用照相机拍照
            case CAMERA:
                if (resultCode == RESULT_OK) {
                    //对应方法一：图片未保存，需保存
                    //TODO
                    //对应方法二：图片已保存，只需读取就行了
                    try {
                        FileInputStream stream = new FileInputStream(photoPath);
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);

                        //预览图片
                        ImageView image = findViewById(R.id.imageView);
                        image.setImageBitmap(bitmap);

                        //上传图片（Android 4.0 之后不能在主线程中请求HTTP请求）
                        File file = new File(photoPath);
                        if (file.exists()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //图片字段
                                    String filename= PathHelper.getFileNameFromPath(photoPath)+".JPEG";
                                    HttpUtil.imageUpload(file,filename,postUrl, new okhttp3.Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            //Log.d(TAG, "onFailure: ");
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            final String responseMsg = response.body().string();
                                            //形如石榴: 56.54%
                                            //Log.d(TAG, "onResponse: "+responseMsg);
                                            String[] strings = responseMsg.split(",");
                                            showPlant(strings[0],strings[1]);
                                        }
                                    });
                                }
                            }).start();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            // 选择图片库的图片
            case CHOOSE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        photoPath = PathHelper.getRealPathFromUri(MainActivity.this, uri);
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                        //压缩图片
                        bitmap = scaleBitmap(bitmap, (float) 0.5);

                        //预览图片
                        ImageView image = findViewById(R.id.imageView);
                        image.setImageBitmap(bitmap);

                        //上传图片（Android 4.0 之后不能在主线程中请求HTTP请求）
                        File file = new File(photoPath);
                        if (file.exists()) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //图片字段
                                    String filename= PathHelper.getFileNameFromPath(photoPath)+".JPEG";
                                    HttpUtil.imageUpload(file,filename,postUrl, new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            //Log.d(TAG, "onFailure: ");
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            final String responseMsg = response.body().string();
                                            //Log.d(TAG, "onResponse: "+responseMsg);
                                            String[] strings = responseMsg.split(",");
                                            showPlant(strings[0],strings[1]);

                                        }
                                    });
                                }
                            }).start();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    //压缩图片
    public Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        return newBM;
    }

    public void showPlant(String plant_name,String picture_url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getPic(picture_url);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        predict_type.setText(plant_name);
                        predict_image.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
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