package cfh.fgk.wt9;

import java.io.File;
import java.util.Objects;

public class Settings {
    
    public final static Settings instance = new Settings();

    private File lastFile = new File(".");  // TODO preferences?
    
    private Settings() {
    }
    
    public String url() {
        // TODO
        return "http://192.168.4.1/";
    }
    
    public int timeout() {
        // TODO
        return 20;
    }
    
    public File lastFile() {
        return lastFile;
    }
    
    public void lastFile(File lastFile) {
        this.lastFile = Objects.requireNonNull(lastFile);
    }
}
