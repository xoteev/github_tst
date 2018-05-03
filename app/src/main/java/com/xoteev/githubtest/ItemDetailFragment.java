package com.xoteev.githubtest;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.xoteev.githubtest.githubapi.GithubCommits;
import com.xoteev.githubtest.githubapi.GithubRepos;

import java.net.URL;
import java.util.List;
import java.util.Locale;


public class ItemDetailFragment extends Fragment {

    private GithubRepos mRepoItem;
    private List<GithubCommits> mCommitsList;

    private CollapsingToolbarLayout appBarLayout;

    public ItemDetailFragment() {
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey("ARG_REPO_ITEM")) {
            mRepoItem = (GithubRepos) getArguments().getSerializable("ARG_REPO_ITEM");
            mCommitsList = (List<GithubCommits>) getArguments().getSerializable("ARG_COMMIT_LIST");

            appBarLayout = (CollapsingToolbarLayout) this.getActivity().findViewById(R.id.toolbar_layout);

            if (appBarLayout != null) {
                appBarLayout.setTitle(String.format(Locale.getDefault(), "%s - %s", mRepoItem.getOwner(), mRepoItem.getName()));
                // в открытом состоянии планируется что на фоне будет аватар, в закрытом фон темы
                appBarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(R.color.colorDark));
                //appBarLayout.setCollapsedTitleTextColor(R.color.colorLight);
                // TODO необходимо разобрать изменение цвета кнопки "назад", в открытом состоянии отличается от цвета текста

                TextView textView = this.getActivity().findViewById(R.id.item_toolbar_subTitle);
                textView.setText(String.format(Locale.getDefault(), "forks - %d, watchers - %d", mRepoItem.getForksCount(), mRepoItem.getWatchersCount()));
                textView.setTextColor(R.color.colorDark);

                // пробуем загрузить аватар
                new GetAvatarTask(mRepoItem.getAvatarUrl(), appBarLayout).execute((Void) null);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        ListView listCommits = (ListView) rootView.findViewById(R.id.commits_list);
        listCommits.setNestedScrollingEnabled(true);
        CommitsAdapter commitAdapter = new CommitsAdapter(getContext(), mCommitsList);
        listCommits.setAdapter(commitAdapter);

        return rootView;
    }

    // AsyncTask для загрузки аватара в фоновом потоке.
    public class GetAvatarTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUrl;
        CollapsingToolbarLayout appBar;
        Bitmap bmp = null;

        GetAvatarTask(String url, CollapsingToolbarLayout appBarLayout) {
            mUrl = url;
            appBar = appBarLayout;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                bmp = BitmapFactory.decodeStream(new URL(mUrl).openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                // при успешной загрузке заменяем фон
                appBar.setBackground(new BitmapDrawable(getResources(), bmp));
            }
        }
    }
}
