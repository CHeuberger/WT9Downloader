package cfh.fgk.wt9;

import static java.awt.GridBagConstraints.*;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        new Main();
    }
    
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
    
    private final Client client;
    
    private Main() {
        client = new TestClient();
        SwingUtilities.invokeLater(this::initGUI);
    }
    
    private void initGUI() {
        start.setModel(startModel);
        startModel.addChangeListener(e -> enable(true));
        
        end.setModel(endModel);
        endModel.addChangeListener(e -> enable(true));
        
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
        log.setColumns(60);
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
        
        log("Download page loaded: %d%n", body.length());
        final var matcher = PAGE_RANGE.matcher(body);
        if (!matcher.find() || matcher.group(1) == null) {
            handle(new IOException("unable to find page range"));
            enable(false);
            return;
        }
        
        final int first = Integer.parseInt(matcher.group(1));
        log("first: %d%n", first);
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
            log("last: %d%n", last);
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
        // TODO
    }
    
    private void enable(boolean enabled) {
        start.setEnabled(enabled);
        end.setEnabled(enabled && (int)endModel.getMaximum() != (int)endModel.getMinimum());
        download.setEnabled(enabled && (int)startModel.getValue() <= (int)endModel.getValue());
    }
    
    private void log(String format, Object... args) {
        log.append(String.format(format, args));
    }
    
    private void handle(Exception ex) {
        ex.printStackTrace();
        log("%n%s%n", ex);
    }
}
