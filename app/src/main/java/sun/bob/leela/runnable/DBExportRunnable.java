package sun.bob.leela.runnable;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import sun.bob.leela.db.DaoMaster;
import sun.bob.leela.events.DBExportEvent;
import sun.bob.leela.utils.AppConstants;

/**
 * Created by bob.sun on 16/8/9.
 */
public class DBExportRunnable implements Runnable {

    private Context context;
    private String dbSrcPath;
    private String dbDstPath;

    public DBExportRunnable(Context context) {
        this.context = context;
        dbSrcPath = context.getDatabasePath(AppConstants.DB_NAME).getAbsolutePath();
//        dbDstPath = Environment.getExternalStorageDirectory() + "/data/data/leela/";
        dbDstPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "puff_export"  + File.separator;
        File dstPath = new File(dbDstPath);
        dstPath.setReadable(true, false);
        dstPath.setWritable(true, false);
        if (!dstPath.exists()) {
            dstPath.mkdirs();
        }
    }

    @Override
    public void run() {
        try {
            File source = new File(dbSrcPath);
            File dest   = new File(dbDstPath + "puff.db");
            dest.setReadable(true, false);
            dest.setWritable(true, false);
            if (dest.exists()) {
                dest.delete();
            } else {
                dest.createNewFile();
            }
            FileInputStream fis = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] cache = new byte[512];
            int byteRead;
            while ((byteRead = fis.read(cache)) > 0) {
                fos.write(cache, 0, byteRead);
            }
            fis.close();
            fos.close();
            EventBus.getDefault().post(new DBExportEvent(true, dest.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new DBExportEvent(false, null));
        }

        try {
            File nomedia   = new File(dbDstPath + ".nomedia");
            if (!nomedia.exists()) {
                nomedia.createNewFile();
            }
        }catch (Exception e){
        }

    }
}
