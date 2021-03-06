package com.example.user.myapplication.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.myapplication.Fragment.Interface.ILoginListener;
import com.example.user.myapplication.Manager.DatabaseManager;
import com.example.user.myapplication.Presenter.LoginPresenter;
import com.example.user.myapplication.R;
import com.example.user.myapplication.View.ILoginView;
import com.example.user.myapplication.Activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;


public class LoginFragment extends Fragment implements ILoginView {

    private View loginView;


    private EditText editTextEmail, editTextPassword;

    private LoginPresenter loginPresenter;

    private ILoginListener loginListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        loginView = inflater.inflate(R.layout.fragment_login, container, false);

        loginPresenter = LoginPresenter.getInstance();
        loginPresenter.attachView(this);
        initializeDatabase();

        initializeView();
        getActivity().setTitle("Login");

        return loginView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            loginListener = (ILoginListener) context;
        } catch (ClassCastException ignored) {
        }
    }


    @Override
    public void onLoginSuccess(String message) {
        Toasty.success(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginError(String message) {
        Toasty.error(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void initializeDatabase(){
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if(databaseManager.getAuthUser() != null){
            getActivity().finish();
            startActivity(new Intent(getContext(), MainActivity.class));
        }
    }

    private void initializeView(){
        Button to_register_btn = loginView.findViewById(R.id.switch_to_reg);
        Button login_btn = loginView.findViewById(R.id.login_btn);
        editTextEmail = loginView.findViewById(R.id.login_email);
        editTextPassword = loginView.findViewById(R.id.login_password);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPresenter.userLogin();
            }
        });
        to_register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginListener.onRegisterSwitchClick();
            }
        });
    }


    @Override
    public String getUserLogin() {
        return editTextEmail.getText().toString().trim();
    }

    @Override
    public String getUserPassword() {
        return editTextPassword.getText().toString().trim();
    }

    @Override
    public void validUserEmail(String message) {
        editTextEmail.setError(message);
        editTextEmail.requestFocus();
    }

    @Override
    public void validUserPassord(String message) {
        editTextPassword.setError(message);
        editTextPassword.requestFocus();
    }

    @Override
    public void addListenerToDatabaseAuth(Task<AuthResult> authResultTask) {
        authResultTask.addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(getContext(), MainActivity.class));
                    getActivity().finish();
                    onLoginSuccess("Login Success");
                } else {
                    onLoginError(task.getException().getMessage());
                }
            }
        });
    }

}
