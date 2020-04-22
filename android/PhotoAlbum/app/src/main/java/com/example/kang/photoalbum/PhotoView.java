package com.example.kang.photoalbum;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PhotoView extends AppCompatActivity {

    GridView photoList;
    //ArrayList<String> files;
    ArrayList<String> filepath;
    String dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity);

        //tool bar--------------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        dir = intent.getStringExtra("dirName");

        getSupportActionBar().setTitle(dir);

        filepath = new ArrayList<String>();//이미지 절대경로
        ArrayList<String> fileList = getAllImage();//이미지 이름만

        Collections.reverse(filepath);
        Collections.reverse(fileList);

        photoList = findViewById(R.id.photoList);
        GridAdapter adapter = new GridAdapter(this,fileList, filepath, 1);
        photoList.setAdapter(adapter);
        photoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(PhotoView.this, Photo.class);
                String photoPath = filepath.get(position);
                photoIntent.putExtra("photo",photoPath);
                startActivity(photoIntent);
            }
        });
    }

    ArrayList<String> getAllImage()
    {
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
        };
        Cursor c = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null,null,null);

        ArrayList<String> result = new ArrayList<String>(c.getCount());

        int bucketColumnIndex = c.getColumnIndex(projection[0]);
        int nameColumnIndex = c.getColumnIndex(projection[1]);
        int pathColumnIndex = c.getColumnIndex(projection[2]);

        if(c == null){
            //Toast.makeText(getApplicationContext(),"사진없음",1);
        } else if (c.moveToFirst()){
            do{
                String bucket = c.getString(bucketColumnIndex);
                String filename = c.getString(nameColumnIndex);
                String Fpath = c.getString(pathColumnIndex);
                if(bucket.equals(dir)) {
                    result.add(filename);
                    filepath.add(Fpath);
                }
            }while(c.moveToNext());
        }
        c.close();
        return result;
    }
}
