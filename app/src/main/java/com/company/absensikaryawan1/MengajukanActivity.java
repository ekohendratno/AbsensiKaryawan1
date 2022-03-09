package com.company.absensikaryawan1;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.company.absensikaryawan1.config.Koneksi;
import com.company.absensikaryawan1.models.AbsensiList;
import com.company.absensikaryawan1.models.PengajuanList;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MengajukanActivity extends AppCompatActivity {
    public static String TAG = "MengajukanActivity";

    SweetAlertDialog sweetAlertDialog;
    Koneksi koneksi;
    static SharedPreferences sharedpreferences;
    static Context context;


    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    ProgressBar progressBar;
    LinearLayout empty1;
    RecyclerView recyclerView;


    LinearLayout lyt_list;
    LinearLayout lyt_form;


    AutoCompleteTextView editJenisPengajuan;
    TextInputEditText editKeterangan;

    TextInputEditText editTanggalMulai;
    TextInputEditText editTanggalSelesai;


    ImageView actBatal;
    ImageView actCreate;

    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mengajukan);


        context = MengajukanActivity.this;

        koneksi = new Koneksi(MengajukanActivity.this);
        koneksi.policyAllow();

        sharedpreferences = getSharedPreferences(Splash.MyPREFERENCES, Context.MODE_PRIVATE);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        findViewById(R.id.actBack).setOnClickListener(v -> onBackPressed());

        /**
         * Loading
         */

        sweetAlertDialog = new SweetAlertDialog(MengajukanActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        sweetAlertDialog.setTitleText("Tunggu sebentar");
        sweetAlertDialog.setContentText("Sedang memperbaharui data");
        sweetAlertDialog.setCancelable(false);


        editJenisPengajuan = findViewById(R.id.editJenisPengajuan);
        editKeterangan = findViewById(R.id.editKeterangan);

        editTanggalMulai = findViewById(R.id.editTanggalMulai);
        editTanggalSelesai = findViewById(R.id.editTanggalSelesai);


        actBatal = findViewById(R.id.actBatal);
        actCreate = findViewById(R.id.actCreate);

        final MaterialButton actSend = findViewById(R.id.actSend);




        ArrayAdapter<String> itemsAdapter1 = new ArrayAdapter<>( MengajukanActivity.this, android.R.layout.simple_list_item_1,
                Arrays.asList(getResources().getStringArray(R.array.mengajukan))
        );
        editJenisPengajuan.setAdapter(itemsAdapter1);

        editTanggalMulai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog(editTanggalMulai);
            }
        });
        editTanggalSelesai.setOnClickListener(view -> showDateDialog(editTanggalSelesai));

        progressBar = findViewById(R.id.progressBar);
        empty1 = findViewById(R.id.empty_view);

        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager( new LinearLayoutManager(context));


        new getDataPengajuanSync().execute();


        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(() -> {
            new getDataPengajuanSync().execute();

            swipeRefresh.setRefreshing(false);
        });

        lyt_list = findViewById(R.id.lyt_list);
        lyt_form = findViewById(R.id.lyt_form);

        actCreate.setOnClickListener(v->{
            lyt_list.setVisibility(View.GONE);
            lyt_form.setVisibility(View.VISIBLE);

            actBatal.setVisibility(View.VISIBLE);
            actCreate.setVisibility(View.GONE);
        });
        actBatal.setOnClickListener(v->{
            lyt_list.setVisibility(View.VISIBLE);
            lyt_form.setVisibility(View.GONE);

            actBatal.setVisibility(View.GONE);
            actCreate.setVisibility(View.VISIBLE);
        });

        actSend.setOnClickListener(v->{
            new setDataPengajuanSync().execute();
        });
    }

    private void showDateDialog(TextInputEditText editTanggal){
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {

            Calendar newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            editTanggal.setText( dateFormatter.format(newDate.getTime()) );

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();


    }



    public class getDataPengajuanSync extends AsyncTask<String, String, Boolean> {

        List<PengajuanList> listArrayList = new ArrayList<>();
        @Override
        protected Boolean doInBackground(String... params) {

            try {
                String uid = sharedpreferences.getString("uid", "");

                JSONObject j1 = koneksi.getDataServer("/api/pengajuan_list?u="+uid);
                if( j1.getBoolean("success") ) {

                    JSONArray jsonArray = j1.getJSONArray("response");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        listArrayList.add(new PengajuanList(
                                jsonObject.getString("pengajuan_id"),
                                jsonObject.getString("pengajuan_jenis"),
                                jsonObject.getString("pengajuan_keterangan"),
                                jsonObject.getString("pengajuan_tanggal"),
                                jsonObject.getString("pengajuan_tanggal_mulai"),
                                jsonObject.getString("pengajuan_tanggal_selesai"),
                                jsonObject.getString("pengajuan_status")
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

            RecyclerViewAdapterListRecent adapter1 = new RecyclerViewAdapterListRecent(context, listArrayList);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter1);

        }
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }
    }



    public class RecyclerViewAdapterListRecent extends RecyclerView.Adapter<RecyclerViewAdapterListRecent.ItemViewHolder> {

        Context context;
        List<PengajuanList> ujianList;

        public RecyclerViewAdapterListRecent(Context context, List<PengajuanList> ujianList) {
            this.context = context;
            this.ujianList = ujianList;
        }


        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pengajuan_list, parent, false);

            return new ItemViewHolder(itemView, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            PengajuanList item = ujianList.get(position);

            holder.tv1.setText(item.pengajuan_jenis);
            holder.tv2.setText(item.pengajuan_status);
            holder.tv3.setText(item.pengajuan_tanggal_mulai+" - "+item.pengajuan_tanggal_selesai);

        }


        @Override
        public int getItemCount() {
            return ujianList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            private TextView tv1,tv2,tv3;

            public ItemViewHolder(View itemView, int viewType) {
                super(itemView);

                tv1 = itemView.findViewById(R.id.tv1);
                tv2 = itemView.findViewById(R.id.tv2);
                tv3 = itemView.findViewById(R.id.tv3);

            }
        }
    }



    public class setDataPengajuanSync extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                String uid = sharedpreferences.getString("uid", "");

                String jenis = editJenisPengajuan.getText().toString();
                String keterangan = editKeterangan.getText().toString();
                String tanggal_mulai = editTanggalMulai.getText().toString();
                String tanggal_selesai = editTanggalSelesai.getText().toString();


                JSONObject j1 = koneksi.getDataServer(
                        "/api/pengajuan?"+
                                "uid="+uid+
                                "&jenis="+jenis+
                                "&keterangan="+keterangan+
                                "&tanggal_mulai="+tanggal_mulai+
                                "&tanggal_selesai="+tanggal_selesai
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

                sweetAlertDialog = new SweetAlertDialog(MengajukanActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Pengajuan berhasil!")
                        .setContentText("Data berhasil dikirim!");
                sweetAlertDialog.setCancelable(false);
                sweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();



                    new getDataPengajuanSync().execute();

                    lyt_list.setVisibility(View.VISIBLE);
                    lyt_form.setVisibility(View.GONE);

                    actBatal.setVisibility(View.GONE);
                    actCreate.setVisibility(View.VISIBLE);



                });
                sweetAlertDialog.show();

            }else{

                sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Gagal mengirim pengajuan!")
                        .setContentText("Maaf ada kesalahan pengajuan kemungkinan sudah pernah mengajukan atau kemungkinan terjadi gangguan jaringan!!");
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