package com.alandy.singlethreaddownload.contants;

import android.os.Environment;

/**
 * Created by AlandyFeng on 2015/10/23.
 */
public class Contants {
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISHED = "ACTION_FINISHED";

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/Downlaods/";

    public static final int MSG_INIT = 0;
}
