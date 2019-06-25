package site.littlehands.dsyyy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import site.littlehands.app.DirectorySelectorDialog;
import site.littlehands.util.NeteaseMusicAPI;
import site.littlehands.dsyyy.util.UnitSelector;

public class ReceiveActivity extends Activity {


    private String TAG = "ReceiveActivity";

    private boolean isConfirm;

    private boolean isSingle;

    private String format;

    private String downloadPath;

    private AlertDialog loadingDialog;

    private TextView loadingMessage;

    private final Pattern pattern = Pattern
            .compile("(?:http|https)://music.163.com/(.*?)(?:/|\\?id=)(.*?)[/&]");

    private final DialogInterface.OnClickListener onCancel = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };

    private final View.OnClickListener onPathViewClick = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            DirectorySelectorDialog.show(ReceiveActivity.this,
                    new DirectorySelectorDialog.onSelectListener() {
                @Override
                public void onDirectorySelected(File directory) {
                    ((TextView) v).setText(directory.getPath());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isConfirm = preferences.getBoolean("isConfirm", true);
        format = preferences.getString(SettingUtils.KEY_FORMAT, SettingUtils.FORMAT_DEFAULT);
        downloadPath = preferences.getString(SettingUtils.KEY_PATH, null);
        if (downloadPath == null) {
            downloadPath = Environment.getExternalStorageDirectory().getPath();
        }

        // 获取分享文本
        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "Type: " + type);
        if (Intent.ACTION_SEND.equals(action) && type != null){
            if ("text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);

                Log.d(TAG, "Text: " + text);

                if (handleSharedText(text)) {
                    return;
                }
            }
        }

        startActivity(new Intent(this, MainActivity.class));
    }

    private boolean handleSharedText(String text) {
        Matcher matcher = pattern.matcher(text);

        if (!matcher.find()) {
            return false;
        }

        final String type = matcher.group(1);
        final long id = Long.valueOf(matcher.group(2));
        Log.d(TAG, "ID: " + id);

        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate(R.layout.alert_loading, null);
        loadingMessage = view.findViewById(R.id.message);
        loadingDialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isSingle = false;
                    if ("song".equals(type)) {
                        isSingle = true;
                        handleId(id);
                    } else {
                        if (isConfirm) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    confirmPath(type, id);
                                }
                            });
                        } else {
                            switchType(type, id);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    private void handleId(long ...ids) throws JSONException {
        String string = httpGetString(NeteaseMusicAPI.details(ids));
        if (string == null) {
            alertNetworkError();
            return;
        }
        JSONObject json = new JSONObject(string);
        JSONArray songs = json.getJSONArray("songs");

        int length = songs.length();
        for (int i = 0; i < length; i++) {
            try {
                JSONObject song = songs.getJSONObject(i);
                preDownload(
                        song.getInt("id"),
                        song.getString("name"),
                        NeteaseMusicAPI.artistsToString(song, "ar"),
                        song.getJSONObject("al").getString("name")
                );
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertNetworkError();
                    }
                });
            }
        }
    }

    private void handleList(long id) throws JSONException {
        String string = httpGetString(NeteaseMusicAPI.list(id));
        if (string == null) {
            alertNetworkError();
            return;
        }
        JSONArray trackIds = new JSONObject(string)
                .getJSONObject("playlist")
                .getJSONArray("trackIds");
        int length = trackIds.length();
        for (int i = 0; i < length; i++) {
            parsing(i, length);
            try {
                handleId(trackIds.getJSONObject(i).getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleArtist(long id) throws JSONException {
        String string = httpGetString(NeteaseMusicAPI.artist(id));
        if (string == null) {
            alertNetworkError();
            return;
        }
        JSONArray hotSongs = new JSONObject(string).getJSONArray("hotSongs");
        int length = hotSongs.length();
        for (int i = 0; i < length; i++) {
            parsing(i, length);
            try {
                JSONObject song = hotSongs.getJSONObject(i);
                preDownload(
                        song.getInt("id"),
                        song.getString("name"),
                        NeteaseMusicAPI.artistsToString(song, "artists"),
                        song.getJSONObject("album").getString("name")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAlbum(long id) throws JSONException {
        String string = httpGetString(NeteaseMusicAPI.album(id));
        if (string == null) {
            alertNetworkError();
            return;
        }
        JSONArray songs = new JSONObject(string).getJSONArray("songs");
        int length = songs.length();
        for (int i = 0; i < length; i++) {
            parsing(i, length);
            try {
                JSONObject song = songs.getJSONObject(i);
                preDownload(
                        song.getInt("id"),
                        song.getString("name"),
                        NeteaseMusicAPI.artistsToString(song, "ar"),
                        song.getJSONObject("al").getString("name")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String httpGetString(String url) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            ResponseBody body = okHttpClient.newCall(request).execute().body();
            if (body != null) {
                return body.string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void alertNetworkError() {
        new AlertDialog.Builder(ReceiveActivity.this)
                .setMessage(R.string.network_error)
                .setPositiveButton(android.R.string.ok, onCancel)
                .setCancelable(false)
                .show();
    }

    private String getSuffix(String str) {
        return str.substring(str.lastIndexOf('.'));
    }

    private void preDownload(long id, String name, String artist, String album) throws JSONException {
        Log.d(TAG, "preDownload: " + NeteaseMusicAPI.urls(9999999, id));
        JSONObject data = new JSONObject(httpGetString(NeteaseMusicAPI.urls(9999999, id)))
                .getJSONArray("data")
                .getJSONObject(0);
        preDownload(
                data.getString("url"),
                data.getInt("br"),
                data.getInt("size"),
                name,
                artist,
                album
        );
    }

    private void preDownload(
            final String url, final int br, final int size,
            final String name, String artist, String album
    ) {

        final String fileName = SettingUtils.format(Objects.requireNonNull(format),
                name.replace('/', '／'),
                artist.replace('/', '／'),
                album.replace('/', '／')
        ) + getSuffix(url);

        if (isConfirm && isSingle) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    @SuppressLint("InflateParams")
                    final View view = LayoutInflater
                            .from(ReceiveActivity.this)
                            .inflate(R.layout.alert_confirm_single, null);

                    final EditText editText = view.findViewById(R.id.edit);
                    final TextView pathView = view.findViewById(R.id.path);
                    TextView brView = view.findViewById(R.id.br);
                    TextView sizeView = view.findViewById(R.id.size);

                    editText.setText(fileName);
                    pathView.setText(downloadPath);
                    pathView.setOnClickListener(onPathViewClick);
                    brView.setText(getString(R.string.format_br, UnitSelector.br(br)));
                    sizeView.setText(getString(R.string.format_size, UnitSelector.size(size)));

                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setTitle(R.string.alert_confirm_msg)
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String path = pathView.getText().toString();
                                            String name = editText.getText().toString();
                                            confirmPath(url, path, name);
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, onCancel)
                            .setCancelable(false)
                            .show();
                }
            });
        } else {
            confirmPath(url, downloadPath, fileName);
        }

    }

    private void confirmPath(final String url, String path, final String name) {
        final File file = new File(path, name);
        if (file.isFile()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isSingle) {
                        new AlertDialog.Builder(ReceiveActivity.this)
                                .setMessage(R.string.alert_file_exists_msg)
                                .setPositiveButton(android.R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface alertDialog,
                                                                final int which) {
                                                overwrite(url, file, name);
                                            }
                                        })
                                .setNegativeButton(android.R.string.no, onCancel)
                                .setCancelable(false)
                                .show();
                    }
                }
            });
        } else {
            download(url, file, name);
        }
    }

    private void overwrite(final String url, final File file, final String name) {
        if (file.delete()) {
            download(url, file, name);
        } else {
            new AlertDialog.Builder(ReceiveActivity.this)
                    .setMessage(R.string.alert_delete_fail)
                    .setCancelable(false)
                    .setPositiveButton(R.string.retry,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    overwrite(url, file, name);
                                }
                            })
                    .setNegativeButton(R.string.skip, onCancel)
                    .show();
        }
    }

    private void download(String url, File file, String name) {
        Uri uri = Uri.fromFile(file);
        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setDestinationUri(uri)
                .setVisibleInDownloadsUi(true)
                .setTitle(getString(R.string.app_name))
                .setDescription(name);
        manager.enqueue(request);
        if (isSingle) {
            downloading();
        }
    }

    private void switchType(String type, long id) throws JSONException {
        switch (type) {
            case "playlist":
                handleList(id);
                break;
            case "artist":
                handleArtist(id);
                break;
            case "album":
                handleAlbum(id);
                break;
        }
        downloading();
    }

    private void confirmPath(final String type, final long id) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater
                .from(ReceiveActivity.this)
                .inflate(R.layout.alert_confirm_path, null);
        final TextView pathView = view.findViewById(R.id.path);
        pathView.setText(downloadPath);
        pathView.setOnClickListener(onPathViewClick);

        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_confirm_path)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                downloadPath = pathView.getText().toString();
                                try {
                                    switchType(type, id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                })
                .setNegativeButton(android.R.string.cancel, onCancel)
                .setCancelable(false)
                .show();
    }

    private void downloading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                }
                Toast.makeText(ReceiveActivity.this, R.string.downloading, Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    private void parsing(final int current, final int count) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingMessage.setText(getString(R.string.parsing_rate, current, count));
            }
        });
    }

}
