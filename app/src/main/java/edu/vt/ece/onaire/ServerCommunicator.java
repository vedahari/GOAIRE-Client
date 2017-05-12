package edu.vt.ece.onaire;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Scanner;


/**
 * Created by vedahari on 4/1/2017.
 */
public class ServerCommunicator extends AsyncTask<Double ,Void,Integer> {
    private final String ServerCommunicator_TAG = "ServerCommunicator";
    //public final String SERVER_ROOT_URL = "http://10.0.0.199:8080/OnAire_Server/OnAireServer";
    //public final String SERVER_ROOT_URL = "http://192.168.43.72:8080/OnAire_Server/OnAireServer";
    public final String SERVER_ROOT_URL = "http://172.29.0.122:8080/OnAire_Server/OnAireServer";
    //public final String SERVER_ROOT_URL = "http://stackoverflow.com";

    public interface ServerCommunicatorAsyncResponse{
        void processFinish(int idleTime, double cost);
    }

    public ServerCommunicatorAsyncResponse srvCommAsyncResp = null;

    private static int B;
    public ServerCommunicator(int b, ServerCommunicatorAsyncResponse resp) {
        B = b;
        srvCommAsyncResp = resp;
    }

    private int idlingTime;
    private double costIncurred;

    @Override
    protected Integer doInBackground(Double... params) {
        //Send HTTP request and get back the stop time
        String query = new String();
        Integer idleTime = -1;
        try {
            query = String.format("Latitude=%s&Longitude=%s&B=%s",
                    URLEncoder.encode(Double.toString(params[0]), "UTF-8"),
                    URLEncoder.encode(Double.toString(params[1]), "UTF-8"),
                    Integer.toString(B));
        }
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
            return idleTime;
        }
        try {
            URL url = new URL(SERVER_ROOT_URL+"?"+query );
            Log.d(ServerCommunicator_TAG,"Requesting GetIdleTime!");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream response = connection.getInputStream();
            int respCode = connection.getResponseCode();
            Log.d(ServerCommunicator_TAG,"::RespCode::"+respCode);
            if (respCode==HttpURLConnection.HTTP_OK) {
                String strCost = connection.getHeaderField("Cost");
                Log.d(ServerCommunicator_TAG,"Got cost function value as "+strCost);
                if (!strCost.isEmpty()){
                    costIncurred = Double.parseDouble(strCost);
                }
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, "UTF8"))) {
                    idleTime = Integer.parseInt(reader.readLine());
                    idlingTime = idleTime;
                    Log.d(ServerCommunicator_TAG, " Received answer is " + idleTime);
                } catch (Exception e) {
                    Log.d(ServerCommunicator_TAG, "ParsingException" + e.toString());
                    return idleTime;
                }
            }
            else{
                //Handle the error messages appropriately. Currently all are treated same.
                Log.e(ServerCommunicator_TAG,"Request failed with response code "+respCode);
            }
        } catch (Exception e) {
            Log.d(ServerCommunicator_TAG, "ConnectionException " + e.toString());
            e.printStackTrace();
            return idleTime;
        }
        return idleTime;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        srvCommAsyncResp.processFinish(idlingTime,costIncurred);
    }
}
