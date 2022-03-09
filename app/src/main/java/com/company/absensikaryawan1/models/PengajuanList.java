package com.company.absensikaryawan1.models;

public class PengajuanList {

    public String pengajuan_id ;
    public String pengajuan_jenis;
    public String pengajuan_keterangan;
    public String pengajuan_tanggal;
    public String pengajuan_tanggal_mulai;
    public String pengajuan_tanggal_selesai;
    public String pengajuan_status;

    public PengajuanList(){}
    public PengajuanList(String pengajuan_id ,
                         String pengajuan_jenis,
                         String pengajuan_keterangan,
                         String pengajuan_tanggal,
                         String pengajuan_tanggal_mulai,
                         String pengajuan_tanggal_selesai,
                         String pengajuan_status){

        this.pengajuan_id  = pengajuan_id ;
        this.pengajuan_jenis = pengajuan_jenis;
        this.pengajuan_keterangan = pengajuan_keterangan;
        this.pengajuan_tanggal = pengajuan_tanggal;
        this.pengajuan_tanggal_mulai = pengajuan_tanggal_mulai;
        this.pengajuan_tanggal_selesai = pengajuan_tanggal_selesai;
        this.pengajuan_status = pengajuan_status;

    }
}
