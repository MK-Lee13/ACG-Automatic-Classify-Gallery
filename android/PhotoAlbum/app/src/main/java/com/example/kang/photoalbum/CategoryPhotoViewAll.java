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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CategoryPhotoViewAll extends AppCompatActivity {

    GridView photoList;
    //ArrayList<String> files;
    ArrayList<String> filepath;
    String cat;
    String start;
    String end;
    String catHan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_photo_view_all);
        Intent intent = getIntent();
        cat = intent.getStringExtra("catName");
        catHan = intent.getStringExtra("catHan");

        //----------------------------------------navigateView
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All-"+catHan);

        filepath = new ArrayList<String>();//이미지 절대경로
        ArrayList<String> fileList = getAllImage(cat);//이미지 이름만

        Collections.reverse(filepath);
        Collections.reverse(fileList);

        photoList = findViewById(R.id.photoList);
        GridAdapter adapter = new GridAdapter(this,fileList, filepath, 1);
        photoList.setAdapter(adapter);
        photoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(CategoryPhotoViewAll.this, Photo.class);
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
                String filename = c.getString(nameColumnIndex);
                String Fpath = c.getString(pathColumnIndex);

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(Fpath);
                }catch(IOException e){
                    e.printStackTrace();
                }
                String userComment =exif.getAttribute(ExifInterface.TAG_USER_COMMENT);

                if(userComment != null && userComment.matches(".*"+classify+".*")) {
                    result.add(filename);
                    filepath.add(Fpath);
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
