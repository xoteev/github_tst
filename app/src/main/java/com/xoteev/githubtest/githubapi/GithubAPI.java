package com.xoteev.githubtest.githubapi;

import java.util.List;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface GithubAPI {
    // основной адресс не меняется
    String ENDPOINT = "https://api.github.com";

    // запрос репозиториев
    @GET("user/repos?per_page=100")
    Single<List<GithubRepos>> getRepos();

    // запрос комитов по выбранному репозиторию
    @GET("/repos/{owner}/{repo}/commits")
    Single<List<GithubCommits>> getCommits(@Path("owner") String owner, @Path("repo") String repository);
}