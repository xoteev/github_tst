package com.xoteev.githubtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xoteev.githubtest.githubapi.GithubCommits;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class CommitsAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Context mContext;
    private List<GithubCommits> mList;


    public CommitsAdapter(Context context, List<GithubCommits> mCommitsList) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mList = mCommitsList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = convertView != null ? convertView
                : mInflater.inflate(R.layout.item_commit_list, parent, false);

        final TextView tvCommiteDate = (TextView) view.findViewById(R.id.item_commite_date);
        final TextView tvCommitterName = (TextView) view.findViewById(R.id.iten_commiter_name);
        final TextView tvSha = (TextView) view.findViewById(R.id.item_sha);
        final TextView tvMessage = (TextView) view.findViewById(R.id.item_message);

        // преобразуем дату к формату для отображения
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        try {
            tvCommiteDate.setText(myFormat.format(fromUser.parse(mList.get(position).getCommitDate())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvCommitterName.setText(mList.get(position).getCommitterName());
        tvSha.setText(mList.get(position).getSha());
        tvMessage.setText(mList.get(position).getMessage());

        return view;
    }
}