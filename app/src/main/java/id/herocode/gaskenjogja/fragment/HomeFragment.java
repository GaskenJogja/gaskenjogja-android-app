package id.herocode.gaskenjogja.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import id.herocode.gaskenjogja.activity.DaftarWisata;
import id.herocode.gaskenjogja.activity.DashboardActivity;
import id.herocode.gaskenjogja.activity.DetailDataWisata;
import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.activity.QRCodeScannerActivity;
import id.herocode.gaskenjogja.activity.WisataTerdekat;
import id.herocode.gaskenjogja.adapter.CardViewWisataAdapter;
import id.herocode.gaskenjogja.iface.FragmentInteraction;
import id.herocode.gaskenjogja.iface.LokasiInteraction;
import id.herocode.gaskenjogja.model.ModelWisata;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.AppUtils;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class HomeFragment extends Fragment implements FragmentInteraction, LokasiInteraction {
    private FragmentActivity listener;
    private FragmentInteraction intListener;
    private LokasiInteraction lokInteraction;
    private PreferenceHelper preferenceHelper;
    private LinearLayoutManager layoutManager;

    private CardViewWisataAdapter wisataAdapter;
    private static final String API_DATA_WISATA = AppConstants.ServiceType.DATA_WISATA;
    private static final String API_DATA_WISATA_SORT = AppConstants.ServiceType.DATA_WISATA + "?sort=true";
    private RecyclerView recyclerView;
    private List<ModelWisata> modelWisatas;
    private SwipeRefreshLayout swipe;
    private LinearLayout menu_isisaldo, menu_wisata, menu_scanqrcode, menu_wisatadekat;
    private TextView tv_username, tv_email, tv_saldo;
    private String name, email, saldo;
    private ImageView img_refresh;
    private RelativeLayout refreshSaldo;

    public static HomeFragment newInstance(String text) {
        Bundle args = new Bundle();
        args.putString("text", text);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.listener = (FragmentActivity) context;
            this.intListener = (FragmentInteraction) listener;
            this.lokInteraction = (LokasiInteraction) listener;
            preferenceHelper = new PreferenceHelper(listener);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = String.valueOf(preferenceHelper.getName());
        email = String.valueOf(preferenceHelper.getEmail());
        saldo = String.valueOf(preferenceHelper.getSaldo());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        recyclerView = view.findViewById(R.id.rv_data_wisata);
        swipe = view.findViewById(R.id.swipe_data_wisata);
        menu_isisaldo = view.findViewById(R.id.menu_saldo);
        menu_wisata = view.findViewById(R.id.menu_wisata_jogja);
        menu_scanqrcode = view.findViewById(R.id.menu_scan_qrcode);
        menu_wisatadekat = view.findViewById(R.id.menu_tourguide);
        tv_username = view.findViewById(R.id.tv_username);
        tv_email = view.findViewById(R.id.tv_emailaddress);
        tv_saldo = view.findViewById(R.id.tv_saldo);
        refreshSaldo = view.findViewById(R.id.btn_refresh);
        img_refresh = view.findViewById(R.id.img_refresh);

        refreshSaldo.setVisibility(View.GONE);

        tv_username.setText("Hai " + name);
        tv_email.setText(email);
        tv_saldo.setText("Rp. " + saldo + ",-");

        modelWisatas = new ArrayList<>();
        wisataAdapter = new CardViewWisataAdapter(listener, modelWisatas);
        layoutManager = new LinearLayoutManager(listener);
        layoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(wisataAdapter);
//        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        loadDataWisata();

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataWisata();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (swipe.isShown()) swipe.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        menu_isisaldo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pindahFragment(2);
            }
        });

        menu_wisata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(listener, DaftarWisata.class);
                startActivity(intent);
            }
        });

        menu_scanqrcode.setOnClickListener(v -> {
            Intent intent = new Intent(listener, QRCodeScannerActivity.class);
            startActivity(intent);
        });

        menu_wisatadekat.setOnClickListener(v -> {
            if (preferenceHelper.getLAT().equals("0") || preferenceHelper.getLON().equals("0")) {
                final AlertDialog.Builder builder =
                        new AlertDialog.Builder(listener);
                final String message = "Anda belum mengupdate lokasi terkini,\n" +
                        "Silahkan update lokasi GPS anda terlebih dahulu!";

                builder.setMessage(message)
                        .setPositiveButton("UPDATE",
                                (d, id) -> {
                                    d.dismiss();
                                    lokInteraction.updateLokasi();
                                })
                        .setNegativeButton("BATAL",
                                (dialog, which) -> dialog.dismiss());
                builder.create().show();
                AppUtils.showSimpleProgressDialog(listener, "Update Lokasi", "Loading...", false);
                lokInteraction.updateLokasi();
                AppUtils.removeSimpleProgressDialog();
            } else if (!preferenceHelper.getLAT().equals("0") && !preferenceHelper.getLON().equals("0")) {
                lokInteraction.updateLokasi();
                Intent intent = new Intent(listener, WisataTerdekat.class);
                startActivity(intent);
            }
        });

        refreshSaldo.setOnClickListener(v -> updateSaldo());
    }

    public void update() {
//        final RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotate.setDuration(200);
//        rotate.setInterpolator(new LinearInterpolator());
        saldo = String.valueOf(preferenceHelper.getSaldo());
        tv_saldo.setText(String.format("Rp. %s,-", saldo));
//        img_refresh.startAnimation(rotate);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                tv_saldo.setText(String.format("Rp. %s,-", saldo));
//                img_refresh.startAnimation(rotate);
//            }
//        }, 500);
    }

    @Override
    public void onStart() {
        super.onStart();
        name = String.valueOf(preferenceHelper.getName());
        email = String.valueOf(preferenceHelper.getEmail());
        saldo = String.valueOf(preferenceHelper.getSaldo());
        tv_username.setText("Hai " + name);
        tv_email.setText(email);
        tv_saldo.setText("Rp. " + saldo + ",-");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        updateSaldo();
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void loadDataWisata() {
        intListener.updateSaldo();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, API_DATA_WISATA_SORT,
                response -> {
                    try {
                        modelWisatas.clear();
                        JSONObject obj = new JSONObject(response);
                        JSONArray array = obj.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject wisata = array.getJSONObject(i);
                            modelWisatas.add(new ModelWisata(
                                    wisata.getInt("id_wisata"),
                                    wisata.getString("nama_wisata"),
                                    wisata.getString("alamat"),
                                    wisata.getString("jam_buka"),
                                    wisata.getString("jam_tutup"),
                                    wisata.getInt("harga"),
                                    wisata.getString("gambar"),
                                    wisata.getDouble("lat"),
                                    wisata.getDouble("lon")
                            ));
                        }
                        wisataAdapter = new CardViewWisataAdapter(listener, modelWisatas);
                        recyclerView.setAdapter(wisataAdapter);

                        wisataAdapter.setClickItem(this::showDataWisata);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {

                }
        );
        Volley.newRequestQueue(listener).add(stringRequest);
    }

    private void showDataWisata(ModelWisata modelWisata) {
        Intent moveData = new Intent(listener, DetailDataWisata.class);
        moveData.putExtra(DetailDataWisata.ID_WISATA, modelWisata.getID_WISATA());
        moveData.putExtra(DetailDataWisata.NAMA_WISATA, modelWisata.getNAMA_WISATA());
        moveData.putExtra(DetailDataWisata.ALAMAT_WISATA, modelWisata.getALAMAT_WISATA());
        moveData.putExtra(DetailDataWisata.JAM_BUKA, modelWisata.getJAM_BUKA());
        moveData.putExtra(DetailDataWisata.JAM_TUTUP, modelWisata.getJAM_TUTUP());
        moveData.putExtra(DetailDataWisata.HTM_WISATA, modelWisata.getHARGA_WISATA());
        moveData.putExtra(DetailDataWisata.IMG_WISATA, modelWisata.getIMG_WISATA());
        moveData.putExtra(DetailDataWisata.LAT, modelWisata.getLAT());
        moveData.putExtra(DetailDataWisata.LON, modelWisata.getLON());
        startActivity(moveData);
    }


    @Override
    public void pindahFragment(int id) {
        intListener.pindahFragment(id);
    }

    @Override
    public void updateSaldo() {
        update();
    }

    @Override
    public void updateLokasi() {
        lokInteraction.updateLokasi();
    }
}