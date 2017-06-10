package com.example.k.lab6;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpService extends IntentService {
    public static final int GAMES_LIST = 1;
    public static final int IN_ROW = 2;
    public static final int REFRESH = 3;
    public static final int GAME_INFO = 4;
    public static final String URL = "URL";
    public static final String METHOD = "Method";
    public static final String PARAMS = "Params";
    public static final String RETURN = "Return";
    public static final String RESPONSE = "Response";
    public static final String LINES = "http://antons.pl/games/lines/";
    public static final String XO = "http://antons.pl/games/xo/";
    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;

    public HttpService() {
        super("HTTP calls handler");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            //Create url object from given string
            String urlstr = intent.getStringExtra(HttpService.URL);
            URL url = new URL(urlstr);
            //Prepare connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //set connection method
            switch (intent.getIntExtra(HttpService.METHOD,1)){
                case HttpService.POST:
                    conn.setRequestMethod("POST");
                    break;
                case HttpService.PUT:
                    conn.setRequestMethod("PUT");
                    break;
                default:
                    conn.setRequestMethod("GET");
            }
            //Using RSA asynhronic sign for authorization request
            Config conf = new Config(getApplicationContext());
            conn.setRequestProperty("PKEY", conf.getPublic().replace("\n",""));
            conn.setRequestProperty("SIGN", conf.sign(urlstr).replace("\n",""));

            //Add parameters to request
            String params = intent.getStringExtra(HttpService.PARAMS);
            Log.d("params", " " + params);
            if(params!=null) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params);
                writer.flush();
                writer.close();
            }
            //send request
            conn.connect();

            //Getting HTTP responseCode
            int responseCode = conn.getResponseCode();

            //Geting response Body only when connection is OK
            String response = "";
            if(responseCode==200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                //Convert response to single string
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            }
            //Close connection
            conn.disconnect();

            //Add response to return intent
            Intent returns = new Intent();
            returns.putExtra(HttpService.RESPONSE, response);
            PendingIntent reply = intent.getParcelableExtra(HttpService.RETURN);
            reply.send(this, responseCode, returns);

        }catch (Exception ex){
            //If connection error occured - show Exception message in logCat
            Log.d("CONNERROR", ex.toString());
        }
    }
}