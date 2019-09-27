package com.rsmartin.fuelapp.presentation.ui.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rsmartin.fuelapp.R;
import com.rsmartin.fuelapp.presentation.ui.AbstractActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AbstractActivity implements LoginPresenter.View, ForgotPasswordDialogFragment.ForgotPasswordListener {

    private String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    @BindView(R.id.container_login)
    ConstraintLayout containerLogin;

    @BindView(R.id.container_register)
    FrameLayout containerRegister;

    @BindView(R.id.email)
    EditText email;

    @BindView(R.id.password)
    EditText pass;

    @BindView(R.id.login)
    Button login;

    @BindView(R.id.register)
    TextView register;

    @BindView(R.id.forgot_pass)
    TextView forgot;

    @BindView(R.id.verification)
    Button verification;

    @Inject
    LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        containerLogin.setVisibility(View.VISIBLE);
        containerRegister.setVisibility(View.GONE);

        getApplicationComponent().inject(this);
        loginPresenter.setView(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

        initAdView();

        email.setText("rafaels.martin.dev@gmail.com");
        pass.setText("123456");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPresenter.signIn(getApplicationContext(),
                        email.getText().toString().trim(), pass.getText().toString().trim(), mAuth);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ForgotPasswordDialogFragment forgotPasswordDialogFragment = new ForgotPasswordDialogFragment();
                forgotPasswordDialogFragment.show(getSupportFragmentManager(), ForgotPasswordDialogFragment.TAG);//
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                loginPresenter.singUpNewUsers(getApplicationContext(),
//                        email.getText().toString().trim(), pass.getText().toString().trim(), mAuth);
                containerLogin.setVisibility(View.GONE);
                containerRegister.setVisibility(View.VISIBLE);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_register, RegisterFragment.newInstance(), RegisterFragment.TAG)
                        .addToBackStack(RegisterFragment.TAG)
                        .commit();

            }
        });

        verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPresenter.sendUserVerification(getApplicationContext(), mAuth);
            }
        });
    }

    private void initAdView() {
        AdView mAdView = findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("8D10FA36EE0D8F85F9C7B1331F2F81D0")
                .addTestDevice("FA02415696106289FBE38417A007FF69")
                .build();
        mAdView.loadAd(adRequest);
    }


    @Override
    public void goToSplash() {
        navigator.navigateToSplash(getApplicationContext());
    }

    @Override
    public void onDialogPositive(String email) {
        Toast.makeText(LoginActivity.this, "listener en activity: " + email, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onDialogPositive: " + email);
        loginPresenter.sendPasswordResetEmail(email);
    }

    @Override
    public void onBackPressed() {
        Fragment current = getSupportFragmentManager().findFragmentByTag(RegisterFragment.TAG);
        if (current instanceof RegisterFragment) {
            containerLogin.setVisibility(View.VISIBLE);
            containerRegister.setVisibility(View.GONE);
            Log.e(TAG, "onBackPressed: if");
        }
        super.onBackPressed();
    }

}
