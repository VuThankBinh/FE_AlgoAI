package com.example.nckh.model;

public class BaiHoc {
    private String id;
    private String tieuDe;
    private String noiDung;
    private String linkYoutube;
    private String linkMoTa;
    private String trangThai;
    private String anhBaiHoc;
    private boolean daLamQuiz;
    private Integer diemQuiz;
    private String mucDoQuiz;
    private boolean daLamCode;
    private String diemCode;
    private String mucDoCode;

    public BaiHoc(String id, String tieuDe, String noiDung, String linkYoutube, String linkMoTa, String trangThai, String anhBaiHoc) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.linkYoutube = linkYoutube;
        this.linkMoTa = linkMoTa;
        this.trangThai = trangThai;
        this.anhBaiHoc = anhBaiHoc;

    }

    public BaiHoc(String id, String tieuDe, String noiDung, String linkYoutube, String linkMoTa, String trangThai, String anhBaiHoc, boolean daLamQuiz, Integer diemQuiz, String mucDoQuiz, boolean daLamCode, String diemCode, String mucDoCode) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.linkYoutube = linkYoutube;
        this.linkMoTa = linkMoTa;
        this.trangThai = trangThai;
        this.anhBaiHoc = anhBaiHoc;
        this.daLamQuiz = daLamQuiz;
        this.diemQuiz = diemQuiz;
        this.mucDoQuiz = mucDoQuiz;
        this.daLamCode = daLamCode;
        this.diemCode = diemCode;
        this.mucDoCode = mucDoCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLinkYoutube() {
        return linkYoutube;
    }

    public void setLinkYoutube(String linkYoutube) {
        this.linkYoutube = linkYoutube;
    }

    public String getLinkMoTa() {
        return linkMoTa;
    }

    public void setLinkMoTa(String linkMoTa) {
        this.linkMoTa = linkMoTa;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getAnhBaiHoc() {
        return anhBaiHoc;
    }

    public void setAnhBaiHoc(String anhBaiHoc) {
        this.anhBaiHoc = anhBaiHoc;
    }

    public boolean isDaLamQuiz() {
        return daLamQuiz;
    }

    public void setDaLamQuiz(boolean daLamQuiz) {
        this.daLamQuiz = daLamQuiz;
    }

    public Integer getDiemQuiz() {
        return diemQuiz;
    }

    public void setDiemQuiz(Integer diemQuiz) {
        this.diemQuiz = diemQuiz;
    }

    public String getMucDoQuiz() {
        return mucDoQuiz;
    }

    public void setMucDoQuiz(String mucDoQuiz) {
        this.mucDoQuiz = mucDoQuiz;
    }

    public boolean isDaLamCode() {
        return daLamCode;
    }

    public void setDaLamCode(boolean daLamCode) {
        this.daLamCode = daLamCode;
    }

    public String getDiemCode() {
        return diemCode;
    }

    public void setDiemCode(String diemCode) {
        this.diemCode = diemCode;
    }

    public String getMucDoCode() {
        return mucDoCode;
    }

    public void setMucDoCode(String mucDoCode) {
        this.mucDoCode = mucDoCode;
    }
}