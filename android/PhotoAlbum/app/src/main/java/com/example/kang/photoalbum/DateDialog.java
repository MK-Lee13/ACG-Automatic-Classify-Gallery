package com.example.kang.photoalbum;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3;

public class DateDialog extends Dialog implements  View.OnClickListener{
    DatePicker startdate;
    DatePicker enddate;
    Button okbt;
    Context context;
    CustomDialogListener customDialogListener;

    public DateDialog(final Context context){
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_date_dialog);
    }

    public String getDate(DatePicker picker){
        String result;
        int month = picker.getMonth();
        int year = picker.getYear();
        int day = picker.getDayOfMonth();

        String monthString = Integer.toString(month+1);
        String dayString = Integer.toString(day);

        if (monthString.length() == 1){
            monthString = "0" + monthString;
        }
        if (dayString.length() == 1){
            dayString = "0" + dayString;
        }


        result = Integer.toString(year) +":"+ monthString+ ":"+dayString;
        return result;
    }

    //인터페이스 설정
    interface CustomDialogListener{
        void onPositiveClicked(String start, String end);
    }

    //호출할 리스너 초기화
    public void setDialogListener(CustomDialogListener customDialogListener){
        this.customDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_date_dialog);

        startdate = findViewById(R.id.startDate);
        enddate = findViewById(R.id.endDate);
        okbt = findViewById(R.id.okButton);

        //버튼 클릭 리스너 등록
        okbt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.okButton: //확인 버튼을 눌렀을 때
                //각각의 변수에 EidtText에서 가져온 값을 저장
                String startdateString = getDate(startdate);
                String enddateStirng = getDate(enddate)+" 25";

                //인터페이스의 함수를 호출하여 변수에 저장된 값들을 Activity로 전달
                customDialogListener.onPositiveClicked(startdateString, enddateStirng);
                dismiss();
                break;
        }
    }
}
