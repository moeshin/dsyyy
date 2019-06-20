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
import site.littlehands.util.UnitSelector;

public class ReceiveActivity extends Activity {


    private String TAG = "ReceiveActivity";

    private SharedPreferences preferences;

    private boolean isConfirm;

    private String format;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isConfirm = preferences.getBoolean("isConfirm", true);
        format = preferences.getString(SettingUtils.KEY_FORMAT, SettingUtils.FORMAT_DEFAULT);


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

        String type = matcher.group(1);
        String id = matcher.group(2);
        Log.d(TAG, "ID: " + id);

        //noinspection SwitchStatementWithTooFewBranches
        switch (type) {
            case "song":
                handleId(id);
                break;
            // TODO Will support more shared text type
        }
        return true;
    }

    private void handleId(final String id) {
        @SuppressLint("InflateParams")
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(R.layout.alert_loading, null))
                .setCancelable(false)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                handleDetails(httpGetString(NeteaseMusicAPI.details(id)));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                    }
                });
            }
        }).start();
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

    @SuppressWarnings("UnnecessaryContinue")
    private void handleDetails(String details) {
        if (details == null) {
            new AlertDialog.Builder(ReceiveActivity.this)
                    .setMessage(R.string.network_error)
                    .setPositiveButton(android.R.string.ok, onCancel)
                    .setCancelable(false)
                    .show();
            return;
        }
        try {
            JSONObject json = new JSONObject(details);
            JSONArray songs = json.getJSONArray("songs");

            int length = songs.length();
            for (int i = 0; i < length; i++) {
                try {
                    JSONObject song = songs.getJSONObject(i);
                    String name = song.getString("name");
                    String artists = NeteaseMusicAPI.artistsToString(song);
                    String album = song.getJSONObject("al").getString("name");
                    int id = song.getInt("id");


                    JSONObject data = new JSONObject(httpGetString(NeteaseMusicAPI.urls(9999999, id)))
                            .getJSONArray("data")
                            .getJSONObject(0);

                    String url = data.getString("url");
                    int br = data.getInt("br");
                    int size = data.getInt("size");

                    preDownload(
                            url,
                            br,
                            size,
                            name,
                            artists,
                            album
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getSuffix(String str) {
        return str.substring(str.lastIndexOf('.'));
    }

    private void preDownload(
            final String url, final int br, final int size,
            final String name, String artist, String album
    ) {


        String tmp = preferences.getString(SettingUtils.KEY_PATH, null);

        if (tmp == null) {
            tmp = Environment.getExternalStorageDirectory().getPath();
        }

        final String path = tmp;

        final String fileName = SettingUtils.format(Objects.requireNonNull(format),
                name.replace('/', '／'),
                artist.replace('/', '／'),
                album.replace('/', '／')
        ) + getSuffix(url);

        if (isConfirm) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    @SuppressLint("InflateParams")
                    final View view = LayoutInflater
                            .from(ReceiveActivity.this)
                            .inflate(R.layout.alert_confirm, null);

                    final EditText editText = view.findViewById(R.id.edit);
                    final TextView pathView = view.findViewById(R.id.path);
                    TextView brView = view.findViewById(R.id.br);
                    TextView sizeView = view.findViewById(R.id.size);

                    editText.setText(fileName);
                    pathView.setText(path);
                    pathView.setOnClickListener(onPathViewClick);
                    brView.setText(getString(R.string.format_br, UnitSelector.bitRate(br)));
                    sizeView.setText(getString(R.string.format_size, UnitSelector.Byte(size)));

                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setTitle(R.string.alert_confirm_name_msg)
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
            confirmPath(url, path, fileName);
        }

    }

    private void confirmPath(final String url, String path, final String name) {
        final File file = new File(path, name);
        if (file.isFile()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setMessage(R.string.alert_file_exists_msg)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface alertDialog, final int which1) {
                                    overwrite(url, file, name);
                                }
                            })
                            .setNegativeButton(android.R.string.no, onCancel)
                            .setCancelable(false)
                            .show();
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
        finish();
    }

}
