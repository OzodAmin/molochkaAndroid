package uz.molochniydomik.molochka;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uz.molochniydomik.molochka.network.ApiService;
import uz.molochniydomik.molochka.network.RetrofitBuilder;

public class MessageActivity extends AppCompatActivity {

    LinearLayout successMessageContainer;
    TextView txtUserName, txtMessage;
    ProgressBar loader;
    Button btnRefresh;

    ApiService service;
    MyApplication application;
    TokenManager tokenManager;
    Call<String> callUserName;
    Call<String> callIsOrder;
    Call<String> callIsAccounting;

    private String userName;
    private String orderSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initViews();
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        application = (MyApplication) this.getApplication();
        showLoading(true);
        getUserName();
        refresh();
    }

    private void initViews() {
        successMessageContainer = (LinearLayout) findViewById(R.id.message_container);
        txtUserName = (TextView) findViewById(R.id.message_username);
        txtMessage = findViewById(R.id.message_message);
        loader = (ProgressBar) findViewById(R.id.message_loader);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
    }

    private void showLoading(boolean is) {
        if (is) {
            loader.setVisibility(View.VISIBLE);
            successMessageContainer.setVisibility(View.GONE);
        } else {
            loader.setVisibility(View.GONE);
            successMessageContainer.setVisibility(View.VISIBLE);
        }
    }

    private boolean haveNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
            return true;
        else
            return false;
    }

    private void noInternetMessage() {
        Toast.makeText(this, "Нету интернет соединения.", Toast.LENGTH_SHORT).show();
        btnRefresh.setVisibility(View.VISIBLE);
        txtMessage.setText("Нету интернет соединения.");
    }

    private void onFailureMethod(Throwable t) {
        startActivity(new Intent(MessageActivity.this, LoginActivity.class));
        finish();
        tokenManager.deleteToken();
    }

    private void getUserName() {
        if (haveNetwork()) {
            callUserName = service.getUserName();
            callUserName.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    userName = response.body();
                    getOrder();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    onFailureMethod(t);
                }
            });
        } else
            noInternetMessage();
    }

    private void getOrder() {
        if (haveNetwork()) {
            callIsOrder = service.getIsOrder();
            callIsOrder.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() == 200) {
                        orderSum = response.body();
                        getAccounting();
                    } else {
                        txtUserName.setText(userName);
                        txtMessage.setText("Сегодня вы не сделали заказ. Пожалуйста оформите заказ.");
                        showLoading(false);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    onFailureMethod(t);
                }
            });
        } else
            noInternetMessage();
    }

    private void getAccounting() {
        if (haveNetwork()) {
            callIsAccounting = service.getIsAccounting();
            callIsAccounting.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() == 200) {
                        application.setUserName(userName);
                        application.setOrderSum(orderSum);
                        startActivity(new Intent(MessageActivity.this, AccountingActivity.class));
                        finish();
                    } else {
                        txtUserName.setText(userName);
                        txtMessage.setText("Спасибо ваше бухгалтерия в порядке.");
                        showLoading(false);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    onFailureMethod(t);
                }
            });
        } else
            noInternetMessage();
    }

    void refresh() {
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });
    }
}
