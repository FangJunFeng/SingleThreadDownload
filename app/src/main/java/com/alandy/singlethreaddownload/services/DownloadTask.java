package com.alandy.singlethreaddownload.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alandy.singlethreaddownload.MainActivity;
import com.alandy.singlethreaddownload.contants.Contants;
import com.alandy.singlethreaddownload.db.ThreadDAO;
import com.alandy.singlethreaddownload.db.ThreadDAOImpl;
import com.alandy.singlethreaddownload.entities.FileInfo;
import com.alandy.singlethreaddownload.entities.ThreadInfo;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 * Created by AlandyFeng on 2015/10/25.
 */
public class DownloadTask {
    private int mFinished;
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDao;
    public boolean isPause = false;
    private static final String TAG = "DownloadTask";

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDAOImpl(mContext);
    }

    public void download(){
        // 读取数据库的线程信息
        List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (0 == threads.size()){
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        }else {
            threadInfo = threads.get(0);
        }
        // 创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread{
        private ThreadInfo mThreadInfo = null;
        public DownloadThread(ThreadInfo info){
            this.mThreadInfo = info;
        }
        @Override
        public void run() {
            //向数据库插入线程信息
            if (!mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())){
                mDao.insertThread(mThreadInfo);
            }
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    //设置下载位置
                    int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                    conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                    //设置文件写入位置
                    File file = new File(Contants.DOWNLOAD_PATH, mFileInfo.getFileName());
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(start);

                    Intent intent = new Intent();
                    intent.setAction(Contants.ACTION_UPDATE);
                    mFinished += mThreadInfo.getFinished();
                    //开始下载
                    if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT){
                        //读取数据
                        inputStream = conn.getInputStream();
                        byte[] buf = new byte[1024 * 4];
                        int len = -1;
                        long time = System.currentTimeMillis();
                        while ((len = inputStream.read(buf)) != -1){
                            //写入文件
                            raf.write(buf, 0, len);
                            //把下载进度通过广播发送给Activity
                            mFinished += len;
                            if (System.currentTimeMillis() - time > 500){
                                time = System.currentTimeMillis();
                                intent.putExtra("finished", mFinished * 100 / mThreadInfo.getEnd());
                                mContext.sendBroadcast(intent);
                            }

                            //在下载暂停时，保存下载进度
                            if (isPause){
                                mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                                return;
                            }
                        }

                        //删除线程信息
                        mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                        Log.i(TAG, "下载完毕");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try{
                        if (conn != null){
                            conn.disconnect();
                        }
                        if (raf != null){
                            raf.close();
                        }
                        if (inputStream != null){
                            inputStream.close();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


        }
    }
}
