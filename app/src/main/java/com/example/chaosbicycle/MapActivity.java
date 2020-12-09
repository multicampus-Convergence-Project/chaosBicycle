package com.example.chaosbicycle;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MapActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CALL_PHONE = 2;
//    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2ta04z8hhgodv-ats.iot.us-east-1.amazonaws.com";


    private MapView mapView;
    private Boolean isCurrentCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        int state = intent.getIntExtra("state",0);

        if(R.id.jongno == state){
            Log.i("chaos","jongno");
            state = 1;
        }else if(R.id.gangseo == state){
            Log.i("chaos","gangseo");
            state = 2;
        }else if(R.id.guro == state){
            Log.i("chaos","guro");
            state = 3;
        }else if(R.id.gwanak == state){
            Log.i("chaos","gwanak");
            state = 4;
        }else if(R.id.geumcheon == state){
            Log.i("chaos","geumcheon");
            state = 5;
        }else if(R.id.yeongdeungpo == state){
            Log.i("chaos","yeongdeungpo");
            state = 6;
        }else if(R.id.yangcheon == state){
            Log.i("chaos","yangcheon");
            state = 7;
        }else if(R.id.songpa == state){
            Log.i("chaos","songpa");
            state = 8;
        }else if(R.id.gangnam == state){
            Log.i("chaos","gangnam");
            state = 9;
        }else if(R.id.seocho == state){
            Log.i("chaos","seocho");
            state = 0;
        }else if(R.id.dongjak == state){
            Log.i("chaos","dongjak");
            state = 1;
        }else if(R.id.gangdong == state){
            Log.i("chaos","gangdong");
            state = 1;
        }else if(R.id.dobong == state){
            Log.i("chaos","dobong");
            state = 1;
        }else if(R.id.seongbuk == state){
            Log.i("chaos","seongbuk");
            state = 1;
        }else if(R.id.jungnang == state){
            Log.i("chaos","jungnang");
            state = 1;
        }else if(R.id.eunpyeong == state){
            Log.i("chaos","eunpyeong");
            state = 1;
        }else if(R.id.jung == state){
            Log.i("chaos","jung");
            state = 1;
        }else if(R.id.seongdong == state){
            Log.i("chaos","seongdong");
            state = 1;
        }else if(R.id.gwangjin == state){
            Log.i("chaos","gwangjin");
            state = 1;
        }else if(R.id.yongsan == state){
            Log.i("chaos","yongsan");
            state = 1;
        }else if(R.id.dongdaemun == state){
            Log.i("chaos","dongdaemun");
            state = 1;
        }else if(R.id.seodaemun == state){
            Log.i("chaos","seodaemun");
            state = 1;
        }else if(R.id.mapo == state){
            Log.i("chaos","mapo");
            state = 1;
        }else if(R.id.gangbuk == state){
            Log.i("chaos","gangbuk");
            state = 1;
        }else if(R.id.nowon == state){
            Log.i("chaos","nowon");
            state = 1;
        }


        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(37.527122, 127.028717);
        mapView.setMapCenterPoint(mapPoint, true);
        //true면 앱 실행 시 애니메이션 효과가 나오고 false면 애니메이션이 나오지않음.
        mapViewContainer.addView(mapView);


        try {
            // DB에서 보관소 위치 정보 받아오기
            String stationResult = new StationAsyncTask(state).execute().get();
            Log.i("chaos",stationResult);

            //지도 만들기 함수 실행
            createMap(stationResult);

            //재배치 필요한 보관소 테이블 생성 함수 실행
            createStationTable(stationResult);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //도난 자전거 테이블 생성 함수 실행
        createBicycleTable();
    }

    //지도 만들기 함수 : 보관소 + 도난자전거 마커 표시
    public void createMap(String stationResult){
        String[] data = stationResult.split("]");
        //Log.i("chaos",stationResult+"\n"+data);

        MapPOIItem[] StationMarker = new MapPOIItem[data.length];

        // 보관소 위치 지도에 마커로 표시
        for (int i=0;i<StationMarker.length;i++){
            String[] stationData = data[i].split(",");
            //Log.i("chaos", i+": "+stationData[0]);
            StationMarker[i] = new MapPOIItem();
            StationMarker[i].setItemName(stationData[3]);
            StationMarker[i].setTag(i);
            StationMarker[i].setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(stationData[1]),Double.parseDouble(stationData[2])));

            if(i<10){
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker[i].setMarkerType(MapPOIItem.MarkerType.YellowPin);
            }else{
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker[i].setMarkerType(MapPOIItem.MarkerType.BluePin);
            }

            // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            StationMarker[i].setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            /* Custom Marker XML 설정 구문 */
            View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
            ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.bike); /* Maker Image 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.main_title)).setText(stationData[4]+":"+stationData[3]); /* Maker Text 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.sub_title)).setText("현황 : "+stationData[5]+"대 부족"); /* Maker 장소 변경 해주는 구문 */
            StationMarker[i].setCustomCalloutBalloon(mView);

        }
        mapView.addPOIItems(StationMarker);


        // 도난자전거 위치 정보 받아오기 ==> >>>>테스트로??<<<<
        //String StolenBicyleResult = new StationAsyncTask().execute().get();

        //custom marker 표시 ==> 도난 자전거 표시에 사용할 예정
        MapPOIItem StolenBicycleMarker = new MapPOIItem();
        StolenBicycleMarker.setItemName("도난 자전거");
        StolenBicycleMarker.setTag(0);
        StolenBicycleMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.5614579595, 126.961949155));
        StolenBicycleMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
        StolenBicycleMarker.setCustomImageResourceId(R.drawable.bike); // 마커 이미지.
        StolenBicycleMarker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
        StolenBicycleMarker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

        /* Custom Marker XML 설정 구문 */
        View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
        ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.alert); /* Maker Image 변경 해주는 구문 */
        ((TextView) mView.findViewById(R.id.main_title)).setText("이미지 클릭 시 부저 울림"); /* Maker Text 변경 해주는 구문 */
        ((TextView) mView.findViewById(R.id.sub_title)).setText("1시간 전 신고"); /* Maker 장소 변경 해주는 구문 */

        StolenBicycleMarker.setCustomCalloutBalloon(mView);
        mapView.addPOIItem(StolenBicycleMarker);
    }

    //재배치 필요한 보관소 테이블 생성 함수
    public void createStationTable(String stationResult){
        TableLayout stationLayout = (TableLayout) findViewById(R.id.stationTable);

        String[] data = stationResult.split("]");
        //Log.i("chaos",StationResult+"\n"+data);

        // 보관소 위치 지도에 마커로 표시
        for (int i=0;i<10;i++){
            TableRow stationRow = new TableRow(this);
            stationRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

            String[] stationData = data[i].split(",");

            //보관소 명 입력
            TextView textview = new TextView(this);
            textview.setText(stationData[3]);
            textview.setGravity(Gravity.CENTER);
            stationRow.addView(textview);

            //현황 입력
            TextView textview2 = new TextView(this);
            textview2.setText(stationData[5]+"개 부족");
            textview2.setGravity(Gravity.CENTER);
            stationRow.addView(textview2);

            //table row 추가
            stationLayout.addView(stationRow);
            stationRow.removeView(stationRow);
        }

    }

    //도난 자전거 테이블 생성 함수 실행
    public void createBicycleTable(){
        TableLayout stationLayout = (TableLayout) findViewById(R.id.bicycleTable);
        TableRow stationRow = new TableRow(this);
        stationRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));

        //보관소 명 입력
        TextView textview = new TextView(this);
        textview.setText("test 자전거");
        textview.setGravity(Gravity.CENTER);
        stationRow.addView(textview);

        //현황 입력
        TextView textview2 = new TextView(this);
        textview2.setText("위치 모름");
        textview2.setGravity(Gravity.CENTER);
        stationRow.addView(textview2);
        stationRow.setId(Integer.parseInt("123"));

        //table row 추가
        stationLayout.addView(stationRow);
    }



    //현재 위치로 지도 이동 함수
    public void goToCurrentLocation(View view) {
        if(isCurrentCheck){
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
            mapView.setShowCurrentLocationMarker(false);
            ((TextView)view.findViewById(R.id.checkCurrentLocation)).setText("현재위치로 이동");
            isCurrentCheck = false;
        }else{
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
            ((TextView)view.findViewById(R.id.checkCurrentLocation)).setText("현재위치 취소");
            isCurrentCheck = true;
        }

    }

    //DB에서 보관소 정보 가져오기 함수
    protected class StationAsyncTask extends AsyncTask<Object[],Object[],String> {

        private int state;

        public StationAsyncTask(int state) {
            this.state = state;
        }

        @Override
        protected String doInBackground(Object[]... Objects) {
            StringBuffer sb = new StringBuffer();
            Connection con = null;
            Log.i("chaos", String.valueOf(state));

            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                con = DriverManager.getConnection("jdbc:mysql://db-bigdata-bicycle.cynn0zdjjttk.us-east-1.rds.amazonaws.com:3306", "admin", "chaosproject");
                Statement st = null;
                ResultSet rs = null;
                Log.i("chaos","db connection complete");

                st = con.createStatement();
                st.execute("use chaos");

                if (st.execute("select id,stationLatitude,stationLongitude,stationName,stationId,parkingBikeTotCnt\n" +
                        "from localInformation,everyOneHourAPI\n" +
                        "where id = local_id and id like '%"+this.state+"' order by parkingBikeTotCnt DESC")) {
                    rs = st.getResultSet();
                }

                while (rs.next()) {
                    ArrayList<String> stationList = new ArrayList<String>();
                    stationList.add(rs.getString("id"));
                    stationList.add(rs.getString("stationLatitude"));
                    stationList.add(rs.getString("stationLongitude"));
                    stationList.add(rs.getString("stationName"));
                    stationList.add(rs.getString("stationId"));
                    stationList.add(rs.getString("parkingBikeTotCnt"));
                    //Log.i("chaos",stationLatitude+" , "+stationLongitude+"");

                    sb.append(stationList);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if(con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            String result = sb.toString();
            return result;
        }

    }

}