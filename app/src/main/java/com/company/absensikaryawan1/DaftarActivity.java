package com.company.absensikaryawan1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.company.absensikaryawan1.config.Koneksi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DaftarActivity extends AppCompatActivity {
    public static String TAG = "LoginActivity";

    String username, password, nama, notelp, email, alamat;
    SweetAlertDialog sweetAlertDialog;
    Koneksi koneksi;
    static SharedPreferences sharedpreferences;
    static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daftar);


        context = DaftarActivity.this;

        koneksi = new Koneksi(DaftarActivity.this);

        koneksi.policyAllow();

        findViewById(R.id.actBack).setOnClickListener(v -> onBackPressed());

        /**
         * Loading
         */

        sweetAlertDialog = new SweetAlertDialog(DaftarActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Tunggu sebentar");
        sweetAlertDialog.setContentText("Sedang memperbaharui data");
        sweetAlertDialog.setCancelable(false);


        final TextInputEditText editUsername = findViewById(R.id.editUsername);
        final TextInputEditText editPassword = findViewById(R.id.editPassword);

        final TextInputEditText editNama = findViewById(R.id.editNama);
        final TextInputEditText editNoTelp = findViewById(R.id.editNoTelp);
        final TextInputEditText editEmail = findViewById(R.id.editEmail);
        final TextInputEditText editAlamat = findViewById(R.id.editAlamat);


        final MaterialButton actSignUp = findViewById(R.id.actSignUp);
        actSignUp.setOnClickListener(v -> {
            username = editUsername.getText().toString();
            password = editPassword.getText().toString();
            nama = editNama.getText().toString();
            notelp = editNoTelp.getText().toString();
            email = editEmail.getText().toString();
            alamat = editAlamat.getText().toString();

            if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                Toast.makeText(v.getContext(),"Username atau Sandi belum diisi!",Toast.LENGTH_LONG).show();
            }else{
                new setSignUpSync().execute();
            }
        });

    }


    public class setSignUpSync extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                JSONObject j1 = koneksi.getDataServer(
                        "/api/signup?"+
                                "u="+username+
                                "&p="+password+
                                "&n="+nama+
                                "&t="+notelp+
                                "&e="+email+
                                "&a="+alamat
                );
                if( j1.getBoolean("success") ) {
                    return true;
                }else{
                    return false;
                }

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return false;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            sweetAlertDialog.dismissWithAnimation();
            if(result){

                sweetAlertDialog = new SweetAlertDialog(DaftarActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Pendaftaran berhasil!")
                        .setContentText("Data berhasil dikirim!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();


                    onBackPressed();


                });
                sweetAlertDialog.show();

            }else{

                sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Terjadi gangguan!")
                        .setContentText("Maaf ada kesalahan pendaftaran akun sudah ada atau terjadi gangguan jaringan!!");
                sweetAlertDialog.setConfirmText("Baiklah!");
                sweetAlertDialog.setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();


                });

                sweetAlertDialog.setCancelText("Batal");
                sweetAlertDialog.showCancelButton(true);
                sweetAlertDialog.setCancelClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    finish();
                    System.exit(0);
                });

                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();
            }

        }
        @Override
        protected void onPreExecute() {
            if(!sweetAlertDialog.isShowing()) sweetAlertDialog.show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}