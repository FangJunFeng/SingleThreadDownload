package com.alandy.singlethreaddownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alandy.singlethreaddownload.contants.Contants;
import com.alandy.singlethreaddownload.entities.FileInfo;
import com.alandy.singlethreaddownload.services.DownloadService;
import com.alandy.singlethreaddownload.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView mFileName;
    private ProgressBar mProgressBar;
    private Button mStartDownload;
    private Button mStopDownload;
    private FileInfo mFileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mFileInfo = new FileInfo(0, "http://sqdd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk", "mobileqq_android.apk", 0, 0);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(Contants.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);

    }

    private void initView() {
        mFileName = (TextView) findViewById(R.id.tv_file_name);
        mFileName.setText("mobileqq_android.apk");
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setMax(100);
        mStartDownload = (Button) findViewById(R.id.bt_start_download);
        mStartDownload.setOnClickListener(this);
        mStopDownload = (Button) findViewById(R.id.bt_stop_download);
        mStopDownload.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_start_download:
                Intent startIntent = new Intent(this, DownloadService.class);
                startIntent.putExtra("fileInfo", mFileInfo);
                startIntent.setAction(Contants.ACTION_START);
                startService(startIntent);
                break;
            case R.id.bt_stop_download:
                Intent stopIntent = new Intent(this, DownloadService.class);
                stopIntent.setAction(Contants.ACTION_STOP);
                stopIntent.putExtra("fileInfo", mFileInfo);
                startService(stopIntent);
                break;
            default:
                break;
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Contants.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                mProgressBar.setProgress(finished);
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 按了返回键时应暂停下载
        if (KeyEvent.KEYCODE_BACK == keyCode && mStopDownload != null){
            mStopDownload.performClick();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
