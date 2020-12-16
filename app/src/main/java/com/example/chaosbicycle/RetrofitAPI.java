package com.example.chaosbicycle;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RetrofitAPI {

    @GET("{state_name}")
    Call<List<Model__station>>  getStationData(@Path("state_name") String state); //이건 바디 요청시 사용하는거

    @GET("sendalert")
    Call<String> sendAlert();
}