package com.zubanoff.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import javax.swing.*;

public class Converter {

    private boolean isDestroyProcess = false;
    private Process process;
    private final JProgressBar progressBarCurrent;
    private final JProgressBar progressBarTotal;
    private final JList<File> leftList;
    private final JList<File> rightList;
    private final Properties properties;
    private final JButton btnStop;
    private final JButton btnStartAll;
    private final JButton btnAddFiles;

    public Converter(JProgressBar progressBarCurrent, JProgressBar progressBarTotal,
                     JList<File> leftList, JList<File> rightList,
                     JButton btnStop, JButton btnStartAll, JButton btnAddFiles, Properties properties){
        this.progressBarCurrent = progressBarCurrent;
        this.progressBarTotal = progressBarTotal;
        this.leftList = leftList;
        this.rightList = rightList;
        this.btnStop = btnStop;
        this.btnStartAll = btnStartAll;
        this.btnAddFiles = btnAddFiles;
        this.properties = properties;
    }

    public void convert() {

        progressBarCurrent.setIndeterminate(true);
        progressBarTotal.setMinimum(0);
        progressBarTotal.setMaximum(leftList.getModel().getSize());

        Runnable runnable = () -> {
            Runtime runtime = Runtime.getRuntime();
            for (int i = 0; i < leftList.getModel().getSize(); i++) {

                if (isDestroyProcess) {
                    continue;
                }

                String sourcePath = leftList.getModel().getElementAt(i).getAbsolutePath();
                String source = "\"" + sourcePath + "\"";
                String name = sourcePath.substring(0, sourcePath.length() - 4);
                String out = "\"" + name + ".mp4" + "\"";
                String outFileName = name + ".mp4";
                File file = new File(outFileName);
                if (file.exists()) {
                    file.delete(); // TODO
                }

                try {
                    String ffmpeg = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "ffmpeg.exe").toString();
                    int processorsCount = Runtime.getRuntime().availableProcessors();
                    String cmd = ffmpeg + " -y -i " + source + " " + properties.getProperty("default") + " " + processorsCount + " " + out;

                    process = runtime.exec(cmd);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            if (line.contains("Duration:")) {
                                String[] sss = line.split(",")[0].split(":");
                                String sTime = sss[1] + ":" + sss[2] + ":" + sss[3];
                                long totalVideoTime = getVideoSecondsLength(sTime);
                                progressBarCurrent.setIndeterminate(false);
                                progressBarCurrent.setMinimum(0);
                                progressBarCurrent.setMaximum((int) totalVideoTime);
                                progressBarCurrent.setValue((int) ((totalVideoTime / 100) * 2));
                            }

                            if (line.contains("frame=") && line.contains("fps")) {
                                String sTime = line.split("=")[5].replace(" bitrate", "");
                                long convertedTime = getVideoSecondsLength(sTime);
                                int min = (int) ((progressBarCurrent.getMaximum() / 100) * 2);
                                if(convertedTime > min){
                                    progressBarCurrent.setValue((int) convertedTime);
                                }
                            }
                        }
                    }
                    process.waitFor();

                    if (!isDestroyProcess) {
                        DefaultListModel<File> rightListModel = new DefaultListModel<>();
                        for (int j = 0; j < rightList.getModel().getSize(); j++) {
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
            if (!isDestroyProcess) {
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

    public void stopConverting() {
        if(process != null){
            isDestroyProcess = true;
            process.destroyForcibly();
            progressBarCurrent.setValue(0);
            progressBarTotal.setValue(0);
        }
    }
}
