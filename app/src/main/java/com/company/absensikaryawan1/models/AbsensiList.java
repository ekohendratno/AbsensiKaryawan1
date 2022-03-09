package com.company.absensikaryawan1.models;

public class AbsensiList {
    public String absensi_id ;
    public String absensi_tanggal;
    public String absensi_jam;
    public String absensi_tanggal_terbaru;
    public String absensi_keterangan;
    public String absensi_catatan;
    public String absensi_lokasi_lat;
    public String absensi_lokasi_long;
    public String absensi_lokasi_alamat;
    public String absensi_foto;

    public AbsensiList(){}
    public AbsensiList(String absensi_id ,
                       String absensi_tanggal,
                       String absensi_jam,
                       String absensi_tanggal_terbaru,
                       String absensi_keterangan,
                       String absensi_catatan,
                       String absensi_lokasi_lat,
                       String absensi_lokasi_long,
                       String absensi_lokasi_alamat,
                       String absensi_foto){

        this.absensi_id  = absensi_id ;
        this.absensi_tanggal = absensi_tanggal;
        this.absensi_jam = absensi_jam;
        this.absensi_tanggal_terbaru = absensi_tanggal_terbaru;
        this.absensi_keterangan = absensi_keterangan;
        this.absensi_catatan = absensi_catatan;
        this.absensi_lokasi_lat = absensi_lokasi_lat;
        this.absensi_lokasi_long = absensi_lokasi_long;
        this.absensi_lokasi_alamat = absensi_lokasi_alamat;
        this.absensi_foto = absensi_foto;

    }
}
