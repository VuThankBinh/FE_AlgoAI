package com.example.nckh.model;

public class CauHoi {
    private int id;
    private int idBaiHoc;
    private String cauHoi;
    private String luaChonA;
    private String luaChonB;
    private String luaChonC;
    private String luaChonD;
    private String dapAnDung;
    private String mucDo;

    public CauHoi(int id, int idBaiHoc, String cauHoi, String luaChonA, String luaChonB, 
                 String luaChonC, String luaChonD, String dapAnDung, String mucDo) {
        this.id = id;
        this.idBaiHoc = idBaiHoc;
        this.cauHoi = cauHoi;
        this.luaChonA = luaChonA;
        this.luaChonB = luaChonB;
        this.luaChonC = luaChonC;
        this.luaChonD = luaChonD;
        this.dapAnDung = dapAnDung;
        this.mucDo = mucDo;
    }

    public int getId() {
        return id;
    }

    public int getIdBaiHoc() {
        return idBaiHoc;
    }

    public String getCauHoi() {
        return cauHoi;
    }

    public String getLuaChonA() {
        return luaChonA;
    }

    public String getLuaChonB() {
        return luaChonB;
    }

    public String getLuaChonC() {
        return luaChonC;
    }

    public String getLuaChonD() {
        return luaChonD;
    }

    public String getDapAnDung() {
        return dapAnDung;
    }

    public String getMucDo() {
        return mucDo;
    }
} 