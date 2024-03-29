package com.zubanoff.converter;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;

public class Main {

    private final JFrame mainFrame = new JFrame();
    private JPanel mainPanel;
    private JList<File> leftList;
    private JList<File> rightList;
    private JProgressBar progressBarTotal;
    private JButton btnAddFiles;
    private JButton btnRemoveFiles;
    private JButton btnStartAll;
    private JButton btnStop;
    private JProgressBar progressBarCurrent;
    private JSplitPane splitPanel;
    private JButton btnStartSelected;
    private final Properties properties;
    private Converter converter;
    private static final String USER_DIR = Paths.get(System.getProperty("user.dir")).toString();
    private static final String USER_DIR_IMG = Paths.get(System.getProperty("user.dir"), "img").toString();
    private static final List<String> EXTENSIONS = List.of(".avi", ".mov", ".mkv", ".3gp", ".wmv", ".wm", ".3g2", ".dat", ".m4v", ".mod",
            ".mpeg", ".mpg", ".vob", ".yuv");

    public Main() throws IOException {
        properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get(USER_DIR, "ffmpeg", "converter.properties")));
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Main main = new Main();
        main.init();
    }

    private void init() throws IOException {

        btnAddFiles.addActionListener(e -> {
            try {
                fileChooser();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        btnAddFiles.setFocusPainted(false);
        btnAddFiles.setSize(32, 32);
        leftList.addListSelectionListener(e -> btnRemoveFiles.setEnabled(true));

        btnRemoveFiles.addActionListener(e -> removeSelectedFiles());
        btnRemoveFiles.setFocusPainted(false);

        btnStartAll.addActionListener(e -> {
            btnAddFiles.setEnabled(false);
            btnRemoveFiles.setEnabled(false);
            btnStartAll.setEnabled(false);
            btnStop.setEnabled(true);
            convert();
        });
        btnStartAll.setFocusPainted(false);

        btnStop.addActionListener(e -> {
            btnAddFiles.setEnabled(true);
            btnRemoveFiles.setEnabled(true);
            btnStartAll.setEnabled(true);
            btnStop.setEnabled(false);
            stopConverting();
        });
        btnStop.setFocusPainted(false);


        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                converter.stopConverting();
                super.windowClosing(e);
                System.exit(0);
            }
        });

        byte[] bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "mp4.png"));
        ImageIcon frameIcon = new ImageIcon(bytes, "2MP4");
        mainFrame.setIconImage(frameIcon.getImage());
        mainFrame.setTitle("2MP4");

        JMenuBar menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenuItem mnuExit = new JMenuItem("Exit");
        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "out.png"));
        ImageIcon imageIcon = new ImageIcon(bytes, "Exit");
        mnuExit.setIcon(imageIcon);
        mnuFile.add(mnuExit);
        mnuExit.addActionListener(actionEvent -> System.exit(0));
        menuBar.add(mnuFile);

        JMenu mnuAbout = new JMenu("Help");
        JMenuItem mnuAboutItem = new JMenuItem("About");
        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "about.png"));
        mnuAboutItem.setIcon(new ImageIcon(bytes, "About"));
        mnuAbout.add(mnuAboutItem);
        mnuAboutItem.addActionListener(actionEvent -> showDialogAbout());
        menuBar.add(mnuAbout);

        mainFrame.setJMenuBar(menuBar);
        mnuFile.grabFocus();

        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setSize(800, 400);
        mainFrame.setVisible(true);
        mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event) {
                splitPanel.setDividerLocation(mainFrame.getWidth() / 2);
            }
        });

        splitPanel.setDividerLocation(400);

        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "add_16.png"));
        ImageIcon imageExit = new ImageIcon(bytes, "Add source video");
        btnAddFiles.setIcon(imageExit);

        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "remove_16.png"));
        imageIcon = new ImageIcon(bytes, "Remove source video");
        btnRemoveFiles.setIcon(imageIcon);

        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "start_16.png"));
        imageIcon = new ImageIcon(bytes, "Start converting");
        btnStartAll.setIcon(imageIcon);

        bytes = Files.readAllBytes(Paths.get(USER_DIR_IMG, "stop_16.png"));
        imageIcon = new ImageIcon(bytes, "Stop converting");
        btnStop.setIcon(imageIcon);

        converter = new Converter(progressBarCurrent, progressBarTotal, leftList, rightList,
                btnStop, btnStartAll, btnAddFiles, properties);
    }

    private void showDialogAbout() {
        JDialog dlgAbout = new JDialog(mainFrame, "About", true);
        dlgAbout.setSize(220, 80);
        dlgAbout.setLocationRelativeTo(mainFrame);

        JLabel lblAbout = new JLabel("It's free, enjoy! dmitry@zubanoff.com");
        lblAbout.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        int gap = 5;
        dlgAbout.getContentPane().setLayout(new BorderLayout(gap, gap));
        dlgAbout.getRootPane().setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
        dlgAbout.add(lblAbout, BorderLayout.CENTER);

        dlgAbout.setVisible(true);
    }

    private void stopConverting() {
        converter.stopConverting();
    }

    private void convert() {
        converter.convert();
    }

    private void fileChooser() throws IOException {

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Source video files");
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = jfc.showOpenDialog(mainFrame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {

            List<File> addedFiles = new ArrayList<>();
            for (int i = 0; i < leftList.getModel().getSize(); i++) {
                addedFiles.add(leftList.getModel().getElementAt(i));
            }

            DefaultListModel<File> leftListModel = new DefaultListModel<>();
            leftListModel.addAll(addedFiles);
            List<File> filteredFiles = new ArrayList<>();
            for (File file : jfc.getSelectedFiles()) {
                if (EXTENSIONS.stream().anyMatch(s -> file.getName().toLowerCase().contains(s))) {
                    filteredFiles.add(file);
                }
            }
            leftListModel.addAll(filteredFiles);
            leftList.setModel(leftListModel);

            if (leftListModel.getSize() > 0) {
                btnStartAll.setEnabled(true);
            }
        }
    }

    private void removeSelectedFiles() {
        List<File> selectedFiles = leftList.getSelectedValuesList();
        DefaultListModel<File> listModel = (DefaultListModel<File>) leftList.getModel();
        List<File> removeElements = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            for (File file : selectedFiles) {
                if (listModel.get(i).equals(file)) {
                    removeElements.add(file);
                }
            }
        }

        for (File file : removeElements) {
            listModel.removeElement(file);
        }

        if (listModel.isEmpty()) {
            btnRemoveFiles.setEnabled(false);
            btnStartAll.setEnabled(false);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setMargin(new Insets(0, 0, 0, 0));
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator2);
        btnAddFiles = new JButton();
        Font btnAddFilesFont = this.$$$getFont$$$("JetBrains Mono", -1, -1, btnAddFiles.getFont());
        if (btnAddFilesFont != null) btnAddFiles.setFont(btnAddFilesFont);
        btnAddFiles.setForeground(new Color(-16744448));
        btnAddFiles.setMargin(new Insets(0, 0, 0, 0));
        btnAddFiles.setMaximumSize(new Dimension(18, 18));
        btnAddFiles.setMinimumSize(new Dimension(18, 18));
        btnAddFiles.setPreferredSize(new Dimension(18, 18));
        btnAddFiles.setText("");
        btnAddFiles.setToolTipText("Add files for convert");
        toolBar1.add(btnAddFiles);
        btnRemoveFiles = new JButton();
        btnRemoveFiles.setEnabled(false);
        Font btnRemoveFilesFont = this.$$$getFont$$$("JetBrains Mono", -1, -1, btnRemoveFiles.getFont());
        if (btnRemoveFilesFont != null) btnRemoveFiles.setFont(btnRemoveFilesFont);
        btnRemoveFiles.setForeground(new Color(-3932160));
        btnRemoveFiles.setMargin(new Insets(0, 0, 0, 0));
        btnRemoveFiles.setMaximumSize(new Dimension(18, 18));
        btnRemoveFiles.setMinimumSize(new Dimension(18, 18));
        btnRemoveFiles.setPreferredSize(new Dimension(18, 18));
        btnRemoveFiles.setText("");
        btnRemoveFiles.setToolTipText("Remove selected files");
        toolBar1.add(btnRemoveFiles);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator3);
        btnStartAll = new JButton();
        btnStartAll.setEnabled(false);
        btnStartAll.setMargin(new Insets(0, 0, 0, 0));
        btnStartAll.setMaximumSize(new Dimension(18, 18));
        btnStartAll.setMinimumSize(new Dimension(18, 18));
        btnStartAll.setPreferredSize(new Dimension(18, 18));
        btnStartAll.setText("");
        btnStartAll.setToolTipText("Convert all files");
        toolBar1.add(btnStartAll);
        btnStop = new JButton();
        btnStop.setEnabled(false);
        btnStop.setMargin(new Insets(0, 0, 0, 0));
        btnStop.setMaximumSize(new Dimension(18, 18));
        btnStop.setMinimumSize(new Dimension(18, 18));
        btnStop.setPreferredSize(new Dimension(18, 18));
        btnStop.setText("");
        btnStop.setToolTipText("Stop convertation");
        toolBar1.add(btnStop);
        splitPanel = new JSplitPane();
        splitPanel.setDividerLocation(300);
        mainPanel.add(splitPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPanel.setLeftComponent(scrollPane1);
        leftList = new JList();
        Font leftListFont = this.$$$getFont$$$(null, -1, 12, leftList.getFont());
        if (leftListFont != null) leftList.setFont(leftListFont);
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        leftList.setModel(defaultListModel1);
        scrollPane1.setViewportView(leftList);
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPanel.setRightComponent(scrollPane2);
        rightList = new JList();
        Font rightListFont = this.$$$getFont$$$(null, -1, 12, rightList.getFont());
        if (rightListFont != null) rightList.setFont(rightListFont);
        scrollPane2.setViewportView(rightList);
        progressBarTotal = new JProgressBar();
        mainPanel.add(progressBarTotal, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBarCurrent = new JProgressBar();
        mainPanel.add(progressBarCurrent, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
