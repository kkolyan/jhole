package jhole.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Bootstrap {

    protected static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        try {
            InputStream config = Bootstrap.class.getClassLoader().getResourceAsStream("config.properties");
            System.getProperties().load(config);
            config.close();

            Collection<String> keys = new ArrayList<String>(System.getProperties().stringPropertyNames());

            boolean replacementsRemaining = true;
            while (replacementsRemaining) {
                replacementsRemaining = false;
                for (String key: keys) {
                    String value = System.getProperty(key);
                    for (String replacementKey: keys) {
                        String nv = value.replace("%"+replacementKey+"%", System.getProperty(replacementKey));
                        if (!nv.equals(value)) {
                            System.setProperty(key, nv);
                            replacementsRemaining = true;
                        }
                    }
                }
            }


            Console.launch(executor);


            if (Desktop.isDesktopSupported()) {
                TrayIcon icon = new TrayIcon(ImageIO.read(Bootstrap.class.getClassLoader().getResource("jhole.gif")));
                MenuItem openConsole = new MenuItem("Console");
                openConsole.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("http://localhost:"+Console.getPort()+"/history.html"));
                        } catch (Exception e1) {
                            throw new IllegalStateException(e1);
                        }
                    }
                });
                MenuItem exit = new MenuItem("Exit");
                exit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });
                MenuItem openLog = new MenuItem("Open Log");
                openLog.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        List<File> files = new ArrayList<File>();
                        File logDir = new File("logs");
                        File[] logFiles = logDir.listFiles();
                        if (logFiles != null) {
                            for (File file: logFiles) {
                                if (file.getName().endsWith(".log")) {
                                    files.add(file);
                                }
                            }
                            File last = Collections.max(files, new Comparator<File>() {
                                @Override
                                public int compare(File o1, File o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            try {
                                Desktop.getDesktop().browse(last.toURI());
                            } catch (IOException e1) {
                                throw new IllegalStateException(e1);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Logs unavailable in " + logDir.getAbsoluteFile());
                        }
                    }
                });
                icon.setPopupMenu(new PopupMenu());
                icon.getPopupMenu().add(openLog);
                icon.getPopupMenu().add(openConsole);
                icon.getPopupMenu().add(exit);
                SystemTray.getSystemTray().add(icon);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
