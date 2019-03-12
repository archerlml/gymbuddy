package com.github.archerlml.gymbuddy.util;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.archerlml.gymbuddy.R;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.model.Entity;
import com.github.archerlml.gymbuddy.model.Event;
import com.github.archerlml.gymbuddy.model.Match;
import com.github.archerlml.gymbuddy.model.UserEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by archerlml on 11/1/17.
 */

@Singleton
public class NetworkUtil {
    @Inject
    OkHttpClient okHttpClient;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Inject
    public NetworkUtil(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    // put
    public <T> T put(T t) {
        return put((Class<T>) t.getClass(), t);
    }

    public <T> T put(Class<T> tClass, Object t) {
        return put(tClass, Util.objToJson(t));
    }

    public <T> T put(Class<T> tClass, String body) {
        if (TextUtils.isEmpty(body)) {
            return null;
        }
        String endpoint = getResourceEndPoint(tClass.getSimpleName());
        Request request = new Request.Builder()
                .url(endpoint)
                .put(RequestBody.create(JSON, body))
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            Log.i("put = ", body, ", response = ", content);
            return Util.jsonToObj(content, tClass);
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    public interface Callback<T> {

        void onError(Response r);

        void onSuccess(T t);
    }

    // get
    public <T extends Entity> void get(T t, Callback<T> callback) {
        if (t == null) {
            callback.onError(null);
            return;
        }
        String endpoint = getResourceEndPoint(t.getClass().getSimpleName());
        String url = Util.getString(endpoint, '/', t._id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                String content = response.body().string();
                callback.onSuccess(Util.jsonToObj(content, t.getClass()));
                return;
            }
        } catch (IOException e) {
            Log.e(e);
        }
        callback.onError(response);
    }


    // query
    public <T extends Entity> List<T> query(Class<T> tClass, Object t) {
        return query(tClass, Util.objToJson(t));
    }

    public <T extends Entity> List<T> query(T t) {
        return query((Class<T>) t.getClass(), t);
    }

    public <T extends Entity> List<T> query(Class<T> tClass, String query) {
        if (query == null) {
            return null;
        }
        String url = Util.getString(getResourceEndPoint(tClass.getSimpleName()), "/query");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, query))
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            Log.i("query = ", query, ", response = ", content);
            return Util.jsonToList(content, tClass);
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    public static class OperateResult {
        public int ok;
        public int nModified;
        public int n;
    }

    // delete
    public <T> OperateResult delete(T t) {
        if (t == null) {
            return null;
        }
        String url = Util.getString(getResourceEndPoint(t.getClass().getSimpleName()), "/delete");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, Util.objToJson(t)))
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            return Util.jsonToObj(content, OperateResult.class);
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    // other api
    public Map<Class<?>, Object> getMyResources(String uid, Class<? extends UserEntity>... classes) {

        List<String> paramPair = new ArrayList<>();
        for (Class<?> cls : classes) {
            paramPair.add(Util.getString("classes=", cls.getSimpleName().toLowerCase()));
        }
        String params = Util.join("&", paramPair);
        String url = Util.getString(getHost(), "/my/", uid, "?", params);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                return null;
            }
            Map<Class<?>, Object> result = new HashMap<>();
            String content = response.body().string();
            JsonNode jsonNode = Util.getObjectMapper().readTree(content);
            for (Class<?> cls : classes) {
                JsonNode node = jsonNode.get(cls.getSimpleName().toLowerCase());
                if (node != null) {
                    result.put(cls, Util.getObjectMapper().treeToValue(node, cls));
                }
            }
            return result;
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    public List<Match> matchUp(Map<String, Object> exercise) {
        String url = Util.getString(getHost(), "/match/query");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON, Util.objToJson(exercise)))
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.code() != 200) {
                Log.e("failed, response code = ", response.code());
                return null;
            }
            return Util.jsonToList(response.body().string(), Match.class);
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    public Event updateEvent(Event event) {
        String endpoint = getEventEndPoint();
        String url = Util.getString(endpoint, '/', event._id);
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                return Util.jsonToObj(response.body().string(), Event.class);
            }
        } catch (IOException e) {
            Log.e(e);
        }
        return null;
    }

    public void autoPickServer() {
        sHost = LOCAL_SERVER;
        String endpoint = getTestEndPoint();
        Request request = new Request.Builder()
                .url(endpoint)
                .build();

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            Log.e(e);
        }
        if (response == null || response.code() != 200) {
            sHost = REMOTE_SERVER;
        }
        Log.d("use server: ", sHost);
    }

    private static String getEventEndPoint() {
        return Util.getString(getHost(), "/event");
    }

    private static String getTestEndPoint() {
        return Util.getString(getHost(), "/hello");
    }

    private static String getResourceEndPoint(String entityType) {
        return Util.getString(getHost(), "/res/", entityType.toLowerCase());
    }

    public static String sHost;

    private static String LOCAL_SERVER = GymBuddyApplication.getApp().getString(R.string.GYM_BUDDY_SERVER_LOACAL);
    private static String REMOTE_SERVER = GymBuddyApplication.getApp().getString(R.string.GYM_BUDDY_SERVER_REMOTE);

    private static String getHost() {
        return sHost == null ? REMOTE_SERVER : sHost;
    }
}
