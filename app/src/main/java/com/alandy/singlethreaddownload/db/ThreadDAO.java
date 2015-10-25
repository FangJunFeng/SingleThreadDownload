package com.alandy.singlethreaddownload.db;

import com.alandy.singlethreaddownload.entities.ThreadInfo;

import java.util.List;

/**
 * 数据访问接口
 * Created by AlandyFeng on 2015/10/23.
 */
public interface ThreadDAO {
    /**
     * 插入线程信息
     * @param threadInfo 线程信息
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程
     * @param url 下载文件地址
     * @param thread_id 线程id
     */
    public void deleteThread(String url, int thread_id);

    /**
     * 更新下载进度
     * @param url 下载文件地址
     * @param thread_id 线程id
     * @param finished 已经完成下载进度
     */
    public void updateThread(String url, int thread_id, int finished);

    /**
     * 查询文件的线程信息
     * @param url 下载文件地址
     * @return 线程信息
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url 下载文件地址
     * @param thread_id 线程id
     * @return
     */
    public boolean isExists(String url, int thread_id);
}
