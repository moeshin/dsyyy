package site.littlehands.dsyyy.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import site.littlehands.dsyyy.R;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE});

        // Test
//        String test = "分享M.Graveyard的专辑《ずっと、ふたり。》: http://music.163.com/album/34678333/?userid=412128285 (来自@网易云音乐)";
//        String test = "分享歌手M.Graveyard http://music.163.com/artist?id=20579&userid=412128285　(来自@网易云音乐)";
//        String test = "分享M.Graveyard的单曲《Hope》: http://music.163.com/song/28306025/?userid=412128285 (来自@网易云音乐)";
//        String test = "分享Littlehands创建的歌单「hope」: http://music.163.com/playlist/2673407205/412128285/?userid=412128285 (来自@网易云音乐)";
//        Intent intent = new Intent(this, ReceiveActivity.class)
//                .setAction(Intent.ACTION_SEND)
//                .setType("text/plain")
//                .putExtra(Intent.EXTRA_TEXT, test);
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
