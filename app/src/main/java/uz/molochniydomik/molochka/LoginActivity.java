package uz.molochniydomik.molochka;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uz.molochniydomik.molochka.entities.Token;
import uz.molochniydomik.molochka.network.ApiService;
import uz.molochniydomik.molochka.network.RetrofitBuilder;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    TextInputLayout tilEmail, tilPassword;
    RelativeLayout container;
    LinearLayout formContainer;
    ProgressBar loader;
    Button btnLogin;

    ApiService service;
    TokenManager tokenManager;
    AwesomeValidation validator;
    Call<Token> call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        notification();
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        if (tokenManager.getToken().getAccessToken() != null) {
            startActivity(new Intent(LoginActivity.this, MessageActivity.class));
            finish();
        }

        service = RetrofitBuilder.createService(ApiService.class);

        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        setupRules();
        login();
    }

    private void initViews() {
        tilEmail = (TextInputLayout) findViewById(R.id.til_email);
        tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        container = (RelativeLayout) findViewById(R.id.container);
        formContainer = (LinearLayout) findViewById(R.id.form_container);
        loader = (ProgressBar) findViewById(R.id.loader);
        btnLogin = (Button) findViewById(R.id.btn_login);
    }

    private void notification() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 19);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

    }

    private boolean haveNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null)
            return true;
        else
            return false;
    }

    private void showLoading() {
        TransitionManager.beginDelayedTransition(container);
        formContainer.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
    }

    private void showForm() {
        TransitionManager.beginDelayedTransition(container);
        formContainer.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
    }

    private void login() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = tilEmail.getEditText().getText().toString();
                String password = tilPassword.getEditText().getText().toString();

                tilEmail.setError(null);
                tilPassword.setError(null);

                validator.clear();
                if (haveNetwork()) {
                    if (validator.validate()) {
                        showLoading();
                        call = service.login(email, password);
                        call.enqueue(new Callback<Token>() {
                            @Override
                            public void onResponse(Call<Token> call, Response<Token> response) {
                                Log.w(TAG, "onResponse: " + response);
                                if (response.isSuccessful()) {
                                    tokenManager.saveToken(response.body());
                                    startActivity(new Intent(LoginActivity.this, MessageActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Введены неправильные данные", Toast.LENGTH_LONG).show();
                                    showForm();
                                }
                            }

                            @Override
                            public void onFailure(Call<Token> call, Throwable t) {
                                Log.w(TAG, "onFailure: " + t.getMessage());
                                showForm();
                            }
                        });
                    }
                } else
                    Toast.makeText(LoginActivity.this, "Нету интернет соединения.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setupRules() {
        validator.addValidation(this, R.id.til_password, RegexTemplate.NOT_EMPTY, R.string.err_password);
        validator.addValidation(this, R.id.til_email, RegexTemplate.NOT_EMPTY, R.string.err_userName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }
}
