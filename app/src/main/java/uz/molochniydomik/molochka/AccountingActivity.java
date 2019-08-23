package uz.molochniydomik.molochka;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uz.molochniydomik.molochka.network.ApiService;
import uz.molochniydomik.molochka.network.RetrofitBuilder;

public class AccountingActivity extends AppCompatActivity {

    @BindView(R.id.accounting_username)
    TextView txtUserName;
    @BindView(R.id.accounting_orderSum)
    TextView txtOrderSum;

    @BindView(R.id.til_field_1)
    TextInputLayout field_vnutrenniy;
    @BindView(R.id.til_field_2)
    TextInputLayout field_inkasasiya;
    @BindView(R.id.til_field_3)
    TextInputLayout field_terminal;

    @BindView(R.id.accounting_loader)
    ProgressBar loader;

    ApiService service;
    TokenManager tokenManager;
    AwesomeValidation validator;
    Call<String> call;

    String orderSum, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);

        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));

        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        orderSum = ((MyApplication) this.getApplication()).getOrderSum();
        userName = ((MyApplication) this.getApplication()).getUserName();

        txtUserName.setText(userName);
        txtOrderSum.setText(orderSum + " Сум");
        setupRules();
    }

    private void setupRules() {
        validator.addValidation(this, R.id.til_field_1, RegexTemplate.NOT_EMPTY, R.string.err_field);
        validator.addValidation(this, R.id.til_field_2, RegexTemplate.NOT_EMPTY, R.string.err_field);
        validator.addValidation(this, R.id.til_field_3, RegexTemplate.NOT_EMPTY, R.string.err_field);
    }

    private void onFailureMethod(Throwable t) {
        startActivity(new Intent(AccountingActivity.this, LoginActivity.class));
        finish();
        tokenManager.deleteToken();
    }

    private boolean haveNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
            return true;
        else
            return false;
    }

    @OnClick(R.id.btn_send)
    void send() {
        if (haveNetwork()) {
            String field_string_1 = field_vnutrenniy.getEditText().getText().toString();
            String field_string_2 = field_inkasasiya.getEditText().getText().toString();
            String field_string_3 = field_terminal.getEditText().getText().toString();

            field_vnutrenniy.setError(null);
            field_inkasasiya.setError(null);
            field_terminal.setError(null);

            validator.clear();

            if (validator.validate()) {
                loader.setVisibility(View.VISIBLE);
                call = service.postAccounting(field_string_1, orderSum, field_string_2, field_string_3);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            startActivity(new Intent(AccountingActivity.this, MessageActivity.class));
                            finish();
                        } else {
                            loader.setVisibility(View.GONE);
                            Toast.makeText(AccountingActivity.this, "Что то пошло не так. Попробуйте ещё раз.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        startActivity(new Intent(AccountingActivity.this, LoginActivity.class));
                        finish();
                        tokenManager.deleteToken();
                    }
                });
            }
        } else
            Toast.makeText(this, "Нету интернет соединения.", Toast.LENGTH_SHORT).show();
    }
}
