package com.example.chaosbicycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.regions.Regions;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CALL_PHONE = 2;
//  private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a2ta04z8hhgodv-ats.iot.us-east-1.amazonaws.com";

    private List<Model__station> checkAlready;

    private MapView mapView;
    private Boolean isCurrentCheck = false;

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a1jpskevl8sr71-ats.iot.us-east-1.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-east-1:af5f05eb-7fbe-45ba-a2ce-04e5f8963b0a";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "sdsadas";

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";
    KeyStore clientKeyStore = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        String state = intent.getStringExtra("state");

        Log.i("chaos",state);

        mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(37.527122, 127.028717);
        mapView.setMapCenterPoint(mapPoint, true);
        //true면 앱 실행 시 애니메이션 효과가 나오고 false면 애니메이션이 나오지않음.
        mapViewContainer.addView(mapView);

        Log.i("chaos","getStation start");
        getStation(state);

        //도난 자전거 테이블 생성 함수 실행
        createBicycleTable();
    }

    //Lambda 사용해서 db연결
    public void getStation(String state) {
        //Retrofit 호출
        Call<List<Model__station>> call = RetrofitClient.getApiService().getStationData(state);
        call.enqueue(new Callback<List<Model__station>>() {
            @Override
            public void onResponse(Call<List<Model__station>> call, Response<List<Model__station>> response) {
                if(!response.isSuccessful()){
                    Log.e("연결이 비정상적 : ", "error code : " + response.code());
                    return;
                }
                checkAlready = response.body();
                Log.d("연결이 성공적 : ", response.body().toString());
                Log.d(" getStation yejin", String.valueOf(checkAlready));

                //지도 만들기 함수 실행
                createMap();
            }
            @Override
            public void onFailure(Call<List<Model__station>> call, Throwable t) {
                Log.e("연결실패", t.getMessage());
            }
        });
    }

    //지도 만들기 함수 : 보관소 + 도난자전거 마커 표시
    public void createMap(){
        int i = 0;
        Log.d("createMap yejin", String.valueOf(checkAlready));

        for(Model__station station : checkAlready){
            MapPOIItem StationMarker = new MapPOIItem();
            StationMarker.setItemName(station.getStationName());
            StationMarker.setTag(i);
            StationMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(station.getStationLatitude()),Double.parseDouble(station.getStationLongitude())));

            if(i<10){
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            }else{
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            }

            // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            StationMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            /* Custom Marker XML 설정 구문 */
            View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
            ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.bike); /* Maker Image 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.main_title)).setText(station.getStationName()); /* Maker Text 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.sub_title)).setText("현황 : "+station.getParkingBikeTotCnt()+"대 부족"); /* Maker 장소 변경 해주는 구문 */
            StationMarker.setCustomCalloutBalloon(mView);

            mapView.addPOIItem(StationMarker);
            i++;
        }

        //재배치 필요한 보관소 테이블 생성 함수 실행
        createStationTable();

    }

    //재배치 필요한 보관소 테이블 생성 함수
    public void createStationTable(){
        ListView listview = (ListView)findViewById(R.id.stationTable);

        //데이터를 저장하게 되는 리스트
        List<String> list = new ArrayList<>();

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);


        //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                //클릭한 아이템의 문자열을 가져옴
                String selected_item = (String)adapterView.getItemAtPosition(position);
                Log.i("yejin",selected_item);
            }
        });


        //리스트뷰에 보여질 아이템을 추가
        Iterator iterator = checkAlready.iterator();
        for (int i=0;i<10;i++){
            Model__station stationData = ((Model__station) iterator.next());
            list.add(stationData.getStationName()+"\n"+stationData.getParkingBikeTotCnt()+"개 부족");
        }

    }


    //도난 자전거 테이블 생성 함수 실행
    public void createBicycleTable(){
        //도난 자전거 표시
        MapPOIItem StolenBicycleMarker = new MapPOIItem();
        StolenBicycleMarker.setItemName("도난 자전거");
        StolenBicycleMarker.setTag(0);
        StolenBicycleMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(37.5614579596, 126.961949154));
        StolenBicycleMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
        StolenBicycleMarker.setCustomImageResourceId(R.drawable.bike); // 마커 이미지.
        StolenBicycleMarker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
        StolenBicycleMarker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

        /* Custom Marker XML 설정 구문 */
        View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
        ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.alert); /* Maker Image 변경 해주는 구문 */
        ((TextView) mView.findViewById(R.id.main_title)).setText("1시간 전 신고"); /* Maker Text 변경 해주는 구문 */
        ((TextView) mView.findViewById(R.id.sub_title)).setText(""); /* Maker 장소 변경 해주는 구문 */

        StolenBicycleMarker.setCustomCalloutBalloon(mView);
        mapView.addPOIItem(StolenBicycleMarker);


        ListView listview = (ListView)findViewById(R.id.bicycleTable);

        //데이터를 저장하게 되는 리스트
        List<String> list = new ArrayList<>();

        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);

        //리스트뷰의 어댑터를 지정해준다.
        listview.setAdapter(adapter);


        //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                //클릭한 아이템의 문자열을 가져옴
                String selected_item = (String)adapterView.getItemAtPosition(position);
                Log.i("yejin",selected_item);
                StolenBicycleMarker.setCustomImageResourceId(R.drawable.alert);
                mapView.addPOIItem(StolenBicycleMarker);

                //mqtt 통신해보자...
                //mqttConntect();
                publishLambda(selected_item);

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        StolenBicycleMarker.setCustomImageResourceId(R.drawable.bike);
                        mapView.addPOIItem(StolenBicycleMarker);
                    }
                }, 6000);// 6초 정도 딜레이를 준 후 시작
            }

            private void publishLambda(String selected_item) {
                Log.i("yejin","publishLambda start");
                //Retrofit 호출
                Call<String> call = RetrofitClient_alert.getApiService().sendAlert();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(!response.isSuccessful()){
                            Log.e("연결이 비정상적 : ", "error code : " + response.code());
                            return;
                        }
                        Log.d("연결이 성공적 : ", response.body().toString());
                        Log.d(" publishLambda yejin", response.body().toString());
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.e("연결실패", t.getMessage());
                    }
                });
            }


//            private void mqttConntect() {
//                // MQTT client IDs are required to be unique per AWS IoT account.
//                // This UUID is "practically unique" but does not _guarantee_
//                // uniqueness.
//                String clientId = UUID.randomUUID().toString();
//
//                // Initialize the AWS Cognito credentials provider
//                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                        getApplicationContext(), // context
//                        COGNITO_POOL_ID, // Identity Pool ID
//                        MY_REGION // Region
//                );
//
//                Region region = Region.getRegion(MY_REGION);
//
//                // MQTT Client
//                AWSIotMqttManager mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);
//
//                // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
//                // MQTT pings every 10 seconds.
//                mqttManager.setKeepAlive(10);
//
//                // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
//                // AWS IoT will publish this message to alert other clients.
//                AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
//                        "Android client lost connection", AWSIotMqttQos.QOS0);
//                mqttManager.setMqttLastWillAndTestament(lwt);
//
//                // IoT Client (for creation of certificate if needed)
//                AWSIotClient mIotAndroidClient = new AWSIotClient(credentialsProvider);
//                mIotAndroidClient.setRegion(region);
//
//                String keystorePath = getFilesDir().getPath();
//
//                // To load cert/key from keystore on filesystem
//                try {
//                    if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, KEYSTORE_NAME)) {
//                        if (AWSIotKeystoreHelper.keystoreContainsAlias(CERTIFICATE_ID, keystorePath,
//                                KEYSTORE_NAME, KEYSTORE_PASSWORD)) {
//                            Log.i(LOG_TAG, "Certificate " + CERTIFICATE_ID
//                                    + " found in keystore - using for MQTT."+keystorePath);
//                            // load keystore from file into memory to pass on connection
//                            clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
//                                    keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);
//                        } else {
//                            Log.i(LOG_TAG, "Key/cert " + CERTIFICATE_ID + " not found in keystore.");
//                        }
//                    } else {
//                        Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + KEYSTORE_NAME + " not found.");
//                    }
//                } catch (Exception e) {
//                    Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
//                }
//
//                if (clientKeyStore == null) {
//                    Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");
//
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                // Create a new private key and certificate. This call
//                                // creates both on the server and returns them to the
//                                // device.
//                                CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
//                                        new CreateKeysAndCertificateRequest();
//                                createKeysAndCertificateRequest.setSetAsActive(true);
//                                final CreateKeysAndCertificateResult createKeysAndCertificateResult;
//                                createKeysAndCertificateResult =
//                                        mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
//                                Log.i(LOG_TAG,
//                                        "Cert ID: " +
//                                                createKeysAndCertificateResult.getCertificateId() +
//                                                " created.");
//
//                                // store in keystore for use in MQTT client
//                                // saved as alias "default" so a new certificate isn't
//                                // generated each run of this application
//                                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(CERTIFICATE_ID,
//                                        createKeysAndCertificateResult.getCertificatePem(),
//                                        createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
//                                        keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);
//
//                                // load keystore from file into memory to pass on
//                                // connection
//                                clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
//                                        keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);
//
//                                // Attach a policy to the newly created certificate.
//                                // This flow assumes the policy was already created in
//                                // AWS IoT and we are now just attaching it to the
//                                // certificate.
//                                AttachPrincipalPolicyRequest policyAttachRequest =
//                                        new AttachPrincipalPolicyRequest();
//                                policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
//                                policyAttachRequest.setPrincipal(createKeysAndCertificateResult
//                                        .getCertificateArn());
//                                mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);
//
//                                //connect
//                                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
//                                    @Override
//                                    public void onStatusChanged(final AWSIotMqttClientStatus status,
//                                                                final Throwable throwable) {
//                                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));
//
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if (status == AWSIotMqttClientStatus.Connecting) {
//                                                    Log.i("yejin","Connecting...");
//
//                                                } else if (status == AWSIotMqttClientStatus.Connected) {
//                                                    Log.i("yejin","Connected");
//                                                    //subscribe
//                                                    mqttManager.subscribeToTopic("test", AWSIotMqttQos.QOS0,
//                                                            new AWSIotMqttNewMessageCallback() {
//                                                                @Override
//                                                                public void onMessageArrived(final String topic, final byte[] data) {
//                                                                    runOnUiThread(new Runnable() {
//                                                                        @Override
//                                                                        public void run() {
//                                                                            try {
//                                                                                String message = new String(data, "UTF-8");
//                                                                                Log.d(LOG_TAG, "Message arrived:");
//                                                                                Log.d(LOG_TAG, "   Topic: " + topic);
//                                                                                Log.d(LOG_TAG, " Message: " + message);
//
//                                                                            } catch (UnsupportedEncodingException e) {
//                                                                                Log.e(LOG_TAG, "Message encoding error.", e);
//                                                                            }
//                                                                        }
//                                                                    });
//                                                                }
//                                                            });
//                                                    //publish
//                                                    mqttManager.publishString("test자전거 send!", "test", AWSIotMqttQos.QOS0);
//
//                                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
//                                                    if (throwable != null) {
//                                                        Log.e(LOG_TAG, "Connection error.", throwable);
//                                                    }
//                                                    Log.i("yejin","Reconnecting");
//                                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
//                                                    if (throwable != null) {
//                                                        Log.e(LOG_TAG, "Connection error.", throwable);
//                                                    }
//                                                    Log.i("yejin","Disconnected");
//                                                } else {
//                                                    Log.i("yejin","Disconnected");
//
//                                                }
//                                            }
//                                        });
//                                    }
//                                });
//
//                            } catch (Exception e) {
//                                Log.e(LOG_TAG,
//                                        "Exception occurred when generating new private key and certificate.",
//                                        e);
//                            }
//                        }
//                    }).start();
//                }
//            }
        });


        //리스트뷰에 보여질 아이템을 추가
        list.add("test자전거");

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
}