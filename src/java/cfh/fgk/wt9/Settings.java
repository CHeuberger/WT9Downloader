package cfh.fgk.wt9;

public class Settings {
    
    public final static Settings instance = new Settings();

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
}
