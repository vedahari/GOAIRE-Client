package edu.vt.ece.onaire;

/**
 * Created by vedahari on 4/15/2017.
 */

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSLocation {
    Timer timer1;
    LocationManager locMan;
    LocationResult locRes;
    boolean isGPSEnabled =false;
    boolean isNWEnabled =false;

    public boolean getLocation(Context context, LocationResult result)
    {
        //LocationResult callback class is used to pass location value from GPSLocation to user code.
        locRes =result;
        if(locMan == null)
            locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{
            isGPSEnabled = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex){

        }
        try{
            isNWEnabled = locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //don't start listeners if no provider is enabled
        if(!isGPSEnabled && !isNWEnabled)
            return false;
        try {
            if (isGPSEnabled)
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            if (isNWEnabled)
                locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
        timer1=new Timer();
        timer1.schedule(new GetLastLocation(), 20000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locRes.gotLocation(location);
            locMan.removeUpdates(this);
            locMan.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locRes.gotLocation(location);
            locMan.removeUpdates(this);
            locMan.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            locMan.removeUpdates(locationListenerGps);
            locMan.removeUpdates(locationListenerNetwork);

            Location net_loc=null, gps_loc=null;
            try {
                if(isGPSEnabled)
                    gps_loc= locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(isNWEnabled)
                    net_loc= locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }

            //if there are both values use the latest one
            if(gps_loc!=null && net_loc!=null){
                if(gps_loc.getTime()>net_loc.getTime())
                    locRes.gotLocation(gps_loc);
                else
                    locRes.gotLocation(net_loc);
                return;
            }

            if(gps_loc!=null){
                locRes.gotLocation(gps_loc);
                return;
            }
            if(net_loc!=null){
                locRes.gotLocation(net_loc);
                return;
            }
            locRes.gotLocation(null);
        }
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
