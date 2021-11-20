package com.zubanoff.converter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class Main {

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
    private Process process;
    private boolean isDestroyProcess = false;
    private final Properties properties;

    public Main() throws IOException {
        properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "ffmpeg", "converter.properties")));
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Main main = new Main();
        main.init();
    }

    private void init() throws IOException {

        btnAddFiles.addActionListener(e -> fileChooser());
        btnAddFiles.setFocusPainted(false);
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

        btnStartSelected.setFocusPainted(false);
        btnStartSelected.setEnabled(false);

        btnStop.addActionListener(e -> {
            btnAddFiles.setEnabled(true);
            btnRemoveFiles.setEnabled(true);
            btnStartAll.setEnabled(true);
            btnStop.setEnabled(false);
            stopConverting();
        });
        btnStop.setFocusPainted(false);


        JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });
        byte[] bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "mp4.png"));
        ImageIcon frameIcon = new ImageIcon(bytes, "2MP4");
        mainFrame.setIconImage(frameIcon.getImage());
        mainFrame.setTitle("2MP4");

        JMenuBar menuBar = new JMenuBar();
        JMenu mnuFile = new JMenu("File");
        JMenuItem mnuExit = new JMenuItem("Exit");
        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "out.png"));
        ImageIcon imageIcon = new ImageIcon(bytes, "Exit");
        mnuExit.setIcon(imageIcon);
        mnuFile.add(mnuExit);
        mnuExit.addActionListener(actionEvent -> System.exit(0));
        menuBar.add(mnuFile);

        JMenu mnuAbout = new JMenu("About");
        JMenuItem mnuAboutItem = new JMenuItem("About");
        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "about.png"));
        mnuAboutItem.setIcon(new ImageIcon(bytes, "About"));
        mnuAbout.add(mnuAboutItem);
        menuBar.add(mnuAbout);

        mainFrame.setJMenuBar(menuBar);
        mnuFile.grabFocus();

        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setSize(800, 400);
        mainFrame.setVisible(true);
        mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent event){
                splitPanel.setDividerLocation(mainFrame.getWidth() / 2);
            }
        });

        splitPanel.setDividerLocation(400);

        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "add.png"));
        ImageIcon imageExit = new ImageIcon(bytes, "Add source video");
        btnAddFiles.setIcon(imageExit);

        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "remove.png"));
        imageIcon = new ImageIcon(bytes, "Remove source video");
        btnRemoveFiles.setIcon(imageIcon);

        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "all.png"));
        imageIcon = new ImageIcon(bytes, "Start converting");
        btnStartAll.setIcon(imageIcon);

        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "start.png"));
        imageIcon = new ImageIcon(bytes, "Start converting");
        btnStartSelected.setIcon(imageIcon);

        bytes = Files.readAllBytes(Paths.get(System.getProperty("user.dir"), "img", "stop.png"));
        imageIcon = new ImageIcon(bytes, "Stop converting");
        btnStop.setIcon(imageIcon);


    }

    private void stopConverting(){
        isDestroyProcess = true;
        process.destroyForcibly();
        progressBarCurrent.setValue(0);
        progressBarTotal.setValue(0);
    }

    private void convert(){

        isDestroyProcess = false;

        progressBarCurrent.setIndeterminate(true);

        progressBarTotal.setMinimum(0);
        progressBarTotal.setMaximum(leftList.getModel().getSize());

        Runnable runnable = () -> {
            Runtime runtime = Runtime.getRuntime();
            for(int i = 0; i < leftList.getModel().getSize(); i++) {

                if(isDestroyProcess){
                    continue;
                }

                String sourcePath = leftList.getModel().getElementAt(i).getAbsolutePath();
                String source = "\"" + sourcePath + "\"";
                String name = sourcePath.substring(0, sourcePath.length() - 4);
                String out = "\"" + name + ".mp4" + "\"";
                String outFileName = name + ".mp4";
                File file = new File(outFileName);
                if(file.exists()){
                    file.delete(); // TODO
                }

                try {
                    String ffmpeg = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "ffmpeg.exe").toString();
                    int processorsCount = Runtime.getRuntime().availableProcessors();
                    String cmd = ffmpeg + " -y -i " + source + " " + properties.getProperty("default") + " " + processorsCount + " " + out;

                    process = runtime.exec(cmd);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null){
                            System.out.println(line);
                            if(line.contains("Duration:")){
                                String[] sss = line.split(",")[0].split(":");
                                String sTime = sss[1] + ":" + sss[2] + ":" + sss[3];
                                long totalVideoTime = getVideoSecondsLength(sTime);
                                progressBarCurrent.setIndeterminate(false);
                                progressBarCurrent.setMinimum(0);
                                progressBarCurrent.setMaximum((int)totalVideoTime);
                            }

                            if(line.contains("frame=") && line.contains("fps")){
                                String sTime = line.split("=")[5].replace(" bitrate", "");
                                long convertedTime = getVideoSecondsLength(sTime);
                                int min = progressBarCurrent.getMaximum() / 100;
                                int value = Math.max((int)convertedTime, min);
                                progressBarCurrent.setValue(value);
                            }
                        }
                    }
                    process.waitFor();

                    if(!isDestroyProcess){
                        DefaultListModel<File> rightListModel = new DefaultListModel<>();
                        for(int j = 0; j < rightList.getModel().getSize(); j++){
                            rightListModel.addElement(rightList.getModel().getElementAt(j));
                        }
                        rightListModel.addElement(new File(outFileName));
                        rightList.setModel(rightListModel);

                        progressBarCurrent.setIndeterminate(true);
                        progressBarTotal.setValue(i + 1);
                    }

                } catch (IOException | InterruptedException | ParseException e) {
                    System.out.println(e);
                }
            }

            progressBarCurrent.setIndeterminate(false);
            if(!isDestroyProcess){
                progressBarCurrent.setValue(progressBarCurrent.getMaximum());
            } else {
                progressBarCurrent.setValue(0);
            }

            btnStop.setEnabled(false);
            btnStartAll.setEnabled(true);
            btnAddFiles.setEnabled(true);
        };

        Executors.newSingleThreadExecutor().execute(runnable);
    }

    private long getVideoSecondsLength(String time) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date reference = dateFormat.parse("00:00:00");
        Date date = dateFormat.parse(time);
        return (date.getTime() - reference.getTime()) / 1000L;
    }

    private void fileChooser(){

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Source video files");
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {

            List<File> addedFiles = new ArrayList<>();
            for(int i = 0; i < leftList.getModel().getSize(); i++){
                addedFiles.add(leftList.getModel().getElementAt(i));
            }

            DefaultListModel<File> leftListModel = new DefaultListModel<>();
            leftListModel.addAll(addedFiles);
            leftListModel.addAll(List.of(jfc.getSelectedFiles()));
            leftList.setModel(leftListModel);

            if(leftListModel.getSize() > 0){
                btnStartAll.setEnabled(true);
            }
        }
    }

    private void removeSelectedFiles(){
        List<File> selectedFiles = leftList.getSelectedValuesList();
        DefaultListModel<File> listModel = (DefaultListModel<File>) leftList.getModel();
        List<File> removeElements = new ArrayList<>();
        for(int i = 0; i < listModel.size(); i++){
            for(File file : selectedFiles){
                if(listModel.get(i).equals(file)){
                    removeElements.add(file);
                }
            }
        }

        for(File file : removeElements){
            listModel.removeElement(file);
        }

        if(listModel.isEmpty()){
            btnRemoveFiles.setEnabled(false);
            btnStartAll.setEnabled(false);
        }
    }
}
