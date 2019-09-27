package com.fanphotographer.utility;


import android.os.Environment;
import android.util.Log;

import java.io.File;



public class ImageUtils {



    public static String getRootDirPath() {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/FanPhoto");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static void deleteRootDirPath() {

        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures/FanPhoto";
        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                files[i].delete();
            }
        }



    }






}
