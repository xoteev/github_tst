package com.xoteev.githubtest.githubapi;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GithubAuthDeserializer implements JsonDeserializer<GithubAuth> {

    @Override
    public GithubAuth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        GithubAuth githubAuth = new GithubAuth();

        JsonObject repoJsonObject =  json.getAsJsonObject();
        githubAuth.setToken(repoJsonObject.get("token").getAsString());
        githubAuth.setHashToken(repoJsonObject.get("hashed_token").getAsString());

        return githubAuth;
    }
}