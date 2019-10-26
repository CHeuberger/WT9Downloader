package cfh.fgk.wt9;

import static java.awt.GridBagConstraints.*;
import static javax.swing.JOptionPane.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

public class Main {

    public static void main(String[] args) {
        new Main();
    }
    
    private final static String DOWNLOAD_URL = "download.html";
    private final static String ACTION_URL = DOWNLOAD_URL + "?spage=%d&npage=%d&action=download";
    
    private final Pattern PAGE_RANGE = Pattern.compile("(?m)"
            + "<b>START PAGE</b><br>"
            + "<br>This sets the starting page of the download\\.<br>"
            + "Range (\\d+)(?:\\.\\.\\.(\\d+))? ");
    
    private final JFrame frame = new JFrame();
    private final JSpinner start = new JSpinner();
    private final JSpinner end = new JSpinner();
    private final JButton download = new JButton("Download");
    private final JTextArea log = new JTextArea();
    
    private final SpinnerNumberModel startModel = new SpinnerNumberModel(1, 1, 9999, 1);
    private final SpinnerNumberModel endModel = new SpinnerNumberModel(9999, 1, 9999, 1);
    
    private final Settings settings;
    private final Client client;

    private Main() {
        settings = Settings.instance;
        client = new TestClient();
        SwingUtilities.invokeLater(this::initGUI);
    }
    
    private void initGUI() {
        start.setModel(startModel);
        end.setModel(endModel);
        
        startModel.addChangeListener(this::doSpinner);
        endModel.addChangeListener(this::doSpinner);
        
        download.addActionListener(this::doDownload);
        
        enable(false);
        
        var insets = new Insets(2, 2, 2, 2);
        var gbcLabel = new GridBagConstraints(0, RELATIVE, 1, 1, 0.0, 0.0,BASELINE_LEADING, NONE, insets , 0, 0);
        var gbcField = new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 1.0, 0.0,BASELINE_LEADING, NONE, insets , 0, 0);
        
        var panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        panel.add(new JLabel("Start: "), gbcLabel);
        panel.add(start, gbcField);
        panel.add(new JLabel("End: "), gbcLabel);
        panel.add(end, gbcField);
        panel.add(download, gbcField);
        
        log.setEditable(false);
        log.setFont(new Font("monospaced", Font.PLAIN, 12));
        log.setColumns(80);
        log.setRows(20);
        
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(new JScrollPane(log), BorderLayout.SOUTH);
        
        frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        reload();
    }
    
    private void reload() {
        log("reload%n");
        String body;
        try {
            body = client.get("download.html");
        } catch (Exception ex) {
            handle(ex);
            return;
        }
        
        log("download page loaded: %d%n", body.length());
        final var matcher = PAGE_RANGE.matcher(body);
        if (!matcher.find() || matcher.group(1) == null) {
            handle(new IOException("unable to find page range"));
            enable(false);
            return;
        }
        
        final int first = Integer.parseInt(matcher.group(1));
        log("  first: %d%n", first);
        if (matcher.group(2) == null) {
            startModel.setValue(first);
            startModel.setMinimum(first);
            startModel.setMaximum(first);
            endModel.setValue(first);
            endModel.setMinimum(first);
            endModel.setMaximum(first);
            enable(true);
        } else {
            final int last = Integer.parseInt(matcher.group(2));
            log("  last: %d%n", last);
            startModel.setValue(first);
            startModel.setMinimum(first);
            startModel.setMaximum(last);
            endModel.setMinimum(first);
            endModel.setMaximum(last);
            endModel.setValue(last);
            enable(true);
        }
    }
    
    private void doDownload(ActionEvent ev) {
        var first = startModel.getNumber().intValue();
        var last = endModel.getNumber().intValue();
        log("download pages %d to %d%n", first, last);
        
        var file = settings.lastFile();
        var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileSelectionMode(chooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(file.getAbsoluteFile().getParentFile());
        chooser.setSelectedFile(file);
        if (chooser.showSaveDialog(frame) != chooser.APPROVE_OPTION) {
            log("canceled%n%n");
            return;
        }
        file = chooser.getSelectedFile();
        settings.lastFile(file);
        
        // TODO exists?
        if (file.exists()) {
            var message = new String[] {
                file.getName(),
                "File already exists.",
                "Overwrite?"
            };
            if (showConfirmDialog(frame, message, "Confirm", YES_NO_OPTION) != YES_OPTION) {
                log("  canceled%n%n");
                return;
            }
            var name = file.getName();
            var index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index);
            }
            var bak = new File(file.getParentFile(), name + ".bak");
            if (bak.exists()) {
                if (bak.delete()) {
                    log("  deleted %s%n", bak);
                }
            }
            if (file.renameTo(bak)) {
                log("  %s renamed to %s%n", file, bak.getName());
            } else {
                log("unable to rename %s to %s%n", file, bak.getName());
                return;
            }
        }
        
        var builder = new StringBuilder();
        try (Writer out = new FileWriter(file)) {
            while (first <= last) {
                var step = Math.min(last-first+1, 5);
                String text = client.get(String.format(ACTION_URL, first, step));
                out.append(text);
                builder.append(text);
                log("  %d + %d: %d%n", first, step, text.length());
                first += step;
            }
        } catch (Exception ex) {
            handle(ex);
        }
        log("  read %d%n<%s>%n", builder.length(), builder);
        log("  saved to %s%n", file);
    }
    
    private void doSpinner(ChangeEvent ev) {
        if (ev.getSource() == startModel) {
            if (endModel.getNumber().intValue() < startModel.getNumber().intValue()) {
                endModel.setValue(start.getValue());
            }
        } else {
            if (startModel.getNumber().intValue() > endModel.getNumber().intValue()) {
                startModel.setValue(endModel.getValue());
            }
        }
        enable(true);
    }
    
    private void enable(boolean enabled) {
        start.setEnabled(enabled);
        end.setEnabled(enabled && (int)endModel.getMaximum() != (int)endModel.getMinimum());
        download.setEnabled(enabled && startModel.getNumber().intValue() <= endModel.getNumber().intValue());
    }
    
    private void log(String format, Object... args) {
        log.append(String.format(format, args));
    }
    
    private void handle(Exception ex) {
        ex.printStackTrace();
        log("%n%s%n", ex);
    }
}
