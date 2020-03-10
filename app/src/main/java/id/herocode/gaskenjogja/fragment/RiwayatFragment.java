package id.herocode.gaskenjogja.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.activity.LoginActivity;
import id.herocode.gaskenjogja.adapter.TransaksiAdapter;
import id.herocode.gaskenjogja.iface.TransaksiInteraction;
import id.herocode.gaskenjogja.model.Transaksi;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.HttpRequest;
import id.herocode.gaskenjogja.utils.ParseContent;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class RiwayatFragment extends Fragment implements TransaksiInteraction {

    private FragmentActivity listener;
    private PreferenceHelper preferenceHelper;
    private static final String API_TRANSAKSI_WISATA = AppConstants.ServiceType.TRANSAKSI;
    private RecyclerView recyclerView;
    private LinearLayoutManager ll;

    public static RiwayatFragment newInstance() {
        Bundle args = new Bundle();
        RiwayatFragment fragment = new RiwayatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.listener = (FragmentActivity) context;
            this.preferenceHelper = new PreferenceHelper(listener);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.recyclerView = listener.findViewById(R.id.rv_data_transaksi);
        this.ll = new LinearLayoutManager(listener);
        loadDataTransaksi();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadDataTransaksi() {
        final HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.Params.ID_USER, String.valueOf(preferenceHelper.getId_user()));
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response;
                try {
                    HttpRequest req = new HttpRequest(API_TRANSAKSI_WISATA);
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                } catch (Exception e) {
                    response = e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String response) {
                Boolean isLogin = preferenceHelper.getIsLogin();
                onTaskCompleted(response, isLogin);
            }
        }.execute();
    }

    private void onTaskCompleted(String response, Boolean task) {
        if (task) {
            String msg = new ParseContent(listener).getErrorMessage(response);
            if (msg.equals("tampil_riwayat")) {
                try {
                    List<Transaksi> transaksiList = new ArrayList<>();
                    JSONObject obj = new JSONObject(response);
                    JSONArray array = obj.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject tr = array.getJSONObject(i);
                        transaksiList.add(new Transaksi(
                                tr.getInt("id_transaksi"),
                                tr.getString("tanggal_transaksi"),
                                tr.getString("status_transaksi"),
                                tr.getString("pilih_metode"),
                                tr.getString("nama_wisata")
                        ));
                    }
                    recyclerView.setLayoutManager(ll);
                    recyclerView.addItemDecoration(new DividerItemDecoration(listener, RecyclerView.VERTICAL));
                    TransaksiAdapter transaksiAdapter = new TransaksiAdapter(listener, transaksiList);
                    recyclerView.setAdapter(transaksiAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (msg.equals("tidak_ada_riwayat")) {
                Toast.makeText(listener, "Tidak ada riwayat pembelian!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(listener, "Gagal Memuat Data Transaksi!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(listener, "Anda harus login terlebih dahulu!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(listener, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            listener.finish();
        }
    }

    @Override
    public void updateTransaksi() {
        loadDataTransaksi();
    }
}