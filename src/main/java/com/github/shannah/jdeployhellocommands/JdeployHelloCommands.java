package com.github.shannah.jdeployhellocommands;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.desktop.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Properties;

public class JdeployHelloCommands extends JFrame {

    private JTextArea fileContentArea;

    public JdeployHelloCommands() {
        this(null);
    }

    public JdeployHelloCommands(String initialContent) {
        setTitle("Hello World");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JLabel label = new JLabel("Hello World", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        JTable systemPropertiesTable = createSystemPropertiesTable();
        JScrollPane tableScrollPane = new JScrollPane(systemPropertiesTable);
        tabbedPane.addTab("System Properties", tableScrollPane);

        fileContentArea = new JTextArea();
        fileContentArea.setEditable(false);
        fileContentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (initialContent != null) {
            fileContentArea.setText(initialContent);
        }
        JScrollPane textScrollPane = new JScrollPane(fileContentArea);
        tabbedPane.addTab("File Content", textScrollPane);

        add(tabbedPane, BorderLayout.CENTER);

        setupOpenFileListener();
    }

    private JTable createSystemPropertiesTable() {
        Properties props = System.getProperties();
        String[] columnNames = {"Property", "Value"};
        Object[][] data = new Object[props.size()][2];

        int i = 0;
        for (String key : props.stringPropertyNames()) {
            data[i][0] = key;
            data[i][1] = props.getProperty(key);
            i++;
        }

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        return new JTable(model);
    }

    private void setupOpenFileListener() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.APP_OPEN_FILE)) {
            return;
        }

        desktop.setOpenFileHandler(new OpenFilesHandler() {
            @Override
            public void openFiles(OpenFilesEvent e) {
                for (File file : e.getFiles()) {
                    if (file.exists() && file.isFile()) {
                        loadFileContent(file);
                    }
                }
            }
        });
    }

    private void loadFileContent(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            fileContentArea.setText(content);
        } catch (IOException e) {
            fileContentArea.setText("Error loading file: " + e.getMessage());
        }
    }

    private void loadURLContent(String urlString) {
        try {
            URL url = new URL(urlString);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                fileContentArea.setText(content.toString());
            }
        } catch (IOException e) {
            fileContentArea.setText("Error loading URL: " + e.getMessage());
        }
    }

    private static void printSystemPropertiesCLI() {
        Properties props = System.getProperties();
        for (String key : props.stringPropertyNames()) {
            System.out.println(key + "=" + props.getProperty(key));
        }
    }

    private static boolean isFileOrURL(String arg) {
        File file = new File(arg);
        if (file.exists() && file.isFile()) {
            return true;
        }

        try {
            new URL(arg);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String loadContent(String arg) {
        File file = new File(arg);
        if (file.exists() && file.isFile()) {
            try {
                return new String(Files.readAllBytes(file.toPath()));
            } catch (IOException e) {
                return "Error loading file: " + e.getMessage();
            }
        }

        try {
            URL url = new URL(arg);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(url.openStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (IOException e) {
            return "Error loading URL: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        String jdeployMode = System.getProperty("jdeploy.mode");
        boolean forceCommandMode = "command".equals(jdeployMode);

        boolean shouldRunAsGUI = false;
        String initialContent = null;

        if (!forceCommandMode && args.length == 1 && isFileOrURL(args[0])) {
            shouldRunAsGUI = true;
            initialContent = loadContent(args[0]);
        } else if (args.length == 0 && !forceCommandMode) {
            shouldRunAsGUI = true;
        }

        if (shouldRunAsGUI) {
            String finalContent = initialContent;
            SwingUtilities.invokeLater(() -> {
                JdeployHelloCommands frame = new JdeployHelloCommands(finalContent);
                frame.setVisible(true);
            });
        } else {
            printSystemPropertiesCLI();
            System.out.println();

            if (args.length > 0) {
                System.out.println(args[0]);
            }

            if (args.length >= 2) {
                try {
                    int exitCode = Integer.parseInt(args[1]);
                    System.exit(exitCode);
                } catch (NumberFormatException e) {
                    System.err.println("Error: Second argument must be an integer but found " + args[1]);
                    System.err.println("Args length is " + args.length);
                    System.err.println("Args: " + String.join(", ", args));
                    System.exit(1);
                }
            }
        }
    }
}
