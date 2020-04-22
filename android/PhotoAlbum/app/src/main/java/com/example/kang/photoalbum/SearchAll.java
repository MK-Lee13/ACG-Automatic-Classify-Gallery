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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SearchAll extends AppCompatActivity {

    ArrayList<String> filepath;
    ArrayList<String> fileList;
    GridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_all);

        final EditText searchLabel = findViewById(R.id.editText);
        Button searchBt = findViewById(R.id.searchBt);
        GridView picGrid = findViewById(R.id.picGrid);

        filepath = new ArrayList<String>();//이미지 절대경로
        fileList = new ArrayList<>();//이미지 이름만

        Collections.reverse(filepath);
        Collections.reverse(fileList);

        adapter = new GridAdapter(this,fileList, filepath, 1);
        picGrid.setAdapter(adapter);

        searchBt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView no = findViewById(R.id.noImage);
                no.setText("");
                String lable = searchLabel.getText().toString();
                fileList.clear();
                filepath.clear();
                getAllImage(lable);
                Collections.reverse(filepath);
                Collections.reverse(fileList);
                adapter.notifyDataSetChanged();
            }
        });
        picGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(SearchAll.this, Photo.class);
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
                    fileList.add(filename);
                    filepath.add(Fpath);
                }
            }while(c.moveToNext());
        }
        c.close();
        if (filepath.isEmpty()){
            TextView no = findViewById(R.id.noImage);
            no.setText("No Image");
        }
        return;
    }
}
