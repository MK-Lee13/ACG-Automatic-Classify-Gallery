package com.example.kang.photoalbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> fileName;
    private ArrayList<String> filepath;
    private int num;//0 이름까지 1: 이름 없이 사진만 2: 추가버튼 넣음 3: 썸네일 없음

    public GridAdapter(Context context, ArrayList<String> fileName, ArrayList<String> filePath) {
        this.context = context;
        this.fileName = fileName;
        this.filepath = filePath;
        num = 0;
    }
    public GridAdapter(Context context, ArrayList<String> fileName, ArrayList<String> filePath, Integer num) {
        this.context = context;
        this.fileName = fileName;
        this.filepath = filePath;
        this.num = num;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int pos = position;
        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item, parent, false);
        }

        if(num == 2 && pos == fileName.size()){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item2, parent, false);

            FloatingActionButton addButton = convertView.findViewById(R.id.addButton);

            return convertView;
        }

        ImageView thumb = (ImageView) convertView.findViewById(R.id.thumbnail);
        if(num != 1) {
            TextView title = (TextView) convertView.findViewById(R.id.photoName);
            String titleString = fileName.get(pos);
            if (titleString.endsWith((".txt"))){
                titleString = titleString.substring(0, titleString.indexOf('.'));
            }
            title.setText(titleString);
        }

        String thumbImag = filepath.get(pos);
        if(thumbImag != "None") {
            Glide.with(context)
                    .load(thumbImag)
                    .into(thumb);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        /*if(num == 2){
            return fileName.size()+1;
        }*/
        return fileName.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}