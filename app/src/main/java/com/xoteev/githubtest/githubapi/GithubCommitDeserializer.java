package com.xoteev.githubtest.githubapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/*
 * Десериализатор списка коммитов
 * */

public class GithubCommitDeserializer implements JsonDeserializer<GithubCommits> {

    @Override
    public GithubCommits deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        GithubCommits githubCommits = new GithubCommits();

        JsonObject repoJsonObject = json.getAsJsonObject();
        githubCommits.setSha(repoJsonObject.get("sha").getAsString());

        JsonElement commitJsonElement = repoJsonObject.get("commit");
        JsonObject commitJsonObject = commitJsonElement.getAsJsonObject();
        githubCommits.setMessage(commitJsonObject.get("message").getAsString());

        JsonElement committerJsonElement = commitJsonObject.get("committer");
        JsonObject committerJsonObject = committerJsonElement.getAsJsonObject();
        githubCommits.setCommitterName(committerJsonObject.get("name").getAsString());
        githubCommits.setCommitDate(committerJsonObject.get("date").getAsString());

        return githubCommits;
    }
}