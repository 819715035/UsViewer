package leltek.viewer.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leltek.viewer.R;
import leltek.viewer.constants.AppConstant;

public class CheckPermission extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_FILE = 1;
    int requestCode;
    public static final String KEY = "key";
    @BindView(R.id.permissionMessage)
    TextView permissionMessage;
    @BindView(R.id.permissionLayout)
    LinearLayout permissionLayout;


    @OnClick(R.id.recheckPermission)
    public void checkPermission()
    {
        checkPermissionHere();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission);
        ButterKnife.bind(this);
        permissionLayout.setVisibility(View.GONE);
        requestCode = getIntent().getIntExtra(KEY, 0);
        checkPermissionHere();
    }

    private void setMessage(String message) {
        permissionMessage.setText(message);
    }

    private void checkPermissionHere() {
        switch (requestCode) {
            case AppConstant.CHECK_STORAGE:
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_FILE);
                break;
        }
    }


    public static boolean isPermissionGranted(Activity activity, String permission) {
        // Here, this is the current activity
        return ContextCompat.checkSelfPermission(activity,
                permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    void setResultForParent() {
        Intent intent = new Intent();
        intent.putExtra(AppConstant.IS_PERMISSION_GRANTED, true);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_FILE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setResult(Activity.RESULT_OK);
                    setResultForParent();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    setMessage(getString(R.string.permission_wr_missing));
                    permissionLayout.setVisibility(View.VISIBLE);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
