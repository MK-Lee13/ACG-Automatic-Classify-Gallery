package com.example.kang.photoalbum;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ClassifiedMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    ArrayList<String> category;
    ArrayList<String> categoryInUserComment;
    ArrayList<String> thumbnail;
    String start;
    String end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classified_main);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");

        category = new ArrayList<String>(Arrays.asList("단체사진", "셀카", "인물","좋은구도", "풍경","음식", "나머지"));
        categoryInUserComment = new ArrayList<String>(Arrays.asList("group", "self", "pbig", "line","land","food", "rest"));
        thumbnail = new ArrayList<String>(Arrays.asList("None", "None","None","None", "None", "None","None"));
        GridView categoryList = findViewById(R.id.categoryList);
//----------------------------------------navigateView
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final String Fname = fileName.substring(0, fileName.indexOf('.'));
        getSupportActionBar().setTitle(Fname);

        Button search = findViewById(R.id.searchBt);
        search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serachIntent = new Intent(ClassifiedMain.this, Search.class);
                serachIntent.putExtra("start", start);
                serachIntent.putExtra("end", end);
                serachIntent.putExtra("fileName", Fname);
                startActivity(serachIntent);
            }
        });
//-------------------파일읽기(날짜)------------------------------
        String line = null; // 한줄씩 읽기
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedReader buf = new BufferedReader(new FileReader(saveFile+"/"+fileName));
            while((line=buf.readLine())!=null){
                if(line.startsWith("start")){
                    start = line.substring(line.indexOf(":")+1);
                }
                if(line.startsWith("end")){
                    end = line.substring(line.indexOf(":")+1);
                }
                if(line.startsWith("group")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(0,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(0,thumb);
                }
                if(line.startsWith("self")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(1,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(1,thumb);
                }
                if(line.startsWith("pbig")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(2,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(2,thumb);
                }
                if(line.startsWith("line")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(3,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(3,thumb);
                }
                if(line.startsWith("land")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(4,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(4,thumb);
                }
                if(line.startsWith("food")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(5,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(5,thumb);
                }
                if(line.startsWith("rest")){
                    String thumb = line.substring(line.indexOf(":")+1);
                    if (thumb.equals("None")){
                        thumbnail.set(6,"android.resource://com.example.kang.photoalbum/"+R.drawable.noimage);
                    }
                    else
                        thumbnail.set(6,thumb);
                }
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//----------------------카테고리 리스트 만들기-----------------------------
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,category);
        GridAdapter adapter = new GridAdapter(this,category, thumbnail);
        categoryList.setAdapter(adapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent catIntent = new Intent(ClassifiedMain.this, CategoryPhotoView.class);
                catIntent.putExtra("catName",categoryInUserComment.get(position));
                catIntent.putExtra("start", start);
                catIntent.putExtra("end",end);
                catIntent.putExtra("fileName",Fname);
                catIntent.putExtra("catHan", category.get(position));
                startActivity(catIntent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
