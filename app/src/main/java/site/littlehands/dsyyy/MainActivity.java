package site.littlehands.dsyyy;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import site.littlehands.app.BaseActivity;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE});

        // Test
//        Intent intent = new Intent(this, ReceiveActivity.class)
//                .setAction(Intent.ACTION_SEND)
//                .setType("text/plain")
//                .putExtra(Intent.EXTRA_TEXT, "分享Roger Subirana的单曲《Between Worlds》: http://music.163.com/song/28661549/?userid=412128285 (来自@网易云音乐)");
//        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
        }
        return super.onKeyDown(keyCode, event);
    }

}
