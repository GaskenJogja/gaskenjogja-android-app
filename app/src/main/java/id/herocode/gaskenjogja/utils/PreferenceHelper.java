package id.herocode.gaskenjogja.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.sql.Blob;

public class PreferenceHelper {
    private final String ID_USER = "id_user";
    private final String INTRO = "intro";
    private final String NAME = "name";
    private final String EMAIL = "email";
    private final String SALDO = "saldo";
    private final String LAT = "lat";
    private final String LON = "lon";
    private final String WISATA_NAME = "nama_wisata";
    private final String WISATA_IMG = "gambar";
    private final String WISATA_HARGA = "harga";
    private final String WISATA_ALAMAT = "alamat";
    private SharedPreferences gaskenjogja_prefs;
    private Context context;

    public PreferenceHelper(Context context) {
        gaskenjogja_prefs = context.getSharedPreferences("shared",
                Context.MODE_PRIVATE);
        this.context = context;
    }

    public void putIsLogin(boolean loginorout) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putBoolean(INTRO, loginorout);
        edit.apply();
    }

    public boolean getIsLogin() {
        return gaskenjogja_prefs.getBoolean(INTRO, false);
    }

    public void putName(String loginorout) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putString(NAME, loginorout);
        edit.apply();
    }

    public String getName() {
        return gaskenjogja_prefs.getString(NAME, "");
    }

    public void putEmail(String loginorout) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putString(EMAIL, loginorout);
        edit.apply();
    }

    public String getEmail() {
        return gaskenjogja_prefs.getString(EMAIL, "");
    }

    public void putId_user(int loginorout) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putInt(ID_USER, loginorout);
        edit.apply();
    }

    public int getId_user() { return gaskenjogja_prefs.getInt(ID_USER, 0); }

    public void putSaldo(int loginorout) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putInt(SALDO, loginorout);
        edit.apply();
    }

    public int getSaldo() { return gaskenjogja_prefs.getInt(SALDO, 0); }


    public void putLAT(String lattitude) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putString(LAT, lattitude);
        edit.apply();
    }

    public String getLAT() { return gaskenjogja_prefs.getString(LAT,"0"); }

    public void putLON(String longtitude) {
        SharedPreferences.Editor edit = gaskenjogja_prefs.edit();
        edit.putString(LON, longtitude);
        edit.apply();
    }

    public String getLON() { return gaskenjogja_prefs.getString(LON,"0"); }



}
