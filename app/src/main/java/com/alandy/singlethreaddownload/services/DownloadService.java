package com.alandy.singlethreaddownload.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alandy.singlethreaddownload.contants.Contants;
import com.alandy.singlethreaddownload.entities.FileInfo;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by AlandyFeng on 2015/10/23.
 */
public class DownloadService extends Service {

    private static final String TAG = "DownloadService";
    private DownloadTask mTask;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Contants.MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.i(TAG, "Init:" + fileInfo);
                    //启动下载任务
                    mTask = new DownloadTask(DownloadService.this, fileInfo);
                    mTask.download();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 获得Activity传过来的参数
        FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
        if (intent.getAction().equals(Contants.ACTION_START)){
            Log.i(TAG, "DownloadService Start" + fileInfo.toString());
            //启动初始化线程
            new InitThread(fileInfo).start();
        }else if (intent.getAction().equals(Contants.ACTION_STOP)){
            Log.i(TAG, "DownloadService Stop" + fileInfo.toString());
            if (mTask != null){
                mTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class InitThread extends Thread{
        private FileInfo mFileInfo;
        public InitThread(FileInfo fileInfo){
            this.mFileInfo = fileInfo;
        }
        @Override
        public void run() {
            RandomAccessFile raf = null;
            //连接网络文件
            HttpURLConnection connection = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");

                int length = -1;
                if (connection.getResponseCode() == HttpStatus.SC_OK){
                    //获取文件长度
                    length = connection.getContentLength();
                }
                if (length <= 0){
                    Log.e(TAG, "Download file error");
                    return;
                }
                //在本地创建文件
                File dir = new File(Contants.DOWNLOAD_PATH);
                if (!dir.exists()){
                    dir.mkdir();
                }
                File file = new File(dir, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(Contants.MSG_INIT, mFileInfo).sendToTarget();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    connection.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
