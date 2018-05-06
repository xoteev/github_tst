package com.xoteev.githubtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xoteev.githubtest.githubapi.GithubAPI;
import com.xoteev.githubtest.githubapi.GithubAuth;
import com.xoteev.githubtest.githubapi.GithubAuthDeserializer;
import com.xoteev.githubtest.githubapi.GithubRepoDeserializer;
import com.xoteev.githubtest.githubapi.GithubRepos;
import com.xoteev.githubtest.util.LoginParam;
import com.xoteev.githubtest.util.PrefUtilis;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity {

    private GithubAPI mGithubAPI;

    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSignInButton;


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameView = (AutoCompleteTextView) findViewById(R.id.userName);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        showProgress(false);
    }

    private void createGithubAPI(GithubAPI.RequestType type) {

        Gson gson = null;
        switch (type) {
            case reqLogin:
                gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .registerTypeAdapter(GithubAuth.class, new GithubAuthDeserializer())
                        .create();
                break;
            case reqRepo:
                gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .registerTypeAdapter(GithubRepos.class, new GithubRepoDeserializer())
                        .create();
                break;
        }

        OkHttpClient okHttpClient = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GithubAPI.ENDPOINT)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mGithubAPI = retrofit.create(GithubAPI.class);
    }

    private void attemptLogin() {

        if(!PrefUtilis.isNetworkAvailable(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, "Ошибка соединения. Проверьте настройки сети.", Toast.LENGTH_SHORT).show();
            return;
        }

        mUserNameView.setError(null);
        mPasswordView.setError(null);

        String email = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        } else if (!isUserNameValid(email)) {
            mUserNameView.setError(getString(R.string.error_invalid_email));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }

        if (mSignInButton != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSignInButton.getWindowToken(), 0);
        }

        getLogin();
    }

    private boolean isUserNameValid(String email) {
        // проверим что пользователь что то ввел
        return !email.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        return !password.isEmpty();
    }

    private void getLogin() {
        // пробуем авторизоваться
        showProgress(true);

        createGithubAPI(GithubAPI.RequestType.reqLogin);

        String credential = mUserNameView.getText().toString() + ":" + mPasswordView.getText().toString();
        String authorization = "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
        LoginParam loginParam = new LoginParam(GithubAPI.CLIENT_SECRET, "GithubTest app");
        compositeDisposable.add(mGithubAPI.basicLogin(authorization, GithubAPI.CLIENT_ID, loginParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getLoginObserver()));
    }

    private void getRepo() {
        // запрашиваем список репозиториев
        showProgress(true);

        createGithubAPI(GithubAPI.RequestType.reqRepo);

        final String oAuthToken = "token " + PrefUtilis.getOAuthToken(LoginActivity.this);
        createGithubAPI(GithubAPI.RequestType.reqRepo);
        compositeDisposable.add(mGithubAPI.getRepos(oAuthToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getRepositoriesObserver()));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private DisposableSingleObserver<GithubAuth> getLoginObserver() {
        return new DisposableSingleObserver<GithubAuth>() {
            @Override
            public void onSuccess(GithubAuth value) {
                // проставим флаг, что приложение авторизовано
                PrefUtilis.setAuthenticated(LoginActivity.this, 1);
                // токен приходит только при первой авторизации
                if (!value.getToken().isEmpty()) {
                    PrefUtilis.setOAuthToken(LoginActivity.this, value.getToken());
                }

                // запрашиваем список репозиториев
                getRepo();
            }

            @Override
            public void onError(Throwable e) {
                showProgress(false);
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "Неверный логин или пароль.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private DisposableSingleObserver<List<GithubRepos>> getRepositoriesObserver() {
        return new DisposableSingleObserver<List<GithubRepos>>() {
            @Override
            public void onSuccess(List<GithubRepos> value) {
                Intent intent = new Intent(LoginActivity.this, ItemListActivity.class);
                intent.putExtra("listRepos", (Serializable) value);
                startActivity(intent);
                // текущая активити больше ненужна
                finish();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                showProgress(false);
                Toast.makeText(LoginActivity.this, "Ошибка при загрузке списка репозиториев.", Toast.LENGTH_SHORT).show();
            }
        };
    }
}

