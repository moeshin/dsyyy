package site.littlehands.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NeteaseMusicAPI {

    public static String urls(int br,int ...ids) {
        StringBuilder api = new StringBuilder("https://music.163.com/api/song/enhance/player/url?ids=[");
        for (int id : ids) {
            api.append(id).append(',');
        }
        int index = api.length() - 1;
        if (api.charAt(index) == ',') {
            api.deleteCharAt(index);
        }
        api.append("]&br=").append(br);
        return api.toString();
    }

    public static String details(String ...ids) {
        StringBuilder api = new StringBuilder("https://music.163.com/api/v3/song/detail?c=[");
        for (String id : ids) {
            api.append("{\"id\":").append(id).append("},");
        }
        int index = api.length() - 1;
        if (api.charAt(index) == ',') {
            api.deleteCharAt(index);
        }
        api.append(']');
        return api.toString();
    }

    public static String artistsToString(JSONArray ar) throws JSONException {
        int length = ar.length();
        StringBuilder artists = new StringBuilder();
        for (int i = 0; i < length; i++) {
            JSONObject vo = ar.getJSONObject(i);
            if (i > 0) {
                artists.append(',');
            }
            artists.append(vo.getString("name"));
        }
        return artists.toString();
    }

    public static String artistsToString(JSONObject song) throws JSONException {
        return artistsToString(song.getJSONArray("ar"));
    }

}
