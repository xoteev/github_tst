package com.xoteev.githubtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xoteev.githubtest.githubapi.GithubAPI;
import com.xoteev.githubtest.githubapi.GithubCommitDeserializer;
import com.xoteev.githubtest.githubapi.GithubCommits;
import com.xoteev.githubtest.githubapi.GithubRepoDeserializer;
import com.xoteev.githubtest.githubapi.GithubRepos;
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

public class ItemListActivity extends AppCompatActivity {

    private GithubAPI githubAPI;
    private static List<GithubRepos> listRepos;

    ProgressBar mUpdateProgress;
    FrameLayout mFrameLayout;
    View mRecyclerView;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PrefUtilis.checkAuthenticated(this) == 0) {
            login();
            return;
        }

        setContentView(R.layout.activity_item_list);

        mUpdateProgress = (ProgressBar) findViewById(R.id.update_progress);
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(ItemListActivity.this, "Обновление списка репозиториев.", Toast.LENGTH_SHORT).show();
                getRepo();
            }
        });

        // лист репозиториев заполненный в LoginActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            listRepos = (List<GithubRepos>) extras.get("listRepos");
        }

        if (listRepos != null) {
            mRecyclerView = findViewById(R.id.item_list);
            assert mRecyclerView != null;
            setupRecyclerView((RecyclerView) mRecyclerView);
        } else {
            getRepo();
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, listRepos));
    }

    private void createGithubAPI(GithubAPI.RequestType type) {
        Gson gson = null;
        switch (type) {
            case reqRepo:
                gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .registerTypeAdapter(GithubRepos.class, new GithubRepoDeserializer())
                        .create();
                break;
            case reqCommits:
                gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .registerTypeAdapter(GithubCommits.class, new GithubCommitDeserializer())
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

        githubAPI = retrofit.create(GithubAPI.class);
    }

    private void getRepo() {
        // запрашиваем список репозиториев
        showProgress(true);
        createGithubAPI(GithubAPI.RequestType.reqRepo);

        final String oAuthToken = "token " + PrefUtilis.getOAuthToken(ItemListActivity.this);
        createGithubAPI(GithubAPI.RequestType.reqRepo);
        compositeDisposable.add(githubAPI.getRepos(oAuthToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getRepositoriesObserver()));
    }

    private void getCommits(GithubRepos item) {
        // запрашиваем список коммитов
        showProgress(true);
        createGithubAPI(GithubAPI.RequestType.reqCommits);

        final String oAuthToken = "token " + PrefUtilis.getOAuthToken(ItemListActivity.this);
        compositeDisposable.add(githubAPI.getCommits(oAuthToken, item.getOwner(), item.getName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCommitsObserver(item)));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFrameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mFrameLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFrameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mUpdateProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mUpdateProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUpdateProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mUpdateProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mFrameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        showProgress(false);
    }

    private DisposableSingleObserver<List<GithubCommits>> getCommitsObserver(final GithubRepos item) {
        return new DisposableSingleObserver<List<GithubCommits>>() {
            @Override
            public void onSuccess(List<GithubCommits> value) {
                //showProgress(false);
                Context context = ItemListActivity.this;
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra("ARG_REPO_ITEM", item);
                intent.putExtra("ARG_COMMIT_LIST", (Serializable) value);
                context.startActivity(intent);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                showProgress(false);
                Toast.makeText(ItemListActivity.this, "Ошибка при загрузке списка коммитов.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private DisposableSingleObserver<List<GithubRepos>> getRepositoriesObserver() {
        return new DisposableSingleObserver<List<GithubRepos>>() {
            @Override
            public void onSuccess(List<GithubRepos> value) {
                showProgress(false);
                listRepos = value;
                mRecyclerView = findViewById(R.id.item_list);
                assert mRecyclerView != null;
                setupRecyclerView((RecyclerView) mRecyclerView);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                showProgress(false);
                Toast.makeText(ItemListActivity.this, "Ошибка при загрузке списка репозиториев.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<GithubRepos> mValues;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PrefUtilis.isNetworkAvailable(ItemListActivity.this)) {
                    Toast.makeText(ItemListActivity.this, "Ошибка соединения. Проверьте настройки сети.", Toast.LENGTH_SHORT).show();
                    return;
                }
                GithubRepos item = (GithubRepos) view.getTag();
                getCommits(item);
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent, List<GithubRepos> items) {
            mValues = items;
            mParentActivity = parent;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mContentView.setText(mValues.get(position).getName());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            PrefUtilis.setAuthenticated(this, 0);
            // TODO Необходимо разобрать ситуацию с озывом авторизации. Пока что просто оставляем старый токен в памяти.
            //PrefUtilis.setOAuthToken(this, "");
            login();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
