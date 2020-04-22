package com.example.kang.photoalbum;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    ArrayList<String> listOfAllImages;
    ExpandableHeightGridView photoListView;
    ExpandableHeightGridView classListView;
    AmazonS3 s3Client;
    String bucket = "BUCKETNAME";
    String accessKey = "ACCESSKEY";
    String secretKey = "SECRETKEY";
    GridAdapter tempadapter;
    ArrayList<String> fileNameList;//분류한 txt파일들의 리스트
    ArrayList<String> thumbnail;
    ArrayList<String> classedThumbnail;
    String startDate;
    String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//-----------tool bar------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ACG");

        Button search = findViewById(R.id.searchBt);
        search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serachIntent = new Intent(MainActivity.this, SearchAll.class);
                startActivity(serachIntent);
            }
        });

//------------AWS client------------
        s3Client = new AmazonS3Client(new BasicSessionCredentials("ACCESSKEY", "SECRETKEY",""));
        s3Client.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
//------------photo------------
        photoListView = findViewById(R.id.categoryList);//기존사진
        photoListView.setExpanded(true);
        thumbnail = new ArrayList<String>();
        listOfAllImages = getAllImage();//폴더 목록
        GridAdapter adapter = new GridAdapter(this,listOfAllImages, thumbnail);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listOfAllImages);
        photoListView.setAdapter(adapter);

        photoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent dirIntent = new Intent(MainActivity.this, PhotoView.class);
                dirIntent.putExtra("dirName",listOfAllImages.get(position));
                startActivity(dirIntent);
            }
        });

        classListView = findViewById(R.id.tempList);//분류 된 사진
        classListView.setExpanded(true);
        fileNameList = new ArrayList<String>();
        getTxtFileList();
        classedThumbnail =  new ArrayList<String>();
        getclassedThumbnail(fileNameList);
        tempadapter = new GridAdapter(this,fileNameList, classedThumbnail,2);
        classListView.setAdapter(tempadapter);

        classListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (fileNameList.get(position).equals("All.txt")){
                    Intent classIntent = new Intent(MainActivity.this, ClassifiedMainAll.class);
                    classIntent.putExtra("fileName", fileNameList.get(position));
                    startActivity(classIntent);
                }
                else {
                    Intent classIntent = new Intent(MainActivity.this, ClassifiedMain.class);
                    classIntent.putExtra("fileName", fileNameList.get(position));
                    startActivity(classIntent);
                }
            }
        });

        classListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showFileRemove(position);
                return true;
            }
        });

//------------select button------------
        Button classifyBt = findViewById(R.id.classifybt);
        classifyBt.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        DateDialog datedialog = new DateDialog(MainActivity.this);

                        datedialog.setDialogListener(new DateDialog.CustomDialogListener() {
                            @Override
                            public void onPositiveClicked(String start, String end) {
                                startDate = start;
                                endDate = end;

                                NameDialog namedialog = new NameDialog(MainActivity.this);//이름받는 다이얼로그
                                namedialog.setDialogListener(new NameDialog.CustomDialogListener() {
                                    @Override
                                    public void onPositiveClicked(String name) {
                                        String classifyName = name;
                                        if(fileNameList.contains(name+".txt")){
                                            Toast.makeText(getApplicationContext(),"해당 이름은 사용할 수 없습니다.",Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            Intent intent = new Intent(MainActivity.this, SelectedPhoto.class);
                                            intent.putExtra("start", startDate);
                                            intent.putExtra("end",endDate);
                                            intent.putExtra("name", classifyName);
                                            intent.putExtra("access",accessKey);
                                            intent.putExtra("secret", secretKey);
                                            intent.putExtra("bucket", bucket);
                                            intent.putExtra("dirList", listOfAllImages);
                                            startActivityForResult(intent,1);
                                        }
                                    }
                                });

                                namedialog.show();
                            }
                        });
                        datedialog.show();
                    }
                }
        );
    }

    void showFileRemove(final int p)//목록에서 사진 제외하는 다이얼로그
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("목록 삭제");
        builder.setMessage("목록에서 제외하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(p == 0){
                            Toast.makeText(getApplicationContext(),"All은 삭제할 수 없습니다.",Toast.LENGTH_LONG).show();
                            return ;
                        }
                        Toast.makeText(getApplicationContext(),"삭제완료",Toast.LENGTH_LONG).show();
                        String deleteFileName = fileNameList.get(p);
                        File deleteFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified/"+deleteFileName);
                        deleteFile.delete();

                        fileNameList.remove(p);
                        classedThumbnail.remove(p);

                        tempadapter.notifyDataSetChanged();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    void getclassedThumbnail(ArrayList<String> fileNameList)//텍스트 파일에서 썸네일 읽어오는 함수
    {
        classedThumbnail.clear();
        for(String fileName: fileNameList) {
            String line = null; // 한줄씩 읽기
            File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified"); // 저장 경로
            // 폴더 생성
            if (!saveFile.exists()) { // 폴더 없을 경우
                saveFile.mkdir(); // 폴더 생성
            }
            try {
                BufferedReader buf = new BufferedReader(new FileReader(saveFile + "/" + fileName));
                while ((line = buf.readLine()) != null) {
                    if (line.startsWith("thumbnail")) {
                        String thumbnail = line.substring(line.indexOf(":") + 1);
                        classedThumbnail.add(thumbnail);
                    }
                }
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void getTxtFileList()//폴더 내의 txt파일들의 리스트를 받는다.
    {
        fileNameList.clear();
        File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified");
        if(!fileDir.exists()){ // 폴더 없을 경우
            fileDir.mkdir(); // 폴더 생성
        }

        File fileList[] = fileDir.listFiles();
        int itda = 0; //All이 있다면 1 없다면 0
        for (File f : fileList){ // All이라는 파일 이름이 없다면
            if (f.getName().equals("All.txt")){
                itda = 1;
                break;
            }
        }

        if (itda == 0){
            String str = "thumbnail:android.resource://com.example.kang.photoalbum/"+R.drawable.folder;
            // 파일 생성
            File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified"); // 저장 경로
            // 폴더 생성
            if(!saveFile.exists()){ // 폴더 없을 경우
                saveFile.mkdir(); // 폴더 생성
            }
            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/All.txt", true));
                buf.append(str); // 파일 쓰기
                buf.newLine(); // 개행
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileList = fileDir.listFiles();

        for (File f : fileList){
            fileNameList.add(f.getName());
        }
    }

    ArrayList<String> getAllImage()//이미지 dir목록 받기
    {
        //String[] filePath = {MediaStore.Images.Media.DATA};
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA};
        //String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor c = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null,null,null);
        ArrayList<String> result = new ArrayList<String>(c.getCount());

        int dataColumnIndex = c.getColumnIndex(projection[0]);
        int dataPathColumnIndex = c.getColumnIndex(projection[1]);

        if(c == null){
            //Toast.makeText(getApplicationContext(),"사진없음",1);
        } else if (c.moveToLast()){
            do{
                String filepath = c.getString(dataColumnIndex);
                String filepathdata = c.getString(dataPathColumnIndex);
                if(!result.contains(filepath)) {
                    result.add(filepath);
                    thumbnail.add(filepathdata);
                }
            }while(c.moveToPrevious());
        }
        c.close();
        return result;
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                getTxtFileList();
                getclassedThumbnail(fileNameList);
                tempadapter.notifyDataSetChanged();
            }
        }
    }
}


