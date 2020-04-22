package com.example.kang.photoalbum;

import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CategoryPhotoView extends AppCompatActivity {

    GridView photoList;
    //ArrayList<String> files;
    ArrayList<String> filepath;
    String cat;
    String start;
    String end;
    String filename;
    ArrayList<String>showfileList;//경로
    ArrayList<String>showfilename;
    String catHan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_photo_view);
        Intent intent = getIntent();
        cat = intent.getStringExtra("catName");
        start = intent.getStringExtra("start");
        end = intent.getStringExtra("end");
        filename = intent.getStringExtra("fileName");
        catHan = intent.getStringExtra("catHan");

//----------------------------------------navigateView
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(filename+"-"+catHan);

        filepath = new ArrayList<String>();//이미지 절대경로
        ArrayList<String> fileNameList = getAllImage(cat);//이미지 이름만
/*
//시기 분류함------------------------------------------------------------
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

        showfileList = new ArrayList<String>();//경로
        showfilename = new ArrayList<String>();//이름

        //사진 시기별 구분
        for(int i =0; i<filepath.size(); i++){
            if(dateList.get(i).compareTo(start) > 0 && dateList.get(i).compareTo(end) < 0){
                showfileList.add(filepath.get(i));
                showfilename.add(fileList.get(i));
            }
        }
*/
        //Collections.reverse(showfileList);
        //Collections.reverse(showfilename);

        photoList = findViewById(R.id.photoList);
        GridAdapter adapter = new GridAdapter(this, fileNameList, filepath, 1);
        photoList.setAdapter(adapter);
        photoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(CategoryPhotoView.this, Photo.class);
                String photoPath = filepath.get(position);
                photoIntent.putExtra("photo",photoPath);
                startActivity(photoIntent);
            }
        });
    }

    ArrayList<String> getAllImage(String classify)
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
                String filenamePath = c.getString(nameColumnIndex);
                String Fpath = c.getString(pathColumnIndex);

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(Fpath);
                }catch(IOException e){
                    e.printStackTrace();
                }
                String userComment =exif.getAttribute(ExifInterface.TAG_USER_COMMENT);

                if(userComment != null && userComment.contains(classify)) {
                    if(userComment.contains(filename)) {
                        result.add(filenamePath);
                        filepath.add(Fpath);
                    }
                }
            }while(c.moveToNext());
        }
        c.close();
        if (filepath.isEmpty()){
            TextView no = findViewById(R.id.noImage);
            no.setText("No Image");
        }
        return result;
    }
}
