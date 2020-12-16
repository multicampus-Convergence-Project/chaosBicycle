package com.example.chaosbicycle;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    HashMap<String, String> stateMap = new HashMap<String,String>() {
        {
            put("종로구","jongno");put("강서구","gangseo");put("구로구","guro");
            put("관악구","gwanak");put("금천구","geumcheon");put("영등포구","yeongdeungpo");
            put("양천구","yangcheon");put("송파구","songpa");put("강남구","gangnam");
            put("서초구","seocho");put("동작구","dongjak");put("강동구","gangdong");
            put("도봉구","dobong");put("성북구","seongbuk");put("중랑구","jungnang");
            put("은평구","eunpyeong");put("중구","jung");put("성동구","seongdong");
            put("광진구","gwangjin");put("용산구","yongsan");put("동대문구","dongdaemun");
            put("서대문구","seodaemun");put("마포구","mapo");put("강북구","gangbuk");put("노원구","nowon");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getHashKey();
        getConnectionMqtt();
    }

    public void goToMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        TextView tv = (TextView)view;
        String id = stateMap.get(tv.getText().toString());
        Log.i("chaos", id);
        intent.putExtra("state", id);
        startActivity(intent);
    }

//    public void goToMqttTest(View view) {
//        Intent intent = new Intent(getApplicationContext(), com.example.chaosbicycle.PubSubActivity.class);
//        startActivity(intent);
//    }

    private void getConnectionMqtt(){

    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }
}