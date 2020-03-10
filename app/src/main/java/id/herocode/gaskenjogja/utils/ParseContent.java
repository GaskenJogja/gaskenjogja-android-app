package id.herocode.gaskenjogja.utils;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseContent {
    private final String KEY_SUCCESS = "status";
    private final String KEY_MSG = "message";
    private final String KEY_DATA = "data";
    private ArrayList<HashMap<String, String>> hashMaps;
    private Activity activity;
    private PreferenceHelper gaskeun_jogja;

    ArrayList<HashMap<String, String>> arrayList;

    public ParseContent(Activity activity) {
        this.activity = activity;
        gaskeun_jogja = new PreferenceHelper(activity);
    }

    public boolean isSuccess(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString(KEY_SUCCESS).equals("true")) {
                return true;
            } else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getMessageQRCode(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString(KEY_MSG).equals("sukses_mode_booking")) {
                return "mode_booking";
            } else if (jsonObject.optString(KEY_MSG).equals("sukses_mode_langsung")) {
                return "mode_langsung";
            } else if (jsonObject.optString(KEY_MSG).equals("gagal_saldo_kurang")) {
                return "saldo_kurang";
            } else if (jsonObject.optString(KEY_MSG).equals("qrcode_not_found")) {
                return "not_found";
            } else if (jsonObject.optString(KEY_MSG).equals("wisata_tutup")) {
                return "tutup";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void bayarWisata(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString(KEY_SUCCESS).equals("true")) {
                Integer saldo = Integer.parseInt(jsonObject.getString(KEY_DATA));
                gaskeun_jogja.putSaldo(saldo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMessageBayar(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString(KEY_MSG).equals("saldo_bertambah")) {
                return "Berhasil Top Up!";
            } else if (jsonObject.getString(KEY_MSG).equals("saldo_berkurang")) {
                return "Berhasil Membayar!";
            } else {
                return "Terjadi galat pada server!";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Mohon maaf, terjadi galat pada server!";
    }

    public String getErrorMessage(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString(KEY_MSG);
        } catch (JSONException e) {
            System.out.print("Error 02: "); e.printStackTrace();
        }
        return "No data";
    }

    public void saveInfo(String response) {
        gaskeun_jogja.putIsLogin(true);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString(KEY_SUCCESS).equals("true")) {
                JSONArray dataArray = jsonObject.getJSONArray(KEY_DATA);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataobj = dataArray.getJSONObject(i);
                    gaskeun_jogja.putName(dataobj.getString(AppConstants.Params.NAME));
                    gaskeun_jogja.putEmail(dataobj.getString(AppConstants.Params.EMAIL));
                    gaskeun_jogja.putId_user(Integer.parseInt(dataobj.getString(AppConstants.Params.ID_USER)));
                    gaskeun_jogja.putSaldo(Integer.parseInt(dataobj.getString(AppConstants.Params.SALDO)));
                }
            }
        } catch (JSONException e) {
            System.out.println("Error 03: "); e.printStackTrace();
        }
    }
}
