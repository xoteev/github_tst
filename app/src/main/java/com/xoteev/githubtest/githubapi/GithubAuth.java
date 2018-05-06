package com.xoteev.githubtest.githubapi;


import java.io.Serializable;

public class GithubAuth implements Serializable{
    private String token;
    private String hashToken;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHashToken() {
        return hashToken;
    }

    public void setHashToken(String hashToken) {
        this.hashToken = hashToken;
    }
}
