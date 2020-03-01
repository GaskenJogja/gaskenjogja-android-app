package id.herocode.gaskenjogja.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.AppUtils;
import id.herocode.gaskenjogja.utils.HttpRequest;
import id.herocode.gaskenjogja.utils.ParseContent;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText inp_nama, inp_email, inp_password;
    private ParseContent parseContent;
    private final int RegTask = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView img_main = findViewById(R.id.img_main);
        Glide
                .with(this)
                .load(R.drawable.bg_wisata_jogja)
                .fitCenter()
                .into(img_main);

        PreferenceHelper preferenceHelper = new PreferenceHelper(this);
        parseContent = new ParseContent(this);

        inp_nama = findViewById(R.id.inp_nama);
        inp_email = findViewById(R.id.inp_email);
        inp_password = findViewById(R.id.inp_password);

        Button btn_daftar = findViewById(R.id.btn_daftar);
        final Button btn_pindah_login = findViewById(R.id.btn_pindah_masuk);

        btn_pindah_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                RegisterActivity.this.finish();
            }
        });

        btn_daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    register();
                } catch (IOException e) {
                    System.out.print("Error 01.0: "); e.printStackTrace();
                } catch (JSONException e) {
                    System.out.print("Error 01.1: "); e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void register() throws IOException, JSONException {
        if (!AppUtils.isNetworkAvailable(RegisterActivity.this)) {
            Toast.makeText(RegisterActivity.this, "Internet is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        AppUtils.showSimpleProgressDialog(RegisterActivity.this);
        final HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.Params.NAME, inp_nama.getText().toString());
        map.put(AppConstants.Params.EMAIL, inp_email.getText().toString());
        map.put(AppConstants.Params.PASSWORD, inp_password.getText().toString());
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void[] params) {
                String response;
                try {
                    HttpRequest request = new HttpRequest(AppConstants.ServiceType.REGISTER);
                    response = request.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                } catch (Exception e) {
                    response = e.getMessage();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String result) {
                onTaskCompleted(result, RegTask);
            }
        }.execute();
    }

    private void onTaskCompleted(String response, int task) {
        AppUtils.removeSimpleProgressDialog();
        if (task == RegTask) {
            if (parseContent.isSuccess(response)) {
                parseContent.saveInfo(response);
                Toast.makeText(RegisterActivity.this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                this.finish();
            } else {
                Toast.makeText(RegisterActivity.this, parseContent.getErrorMessage(response), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
