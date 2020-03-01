package id.herocode.gaskenjogja.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.activity.LoginActivity;
import id.herocode.gaskenjogja.iface.FragmentInteraction;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.AppUtils;
import id.herocode.gaskenjogja.utils.HttpRequest;
import id.herocode.gaskenjogja.utils.ParseContent;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class SaldoFragment extends Fragment implements FragmentInteraction {

    private FragmentActivity listener;
    private FragmentInteraction intListener;
    private PreferenceHelper preferenceHelper;
    private ParseContent parseContent;
    private static final String API_UPDATE_SALDO = AppConstants.ServiceType.UPDATE_SALDO;

    private TextView tvSaldo, tvInfoSaldo;
    private Button btnTambah, btnKembali, btnTopup;
    private LinearLayout radio_button;
    private RadioGroup rg_nominal;
    private EditText etNominalSaldo;
    private String saldo;

    public static SaldoFragment newInstance() {
        Bundle args = new Bundle();
        SaldoFragment fragment = new SaldoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            this.listener = (FragmentActivity) context;
            this.intListener = (FragmentInteraction) listener;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saldo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceHelper = new PreferenceHelper(listener);
        parseContent = new ParseContent(listener);

        tvSaldo = view.findViewById(R.id.tv_saldo);
        tvInfoSaldo = view.findViewById(R.id.tv_info_saldo);
        btnTambah = view.findViewById(R.id.btn_tambah_saldo);
        btnKembali = view.findViewById(R.id.btn_batal);
        btnTopup = view.findViewById(R.id.btn_topup_saldo);
        etNominalSaldo = view.findViewById(R.id.et_tambah_saldo);
        radio_button = view.findViewById(R.id.radio_nominal);
        rg_nominal = view.findViewById(R.id.rg_nominal);

        saldo = String.valueOf(preferenceHelper.getSaldo());
        tvSaldo.setText(String.format("Rp. %s,-", saldo));

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) { // Accept only digits ; otherwise just return
                        Toast.makeText(listener,"Invalid Input",Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }
        };
        etNominalSaldo.setFilters(new InputFilter[] { filter });

        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnKembali.setVisibility(View.VISIBLE);
                tvInfoSaldo.setVisibility(View.INVISIBLE);
                tvSaldo.setVisibility(View.INVISIBLE);
                etNominalSaldo.setVisibility(View.VISIBLE);
                btnTopup.setVisibility(View.VISIBLE);
                btnTambah.setVisibility(View.GONE);
            }
        });

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnKembali.setVisibility(View.GONE);
                tvInfoSaldo.setVisibility(View.VISIBLE);
                tvSaldo.setVisibility(View.VISIBLE);
                etNominalSaldo.setText(""); etNominalSaldo.setVisibility(View.INVISIBLE);
                btnKembali.setVisibility(View.GONE);
                btnTopup.setVisibility(View.GONE);
                btnTambah.setVisibility(View.VISIBLE);
            }
        });

        btnTopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((etNominalSaldo.getText().toString()).isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        etNominalSaldo.setFocusable(View.FOCUSABLE);
                    }
                    Toast.makeText(listener, "Masukkan Jumlah Saldo yang ingin di TopUp", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        topUp();
                        btnKembali.performClick();
                    } catch (IOException e) {
                        System.out.print("Error 001: "); e.printStackTrace();
                    } catch (JSONException e) {
                        System.out.print("Error 002: "); e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        updateSaldo();
        update();
        super.onStart();
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

    public void update() {
        saldo = String.valueOf(preferenceHelper.getSaldo());
        tvSaldo.setText(String.format("Rp. %s,-", saldo));
    }


    @SuppressLint("StaticFieldLeak")
    private void topUp() throws IOException, JSONException {
        if (!AppUtils.isNetworkAvailable(listener)) {
            Toast.makeText(listener, "Internet is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        AppUtils.showDialogTopup(listener);
        final HashMap<String,String> map = new HashMap<>();
        map.put(AppConstants.Params.ID_USER, String.valueOf(preferenceHelper.getId_user()));
        map.put(AppConstants.Params.SALDO, String.valueOf(preferenceHelper.getSaldo()));
        map.put(AppConstants.Params.TOPUP, etNominalSaldo.getText().toString());
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response;
                try {
                    HttpRequest req = new HttpRequest(API_UPDATE_SALDO);
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
            parseContent.bayarWisata(response);
            intListener.updateSaldo();
//            saldo = String.valueOf(preferenceHelper.getSaldo());
            String msg = parseContent.getMessageBayar(response);
//            tvSaldo.setText(String.format("Rp. %s,-", saldo));
            Toast.makeText(listener, msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(listener, "Anda harus login terlebih dahulu!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(listener, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            listener.finish();
        }
        AppUtils.removeSimpleProgressDialog();
    }

    @Override
    public void pindahFragment(int id) {
        intListener.pindahFragment(id);
    }

    @Override
    public void updateSaldo() {
        update();

    }
}