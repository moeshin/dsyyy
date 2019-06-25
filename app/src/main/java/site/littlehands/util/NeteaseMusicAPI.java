package site.littlehands.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NeteaseMusicAPI {

    public static String urls(int br, long ...ids) {
        StringBuilder api = new StringBuilder("https://music.163.com/api/song/enhance/player/url?ids=[");
        for (long id : ids) {
            api.append(id).append(',');
        }
        int index = api.length() - 1;
        if (api.charAt(index) == ',') {
            api.deleteCharAt(index);
        }
        api.append("]&br=").append(br);
        return api.toString();
    }

    public static String details(long ...ids) {
        StringBuilder api = new StringBuilder("https://music.163.com/api/v3/song/detail?c=[");
        for (long id : ids) {
            api.append("{\"id\":").append(id).append("},");
        }
        int index = api.length() - 1;
        if (api.charAt(index) == ',') {
            api.deleteCharAt(index);
        }
        api.append(']');
        return api.toString();
    }

    public static String dj(long id, int limit) {
        return "https://music.163.com/api/dj/program/byradio?radioId=" +
                id + "&limit=" + limit;
    }

    public static String dj(int limit) {
        return dj(limit, 10000);
    }

    public static String list(long id, int n) {
        return "https://music.163.com/api/v3/playlist/detail/?id=" +
                id + "&n=" + n;
    }

    public static String list(long id) {
        return list(id, 0);
    }

    public static String album(long id) {
        return "https://music.163.com/api/v1/album/" + id;
    }

    public static String artist(long id) {
        return "https://music.163.com/api/artist/" + id;
    }

    public static String artistsToString(JSONArray artists) throws JSONException {
        int length = artists.length();
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            JSONObject artist = artists.getJSONObject(i);
            if (i > 0) {
                string.append(',');
            }
            string.append(artist.getString("name"));
        }
        return string.toString();
    }

    public static String artistsToString(JSONObject song, String name) throws JSONException {
        return artistsToString(song.getJSONArray(name));
    }

}
