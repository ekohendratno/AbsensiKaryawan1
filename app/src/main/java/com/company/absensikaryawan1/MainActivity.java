package com.company.absensikaryawan1;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.company.absensikaryawan1.config.AppController;
import com.company.absensikaryawan1.config.CustomDigitalClock;
import com.company.absensikaryawan1.config.Koneksi;
import com.company.absensikaryawan1.models.AbsensiList;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    static SharedPreferences sharedpreferences;
    Context context;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefresh;

    LinearLayout empty1;
    RecyclerView recyclerViewHariIni;
    Koneksi koneksi;

    TextView tv_nama;

    String keterangan;

    CircleImageView imageProfile;
    String uid, nama;

    TextView tv_jam_shift;


    Handler handler = new Handler();

    private Runnable runTask = new Runnable() {
        @Override
        public void run() {
            // Execute tasks on main thread
            new postDataRecentsyncTask().execute();
            Log.d("Handlers", "Called on main thread");
            handler.postDelayed(this, 1000*60);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        context = MainActivity.this;

        koneksi = new Koneksi(MainActivity.this);
        koneksi.policyAllow();


        sharedpreferences = getSharedPreferences(Splash.MyPREFERENCES, Context.MODE_PRIVATE);
        uid  = sharedpreferences.getString("uid", "");
        nama  = sharedpreferences.getString("nama", "");


        tv_jam_shift = findViewById(R.id.tv_jam_shift);
        imageProfile = findViewById(R.id.imageProfile);
        tv_nama = findViewById(R.id.tv_nama);
        tv_nama.setText(nama);

        progressBar = findViewById(R.id.progressBar);
        empty1 = findViewById(R.id.empty_view1);

        recyclerViewHariIni = findViewById(R.id.recyclerViewHariIni);
        recyclerViewHariIni.setNestedScrollingEnabled(false);
        recyclerViewHariIni.setLayoutManager( new LinearLayoutManager(context));



        tv_jam_shift.setText("Lama bekerja 0");


        new postDataRecentsyncTask().execute();

        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            new postDataRecentsyncTask().execute();

            swipeRefresh.setRefreshing(false);
        });

        CustomDigitalClock tv_jam = findViewById(R.id.tv_jam);
        TextView tv_tanggal = findViewById(R.id.tv_tanggal);

        TextView today_text = findViewById(R.id.today_text);



        tv_tanggal.setText( getDayTime() );
        today_text.setText( getDayTime() );


        MaterialButton actAbsensiMasuk = findViewById(R.id.actAbsensiMasuk);
        actAbsensiMasuk.setOnClickListener(v->{

            keterangan = "Absensi Masuk";

            takeFotoAbsensiSelfi();
        });

        MaterialButton actAbsensiPulang = findViewById(R.id.actAbsensiPulang);
        actAbsensiPulang.setOnClickListener(v->{

            keterangan = "Absensi Pulang";

            takeFotoAbsensiSelfi();
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showPictureDialog(v.getContext());

            }
        });

        String foto  = sharedpreferences.getString("foto", "");
        if(!TextUtils.isEmpty(foto)){
            Picasso.with(context).load(foto)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(imageProfile);
        }else{
            //showPictureDialog(MainActivity.this);
        }

        findViewById(R.id.actionLogout).setOnClickListener(v->{

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Anda yakin?")
                    .setContentText("Ingin logout dari akun!");
            sweetAlertDialog.setConfirmText("Ok!");
            sweetAlertDialog.setConfirmClickListener(sDialog -> {
                sDialog.dismissWithAnimation();

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.remove("username");
                editor.remove("password");
                editor.remove("uid");
                editor.remove("nama");
                editor.remove("foto");
                editor.remove("level");
                editor.apply();


                startActivity(new Intent(context, LoginActivity.class));
                finish();

            });
            sweetAlertDialog.setCancelText("Batal");
            sweetAlertDialog.showCancelButton(true);
            sweetAlertDialog.setCancelClickListener(sDialog -> sDialog.dismissWithAnimation());
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.show();

        });

        findViewById(R.id.actMengajukan).setOnClickListener(v->{
            startActivity(new Intent(context, MengajukanActivity.class));
        });

    }

    String getDayTime(){
        Date today = Calendar.getInstance().getTime();
        today.setTime(System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy ");
        return formatter.format(today);
    }

    private void showPictureDialog(Context c){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(c);
        pictureDialog.setTitle("Ganti Profilmu");
        String[] pictureDialogItems = {
                "Ubah Foto Profile dari Gallery",
                "Ubah Foto Profile dari Selfi",
                "Ubah Data Profile lainnya"
        };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallary();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                            case 2:
                                Toast.makeText(MainActivity.this,"Belum didukung!",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });



        String foto = sharedpreferences.getString("foto", "");
        if(!TextUtils.isEmpty(foto)){
            pictureDialog.setCancelable(true);
        }else{
            pictureDialog.setCancelable(false);
        }

        pictureDialog.show();//.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);




        return;

    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, 111010);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 111020);
    }


    private void takeFotoAbsensiSelfi() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivityForResult(intent, 11102);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 11102 && resultCode == RESULT_OK && data != null) {


            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            Bitmap bitmap = scaleImageBitmap(imageBitmap,650);


            Log.e("result1", String.valueOf("bitmap.get"));


            Intent i = new Intent(this, AbsensiSelfiActivity.class);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bs);
            i.putExtra("selfi_foto", bs.toByteArray());
            i.putExtra("keterangan", keterangan);
            startActivity(i);


        }else if ((requestCode == 111010 || requestCode == 111020) && resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = null;

            if (requestCode == 111010 && data != null) {
                Uri picUri = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(picUri);
                    Drawable drawing = Drawable.createFromStream(inputStream, picUri.toString() );
                    bitmap = ((BitmapDrawable)drawing).getBitmap();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = scaleImageBitmap(bitmap,650);

            } else if (requestCode == 111020 && data != null) {

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                bitmap = scaleImageBitmap(imageBitmap,650);

            }

            BitmapDrawable result = new BitmapDrawable(bitmap);
            imageProfile.setImageDrawable(result);
            imageProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            final byte[] bytes = baos.toByteArray();


            AsyncTaskRunnerUploadProfile profile = new AsyncTaskRunnerUploadProfile(bitmap);
            profile.execute();



        }

    }




    private Bitmap scaleImageBitmap(Bitmap bitmap, int boundBoxInDp) {

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();

        return scaledBitmap;
    }




    public class AsyncTaskRunnerUploadProfile extends AsyncTask<String, String, Boolean> {

        SweetAlertDialog sweetAlertDialog;
        boolean lancar = false;
        Bitmap bitmap;

        public AsyncTaskRunnerUploadProfile(Bitmap bitmap) {
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
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                final okhttp3.MediaType MEDIA_TYPE = f.getPath().endsWith("png") ? okhttp3.MediaType.parse("image/png") : okhttp3.MediaType.parse("image/jpeg");
                multipartBody.addFormDataPart("uploaded_file", f.getName(), okhttp3.RequestBody.create(MEDIA_TYPE, f));
                multipartBody.addFormDataPart("uid", uid);

                okhttp3.RequestBody requestBody = multipartBody.build();

                String url = Koneksi.url_server +"/api/uploadprofile";
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

                Log.e("res",res);

                JSONObject j1 = new JSONObject( res );
                if(j1.getBoolean("success")){

                    JSONObject jsonObject = j1.getJSONObject("response");

                    Log.e("foo",jsonObject.getString("foto"));

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("foto", jsonObject.getString("foto"));
                    editor.apply();

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

            if(result){

                sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Good job!")
                        .setContentText("Data berhasil dikirim!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();

                });
                sweetAlertDialog.show();

            }else{

                sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Koneksi gagal!")
                        .setContentText("Terdapat gangguan koneksi!");
                sweetAlertDialog.setConfirmText("Lagi!");
                sweetAlertDialog.setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();

                    AsyncTaskRunnerUploadProfile runner = new AsyncTaskRunnerUploadProfile(bitmap);
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

            sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            sweetAlertDialog.setTitleText("Tunggu sebentar");
            sweetAlertDialog.setContentText("Sedang memperbaharui data");
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.show();

        }
    }



    public class postDataRecentsyncTask extends AsyncTask<String, String, Boolean> {

        String lamakerja;
        List<AbsensiList> listArrayList = new ArrayList<>();
        @Override
        protected Boolean doInBackground(String... params) {

            try {
                String uid = sharedpreferences.getString("uid", "");

                JSONObject j1 = koneksi.getDataServer("/api/absensi_list?u="+uid);
                if( j1.getBoolean("success") ) {

                    lamakerja = j1.getString("lamakerja");

                    JSONArray jsonArray = j1.getJSONArray("response");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        listArrayList.add(new AbsensiList(
                                jsonObject.getString("absensi_id"),
                                jsonObject.getString("absensi_tanggal"),
                                jsonObject.getString("absensi_jam"),
                                jsonObject.getString("absensi_tanggal_terbaru"),
                                jsonObject.getString("absensi_keterangan"),
                                jsonObject.getString("absensi_catatan"),
                                jsonObject.getString("absensi_lokasi_lat"),
                                jsonObject.getString("absensi_lokasi_long"),
                                jsonObject.getString("absensi_lokasi_alamat"),
                                jsonObject.getString("absensi_foto")
                        ));

                    }

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
            progressBar.setVisibility(View.GONE);


            if(listArrayList.size() > 0){
                empty1.setVisibility(View.GONE);
            }

            tv_jam_shift.setText("Lama bekerja "+lamakerja);

            RecyclerViewAdapterListRecent adapter1 = new RecyclerViewAdapterListRecent(context, listArrayList);
            recyclerViewHariIni.setItemAnimator(new DefaultItemAnimator());
            recyclerViewHariIni.setAdapter(adapter1);

        }
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }
    }


    public class RecyclerViewAdapterListRecent extends RecyclerView.Adapter<RecyclerViewAdapterListRecent.ItemViewHolder> {

        Context context;
        List<AbsensiList> ujianList;

        public RecyclerViewAdapterListRecent(Context context, List<AbsensiList> ujianList) {
            this.context = context;
            this.ujianList = ujianList;
        }


        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_absensi_list, parent, false);

            return new ItemViewHolder(itemView, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            AbsensiList item = ujianList.get(position);

            holder.tv1.setText(item.absensi_keterangan);
            holder.tv2.setText(item.absensi_jam);

        }


        @Override
        public int getItemCount() {
            return ujianList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            private TextView tv1,tv2;

            public ItemViewHolder(View itemView, int viewType) {
                super(itemView);

                tv1 = itemView.findViewById(R.id.tv1);
                tv2 = itemView.findViewById(R.id.tv2);

            }
        }
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Ketuk sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);


    }




}