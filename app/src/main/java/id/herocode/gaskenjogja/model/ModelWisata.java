package id.herocode.gaskenjogja.model;

import android.util.Base64;

public class ModelWisata {

    private int ID_WISATA;
    private String NAMA_WISATA;
    private String ALAMAT_WISATA;
    private String JAM_BUKA, JAM_TUTUP;
    private int HARGA_WISATA;
    private byte[] IMG_WISATA;
    private Double LAT, LON;
    private boolean expanded = false;

    public ModelWisata(int ID_WISATA, String NAMA_WISATA, String ALAMAT_WISATA, String JAM_BUKA, String JAM_TUTUP, int HARGA_WISATA, String IMG_WISATA, Double lat, Double lon) {
        this.ID_WISATA = ID_WISATA;
        this.NAMA_WISATA = NAMA_WISATA;
        this.ALAMAT_WISATA = ALAMAT_WISATA;
        this.JAM_BUKA = JAM_BUKA;
        this.JAM_TUTUP = JAM_TUTUP;
        this.HARGA_WISATA = HARGA_WISATA;
        this.IMG_WISATA = Base64.decode(IMG_WISATA, Base64.DEFAULT);
        this.LAT = lat;
        this.LON = lon;
    }

    public String getNAMA_WISATA() {
        return NAMA_WISATA;
    }

    public void setNAMA_WISATA(String NAMA_WISATA) {
        this.NAMA_WISATA = NAMA_WISATA;
    }

    public String getALAMAT_WISATA() {
        return ALAMAT_WISATA;
    }

    public String getJAM_BUKA() {
        return JAM_BUKA;
    }

    public void setJAM_BUKA(String JAM_BUKA) {
        this.JAM_BUKA = JAM_BUKA;
    }

    public String getJAM_TUTUP() {
        return JAM_TUTUP;
    }

    public void setJAM_TUTUP(String JAM_TUTUP) {
        this.JAM_TUTUP = JAM_TUTUP;
    }

    public void setALAMAT_WISATA(String ALAMAT_WISATA) {
        this.ALAMAT_WISATA = ALAMAT_WISATA;
    }

    public int getHARGA_WISATA() {
        return HARGA_WISATA;
    }

    public void setHARGA_WISATA(int HARGA_WISATA) {
        this.HARGA_WISATA = HARGA_WISATA;
    }

    public byte[] getIMG_WISATA() {
        return IMG_WISATA;
    }

    public void setIMG_WISATA(String IMG_WISATA) {
        this.IMG_WISATA = Base64.decode(IMG_WISATA, Base64.DEFAULT);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public Double getLAT() {
        return LAT;
    }

    public void setLAT(Double LAT) {
        this.LAT = LAT;
    }

    public Double getLON() {
        return LON;
    }

    public void setLON(Double LON) {
        this.LON = LON;
    }

    public int getID_WISATA() {
        return ID_WISATA;
    }

    public void setID_WISATA(int ID_WISATA) {
        this.ID_WISATA = ID_WISATA;
    }
}
