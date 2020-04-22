package com.example.kang.photoalbum;

import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class Search extends AppCompatActivity {

    ArrayList<String> filepath;
    ArrayList<String> fileList;
    GridAdapter adapter;
    ArrayList<String> showfileList;
    ArrayList<String> showfilename;
    String start;
    String end;
    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all);

        Intent intent = getIntent();
        start = intent.getStringExtra("start");
        end = intent.getStringExtra("end");
        filename = intent.getStringExtra("fileName");

        final EditText searchLabel = findViewById(R.id.editText);
        Button searchBt = findViewById(R.id.searchBt);
        GridView picGrid = findViewById(R.id.picGrid);

        filepath = new ArrayList<String>();//이미지 절대경로
        fileList = new ArrayList<>();//이미지 이름만

        //showfileList = new ArrayList<>();
        //showfilename = new ArrayList<>();

        Collections.reverse(filepath);
        Collections.reverse(fileList);

        adapter = new GridAdapter(this, fileList, filepath, 1);
        picGrid.setAdapter(adapter);

        searchBt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView no = findViewById(R.id.noImage);
                no.setText("");
                String lable = searchLabel.getText().toString();
                fileList.clear();
                filepath.clear();
                /*
                showfileList.clear();
                showfilename.clear();*/
                getAllImage(lable);
                Collections.reverse(filepath);
                Collections.reverse(fileList);
/*
                ArrayList<String> dateList = new ArrayList<>();
                for (int i = 0; i < filepath.size(); i++){
                    String path = filepath.get(i);
                    try {
                        ExifInterface exif = new ExifInterface(path);
                        String dateinfo =exif.getAttribute(ExifInterface.TAG_DATETIME);
                        if (dateinfo == null) {
                            File nullFile = new File(path);
                            SimpleDateFormat sf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                            dateinfo=sf.format(nullFile.lastModified());
                        }
                        dateList.add(dateinfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //사진 시기별 구분
                for(int i =0; i<filepath.size(); i++){
                    if(dateList.get(i).compareTo(start) > 0 && dateList.get(i).compareTo(end) < 0){
                        showfileList.add(filepath.get(i));
                        showfilename.add(fileList.get(i));
                    }
                }
*/
                if (fileList.isEmpty()){
                    no = findViewById(R.id.noImage);
                    no.setText("No Image");
                }

                adapter.notifyDataSetChanged();
            }
        });
        picGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(Search.this, Photo.class);
                String photoPath = filepath.get(position);
                photoIntent.putExtra("photo",photoPath);
                startActivity(photoIntent);
            }
        });

    }

    void getAllImage(String classify)
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
                String filename = c.getString(nameColumnIndex);
                String Fpath = c.getString(pathColumnIndex);

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(Fpath);
                }catch(IOException e){
                    e.printStackTrace();
                }
                String userComment =exif.getAttribute(ExifInterface.TAG_USER_COMMENT);

                if(userComment != null && userComment.toLowerCase().contains(classify)) {
                    if(userComment.toLowerCase().contains(filename)) {
                        fileList.add(filename);
                        filepath.add(Fpath);
                    }
                }
            }while(c.moveToNext());
        }
        c.close();
        return;
    }
}
