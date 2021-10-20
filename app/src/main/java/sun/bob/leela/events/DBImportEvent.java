package sun.bob.leela.events;

/**
 * Created by bob.sun on 16/8/9.
 */
public class DBImportEvent {
    public boolean success;
    public String filePath;
    public String extraInfo;

    public DBImportEvent(boolean success, String filePath,String extraInfo) {
        this.success = success;
        this.filePath = filePath;
        this.extraInfo = extraInfo;
    }
}
