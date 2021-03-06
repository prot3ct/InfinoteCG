package com.infinote.differentthinking.infinote.views.auth.register;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v8.renderscript.ScriptIntrinsicBLAS;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.infinote.differentthinking.infinote.R;
import com.infinote.differentthinking.infinote.utils.InfinoteProgressDialog;
import com.infinote.differentthinking.infinote.views.auth.login.LoginActivity;
import com.infinote.differentthinking.infinote.views.list_notes.ListNotesActivity;
import com.infinote.differentthinking.infinote.views.auth.register.base.RegisterContract;

public class RegisterFragment extends Fragment implements RegisterContract.View {
    private TextView alreadyHaveAccountView;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private EditText usernameEditText;
    private CircularProgressButton registerButton;
    private CountDownTimer timer;

    private TextView signUpText;
    private Typeface typeFace;

    private RegisterContract.Presenter presenter;
    private InfinoteProgressDialog progressDialog;
    private Context context;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        this.alreadyHaveAccountView = (TextView) view.findViewById(R.id.tv_signin);

        this.emailEditText = (EditText) view.findViewById(R.id.et_email);
        this.passwordEditText = (EditText) view.findViewById(R.id.et_password);
        this.firstnameEditText = (EditText) view.findViewById(R.id.et_first_name);
        this.lastnameEditText = (EditText) view.findViewById(R.id.et_last_name);
        this.usernameEditText = (EditText) view.findViewById(R.id.et_username);

        this.timer = new CountDownTimer(5000, 1000) {
            @ScriptIntrinsicBLAS.Diag
            public void onFinish() {
                registerButton.setProgress(0);
            }
            @Override
            public void onTick(long millisUntilFinished) {

            }
        };

        typeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/SIMPLIFICA.ttf");
        signUpText = (TextView) view.findViewById(R.id.tv_create_acc);
        signUpText.setTypeface(typeFace);

        this.registerButton = (CircularProgressButton) view.findViewById(R.id.btn_signup);
        registerButton.setIndeterminateProgressMode(true);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameTextField = usernameEditText.getText().toString();
                String emailTextField = emailEditText.getText().toString();
                String firstnameTextField = firstnameEditText.getText().toString();
                String lastnameTextField = lastnameEditText.getText().toString();
                String passwordTextField = passwordEditText.getText().toString();

                if (registerButton.getProgress() == 0) {
                    registerButton.setProgress(50);
                    if (!presenter.validateEmail(emailTextField)) {
                        registerButton.setErrorText("Invalid email");
                        registerButton.setProgress(-1);
                        timer.start();
                        return;
                    }
                    if (!presenter.validateUsernameAndPassword(usernameTextField, passwordTextField)) {
                        registerButton.setErrorText("Invalid username/password");
                        registerButton.setProgress(-1);
                        timer.start();
                        return;
                    }

                    presenter.registerUser(usernameTextField, emailTextField, firstnameTextField, lastnameTextField, passwordTextField);
                } else if (registerButton.getProgress() == 100) {
                    registerButton.setProgress(0);
                } else {
                    registerButton.setProgress(100);
                }
            }
        });

        alreadyHaveAccountView.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        presenter.onHasAccountClicked();
                    }
                }
        );

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showListNotesActivity() {
        Intent intent = new Intent(this.context, ListNotesActivity.class);
        startActivity(intent);
    }

    @Override
    public void showLoginActivity() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void showDialogForLoading() {
        this.registerButton.setProgress(50);
    }

    @Override
    public void notifySuccessful() {
        Toast.makeText(getContext(), this.firstnameEditText.getText().toString() + getString(R.string.user_registered_notify_message), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void notifyError(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG)
                .show();
    }
}
