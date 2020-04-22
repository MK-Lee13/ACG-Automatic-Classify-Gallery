package com.example.kang.photoalbum;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NameDialog extends Dialog implements  View.OnClickListener {
    Context context;
    Button okbt;
    EditText nameTextView;
    CustomDialogListener customDialogListener;

    public NameDialog(final Context context){
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.name_dialog);
    }

    //인터페이스 설정
    interface CustomDialogListener{
        void onPositiveClicked(String name);
    }

    //호출할 리스너 초기화
    public void setDialogListener(NameDialog.CustomDialogListener customDialogListener){
        this.customDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.name_dialog);

        nameTextView = findViewById(R.id.Name);
        okbt = findViewById(R.id.okButton);

        //버튼 클릭 리스너 등록
        okbt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.okButton: //확인 버튼을 눌렀을 때
                //각각의 변수에 EidtText에서 가져온 값을 저장
                String name = String.valueOf(nameTextView.getText());

                //인터페이스의 함수를 호출하여 변수에 저장된 값들을 Activity로 전달
                customDialogListener.onPositiveClicked(name);
                dismiss();
                break;
        }
    }
}
