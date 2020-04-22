package com.example.kang.photoalbum;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

public class SelectedPhoto extends AppCompatActivity {
    ArrayList<String> dateList;
    GridView selectedList;
    ArrayList<String> filename;
    AmazonS3 s3Client;
    TransferUtility transferUtility;
    ArrayList<String> showfileList;
    ArrayList<String> showfilename;
    GridAdapter listadapter;
    String name;
    String access;
    String secret;
    String bucket;
    String start;
    String end;
    ArrayAdapter dirAdapter;
    ListView dirListView;
    ArrayList<String> dirList;
    ArrayList<String> Classifiedthumbnail = new ArrayList<String>(Arrays.asList("None", "None","None","None", "None", "None","None"));
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_photo);

        Intent intent = getIntent();
        start = intent.getStringExtra("start");
        end = intent.getStringExtra("end");
        access = intent.getStringExtra("access");
        secret = intent.getStringExtra("secret");
        bucket = intent.getStringExtra("bucket");
        name  = intent.getStringExtra("name");
        dirList= intent.getStringArrayListExtra("dirList");

        s3Client = new AmazonS3Client(new BasicSessionCredentials(access, secret,""));
        s3Client.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        transferUtility = new TransferUtility(s3Client, getApplicationContext());

        showfileList = new ArrayList<String>();//경로
        showfilename = new ArrayList<String>();//이름

        //디렉토리 체크박스
        dirListView = findViewById(R.id.dirList);
        dirAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, dirList);
        dirListView.setAdapter(dirAdapter);
        int count = dirAdapter.getCount();
        for (int i=0; i<count; i++) {
            dirListView.setItemChecked(i, true);
        }
        dirListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setFileList();
                listadapter.notifyDataSetChanged();
            }
        });

//사진 시기 정하는 곳-----------------------------------------------------

        setFileList();

        selectedList = findViewById(R.id.selected);
        listadapter = new GridAdapter(this, showfilename, showfileList, 1);
        selectedList.setAdapter(listadapter);

        selectedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent photoIntent = new Intent(SelectedPhoto.this, Photo.class);
                String photoPath = showfileList.get(position);
                photoIntent.putExtra("photo",photoPath);
                startActivity(photoIntent);
            }
        });

        selectedList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showFileRemove(position);
                return true;
            }
        });

        i = 0;
        Button uploadbt = findViewById(R.id.uploadBt);
        uploadbt.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v) {
                        if (showfileList.isEmpty()) { // 사진 없는 경우
                            Toast.makeText(getApplicationContext(), "파일이 없습니다.", Toast.LENGTH_LONG).show();
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                        for (String path : showfileList) {
                            File uploadToS3 = new File(path);

                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int oriet = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 4;
                            Bitmap orgImage = BitmapFactory.decodeFile(path, options);
                            orgImage = rotateBitmap(orgImage, oriet);

                            int target = path.lastIndexOf(".");
                            String format = path;
                            format = format.substring(target);

                            String tempName = "temp" + Integer.toString(i);
                            i += 1;

                            String btofPath = bitmapToFile(getApplicationContext(), orgImage, tempName, format);
                            File file = new File(btofPath);

                            String name = path.replace('/', '~');
                            TransferObserver transferObserver = transferUtility.upload(
                                    bucket,          /* The bucket to upload to */
                                    name,           /* The key for the uploaded object */
                                    file       /* The file where the data to upload exists */
                            );
                        }
                        //올리기 완료
                        //for trigger---------------------------------------------------------------
                        if (!showfileList.isEmpty()) {
                            //File trigger = new File(showfileList.get(0));
                            File trigger = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified/All.txt"); // 저장 경로
                            TransferObserver transferObserver = transferUtility.upload(
                                    bucket,          /* The bucket to upload to */
                                    "a.txt",/* The key for the uploaded object */
                                    trigger       /* The file where the data to upload exists */
                            );
                            creatTextFile(name, start, end, showfileList.get(0));//test파일 생성(구별한 카테고리 들어가있음)
//==================로딩화면으로
                            Intent intent = new Intent(SelectedPhoto.this, Loading.class);
                            intent.putExtra("bucket", bucket);
                            intent.putExtra("access", access);
                            intent.putExtra("secret", secret);
                            startActivityForResult(intent,2);
                        }
                    }
                }
        );
    }

    void setFileList()//사진 목록 구성
    {
        filename = new ArrayList<String>();
        ArrayList<String> dataList = getAllImage();
        dateList = new ArrayList<String>();

        for (int i = 0; i < dataList.size(); i++){
            String path = dataList.get(i);
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

        showfileList.clear();//경로
        showfilename.clear();//이름

        //사진 시기별 구분
        for(int i =0; i<dataList.size(); i++){
            if(dateList.get(i).compareTo(start) > 0 && dateList.get(i).compareTo(end) < 0){
                showfileList.add(dataList.get(i));
                showfilename.add(filename.get(i));
            }
        }

        Collections.reverse(showfileList);
        Collections.reverse(showfilename);
    }

    void showFileRemove(final int p)//목록에서 사진 제외하는 다이얼로그
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 삭제");
        builder.setMessage("목록에서 사진을 제외하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
                        showfileList.remove(p);
                        showfilename.remove(p);
                        listadapter.notifyDataSetChanged();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    void creatTextFile(String name, String start, String end, String thumbnailPath)
    {
        String str = "start:"+start+"\nend:"+end+"\nthumbnail:"+thumbnailPath;
        // 파일 생성
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/"+name+".txt", true));
            buf.append(str); // 파일 쓰기
            buf.newLine(); // 개행
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<String> getAllImage()
    {
        ArrayList<String> checkedDirList = new ArrayList<>();
        int count = dirAdapter.getCount();
        for(int i = 0; i < count; i++){
            if(dirListView.isItemChecked(i)){
                checkedDirList.add(dirList.get(i));
            }
        }

        //String[] projection = {MediaStore.Images.Media.DATA};
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        //String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor c = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null,null,null);

        ArrayList<String> result = new ArrayList<String>(c.getCount());

        int dataColumnIndex = c.getColumnIndex(projection[0]);
        int nameColumnIndex = c.getColumnIndex(projection[1]);
        int dirColumnIndex = c.getColumnIndex(projection[2]);

        if(c == null){
            //Toast.makeText(getApplicationContext(),"사진없음",1);
        } else if (c.moveToFirst()){
            do{
                if (checkedDirList.contains(c.getString(dirColumnIndex))) {
                    filename.add(c.getString(nameColumnIndex));
                    result.add(c.getString(dataColumnIndex));
                }
            }while(c.moveToNext());
        }
        c.close();
        return result;
    }

    public String bitmapToFile(Context context, Bitmap bitmap, String name, String form)
    {
        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로

        String fileName = name + form;  // 파일이름은 마음대로!

        File tempFile = new File(storage,fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고

            FileOutputStream out = new FileOutputStream(tempFile);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌

            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int ore){
        Matrix matrix = new Matrix();
        switch(ore){
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1,1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1,1);
                break;

            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1,1);
                break;

            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1,1);
                break;

            default:
                return bitmap;
        }
        try{
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch(OutOfMemoryError e){
            e.printStackTrace();
            return null;
        }
    }

    void getUserComment(File file){
        String line = null; // 한줄씩 읽기
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            line=buf.readLine();
            String[] photoAndcommets = line.split(">");//형식: 파일이름<Temp,fdf,dfdf
            for(String temp : photoAndcommets){
                String[] photoCommet = temp.split("<");//0번은 파일 이름 1번은 comment
                String filename = photoCommet[0];
                String comment = photoCommet[1];
                filename = filename.replace('~', '/');
                if (!filename.endsWith("txt")){
                    String UserComment = "ACG: "+comment;
                    setUserComment(filename, UserComment);
                }
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setUserComment(String filePath, String comment){
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //추가
        String old =exif.getAttribute(ExifInterface.TAG_USER_COMMENT);
        if(old == null){
            old = "";
        }
        exif.setAttribute("UserComment", old + " <"+name+">"+comment);

        if(comment.contains("group")){
            Classifiedthumbnail.set(0,filePath);
        }
        if(comment.contains("self")){
            Classifiedthumbnail.set(1,filePath);
        }
        if(comment.contains("line")){
            Classifiedthumbnail.set(2,filePath);
        }
        if(comment.contains("land")){
            Classifiedthumbnail.set(3,filePath);
        }
        if(comment.contains("food")){
            Classifiedthumbnail.set(4,filePath);
        }
        if(comment.contains("rest")){
            Classifiedthumbnail.set(5,filePath);
        }
        if(comment.contains("pbig")){
            Classifiedthumbnail.set(6,filePath);
        }

        try {
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setThumbnail(String name)
    {
        String str = "group:"+Classifiedthumbnail.get(0) + "\nself:"+Classifiedthumbnail.get(1) + "\nline:"+Classifiedthumbnail.get(2)+
                "\nland:"+Classifiedthumbnail.get(3) + "\nfood:"+Classifiedthumbnail.get(4) + "\nrest:"+Classifiedthumbnail.get(5)
                + "\npbig:"+Classifiedthumbnail.get(6);
        // 파일 생성
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/"+name+".txt", true));
            buf.append(str); // 파일 쓰기
            buf.newLine(); // 개행
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                deleteTask deletetask = new deleteTask();
                deletetask.execute(bucket, access, secret);
            }
        }
    }

    class deleteTask extends AsyncTask<String, Integer, String> {
        AmazonS3 s3Client;
        TransferUtility transferUtility;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            s3Client = new AmazonS3Client(new BasicSessionCredentials(access, secret,""));
            s3Client.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
            transferUtility = new TransferUtility(s3Client, getApplicationContext());
        }

        @Override
        protected String doInBackground(String... bucket) {
            ObjectListing listing = new ObjectListing();
            String key = "";

            s3Client.deleteObject(bucket[0], "comment.txt");

            return key;
        }
        @Override
        protected  void onProgressUpdate(Integer... params){
        }
        @Override
        protected void onPostExecute(String result) {

            File commentFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified/context.txt");
            getUserComment(commentFile);
            commentFile.delete();

            setThumbnail(name);

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
