package sun.bob.leela.runnable;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import sun.bob.leela.events.DBImportEvent;
import sun.bob.leela.utils.AppConstants;

import static sun.bob.leela.utils.StringUtil.isNullOrEmpty;

/**
 * Created by bob.sun on 16/8/9.
 */
public class DBImportRunnable implements Runnable {

    private Context context;
    private String dbSrcPath;
    private String dbDstPath;

    public DBImportRunnable(Context context, String dbSrc) {
        this.context = context;
        dbSrcPath = dbSrc;
        dbDstPath = context.getDatabasePath(AppConstants.DB_NAME).getAbsolutePath();
    }

    @Override
    public void run() {
        try {
            File source = new File(dbSrcPath);
            if (isNullOrEmpty(dbSrcPath) ||  !dbSrcPath.endsWith(".db") || !source.exists()) {
                EventBus.getDefault().post(new DBImportEvent(false, null, dbSrcPath));
                return;
            }
            File dest   = new File(dbDstPath);
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { }
            EventBus.getDefault().post(new DBImportEvent(true, dest.getAbsolutePath(), null));
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new DBImportEvent(false, null, e.getMessage()));
        }

    }
}
