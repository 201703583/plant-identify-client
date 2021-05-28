package com.example.plantidentify;

import java.io.File;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    public static void sendOkHttpReqest(String address ,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(address)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void imageUpload(File file,String filename,String address,okhttp3.Callback callback){
        MediaType MEDIA_TYPE_JPG = MediaType.parse("image/*");
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody= new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //1,参数名称 这个参数名称是服务端 request.getParmars()要用的
                //2,本地上传文件的文件名
                .addFormDataPart("file",filename,
                        RequestBody.create(file,MEDIA_TYPE_JPG))
                .build();
        Request request = new Request.Builder()
                .url(address)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
