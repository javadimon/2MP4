package com.zubanoff.converter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {

    private JPanel mainPanel;
    private JList<File> leftList;
    private JList<File> rightList;
    private JProgressBar progressBar;
    private JButton btnAddFiles;
    private JButton btnRemoveFiles;
    private JButton btnStart;
    private JButton btnStop;

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
        mainFrame.setSize(600, 400);
        mainFrame.setVisible(true);
    }

    private void convert(){
        Runtime runtime = Runtime.getRuntime();
        //String cmd = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "convert.bat").toString();
        for(int i = 0; i < leftList.getModel().getSize(); i++) {
            String sourcePath = leftList.getModel().getElementAt(i).getAbsolutePath();
            String source = "\"" + sourcePath + "\"";
            String out = "\"" + sourcePath.substring(0, sourcePath.length() - 4) + ".mp4" + "\"";

            //convert.bat "C:\Users\zubanov.d\Videos\Billions 1 - LostFilm.TV\Billions.S01E01.rus.LostFilm.TV.avi" "C:\Users\zubanov.d\Videos\Billions 1 - LostFilm.TV\Billions.S01E01.rus.LostFilm.TV.mp4"
            try {

//                runtime.exec("cmd", new String[]{"cd", Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin").toString()});

//                Process process = runtime.exec(
//                        cmd,
//                        new String[]{source, out});
                // ffmpeg.exe -i %1 -vcodec libx264 -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -map 0:0 -map 0:1 -threads 8 %2

                String ffmpeg = Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "ffmpeg.exe").toString();
                String cmd = "ffmpeg -i " + source + " -vcodec libx264 -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -map 0:0 -map 0:1 -threads 8 " + out + " > \"C:\\IdeaProjects\\converter-plain\\ffmpeg\\bin\\log.txt\" 2>&1 \r\n";
                //String cmd = "ffmpeg -i " + source + " -vcodec libx264 -bufsize 10000k -b:v 1000k -bt 1000k -maxrate 1000k -map 0:0 -map 0:1 -threads 8 " + out + "\r\n";

                //Process process = runtime.exec(cmd);
//                ProcessBuilder   processBuilder=new ProcessBuilder("ffmpeg", cmd);
//                //processBuilder.directory(new File("C:\\IdeaProjects\\converter-plain\\ffmpeg\\bin"));
//                processBuilder.redirectErrorStream();
//                Process process = processBuilder.start();

                Process process = runtime.exec("cmd");
                OutputStream outputStream = process.getOutputStream();
                outputStream.write(cmd.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();

//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                    String line;
//                    while ((line = reader.readLine()) != null){
//                        System.out.println(line);
//                    }
//                }
                Executors.newSingleThreadExecutor().execute(readLog());
                process.waitFor();

            } catch (IOException | InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    private Runnable readLog(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String log =Files.readString(Paths.get(System.getProperty("user.dir"), "ffmpeg", "bin", "log.txt"));
                    System.out.println(log);
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        return runnable;
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
