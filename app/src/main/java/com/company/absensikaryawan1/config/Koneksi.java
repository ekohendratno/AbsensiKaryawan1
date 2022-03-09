package com.company.absensikaryawan1.config;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.ALARM_SERVICE;

public class Koneksi {
    public static String url_server = "https://absensikaryawan1.kopas.id";
    //public static String url_server = "http://192.168.10.254/absensikaryawan1";
    static Context context;

    public Koneksi(Context context){

        this.context = context;
    }

    public static JSONObject getDataServer(String requestPath) throws IOException, JSONException {
        Log.e("getDataServer",url_server + requestPath);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url( url_server + requestPath )
                .build();
        Response response = client.newCall(request).execute();
        String response_body = response.body().string();

        Log.e("response_body",response_body);

        return new JSONObject(response_body);

    }

    public static JSONObject setDataFileServer(String requestPath, byte[] byteArray) throws IOException, JSONException {
        Log.e("uploadDataServer",url_server + requestPath);

        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("capture", "filename.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        Request request = new Request.Builder()
                .url( url_server + requestPath )
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        String response_body = response.body().string();

        Log.e("response_body",response_body);

        return new JSONObject(response_body);

    }



    public static JSONObject setDataFileServer2(String requestPath, byte[] byteArray) throws IOException, JSONException {
        Log.e("uploadDataServer",url_server + requestPath);

        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "filename.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        Request request = new Request.Builder()
                .url( url_server + requestPath )
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        String response_body = response.body().string();

        Log.e("response_body",response_body);

        return new JSONObject(response_body);

    }

    public void policyAllow(){
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

}
