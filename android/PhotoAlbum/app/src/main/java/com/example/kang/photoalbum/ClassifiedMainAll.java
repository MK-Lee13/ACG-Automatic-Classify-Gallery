package com.example.kang.photoalbum;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Arrays;

public class ClassifiedMainAll extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    ArrayList<String> category;
    ArrayList<String> categoryInUserComment;
    ArrayList<String> thumbnail;
    String start;
    String end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classified_main_all);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");

        category = new ArrayList<String>(Arrays.asList("단체사진", "셀카", "인물", "좋은구도", "풍경","음식", "나머지"));
        categoryInUserComment = new ArrayList<String>(Arrays.asList("group", "self", "pbig", "line","land","food", "rest"));
        thumbnail = new ArrayList<String>(Arrays.asList("android.resource://com.example.kang.photoalbum/"+R.drawable.group,
                "android.resource://com.example.kang.photoalbum/"+R.drawable.zzz,"android.resource://com.example.kang.photoalbum/"+R.drawable.human,
                "android.resource://com.example.kang.photoalbum/"+R.drawable.line, "android.resource://com.example.kang.photoalbum/"+R.drawable.zz,
                "android.resource://com.example.kang.photoalbum/"+R.drawable.food, "android.resource://com.example.kang.photoalbum/"+R.drawable.rest));
        GridView categoryList = findViewById(R.id.categoryList);
//----------------------------------------navigateView
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All");

        Button search = findViewById(R.id.searchBt);
        search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serachIntent = new Intent(ClassifiedMainAll.this, SearchAll.class);
                startActivity(serachIntent);
            }
        });

//----------------------카테고리 리스트 만들기-----------------------------
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,category);
        GridAdapter adapter = new GridAdapter(this,category, thumbnail);
        categoryList.setAdapter(adapter);
        categoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent catIntent = new Intent(ClassifiedMainAll.this, CategoryPhotoViewAll.class);
                catIntent.putExtra("catName",categoryInUserComment.get(position));
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
