package cfh.fgk.wt9;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TestClient implements Client {

    @Override
    public String get(String addr) throws IOException, InterruptedException {
        if (addr.equals("download.html")) {
            return download();
        }
        Matcher matcher = Pattern.compile("download\\.html\\?spage=(\\d+)&npage=(\\d+)&action=download").matcher(addr);
        if (matcher.matches()) {
            int first = Integer.parseInt(matcher.group(1));
            int step = Integer.parseInt(matcher.group(2));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < step; i++) {
                builder.append(page(first+i));
            }
            return builder.toString();
        }
        throw new IllegalArgumentException("not implemented: " + addr);
    }
    
    private String download() {
        return ""
                + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n"
                + "<html>\n"
                + "<head>\n"
                + "<title>AirDrive - Download</title>\n"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">\n"
                + "<meta name=\"title\" content=\"AirDrive\">\n"
                + "<style type=\"text/css\">\n"
                + "body, table {\n"
                + "font-family: Verdana, Arial, Helvetica, sans-serif; \n"
                + "background-color: #474747;\n"
                + "color: #FFFFFF;\n"
                + "text-align:center; \n"
                + "word-wrap: break-word;\n"
                + "table-layout: fixed; \n"
                + "}\n"
                + "#container {\n"
                + "height: 100%;\n"
                + "width: 100%;\n"
                + "}\n"
                + "#row0 {\n"
                + "background: #474747;\n"
                + "font-size: 60px;\n"
                + "height: 150px;\n"
                + "}\n"
                + "#row1 {\n"
                + "background: #7d7d7d;\n"
                + "font-size: 30px;\n"
                + "}\n"
                + "#row2 {\n"
                + "background: #474747;\n"
                + "font-size: 45px;\n"
                + "height: 120px;\n"
                + "}\n"
                + "#row3 {\n"
                + "background: #7d7d7d;\n"
                + "font-size: 45px;\n"
                + "height: 120px;\n"
                + "}\n"
                + "input {\n"
                + "font-family:Verdana, Arial, Helvetica, sans-serif;\n"
                + "font-size: 30px;\n"
                + "color: #000000;\n"
                + "text-align:center;\n"
                + "width: 80%;\n"
                + "}\n"
                + "input.checkbox\n"
                + "{\n"
                + "width: 30px;\n"
                + "height: 30px;\n"
                + "}\n"
                + "select\n"
                + "{\n"
                + "font-size: 30px;\n"
                + "}\n"
                + "b {\n"
                + "color: #faffbe;\n"
                + "}\n"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<table class=\"center\" id=\"container\">\n"
                + "<tr id=\"row0\"><td colspan=\"2\">Download Log File</td></tr>\n"
                + "<tr id=\"row1\"><td><form action=\"download.html\" method=\"GET\"><br><b>START PAGE</b><br><br>This sets the starting page of the download.<br>Range 1...8        </td><td><input type=\"text\" name=\"spage\" maxlength=\"4\" value=\"1\"></td></tr>\n"
                + "\n"
                + "<tr id=\"row1\"><td><br><br><b>PAGE COUNT</b><br><br>This sets number of pages to download<br>Range 1...5        </td><td><input type=\"text\" name=\"npage\" maxlength=\"1\" value=\"1\"></td></tr>\n"
                + "\n"
                + "<tr id=\"row2\"><td colspan=\"2\"><input type=\"hidden\" name=\"action\" value=\"download\"><input type=\"submit\" value=\"Download log file\"></form></td></tr>\n"
                + "<tr id=\"row3\"><td colspan=\"2\"><a href=\"index.html\">Data Log</a> &nbsp;&nbsp; <a href=\"download.html\">Download</a> &nbsp;&nbsp; <a href=\"settings.html\">Settings</a></td></tr>\n"
                + "</table>\n"
                + "</body>\n"
                + "</html>";
    }
    
    private String page(int i) {
        return new String[] {
                null,
                "A12345678A",
                "B12345678B",
                "C12345678C",
                "D12345678D",
                "E12345678E",
                "F12345678F",
                "G12345678G",
                "H12345678H",
        }[i];
    }
}
