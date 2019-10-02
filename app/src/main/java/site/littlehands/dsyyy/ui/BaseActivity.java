package site.littlehands.dsyyy.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * 权限请求代码
     */
    private static final int REQUEST_CODE_PERMISSION = 1;

    /**
     * 权限请求结果
     *
     * @param requestCode   权限请求代码
     * @param permissions   请求的权限
     * @param grantResults  请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == REQUEST_CODE_PERMISSION) {
            List<String> permList = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    permList.add(permissions[i]);
                }
            }
            if (permList.size() != 0) {
                final String[] permArr = new String[permList.size()];
                permList.toArray(permArr);
                requestPermissions(permArr, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onCreated();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onCreated();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        onCreated();
    }

    /**
     * 请求权限
     *
     * @param permissions 请求的权限
     */
    public void requestPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * 创建完毕
     */
    protected void onCreated() {}

}
