package com.example.marcin.eventtracer;

import java.io.Serializable;

/**
 * Created by Marcin on 2018-05-07.
 */

public class EventClassItem implements Serializable {
    private int id;
    private String nazwa;
    private String organizator;
    private String miasto;
    private String adres;
    private String dataOd;
    private String dataDo;
    private String opis;
    private String bilety;
    private String social;
    private String dataOutput;

    public EventClassItem(int id, String nazwa, String organizator, String miasto, String adres, String dataOd, String dataDo, String opis, String bilety, String social){
        this.id = id;
        this.nazwa = nazwa;
        this.organizator = organizator;
        this.miasto = miasto;
        this.adres =adres;
        this.dataOd = dataOd;
        this.dataDo = dataDo;
        this.opis = opis;
        this.bilety = bilety;
        this.social = social;
    }

    public int getId() {
        return id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public String getOrganizator() {
        return organizator;
    }

    public String getMiasto() {
        return miasto;
    }

    public String getAdres() {
        return adres;
    }

    public String getDataOd() {
        return dataOd;
    }

    public String getDataDo() {
        return dataDo;
    }

    public String getOpis() {
        return opis;
    }

    public String getBilety() {
        return bilety;
    }

    public String getSocial() {
        return social;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public void setOrganizator(String organizator) {
        this.organizator = organizator;
    }

    public void setMiasto(String miasto) {
        this.miasto = miasto;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public void setDataOd(String dataOd) {
        this.dataOd = dataOd;
    }

    public void setDataDo(String dataDo) {
        this.dataDo = dataDo;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public void setBilety(String bilety) {
        this.bilety = bilety;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public String getDataOutput(){
        return dataOutput;
    }

    public void setDataOutput(String dataOutput){
        this.dataOutput = dataOutput;
    }

}
