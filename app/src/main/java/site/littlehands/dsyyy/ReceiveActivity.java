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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import site.littlehands.app.DirectorySelectorDialog;

public class ReceiveActivity extends Activity {


    private String TAG = "ReceiveActivity";

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

        // 获取分享文本
        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "Type: " + type);
        if (Intent.ACTION_SEND.equals(action) && type != null){
            if ("text/plain".equals(type)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);

                Log.d(TAG, "Text: " + text);

                Pattern pattern = Pattern.compile("/song/(\\d+)/");
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    String id = matcher.group(1);
                    Log.d(TAG, "ID: " + id);
                    parseId(id);
                    return;
                }
            }
        }

        startActivity(new Intent(this, MainActivity.class));
    }

    private void parseId(final String id) {

        @SuppressLint("InflateParams")
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(R.layout.alert_loading, null))
                .setCancelable(false)
                .show();



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://api.littlehands.site/dsyyy/?id=" + id);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuilder json = new StringBuilder();
                    String tmp;
                    while ((tmp = reader.readLine()) != null) {
                        json.append(tmp);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                        }
                    });

                    parseJson(json.toString());
                    reader.close();
                    isr.close();
                    is.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.getInt("code") == 1) {
                JSONObject data = obj.getJSONObject("data");
                preDownload(
                        data.getString("url"),
                        data.getString("name"),
                        getArtist(data),
                        data.getString("album")
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getArtist(JSONObject data) throws JSONException {
        JSONArray artists = data.getJSONArray("artist");
        StringBuilder artist = new StringBuilder();
        int size = artists.length();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                artist.append(',');
            }
            artist.append(artists.getString(i));
        }
        return artist.toString();
    }

    private String getSuffix(String str) {
        return str.substring(str.lastIndexOf('.'));
    }

    private void preDownload(final String url, final String name, String artist, String album) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        String tmp = preferences.getString(SettingUtils.KEY_PATH, null);
        String format = preferences.getString(SettingUtils.KEY_FORMAT, "%song% - %author%");

        if (tmp == null) {
            tmp = Environment.getExternalStorageDirectory().getPath();
        }

        final String path = tmp;

        final String fileName = SettingUtils.format(Objects.requireNonNull(format), name, artist, album) + getSuffix(url);

        if (preferences.getBoolean("isConfirm", true)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    @SuppressLint("InflateParams")
                    final View view = LayoutInflater
                            .from(ReceiveActivity.this)
                            .inflate(R.layout.alert_confirm, null);

                    final EditText editText = view.findViewById(R.id.edit);
                    final TextView pathView = view.findViewById(R.id.path);

                    editText.setText(fileName);
                    pathView.setText(path);
                    pathView.setOnClickListener(onPathViewClick);

                    new AlertDialog.Builder(ReceiveActivity.this)
                            .setTitle(R.string.alert_confirm_name_msg)
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String path = pathView.getText().toString();
                                    String name = editText.getText().toString();
                                    confirmPath(url, path, name);
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
                    final AlertDialog alertDialog = new AlertDialog.Builder(ReceiveActivity.this)
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
