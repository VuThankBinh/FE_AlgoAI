package com.example.nckh.model;

public class BaiHoc {
    private String id;
    private String tieuDe;
    private String noiDung;
    private String linkYoutube;
    private String linkMoTa;
    private String trangThai;
    private String anhBaiHoc;

    public BaiHoc(String id, String tieuDe, String noiDung, String linkYoutube, String linkMoTa, String trangThai, String anhBaiHoc) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.linkYoutube = linkYoutube;
        this.linkMoTa = linkMoTa;
        this.trangThai = trangThai;
        this.anhBaiHoc = anhBaiHoc;
    }

    public String getId() {
        return id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getLinkYoutube() {
        return linkYoutube;
    }

    public String getLinkMoTa() {
        return linkMoTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getAnhBaiHoc() {
        return anhBaiHoc;
    }
} 