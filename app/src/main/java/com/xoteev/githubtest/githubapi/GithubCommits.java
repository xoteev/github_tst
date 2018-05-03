package com.xoteev.githubtest.githubapi;


import java.io.Serializable;

/*
 * Модель данных коммитов
 * */

public class GithubCommits implements Serializable {
    private String sha;
    private String message;
    private String committerName;
    private String commitDate;

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(String commitDate) {
        this.commitDate = commitDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return sha;
    }
}