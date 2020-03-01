package id.herocode.gaskenjogja.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    private EditText inp_email, inp_password;
    private ParseContent parseContent;
    private final int LoginTask = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageView img_main = findViewById(R.id.img_main);
        Glide
                .with(this)
                .load(R.drawable.bg_wisata_jogja)
                .fitCenter()
                .into(img_main);

        parseContent = new ParseContent(this);
        PreferenceHelper preferenceHelper = new PreferenceHelper(this);

        if (preferenceHelper.getIsLogin()) {
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            this.finish();
        }

        inp_email = findViewById(R.id.inp_email);
        inp_password = findViewById(R.id.inp_password);

        Button btn_masuk = findViewById(R.id.btn_masuk);
        Button btn_pindah_daftar = findViewById(R.id.btn_pindah_daftar);

        btn_pindah_daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                LoginActivity.this.finish();
            }
        });

        btn_masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (IOException e) {
                    System.out.print("Error 001: "); e.printStackTrace();
                } catch (JSONException e) {
                    System.out.print("Error 002: "); e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void login() throws IOException, JSONException {
        if (!AppUtils.isNetworkAvailable(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Internet is required!", Toast.LENGTH_SHORT).show();
            return;
        }
        AppUtils.showSimpleProgressDialog(LoginActivity.this);
        final HashMap<String, String> map = new HashMap<>();
        map.put(AppConstants.Params.EMAIL, inp_email.getText().toString());
        map.put(AppConstants.Params.PASSWORD, inp_password.getText().toString());
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void[] params) {
                String response;
                try {
                    HttpRequest req = new HttpRequest(AppConstants.ServiceType.LOGIN);
                    response = req.prepare(HttpRequest.Method.POST).withData(map).sendAndReadString();
                } catch (Exception e) {
                    response = e.getMessage();
                }
                return response;
            }
            protected void onPostExecute(String result) {
                onTaskCompleted(result,LoginTask);
            }
        }.execute();
    }

    private void onTaskCompleted(String response, int task) {
        Log.d("responsejson", response);
        AppUtils.removeSimpleProgressDialog();
        if (task == LoginTask) {
            if (parseContent.isSuccess(response)) {
                parseContent.saveInfo(response);
                Toast.makeText(LoginActivity.this, "Berhasil Login!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                this.finish();
            } else {
                Toast.makeText(LoginActivity.this, parseContent.getErrorMessage(response), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
