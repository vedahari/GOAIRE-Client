package edu.vt.ece.onaire;
import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

/*
TODO: Request the server only if the vehicle is stationed for more than two seconds.
 */

public class MainActivity extends AppCompatActivity {

    private static final float MIN_DISTANCE = 1.0f;
    private static final int REQUESTCODE_SIMPLE_SETTINGS_ACTIVITY = 1;
    private final int REQUESTCODE_GPS = 10;

    //Views and Buttons values
    private Button mGetLocationButton;
    private TextView mLatView;
    private TextView mLonView;
    private TextView mSpeedView;

    // Reference to the LocationManager and LocationListener
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private final String TAG = "GetLocationActivity";
    private PositionInfo mCurrPosInfo;
    private PositionInfo mPrevPosInfo;

    private int B;
    private SharedPreferences sharedPref;

    private boolean isMoving;
    private boolean isTrackingEnabled;

    MainActivity(){
        mCurrPosInfo = new PositionInfo();
        mPrevPosInfo = new PositionInfo();
        isMoving = false;
        isTrackingEnabled=false;
    }

    private void loadPreferences(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        B = sharedPref.getInt("B",27);
        isTrackingEnabled=sharedPref.getBoolean("isTrackingEnabled",false);
        Log.d(TAG,"B is loaded as "+B+" & isTrackingEnabled "+isTrackingEnabled);
    }

    private void savePreferences(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPref.edit().putInt("B",B);
        sharedPref.edit().putBoolean("isTrackingEnabled",isTrackingEnabled);
        sharedPref.edit().apply();
        Log.d(TAG,"B is saved as "+B+" & isTrackingEnabled "+isTrackingEnabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMoving = false;
        Log.d(TAG, "OnCreate entered!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGetLocationButton = (Button) findViewById(R.id.get_location_button);
        mLatView = (TextView) findViewById(R.id.latitude_view);
        mLonView = (TextView) findViewById(R.id.longitude_view);
        mSpeedView = (TextView) findViewById(R.id.speed_view);
        loadPreferences();

        if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
            finish();
        }
        mLocationListener = new LocationListener() {
            // Called back when location changes
            public void onLocationChanged(Location location) {
                Log.d(TAG, "Location has been changed!");
                mCurrPosInfo.setLoc(location);
                mCurrPosInfo.setSpeed(0.0);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ){
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTCODE_GPS);
                };
            }
        else{
            configureGetLocationButton();
        }

        if (savedInstanceState!=null){
            mCurrPosInfo =((PositionInfo) savedInstanceState.getParcelable("PositionInfo"));
        }
        updateGUILocation();
    }

    private void updateGUILocation() {
        if (mCurrPosInfo.isLocationValid()) {
            mLatView.setText(String.format("%.5f", mCurrPosInfo.getLoc().getLatitude()));
            mLonView.setText(String.format("%.5f", mCurrPosInfo.getLoc().getLongitude()));
        }
        mSpeedView.setText(String.format("%.5f",mCurrPosInfo.getSpeed()*2.23694));
    }


    private void vHandleStartTracking(Button b){
        Log.d(TAG,"vHandleStartTracking Entered!");
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, Constants.Time.POLLING_FREQ,
                    MIN_DISTANCE, mLocationListener);
            Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation!=null) {
                mCurrPosInfo.setLoc(lastLocation);
                mPrevPosInfo.setLoc(mCurrPosInfo.getLoc());
            }
            timerHandler.postDelayed(timerRunnable,Constants.Time.ONE_SECOND*2);
            b.setText("Stop Tracking");
            updateGUILocation();
            //Toast.makeText(MainActivity.this, "Updating the location!", Toast.LENGTH_LONG).show();
        }
        catch (SecurityException s){
            Log.e(TAG, "onResume:User Disabled Perm"+s.getMessage());
        }
    }

    private void vHandleStopTracking(Button b){
        Log.d(TAG,"vHandleStopTracking Entered!");
        timerHandler.removeCallbacks(timerRunnable);
        //mLocationManager.removeUpdates(mLocationListener);
        b.setText("Start Tracking");
        mCurrPosInfo.setSpeed(0.0);
        updateGUILocation();
        findViewById(R.id.result_textView).setVisibility(View.INVISIBLE);
    }

    //TODO: handle toggle button case==> start and stop gps activity
    public void onGetLocationButtonClick (View V)
    {
        Button b = (Button)V;
        if (b.getText().equals("Start Tracking")){
            Log.d(TAG, "Start Tracking clicked!");
            isTrackingEnabled = true;
            vHandleStartTracking(b);
        }
        else if (b.getText().equals("Stop Tracking")){
            Log.d(TAG, "Stop Tracking clicked!");
            isTrackingEnabled = false;
            vHandleStopTracking(b);
        }
        else {
            Log.d(TAG,"Unexpected value in GetLocationButton "+b.getText());
        }
    }

    public void onGetStopTimeButtonClick (View V)
    {
        postIdlingTimeRequest();
    }

    private void postIdlingTimeRequestIfRequired(){
        if (Double.compare(mCurrPosInfo.getSpeed(),0.0)==0){
            postIdlingTimeRequest();
        }
        else{
            //Vehicle is moving. Hence need not display the text.
            findViewById(R.id.result_textView).setVisibility(View.INVISIBLE);
        }
    }

    private void postIdlingTimeRequest() {
        //Make a HTTP request and get the response and print in the toast
        ServerCommunicator oSrvComm = new ServerCommunicator(B, new ServerCommunicator.ServerCommunicatorAsyncResponse() {
            @Override
            public void processFinish(int idleTime, double cost) {
                //Toast.makeText(MainActivity.this,"Advised idling time "+idleTime+" with Cost "+cost,Toast.LENGTH_LONG).show();
                if (idleTime>=0) {
                    TextView resultView = (TextView) findViewById(R.id.result_textView);
                    resultView.setVisibility(View.VISIBLE);
                    resultView.setText(String.format("Advised Idling Time: \t %d seconds \nExpected Online Cost: \t %.2f seconds", idleTime, cost));
                }
                else{
                    Toast.makeText(MainActivity.this, "Unable to connect to the server!", Toast.LENGTH_LONG).show();
                }
            }
        });
        if(mCurrPosInfo.isLocationValid()){
        oSrvComm.execute(mCurrPosInfo.getLoc().getLatitude(),mCurrPosInfo.getLoc().getLongitude());
        }
    }

    private void configureGetLocationButton() {
        mGetLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, Constants.Time.POLLING_FREQ,
                            MIN_DISTANCE, mLocationListener);
                }
                catch (SecurityException s){
                    Log.e(TAG, "onResume:User Disabled Perm"+s.getMessage());
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case REQUESTCODE_GPS:
                if (grantResults.length>0 &&
                        grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    configureGetLocationButton();
                }
                break;
            default:
                Log.e(TAG,"Unhandled permission result");
                break;
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"OnPause entered!");
        super.onPause();
        savePreferences();
        Button b = (Button)findViewById(R.id.get_location_button);
        vHandleStopTracking(b);
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"OnStop entered!");
        super.onStop();
        savePreferences();
        Button b = (Button)findViewById(R.id.get_location_button);

        vHandleStopTracking(b);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume entered!");
        super.onResume();
        loadPreferences();
//        isMoving = false;
//        updateGUILocation();
        if(isTrackingEnabled){
            vHandleStartTracking((Button)findViewById(R.id.get_location_button));
        }
        else{
            vHandleStopTracking((Button)findViewById(R.id.get_location_button));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "OnSaveInstanceState entered!");
        super.onSaveInstanceState(outState);
        outState.putParcelable("PositionInfo",(Parcelable) mCurrPosInfo);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState entered!");
        super.onRestoreInstanceState(savedInstanceState);
        mCurrPosInfo = (PositionInfo) savedInstanceState.getParcelable("PositionInfo");
        updateGUILocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case (REQUESTCODE_SIMPLE_SETTINGS_ACTIVITY ):
                if (resultCode == Activity.RESULT_OK) {
                    B = data.getIntExtra("B", 0);
                    Log.d(TAG, "Received B as " + B);
                }
                else{
                    Log.e(TAG,"Settings Activity RESULT not OK!");
                }
                break;
            default:
                Log.e(TAG,"Unknown ActivityResult received!");
                break;
        }
    }

    /*
        * Functions for the Options Menu
        * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Log.d(TAG, "Settings has been selected!");
                onClickSettingsItem();
                break;
            case R.id.action_about:
                Log.d(TAG,"About icon has been pressed");
                onClickAboutItem();
                break;
//            case R.id.action_help:
//                Log.d(TAG,"Help has been clicked");
//                onClickHelpItem();
//                break;
            default:
                Log.d(TAG,"unhandled option selected");
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickSettingsItem() {
        Bundle bundle = new Bundle();
        bundle.putInt("B",B);
        Intent settingIntent = new Intent(MainActivity.this, SimpleSettingsActivity.class);
        settingIntent.putExtras(bundle);
        startActivityForResult(settingIntent,REQUESTCODE_SIMPLE_SETTINGS_ACTIVITY,bundle);
    }

    private void onClickAboutItem() {
        Intent aboutIntent = new Intent(MainActivity.this,AboutActivity.class);
        startActivity(aboutIntent);
    }

    private void onClickHelpItem() {
    }

    /*
        * Functions with timer for measuring the speed of the system
        * */
    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            timerHandler.postDelayed(this, Constants.Time.ONE_SECOND*2);
            //display the speed
            if (!mCurrPosInfo.equals(mPrevPosInfo)) {
                Log.d(TAG,"CurrPos is different from PrevPos");
                if(isMoving){
                    //already moving
                    mCurrPosInfo.setSpeed(calculateSpeed());
                    updateGUILocation();
                    mPrevPosInfo.setLoc(mCurrPosInfo.getLoc());
                }
                else {
                    //starting to move
                    isMoving = true;
                    mCurrPosInfo.setSpeed(calculateSpeed());
                    updateGUILocation();
                    mPrevPosInfo.setLoc(mCurrPosInfo.getLoc());
                    findViewById(R.id.result_textView).setVisibility(View.INVISIBLE);
                }

            }
            else {
                //The vehicle is stopped at the location. Need not update the view.
                Log.d(TAG,"CurrPos is same as PrevPos");
                if (isMoving){
                    //Device has come to a halt now. Update speed and request the idling time.
                    isMoving = false;
                    mPrevPosInfo.getLoc().setTime(mPrevPosInfo.getLoc().getTime()+2*Constants.Time.ONE_SECOND);
                    mCurrPosInfo.setSpeed(0.0);
                    postIdlingTimeRequest();
                    updateGUILocation();
                }
                else{
                    mPrevPosInfo.getLoc().setTime(mPrevPosInfo.getLoc().getTime()+2*Constants.Time.ONE_SECOND);
                }

            }
        }
    };
    private double calculateSpeed() {
        double resSpeed = 0.0;
        if (!mPrevPosInfo.isLocationValid() || !mCurrPosInfo.isLocationValid()){
            Log.e(TAG,"Invalid location while speed calculation");
            return resSpeed;
        }
        double distance = DistanceCalculator.computeDistance(
                mCurrPosInfo.getLoc().getLatitude(), mCurrPosInfo.getLoc().getLongitude(),
                mPrevPosInfo.getLoc().getLatitude(),mPrevPosInfo.getLoc().getLongitude());
        double time = Math.abs(mPrevPosInfo.getLoc().getTime() - mCurrPosInfo.getLoc().getTime())/1000;
        Log.d(TAG,"Distance "+distance+"\t time "+time);
        resSpeed = distance/time;
        //TODO: uncomment the following lines once all testing is done.
//                if (resSpeed>500){
//                    return 0.0;
//                }
        return resSpeed;
    }

}