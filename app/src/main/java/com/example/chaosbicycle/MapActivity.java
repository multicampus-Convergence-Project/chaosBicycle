package com.example.chaosbicycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CALL_PHONE = 2;

    private List<Model__station> checkAlready;

    //도난 자전거 데이터를 저장하게 되는 리스트
    List<String> stealList = new ArrayList<>();
    //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
    ArrayAdapter<String> adapter;
    List<MapPOIItem> StolenBicycleMarkerList = new ArrayList<>();

    private MapView mapView;
    private Boolean isCurrentCheck = false;

    static final String LOG_TAG = PubSubMqtt.class.getCanonicalName();

    // --- Constants to modify per your configuration ---

    // IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a1jpskevl8sr71-ats.iot.us-east-1.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-east-1:af5f05eb-7fbe-45ba-a2ce-04e5f8963b0a";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "sdsadas";
    // TOPIC
    private static final String TOPIC = "test/topic";
    AWSIotMqttManager mqttManager;

    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";

    KeyStore clientKeyStore = null;
    String clientId;
    TextView tvLastMessage;
    TextView tvStatus;

    Button btnConnect;

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

        Log.i("yejin","getConnection start");
        getConnection();
    }

    private void getConnection() {
        tvLastMessage = (TextView) findViewById(R.id.tvLastMessage);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(connectClick);
        btnConnect.setEnabled(false);

        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        String clientId = UUID.randomUUID().toString();
//        tvClientId.setText(clientId);

        // Initialize the AWS Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        AWSIotClient mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        String keystorePath = getFilesDir().getPath();

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, KEYSTORE_NAME)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(CERTIFICATE_ID, keystorePath,
                        KEYSTORE_NAME, KEYSTORE_PASSWORD)) {
                    Log.i(LOG_TAG, "Certificate " + CERTIFICATE_ID
                            + " found in keystore - using for MQTT."+keystorePath);
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
                            keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);
                    btnConnect.setEnabled(true);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + CERTIFICATE_ID + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + KEYSTORE_NAME + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(CERTIFICATE_ID,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
                                keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }
    }

    View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(LOG_TAG, "clientId = " + clientId);

            try {
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status,
                                                final Throwable throwable) {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == AWSIotMqttClientStatus.Connecting) {
                                    tvStatus.setText("Connecting...");

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    tvStatus.setText("Connected");
                                    subscribe();

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Reconnecting");
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Disconnected");
                                } else {
                                    tvStatus.setText("Disconnected");

                                }
                            }
                        });
                    }
                });


            } catch (final Exception e) {
                Log.e(LOG_TAG, "Connection error.", e);
                tvStatus.setText("Error! " + e.getMessage());
            }
        }

        private void subscribe() {
            mqttManager.subscribeToTopic(TOPIC, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        Log.d(LOG_TAG, "Message arrived:");
                                        Log.d(LOG_TAG, "   Topic: " + topic);
                                        Log.d(LOG_TAG, " Message: " + message);

                                        tvLastMessage.setText(message);
                                        JSONObject jObject = new JSONObject(message);
                                        String lat = jObject.getString("lat");
                                        String log = jObject.getString("log");
                                        String shock = jObject.getString("shock");

                                        ListView listview = (ListView)findViewById(R.id.bicycleTable);

                                        createBicycleTable(listview,stealList,lat,log);

                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        }
    };

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String msg = "{\"alert\":\"True\"}";

            try {
                mqttManager.publishString(msg, "test/test", AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };

    //Lambda 사용해서 db연결
    public void getStation(String state) {
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
                checkAlready = response.body();
                Log.d("연결이 성공적 : ", response.body().toString());
                createMap();
            }
            @Override
            public void onFailure(Call<List<Model__station>> call, Throwable t) {
                Log.e("연결실패", t.getMessage());
            }
        });
    }

    //지도 만들기 함수 : 보관소 마커 표시
    public void createMap(){
        int i = 0;
        Log.d("createMap yejin", String.valueOf(checkAlready));

        for(Model__station station : checkAlready){
            MapPOIItem StationMarker = new MapPOIItem();
            StationMarker.setItemName(station.getStationName());
            StationMarker.setTag(i);
            StationMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(station.getStationLatitude()),Double.parseDouble(station.getStationLongitude())));

            if(station.getPredict() > station.getRackTotCnt()){
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            }else if(station.getPredict() > station.getRackTotCnt() * 0.7){
                // 기본으로 제공하는 BluePin 마커 모양.
                StationMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            }else{
                StationMarker.setMarkerType(MapPOIItem.MarkerType.RedPin);
            }

            // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//            StationMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            /* Custom Marker XML 설정 구문 */
            View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
            ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.bike); /* Maker Image 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.main_title)).setText(station.getStationName()); /* Maker Text 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.sub_title)).setText("현재 사용가능 대수 : "+station.getParkingBikeTotCnt()+"/"+station.getRackTotCnt()+"("+station.getShared()+"%)"); /* Maker 장소 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.sub_title2)).setText("1시간 후 사용가능 : "+station.getPredict()+"대 예상"); /* Maker 장소 변경 해주는 구문 */
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
        while(iterator.hasNext()){
            Model__station stationData = ((Model__station) iterator.next());
            list.add(stationData.getStationName()+"\n 현재"+stationData.getParkingBikeTotCnt()+"대 사용가능"+"\n"+stationData.getPredict()+"대 사용가능 예상");
        }

    }

    //도난 자전거 테이블 생성 함수 실행
    public void createBicycleTable(ListView listview, List<String> list,String lat,String log){
        try{
            Log.d("createBicycleTable",lat+", "+log);

            //도난 자전거 표시
            MapPOIItem StolenBicycleMarker = new MapPOIItem();
            StolenBicycleMarker.setItemName("도난 자전거");
            StolenBicycleMarker.setTag(0);
            StolenBicycleMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(lat), Double.parseDouble(log)));
            StolenBicycleMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
            StolenBicycleMarker.setCustomImageResourceId(R.drawable.bike); // 마커 이미지.
            StolenBicycleMarker.setCustomImageAutoscale(true); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
            StolenBicycleMarker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.

            /* Custom Marker XML 설정 구문 */
            View mView = getLayoutInflater().inflate(R.layout.activity_custommaker, null);
            ((ImageView) mView.findViewById(R.id.marker_image)).setImageResource(R.drawable.alert); /* Maker Image 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.main_title)).setText("도난 자전거 신고"); /* Maker Text 변경 해주는 구문 */
            ((TextView) mView.findViewById(R.id.sub_title)).setText(""); /* Maker 장소 변경 해주는 구문 */

            StolenBicycleMarker.setCustomCalloutBalloon(mView);
            mapView.addPOIItem(StolenBicycleMarker);

            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, stealList);

            //리스트뷰의 어댑터를 지정해준다.
            listview.setAdapter(adapter);

            //리스트뷰에 보여질 아이템을 추가
            list.add("도난 자전거");
            StolenBicycleMarkerList.add(StolenBicycleMarker);

            //리스트뷰의 아이템을 클릭시 해당 아이템의 문자열을 가져오기 위한 처리
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView,
                                        View view, int position, long id) {
                    //클릭한 아이템의 문자열을 가져옴
                    String selected_item = (String)adapterView.getItemAtPosition(position);
                    Log.i("yejin",selected_item);
                    StolenBicycleMarkerList.get(position).setCustomImageResourceId(R.drawable.alert);
                    mapView.addPOIItem(StolenBicycleMarkerList.get(position));

                    publishClick.onClick(view);

                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            StolenBicycleMarkerList.get(position).setCustomImageResourceId(R.drawable.bike);
                            mapView.addPOIItem(StolenBicycleMarkerList.get(position));
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
            });

            //리스트뷰에 보여질 아이템을 추가
//            list.add("test자전거");

        }catch (Exception e){
            Log.e("errorCreateBicycleTable",e.getMessage());
            return;
        }
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