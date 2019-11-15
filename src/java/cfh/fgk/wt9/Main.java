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
import java.io.PrintStream;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;

public class Main {

    public static void main(String[] args) {
        boolean test = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h")) {
                usage(null);
                return;
            } else if (arg.equals("-t")) {
                test = true;
            } else {
                usage("unrecognized parameter " + arg);
                return;
            }
        }
        new Main(test);
    }
    
    private static void usage(String message) {
        PrintStream out;
        if (message != null) {
            out = System.err;
            out.println();
            out.println(message);
        } else {
            out = System.out;
        }
        out.println();
        out.println("Options:\n"
                + "    -h    this help\n"
                + "    -t    test mode, read test data\n");
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
    private final JButton reload = new JButton("Reload");
    private final JButton download = new JButton("Download");
    private final JButton clear = new JButton("Clear");
    private final JTextArea log = new JTextArea();
    
    private final SpinnerNumberModel startModel = new SpinnerNumberModel(1, 1, 9999, 1);
    private final SpinnerNumberModel endModel = new SpinnerNumberModel(9999, 1, 9999, 1);
    
    private final Settings settings = Settings.instance;
    private final Client client;
    
    private Main(boolean test) {
        client = test ? new TestClient() : new Client13();
        SwingUtilities.invokeLater(this::initGUI);
    }
    
    private void initGUI() {
        start.setModel(startModel);
        end.setModel(endModel);
        
        startModel.addChangeListener(this::doSpinner);
        endModel.addChangeListener(this::doSpinner);
        
        reload.addActionListener(this::doReload);
        download.addActionListener(this::doDownload);
        clear.addActionListener(this::doClear);
        
        Box buttons = Box.createHorizontalBox();
        buttons.add(reload);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(download);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(clear);
        
        enable(false);
        
        Insets insets = new Insets(2, 2, 2, 2);
        GridBagConstraints gbcLabel = new GridBagConstraints(0, RELATIVE, 1, 1, 0.0, 0.0,BASELINE_LEADING, NONE, insets , 0, 0);
        GridBagConstraints gbcField = new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 1.0, 0.0,BASELINE_LEADING, NONE, insets , 0, 0);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        panel.add(new JLabel("Start: "), gbcLabel);
        panel.add(start, gbcField);
        panel.add(new JLabel("End: "), gbcLabel);
        panel.add(end, gbcField);
        panel.add(buttons, new GridBagConstraints(RELATIVE, RELATIVE, REMAINDER, 1, 1.0, 0.0,BASELINE_LEADING, HORIZONTAL, insets , 0, 0));
        
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
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return client.get("download.html");
            }
            @Override
            protected void done() {
                reload.setEnabled(true);
                if (!isCancelled()) {
                    String body;
                    try {
                        body = get();
                    } catch (ExecutionException ex) {
                        handle(ex.getCause());
                        return;
                    } catch (Exception ex) {
                        handle(ex);
                        return;
                    }
                    log("download page loaded: %d%n", body.length());
                    final Matcher matcher = PAGE_RANGE.matcher(body);
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
            }
        };
        
        reload.setEnabled(false);
        enable(false);
        log("reloading...%n");
        worker.execute();
    }
    
    private void doReload(ActionEvent ev) {
        enable(false);
        reload();
    }
    
    private void doDownload(ActionEvent ev) {
        int first = startModel.getNumber().intValue();
        int last = endModel.getNumber().intValue();
        log("pages %d to %d%n", first, last);
        
        File file = settings.lastFile();
        JFileChooser chooser = new JFileChooser();
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
        
        if (file.exists()) {
            Object message = new String[] {
                file.getName(),
                "File already exists.",
                "Overwrite?"
            };
            if (showConfirmDialog(frame, message, "Confirm", YES_NO_OPTION) != YES_OPTION) {
                log("  canceled%n%n");
                return;
            }
            String name = file.getName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index);
            }
            File bak = new File(file.getParentFile(), name + ".bak");
            if (bak.exists()) {
                if (bak.delete()) {
                    log("  deleted %s%n", bak);
                }
            }
            if (file.renameTo(bak)) {
//                log("  %s renamed to %s%n", file, bak.getName());
            } else {
                log("unable to rename %s to %s%n", file, bak.getName());
                return;
            }
        }
        
        StringBuilder builder = new StringBuilder();
        try (Writer out = new FileWriter(file)) {
            while (first <= last) {
                int step = Math.min(last-first+1, 5);
                String text = client.get(String.format(ACTION_URL, first, step));
                out.append(text);
                builder.append(text);
                log("  %d + %d: %d%n", first, step, text.length());
                first += step;
            }
        } catch (Exception ex) {
            handle(ex);
        }
        log("  read %d%n", builder.length());
        log("  saved to %s%n", file);
        System.out.printf("read <%s>%n%d%n", builder, builder.length());
    }
    
    private void doClear(ActionEvent ev) {
        log.setText("");
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
        end.setEnabled(enabled && !Objects.equals(endModel.getMaximum(), endModel.getMinimum()));
        download.setEnabled(enabled && startModel.getNumber().intValue() <= endModel.getNumber().intValue());
    }
    
    private void log(String format, Object... args) {
        log.append(String.format(format, args));
        // TODO scroll
    }
    
    private void handle(Throwable throwable) {
        throwable.printStackTrace();
        log("%n%s%n", throwable);
    }
}
