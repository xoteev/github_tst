package com.xoteev.githubtest.githubapi;

import com.xoteev.githubtest.util.LoginParam;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface GithubAPI {
    // основной адресс не меняется
    String ENDPOINT = "https://api.github.com";
    String CLIENT_ID = "fba00360aa8d9a211dd4";
    String CLIENT_SECRET = "f6a1905863a45daa27c25966c9f9709567e1493c";

    public enum RequestType {
        reqLogin, reqRepo, reqCommits
    }

    // запрос авторизации
    @Headers("Content-Type: application/json")
    @PUT("/authorizations/clients/{clientId}")
    Single<GithubAuth> basicLogin(@Header("Authorization") String authorization, @Path("clientId") String clientId, @Body LoginParam loginParam);

    // запрос репозиториев
    @GET("user/repos?per_page=100")
    Single<List<GithubRepos>> getRepos(@Header("Authorization") String token);

    // запрос комитов по выбранному репозиторию
    @GET("/repos/{owner}/{repo}/commits")
    Single<List<GithubCommits>> getCommits(@Header("Authorization") String token, @Path("owner") String owner, @Path("repo") String repository);
}