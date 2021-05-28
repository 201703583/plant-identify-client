package com.example.plantidentify;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ServiceConfigurationError;

public class PlantAdapter extends BaseAdapter {
    private LinkedList<Plant> mData;
    private Context mContext;

    public PlantAdapter(LinkedList<Plant> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);

        ImageView plant_image  = (ImageView) convertView.findViewById(R.id.item_img);
        TextView plant_name  = (TextView) convertView.findViewById(R.id.plant_name);
        TextView plant_english_name  = (TextView) convertView.findViewById(R.id.plant_englishname);

        plant_image.setImageBitmap(mData.get(position).getPlantBitmap());
        plant_name.setText(mData.get(position).getPlantName());
        plant_english_name.setText(mData.get(position).getPlantEnglishName());

        return convertView ;
    }

}
