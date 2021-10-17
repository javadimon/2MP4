package com.zubanoff.converter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {

    private JPanel mainPanel;
    private JList<File> leftList;
    private JList<File> rightList;
    private JProgressBar progressBarTotal;
    private JButton btnAddFiles;
    private JButton btnRemoveFiles;
    private JButton btnStart;
    private JButton btnStop;
    private JProgressBar progressBarCurrent;

    public static void main(String args[]) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Main main = new Main();
        main.init();
    }

    private void init(){

        btnAddFiles.addActionListener(e -> fileChooser());
        leftList.addListSelectionListener(e -> btnRemoveFiles.setEnabled(true));
        btnRemoveFiles.addActionListener(e -> removeSelectedFiles());
        btnStart.addActionListener(e -> convert());

        JFrame mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem size = new JMenuItem("Exit");
        menu.add(size);
        menubar.add(menu);
        mainFrame.setJMenuBar(menubar);

        mainFrame.getContentPane().add(mainPanel);
        mainFrame.setSize(800, 400);
        mainFrame.setVisible(true);
    }

    private void convert(){

        progressBarCurrent.setIndeterminate(true);

        progressBarTotal.setMinimum(0);
        progressBarTotal.setMaximum(leftList.getModel().getSize());

        Runnable runnable = () -> {
            Runtime runtime = Runtime.getRuntime();
            for(int i = 0; i < leftList.getModel().getSize(); i++) {

                String sourcePath = leftList.getModel().getElementAt(i).getAbsolutePath();
                String source = "\"" + sourcePath + "\"";
                String out = "\"" + sourcePath.substring(0, sourcePath.length() - 4) + ".mp4" + "\"";
                String outFileName = sourcePath.substring(0, sourcePath.length() - 4) + ".mp4";
                File file = new File(outFileName);
                if(file.exists()){
                    file.delete(); // TODO
                }

                try {
                    String ffmpeg = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "ffmpeg.exe").toString();
                    int processorsCount = Runtime.getRuntime().availableProcessors();
                    String cmd = ffmpeg + " -y -i " + source + " -vcodec libx264 -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -map 0:0 -map 0:1 -threads " + processorsCount + " " + out;

                    Process process = runtime.exec(cmd);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null){
                            System.out.println(line);
                            if(line.contains("Duration:")){
                                String ss[] = line.split(",");
                                String sss[] = ss[0].split(":");
                                String sTime = sss[1] + ":" + sss[2] + ":" + sss[3];
                                long totalVideoTime = getVideoSecondsLength(sTime);
                                System.out.println(totalVideoTime);
                                progressBarCurrent.setIndeterminate(false);
                                progressBarCurrent.setMinimum(0);
                                progressBarCurrent.setMaximum((int)totalVideoTime);
                            }

                            if(line.contains("frame=") && line.contains("fps")){
                                String ss[] = line.split("=");
                                String sTime = ss[5].replace(" bitrate", "");
                                System.out.println(sTime);
                                long convertedTime = getVideoSecondsLength(sTime);
                                progressBarCurrent.setValue((int)convertedTime);
                            }
                        }
                    }
                    process.waitFor();

                    DefaultListModel<File> rightListModel = new DefaultListModel<>();
                    for(int j = 0; j < rightList.getModel().getSize(); j++){
                        rightListModel.addElement(rightList.getModel().getElementAt(j));
                    }
                    rightListModel.addElement(new File(outFileName));
                    rightList.setModel(rightListModel);

                    progressBarCurrent.setIndeterminate(true);
                    progressBarTotal.setValue(i + 1);

                } catch (IOException | InterruptedException | ParseException e) {
                    System.out.println(e);
                }
            }

            progressBarCurrent.setIndeterminate(false);
            progressBarCurrent.setValue(progressBarCurrent.getMaximum());
        };

        Executors.newSingleThreadExecutor().execute(runnable);
    }

    private long getVideoSecondsLength(String time) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date reference = dateFormat.parse("00:00:00");
        Date date = dateFormat.parse(time);
        long seconds = (date.getTime() - reference.getTime()) / 1000L;
        return seconds;
    }

    private void fileChooser(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Source video files");
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] files = jfc.getSelectedFiles();
            System.out.println("Directories found\n");
            Arrays.asList(files).forEach(x -> {
                if (x.isDirectory()) {
                    System.out.println(x.getName());
                }
            });
            System.out.println("\n- - - - - - - - - - -\n");
            System.out.println("Files Found\n");
            Arrays.asList(files).forEach(x -> {
                if (x.isFile()) {
                    System.out.println(x.getAbsolutePath());
                }
            });

            DefaultListModel<File> leftListModel = new DefaultListModel<>();
            leftListModel.addAll(List.of(files));
            leftList.setModel(leftListModel);

            if(leftListModel.getSize() > 0){
                btnStart.setEnabled(true);
            }
        }
    }

    private void removeSelectedFiles(){
        // TODO
    }
}
