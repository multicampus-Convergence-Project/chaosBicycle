package com.example.chaosbicycle;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class CallRetrofit {
    public void callPhoneAlreadyCheck(String state){

        //Retrofit 호출
        Model__station modelCheckAlready = new Model__station();
        Call<List<Model__station>> call = RetrofitClient.getApiService().getStationData(state);
        call.enqueue(new Callback<List<Model__station>>() {
            @Override
            public void onResponse(Call<List<Model__station>> call, Response<List<Model__station>> response) {
                if(!response.isSuccessful()){
                    Log.e("연결이 비정상적 : ", "error code : " + response.code());
                    return;
                }
                List<Model__station> checkAlready = response.body();
                Log.d("연결이 성공적 : ", response.body().toString());
//                if(modelCheckAlready.getMessage() == "can use this number"){
//                    Log.d("중복검사: ", "중복된 번호가 아닙니다");
//                    modelCheckAlready.setRight(true);
//                }
            }
            @Override
            public void onFailure(Call<List<Model__station>> call, Throwable t) {
                Log.e("연결실패", t.getMessage());
            }
        });
    }
}
