package com.xoteev.githubtest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xoteev.githubtest.githubapi.GithubAPI;
import com.xoteev.githubtest.githubapi.GithubCommitDeserializer;
import com.xoteev.githubtest.githubapi.GithubCommits;
import com.xoteev.githubtest.githubapi.GithubRepos;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ItemListActivity extends AppCompatActivity {

    private GlobalData globalData;

    private GithubAPI githubAPI;
    private static List<GithubRepos> listRepos;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        globalData = (GlobalData) getApplication();

        // лист репозиториев заполненный в LoginActivity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            listRepos = (List<GithubRepos>) extras.get("listRepos");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        /* TODO необходимо обновлять список репозиториев на текущей активити, как вариант повесить обновление на FloatingActionButton
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        createGithubAPI();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, listRepos));
    }

    private void createGithubAPI() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(GithubCommits.class, new GithubCommitDeserializer())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        // данные авторизации берм из globalData
                        Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                                Credentials.basic(globalData.getUserName(), globalData.getPassword()));

                        Request newRequest = builder.build();
                        return chain.proceed(newRequest);
                    }
                }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GithubAPI.ENDPOINT)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        githubAPI = retrofit.create(GithubAPI.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    private DisposableSingleObserver<List<GithubCommits>> getCommitsObserver(final GithubRepos item) {
        return new DisposableSingleObserver<List<GithubCommits>>() {
            @Override
            public void onSuccess(List<GithubCommits> value) {
                Context context = ItemListActivity.this;
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra("ARG_REPO_ITEM", item);
                intent.putExtra("ARG_COMMIT_LIST", (Serializable) value);
                context.startActivity(intent);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                // TODO разделить отсутствие доступа к сети(broadcast) и не отсутствие репозиториев
                Toast.makeText(ItemListActivity.this, "Не удалось получить данные.", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<GithubRepos> mValues;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GithubRepos item = (GithubRepos) view.getTag();

                mParentActivity.compositeDisposable.add(mParentActivity.githubAPI.getCommits(item.getOwner(), item.getName())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(mParentActivity.getCommitsObserver(item)));
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
}
