package com.example.kang.photoalbum;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Loading extends AppCompatActivity {

    String access;
    String secrete;
    String bucket;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent intent = getIntent();
        access = intent.getStringExtra("access");
        secrete = intent.getStringExtra("secret");
        bucket = intent.getStringExtra("bucket");

        progressbar = findViewById(R.id.loading);

        MyAsyncTask task = new MyAsyncTask();
        task.execute(bucket, access, secrete);
    }

    class MyAsyncTask extends AsyncTask<String, Integer, String> {
        AmazonS3 s3Client;
        TransferUtility transferUtility;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            s3Client = new AmazonS3Client(new BasicSessionCredentials(access, secrete,""));
            s3Client.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
            transferUtility = new TransferUtility(s3Client, getApplicationContext());
        }

        @Override
        protected String doInBackground(String... bucket) {
            ObjectListing listing = new ObjectListing();
            String key = "";

            while(true) {
                listing = s3Client.listObjects(bucket[0]);
                List<S3ObjectSummary> bucketList  = listing.getObjectSummaries();

                for (S3ObjectSummary object:bucketList){
                    key = object.getKey();
                    if (key.equals("comment.txt")){
                        return key;
                    }
                }
                if (key == "comment.txt"){
                    break;
                }
            }
            return key;
        }
        @Override
        protected  void onProgressUpdate(Integer... params){
        }
        @Override
        protected void onPostExecute(String result) {

            File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/classified/context.txt");

            TransferObserver transferObserver = transferUtility.download(
                    bucket,
                    result,
                    saveFile
            );

            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
            //deleteTask deletetask = new deleteTask();
            //deletetask.execute(bucket, access, secrete);
        }
    }
}
