package com.xoteev.githubtest.githubapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/*
* Десериализатор списка репозиториев
* */

public class GithubRepoDeserializer implements JsonDeserializer<GithubRepos> {

    @Override
    public GithubRepos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        GithubRepos githubRepo = new GithubRepos();

        JsonObject repoJsonObject =  json.getAsJsonObject();
        githubRepo.setName(repoJsonObject.get("name").getAsString());
        githubRepo.setDescription(repoJsonObject.get("description").toString());
        githubRepo.setCommitsUrl(repoJsonObject.get("commits_url").getAsString());
        githubRepo.setForksCount(repoJsonObject.get("forks_count").getAsInt());
        githubRepo.setWatchersCount(repoJsonObject.get("watchers_count").getAsInt());

        JsonElement ownerJsonElement = repoJsonObject.get("owner");
        JsonObject ownerJsonObject = ownerJsonElement.getAsJsonObject();
        githubRepo.setOwner(ownerJsonObject.get("login").getAsString());
        githubRepo.setAvatarUrl(ownerJsonObject.get("avatar_url").getAsString());

        return githubRepo;
    }
}