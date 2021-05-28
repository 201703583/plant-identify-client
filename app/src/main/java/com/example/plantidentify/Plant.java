package com.example.plantidentify;

import android.graphics.Bitmap;

public class Plant {
    private String plantName;
    private String plantEnglishName;
    private Bitmap plantBitmap;

    public Plant() {
    }

    public Plant(String plantName, String plantEnglishName, Bitmap plantBitmap) {
        this.plantName = plantName;
        this.plantEnglishName = plantEnglishName;
        this.plantBitmap = plantBitmap;
    }

    public String getPlantEnglishName() {
        return plantEnglishName;
    }

    public void setPlantEnglishName(String plantEnglishName) {
        this.plantEnglishName = plantEnglishName;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public Bitmap getPlantBitmap() {
        return plantBitmap;
    }

    public void setPlantBitmap(Bitmap plantBitmap) {
        this.plantBitmap = plantBitmap;
    }
}
