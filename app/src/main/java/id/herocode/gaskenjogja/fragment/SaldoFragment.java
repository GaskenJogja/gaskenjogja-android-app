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
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.PaymentMethod;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.BankType;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import id.herocode.gaskenjogja.R;
import id.herocode.gaskenjogja.activity.LoginActivity;
import id.herocode.gaskenjogja.iface.FragmentInteraction;
import id.herocode.gaskenjogja.utils.AppConstants;
import id.herocode.gaskenjogja.utils.AppUtils;
import id.herocode.gaskenjogja.utils.HttpRequest;
import id.herocode.gaskenjogja.utils.ParseContent;
import id.herocode.gaskenjogja.utils.PreferenceHelper;

public class SaldoFragment extends Fragment implements FragmentInteraction, TransactionFinishedCallback {

    private FragmentActivity listener;
    private FragmentInteraction intListener;
    private PreferenceHelper preferenceHelper;
    private ParseContent parseContent;
    private static final String API_UPDATE_SALDO = AppConstants.ServiceType.UPDATE_SALDO;
    private BottomSheetBehavior sheetBehavior;

    private TextView tvSaldo, tvInfoSaldo;
    private Button btnTambah, btnKembali, btnTopup;
    private LinearLayout radio_button, bottom_sheet, payment_bca, payment_alfamart, payment_gopay;
    private RadioGroup rg_nominal;
    private RadioButton nominal;
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
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initMidtransSdk();

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
        bottom_sheet = listener.findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        payment_bca = listener.findViewById(R.id.payment_bca);
        payment_alfamart = listener.findViewById(R.id.payment_alfamart);
        payment_gopay = listener.findViewById(R.id.payment_gopay);

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
//                tvInfoSaldo.setVisibility(View.INVISIBLE);
//                tvSaldo.setVisibility(View.INVISIBLE);
//                etNominalSaldo.setVisibility(View.VISIBLE);
                radio_button.setVisibility(View.VISIBLE);
                btnTopup.setVisibility(View.VISIBLE);
                btnTambah.setVisibility(View.GONE);
            }
        });

        btnKembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnKembali.setVisibility(View.GONE);
//                tvInfoSaldo.setVisibility(View.VISIBLE);
//                tvSaldo.setVisibility(View.VISIBLE);
//                etNominalSaldo.setText(""); etNominalSaldo.setVisibility(View.INVISIBLE);
                radio_button.setVisibility(View.INVISIBLE);
                rg_nominal.clearCheck();
                btnKembali.setVisibility(View.GONE);
                btnTopup.setVisibility(View.GONE);
                btnTambah.setVisibility(View.VISIBLE);
            }
        });

        btnTopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = rg_nominal.getCheckedRadioButtonId();
                nominal = view.findViewById(selectedId);
                if(selectedId != -1){
                    MidtransSDK.getInstance().setTransactionRequest(initTransactionRequest());
                    MidtransSDK.getInstance().startPaymentUiFlow(listener, PaymentMethod.GO_PAY);

//                    if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
//                        bottom_sheet.setVisibility(View.VISIBLE);
//                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                    } else {
//                        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                        bottom_sheet.setVisibility(View.GONE);
//                    }

//                    Toast.makeText(listener, "Silahkan Lanjutkan ke Metode Pembayaran!", Toast.LENGTH_SHORT).show();
//                    Intent pembayaran = new Intent(listener, MidtransPayment.class);
//                    pembayaran.putExtra("nominal", Double.parseDouble(nominal.getText().toString().replaceAll("\\D+","")));
//                    startActivity(pembayaran);
                } else {
                    Toast.makeText(listener, "Pilih Jumlah Saldo yang ingin di TopUp", Toast.LENGTH_SHORT).show();
                }
                /*
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
                 */
            }
        });

        payment_bca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransactionRequest());
                MidtransSDK.getInstance().startPaymentUiFlow(listener, PaymentMethod.BANK_TRANSFER_BCA);
            }
        });

        payment_alfamart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransactionRequest());
                MidtransSDK.getInstance().startPaymentUiFlow(listener, PaymentMethod.GIFT_CARD_INDONESIA);
            }
        });

        payment_gopay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MidtransSDK.getInstance().setTransactionRequest(initTransactionRequest());
                MidtransSDK.getInstance().startPaymentUiFlow(listener, PaymentMethod.GO_PAY);
            }
        });

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    private CustomerDetails initCustomerDetails() {
        CustomerDetails mCustomerDetails = new CustomerDetails();
        mCustomerDetails.setFirstName(preferenceHelper.getName());
        mCustomerDetails.setEmail(preferenceHelper.getEmail());
        return mCustomerDetails;
    }

    private TransactionRequest initTransactionRequest() {
        Double nomDouble = Double.parseDouble(nominal.getText().toString().replaceAll("\\D+",""));
        TransactionRequest transactionRequestNew = new TransactionRequest(System.currentTimeMillis() + "", nomDouble);
        transactionRequestNew.setCustomerDetails(initCustomerDetails());
        ItemDetails itemDetails = new ItemDetails("1", nomDouble, 1, "Topup-"+nominal.getText().toString().replaceAll("\\D+",""));

        ArrayList<ItemDetails> listItemDetails = new ArrayList<>();
        listItemDetails.add(itemDetails);
        transactionRequestNew.setItemDetails(listItemDetails);

        CreditCard creditCard = new CreditCard();
        creditCard.setSaveCard(false);
        creditCard.setAuthentication(CreditCard.AUTHENTICATION_TYPE_3DS);
        creditCard.setBank(BankType.BCA);
        transactionRequestNew.setCreditCard(creditCard);
        return transactionRequestNew;
    }

    private void initMidtransSdk() {
        SdkUIFlowBuilder.init()
                .setContext(listener)
                .setClientKey(AppConstants.Params.MERCHANT_CLIENT_KEY)
                .setMerchantBaseUrl(AppConstants.ServiceType.MERCHANT_BASE_CHECKOUT_URL)
                .setTransactionFinishedCallback(this)
                .enableLog(true)
                .setColorTheme(
                        new CustomColorTheme(
                                "#FF028AC4",
                                "#38B5E2",
                                "#FFEEEEEE"
                        )
                ).buildSDK();
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
        map.put(AppConstants.Params.TOPUP, nominal.getText().toString().replaceAll("\\D+",""));
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

    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
        switch (result.getStatus()) {
            case TransactionResult.STATUS_SUCCESS : Toast.makeText(
                    listener,
                    "Transaction Finished. ID: " + result.getResponse().getTransactionId(),
                    Toast.LENGTH_LONG
            ).show();
                try {
                    topUp();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                rg_nominal.clearCheck();
                btnKembali.performClick();
                break;
            case TransactionResult.STATUS_PENDING : Toast.makeText(
                    listener,
                    "Transaction Pending. ID: " + result.getResponse().getTransactionId(),
                    Toast.LENGTH_LONG
            ).show(); break;
            case TransactionResult.STATUS_FAILED : Toast.makeText(
                    listener,
                    "Transaction Failed. ID: " + result.getResponse().getTransactionId() + ". Message: " + result.getResponse().getStatusMessage(),
                    Toast.LENGTH_LONG
            ).show(); break;
        }
        result.getResponse().getValidationMessages();
    } else if (result.isTransactionCanceled()) {
        Toast.makeText(listener, "Transaction Canceled", Toast.LENGTH_LONG).show();
    } else {
        if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
            Toast.makeText(listener, "Transaction Invalid", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(listener, "Transaction Finished with failure.", Toast.LENGTH_LONG).show();
        }
    }
}
}