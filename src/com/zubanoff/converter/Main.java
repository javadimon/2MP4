package com.zubanoff.converter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {

    private JPanel mainPanel;
    private JList leftList;
    private JList rightList;
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

        JFrame mainFrame = new JFrame();

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
        }
    }

    private void removeSelectedFiles(){
        // TODO
    }
}
