package cfh.fgk.wt9;

import java.io.IOException;
import java.util.regex.Pattern;


public class TestClient extends Client {

    @Override
    public String get(String addr) throws IOException, InterruptedException {
        if (addr.equals("download.html")) {
            return download();
        }
        var matcher = Pattern.compile("download\\.html\\?spage=(\\d+)&npage=(\\d+)&action=download").matcher(addr);
        if (matcher.matches()) {
            var first = Integer.parseInt(matcher.group(1));
            var step = Integer.parseInt(matcher.group(2));
            var builder = new StringBuilder();
            for (var i = 0; i < step; i++) {
                builder.append(page(first+i));
            }
            return builder.toString();
        }
        throw new IllegalArgumentException("not implemented: " + addr);
    }
    
    private String download() {
        return """
                <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
               <html>
               <head>
               <title>AirDrive - Download</title>
               <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
               <meta name="title" content="AirDrive">
               <style type="text/css">
               body, table {
               font-family: Verdana, Arial, Helvetica, sans-serif; 
               background-color: #474747;
               color: #FFFFFF;
               text-align:center; 
               word-wrap: break-word;
               table-layout: fixed; 
               }
               #container {
               height: 100%;
               width: 100%;
               }
               #row0 {
               background: #474747;
               font-size: 60px;
               height: 150px;
               }
               #row1 {
               background: #7d7d7d;
               font-size: 30px;
               }
               #row2 {
               background: #474747;
               font-size: 45px;
               height: 120px;
               }
               #row3 {
               background: #7d7d7d;
               font-size: 45px;
               height: 120px;
               }
               input {
               font-family:Verdana, Arial, Helvetica, sans-serif;
               font-size: 30px;
               color: #000000;
               text-align:center;
               width: 80%;
               }
               input.checkbox
               {
               width: 30px;
               height: 30px;
               }
               select
               {
               font-size: 30px;
               }
               b {
               color: #faffbe;
               }
               </style>
               </head>
               <body>
               <table class="center" id="container">
               <tr id="row0"><td colspan="2">Download Log File</td></tr>
               <tr id="row1"><td><form action="download.html" method="GET"><br><b>START PAGE</b><br><br>This sets the starting page of the download.<br>Range 1...8        </td><td><input type="text" name="spage" maxlength="4" value="1"></td></tr>

               <tr id="row1"><td><br><br><b>PAGE COUNT</b><br><br>This sets number of pages to download<br>Range 1...5        </td><td><input type="text" name="npage" maxlength="1" value="1"></td></tr>

               <tr id="row2"><td colspan="2"><input type="hidden" name="action" value="download"><input type="submit" value="Download log file"></form></td></tr>
               <tr id="row3"><td colspan="2"><a href="index.html">Data Log</a> &nbsp;&nbsp; <a href="download.html">Download</a> &nbsp;&nbsp; <a href="settings.html">Settings</a></td></tr>
               </table>
               </body>
               </html>
               """;
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
