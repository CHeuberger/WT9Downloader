package cfh.fgk.wt9;

import java.io.IOException;


public interface Client {
    
    public String get(String addr) throws IOException, InterruptedException;
}
