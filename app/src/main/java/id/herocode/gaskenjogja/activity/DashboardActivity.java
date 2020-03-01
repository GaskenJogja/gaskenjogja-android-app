package id.herocode.gaskenjogja.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.fragment.AkunFragment;
import id.herocode.gaskenjogja.fragment.HomeFragment;
import id.herocode.gaskenjogja.fragment.RiwayatFragment;
import id.herocode.gaskenjogja.fragment.SaldoFragment;
import id.herocode.gaskenjogja.iface.FragmentInteraction;
import id.herocode.gaskenjogja.iface.LokasiInteraction;
import id.herocode.gaskenjogja.iface.TransaksiInteraction;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.AppUtils;
import id.herocode.gaskenjogja.utils.HttpRequest;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class DashboardActivity extends AppCompatActivity implements FragmentInteraction, TransaksiInteraction, LokasiInteraction {

    private static LocationManager locationManager;

    private HomeFragment homeFragment;
    private RiwayatFragment riwayatFragment;
    private SaldoFragment saldoFragment;
    private AkunFragment akunFragment;
    private BottomNavigationView bottomNav;
    private final static String API_SALDO = AppConstants.ServiceType.UPDATE_SALDO;
    private PreferenceHelper preferenceHelper;
    private ActionMenuItemView item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        preferenceHelper = new PreferenceHelper(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.newInstance("Home");
            riwayatFragment = RiwayatFragment.newInstance();
            saldoFragment = SaldoFragment.newInstance();
            akunFragment = AkunFragment.newInstance(preferenceHelper.getName(), preferenceHelper.getEmail());
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        item = findViewById(R.id.akses_lokasi);

        bottomNav = findViewById(R.id.bottom_navigation_menu);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        addAllFragment();

        if (getIntent().getIntExtra("openFragment",9) != 9) {
            int fragment = getIntent().getIntExtra("openFragment",0);
            pindahFragment(fragment);
        }

        if (getIntent().getBooleanExtra("updateSaldo", false)) {
            updateSaldo();
        }

        if (getIntent().getBooleanExtra("updateTransaksi", false)) {
            updateTransaksi();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_topbar, menu);
        return true;
    }

    @Override
    public void updateLokasi() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayPromptForEnablingGPS(this);
        } else {
            if (!AppUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "Internet is required!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
//                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0F, locationListener);
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
                this.item.setVisibility(View.GONE);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.akses_lokasi) {
            updateLokasi();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void displayPromptForEnablingGPS(final Activity activity)
    {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "GPS belum dinyalakan, silahkan\n" +
                               "nyalakan GPS anda terlebih dahulu";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int id) {
                                activity.startActivity(new Intent(action));
                                d.dismiss();
                            }
                        });
        builder.create().show();
    }

    private LocationListener locationListener = new LocationListener() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void onLocationChanged(Location location) {
            preferenceHelper.putLON(String.valueOf(location.getLongitude()));
            preferenceHelper.putLAT(String.valueOf(location.getLatitude()));
            try {
                final HashMap<String, String> map = new HashMap<>();
                map.put(AppConstants.Params.ID_USER, String.valueOf(preferenceHelper.getId_user()));
                map.put("lat", String.valueOf(location.getLatitude()));
                map.put("lon", String.valueOf(location.getLongitude()));

                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        String data;
                        JSONObject response;
                        try {
                            HttpRequest request = new HttpRequest(AppConstants.ServiceType.LOKASI);
                            response = request.prepare(HttpRequest.Method.POST).withData(map).sendAndReadJSON();
                            data = response.getString("status");
                        } catch (Exception e) {
                            data = e.getMessage();
                        }
                        return data;
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s.equals("true")) {
                            Toast.makeText(DashboardActivity.this, "Lokasi Berhasil Diupdate!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DashboardActivity.this, "Gagal Mengupdate Lokasi!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();

                item.setVisibility(View.VISIBLE);
//                Toast.makeText(DashboardActivity.this, "Lokasi Tersimpan!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            System.out.println(location.getLatitude());
//            System.out.println(location.getLongitude());
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
        @Override
        public void onProviderEnabled(String provider) {

        }
        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferenceHelper.putLAT("0");
        preferenceHelper.putLON("0");
        try {
            trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void updateSaldo() {
        String URL = API_SALDO + "?id=" + preferenceHelper.getId_user();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject arr = new JSONObject(response);
                            String sSaldo = arr.getString(AppConstants.Params.SALDO);
                            int saldo = Integer.parseInt(sSaldo);
                            preferenceHelper.putSaldo(saldo);
                            if (saldoFragment.getFragmentManager() != null && homeFragment.getFragmentManager() != null) {
                                SaldoFragment a = (SaldoFragment) saldoFragment.getFragmentManager().findFragmentByTag("Saldo");
                                if (a != null) a.update();
                                HomeFragment h = (HomeFragment) homeFragment.getFragmentManager().findFragmentByTag("Home");
                                if (h != null) h.update();
                            }
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                });
        Volley.newRequestQueue(this).add(stringRequest);

    }

    private void addAllFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (!homeFragment.isAdded()) {
            ft.add(R.id.fl_container, homeFragment, "Home");
            ft.show(homeFragment);
        }
        if (!riwayatFragment.isAdded()) {
            ft.add(R.id.fl_container, riwayatFragment, "Riwayat");
            ft.hide(riwayatFragment);
        }
        if (!saldoFragment.isAdded()) {
            ft.add(R.id.fl_container, saldoFragment, "Saldo");
            ft.hide(saldoFragment);
        }
        if (!akunFragment.isAdded()) {
            ft.add(R.id.fl_container, akunFragment, "Akun");
            ft.hide(akunFragment);
        }
        ft.commit();
    }

    @Override
    public void pindahFragment(int id) {
        switch (id) {
            case 0 :
                bottomNav.setSelectedItemId(R.id.nav_home);
                navListener.onNavigationItemSelected(bottomNav.getMenu().getItem(0));
                break;
            case 1 :
                bottomNav.setSelectedItemId(R.id.nav_riwayat);
                navListener.onNavigationItemSelected(bottomNav.getMenu().getItem(1));
                break;
            case 2 :
                bottomNav.setSelectedItemId(R.id.nav_saldo);
                navListener.onNavigationItemSelected(bottomNav.getMenu().getItem(2));
                break;
            case 3 :
                bottomNav.setSelectedItemId(R.id.nav_akun);
                navListener.onNavigationItemSelected(bottomNav.getMenu().getItem(3));
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            if (!homeFragment.isVisible()) {
                                ft.show(homeFragment);
                                ft.hide(riwayatFragment); ft.hide(saldoFragment); ft.hide(akunFragment);
                            }
                            break;
                        case R.id.nav_riwayat:
                            if (!riwayatFragment.isVisible()) {
                                ft.show(riwayatFragment);
                                ft.hide(homeFragment); ft.hide(saldoFragment); ft.hide(akunFragment);
                            }
                            break;
                        case R.id.nav_saldo:
                            if (!saldoFragment.isVisible()) {
                                ft.show(saldoFragment);
                                ft.hide(homeFragment); ft.hide(riwayatFragment); ft.hide(akunFragment);
                            }
                            break;
                        case R.id.nav_akun:
                            if (!akunFragment.isVisible()) {
                                ft.show(akunFragment);
                                ft.hide(homeFragment); ft.hide(riwayatFragment); ft.hide(saldoFragment);
                            }
                            break;
                    }

                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                    return true;
                }
            };

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        // The directory is now empty so delete it
            return Objects.requireNonNull(dir).delete();
    }

    @Override
    public void updateTransaksi() {
        try {
            if (riwayatFragment.getFragmentManager() != null) {
                RiwayatFragment r = (RiwayatFragment) riwayatFragment.getFragmentManager().findFragmentByTag("Riwayat");
                if (r != null) {
                    r.updateTransaksi();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}