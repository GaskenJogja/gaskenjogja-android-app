package id.herocode.gaskenjogja.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import id.herocode.gaskenjogja.activity.LoginActivity;
import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class AkunFragment extends Fragment {

    private FragmentActivity listener;
    private PreferenceHelper preferenceHelper;

    public static AkunFragment newInstance(String nama, String email) {
        Bundle args = new Bundle();
        args.putString("nama", nama);
        args.putString("email", email);
        AkunFragment fragment = new AkunFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.listener = (FragmentActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceHelper = new PreferenceHelper(listener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_akun, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvNama = view.findViewById(R.id.tv_akun_username);
        TextView tvEmail = view.findViewById(R.id.tv_akun_email);
        tvNama.setText(preferenceHelper.getName());
        tvEmail.setText(preferenceHelper.getEmail());
        Button btnKeluar = view.findViewById(R.id.btn_keluar);
        btnKeluar.setOnClickListener(v -> {
            preferenceHelper.putIsLogin(false);
            Intent intent = new Intent(listener, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            listener.finish();
        });
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
}