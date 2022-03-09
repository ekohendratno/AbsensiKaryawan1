package com.company.absensikaryawan1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

import com.company.absensikaryawan1.config.Koneksi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AbsensiSelfiActivity extends AppCompatActivity {

    static SharedPreferences sharedpreferences;
    Context context;
    Koneksi koneksi;

    String uid;
    ImageView iv_selfi_foto;
    TextView tv_alamat;
    AppCompatEditText et_lokasi_lat, et_lokasi_long;

    TextInputEditText editCatatan;
    MaterialButton actionKirim;

    AppCompatEditText et_addressLine1;
    AppCompatEditText et_addressLine2;
    AppCompatEditText et_city_sub;
    AppCompatEditText et_city;
    AppCompatEditText et_state_sub;
    AppCompatEditText et_state;
    AppCompatEditText et_country;
    AppCompatEditText et_postalCode;
    AppCompatEditText et_knownName;

    Bitmap b;
    String k;


    ProgressBar progressBar, actRefreshLokasiLoading;
    ImageView actRefreshLokasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absensiselfi);


        b = BitmapFactory.decodeByteArray(
                getIntent().getByteArrayExtra("selfi_foto"), 0, getIntent()
                        .getByteArrayExtra("selfi_foto").length);


        k = getIntent().getStringExtra("keterangan");


        context = AbsensiSelfiActivity.this;

        koneksi = new Koneksi(AbsensiSelfiActivity.this);
        koneksi.policyAllow();


        sharedpreferences = getSharedPreferences(Splash.MyPREFERENCES, Context.MODE_PRIVATE);
        uid  = sharedpreferences.getString("uid", "");

        findViewById(R.id.actBack).setOnClickListener(v -> onBackPressed());

        TextView title = findViewById(R.id.title);
        title.setText(k);

        progressBar = findViewById(R.id.progressBar);
        actRefreshLokasiLoading = findViewById(R.id.actRefreshLokasiLoading);
        actRefreshLokasi = findViewById(R.id.actRefreshLokasi);

        iv_selfi_foto = findViewById(R.id.selfi_foto);
        tv_alamat = findViewById(R.id.tv_alamat);
        et_lokasi_lat = findViewById(R.id.lokasi_lat);
        et_lokasi_long = findViewById(R.id.lokasi_long);

        editCatatan = findViewById(R.id.editCatatan);


        et_addressLine1 = findViewById(R.id.addressLine1);
        et_addressLine2 = findViewById(R.id.addressLine2);
        et_city_sub = findViewById(R.id.city_sub);
        et_city = findViewById(R.id.city);
        et_state_sub = findViewById(R.id.state_sub);
        et_state = findViewById(R.id.state);
        et_country = findViewById(R.id.country);
        et_postalCode = findViewById(R.id.postalCode);
        et_knownName = findViewById(R.id.knownName);

        actionKirim = findViewById(R.id.actionKirim);
        actionKirim.setEnabled(false);


        BitmapDrawable resultBitmap = new BitmapDrawable(b);
        iv_selfi_foto.setImageDrawable(resultBitmap);
        iv_selfi_foto.setScaleType(ImageView.ScaleType.CENTER_CROP);


        cariLokasiSaatIni();


        actRefreshLokasi.setOnClickListener(v->{
            cariLokasiSaatIni();
        });

        actionKirim.setOnClickListener(v->{



            AsyncTaskRunnerUploadAbsensi profile = new AsyncTaskRunnerUploadAbsensi(b);
            profile.execute();


        });

    }

    void cariLokasiSaatIni(){

        progressBar.setVisibility(View.VISIBLE);
        actRefreshLokasiLoading.setVisibility(View.VISIBLE);
        actRefreshLokasi.setVisibility(View.GONE);

        // GET CURRENT LOCATION
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocation.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Do it all with location
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();


                        et_lokasi_lat.setText(String.valueOf(location.getLatitude()));
                        et_lokasi_long.setText(String.valueOf(location.getLongitude()));


                        try {
                            Geocoder geocoder = new Geocoder(AbsensiSelfiActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);


                                String addressLine1 = address.getAddressLine(0);
                                String addressLine2 = address.getAddressLine(1);

                                String city_sub = address.getSubLocality();
                                String city = address.getLocality();
                                String state_sub = address.getSubAdminArea();
                                String state = address.getAdminArea();
                                String country = address.getCountryName();
                                String postalCode = address.getPostalCode();
                                String knownName = address.getFeatureName();

                                et_addressLine1.setText( addressLine1 );
                                et_addressLine2.setText( addressLine2 );
                                et_city_sub.setText( city_sub );
                                et_city.setText( city );
                                et_state_sub.setText( state_sub );
                                et_state.setText( state );
                                et_country.setText( country );
                                et_postalCode.setText( postalCode );
                                et_knownName.setText( knownName );

                                //tv_alamat.setText( addressLine1 );
                                tv_alamat.setText( getCompleteAddress(address) );

                                if( !TextUtils.isEmpty(tv_alamat.getText().toString()) ){
                                    progressBar.setVisibility(View.GONE);
                                    actRefreshLokasiLoading.setVisibility(View.GONE);
                                    actRefreshLokasi.setVisibility(View.VISIBLE);
                                    actionKirim.setEnabled(true);
                                    Toast.makeText(AbsensiSelfiActivity.this,"Alamat ditemukan",Toast.LENGTH_SHORT).show();
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();

                            tv_alamat.setText("Tidak dapat menemukan alamat");
                            progressBar.setVisibility(View.GONE);
                            actRefreshLokasiLoading.setVisibility(View.GONE);
                            actRefreshLokasi.setVisibility(View.VISIBLE);
                            actionKirim.setEnabled(false);

                            Toast.makeText(AbsensiSelfiActivity.this,"Alamat tidak ditemukan",Toast.LENGTH_SHORT).show();

                        }



                    }
                })
                .addOnFailureListener(this, e -> {

                    tv_alamat.setText("Tidak dapat menemukan alamat");
                    progressBar.setVisibility(View.GONE);
                    actRefreshLokasiLoading.setVisibility(View.GONE);
                    actRefreshLokasi.setVisibility(View.VISIBLE);
                    actionKirim.setEnabled(false);

                    Toast.makeText(AbsensiSelfiActivity.this,"Alamat tidak ditemukan",Toast.LENGTH_SHORT).show();

                });
    }




    public class AsyncTaskRunnerUploadAbsensi extends AsyncTask<String, String, Boolean> {

        SweetAlertDialog sweetAlertDialog;
        Bitmap bitmap;

        public AsyncTaskRunnerUploadAbsensi(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {

                okhttp3.MultipartBody.Builder multipartBody = new okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM);

                //create a file to write bitmap data
                File f = new File( getCacheDir(), uid+".png" );
                f.createNewFile();


                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                final okhttp3.MediaType MEDIA_TYPE = f.getPath().endsWith("png") ? okhttp3.MediaType.parse("image/png") : okhttp3.MediaType.parse("image/jpeg");
                multipartBody.addFormDataPart("uploaded_file", f.getName(), okhttp3.RequestBody.create(MEDIA_TYPE, f));
                multipartBody.addFormDataPart("uid", uid);


                String alamat = tv_alamat.getText().toString();
                String lokasi_lat = et_lokasi_lat.getText().toString();
                String lokasi_long = et_lokasi_long.getText().toString();
                String catatan = editCatatan.getText().toString();

                multipartBody.addFormDataPart("keterangan",k);
                multipartBody.addFormDataPart("catatan",catatan);
                multipartBody.addFormDataPart("lat",lokasi_lat);
                multipartBody.addFormDataPart("long",lokasi_long);
                multipartBody.addFormDataPart("alamat",alamat);

                okhttp3.RequestBody requestBody = multipartBody.build();

                String url = Koneksi.url_server +"/api/absensi";
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
                builder.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                        .readTimeout(5, TimeUnit.MINUTES); // read timeout

                okhttp3.OkHttpClient okHttpClient = builder.build();
                okhttp3.Response response = okHttpClient.newCall(request).execute();
                String res = response.body().string();
                JSONObject j1 = new JSONObject(res);
                Log.e("res",res);

                if( j1.getBoolean("success") ) {
                    return true;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            sweetAlertDialog.dismiss();
            if( result ){

                sweetAlertDialog = new SweetAlertDialog(AbsensiSelfiActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Absensi berhasil!")
                        .setContentText("Data berhasil dikirim!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();

                    Intent i = new Intent(AbsensiSelfiActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();


                });
                sweetAlertDialog.show();


            }else{

                sweetAlertDialog = new SweetAlertDialog(AbsensiSelfiActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Gagal absensi!")
                        .setContentText("Terdapat gangguan koneksi!");
                sweetAlertDialog.setConfirmText("Lagi!");
                sweetAlertDialog.setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    AsyncTaskRunnerUploadAbsensi runner = new AsyncTaskRunnerUploadAbsensi(bitmap);
                    runner.execute();

                });

                sweetAlertDialog.setCancelText("Batal");
                sweetAlertDialog.showCancelButton(true);
                sweetAlertDialog.setCancelClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                });

                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.show();
            }

        }
        @Override
        protected void onPreExecute() {


            sweetAlertDialog = new SweetAlertDialog(AbsensiSelfiActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            sweetAlertDialog.setTitleText("Tunggu sebentar");
            sweetAlertDialog.setContentText("Sedang memperbaharui data");
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.show();

        }
    }


    public String getCompleteAddress(Address address) {
        String location = "";
        String state, city, zip, street;


        if (address.getSubLocality() != null) {
            location+= address.getSubLocality();
        }

        if (address.getLocality() != null) {
            location+= ", "+address.getLocality();
        }


        if (address.getSubAdminArea() != null) {
            location+= ", "+address.getSubAdminArea();
        }

        if (address.getAdminArea() != null) {
            location+= ", "+address.getAdminArea();
        }

        if (address.getCountryName() != null) {
            location+= ", "+address.getCountryName();
        }


        if (address.getThoroughfare() != null) {
            location+= ", " + address.getThoroughfare();
        } else {
            location+= ", " + address.getFeatureName();
        }



        if (address.getPostalCode() != null) {
            location+=  ", "+address.getPostalCode();
        }

        return location;
    }






    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}