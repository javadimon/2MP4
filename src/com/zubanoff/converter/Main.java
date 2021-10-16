package com.zubanoff.converter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main {

    private JPanel mainPanel;
    private JList leftList;
    private JList rightList;
    private JProgressBar progressBar1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;

    public static void main(String args[]) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        Main main = new Main();
        main.init();
    }

    private void init(){

        DefaultListModel<String> leftListModel = new DefaultListModel<>();
        leftListModel.addAll(List.of("Input files", "456", "789"));
        leftList.setModel(leftListModel);

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
//
//        JList<String> leftList = new JList<>();
//        DefaultListModel<String> leftListModel = new DefaultListModel<>();
//        leftListModel.addAll(List.of("123", "456", "789"));
//        leftList.setModel(leftListModel);
//        JScrollPane leftScrollPane = new JScrollPane();
//        leftScrollPane.add(leftList);
//
//        JPanel rightPanel = new JPanel();
//        JList<String> rightList = new JList<>();
//        DefaultListModel<String> rightListModel = new DefaultListModel<>();
//        leftListModel.addAll(List.of("123", "456", "789"));
//        leftList.setModel(rightListModel);
//        rightList.setVisible(true);
////        JScrollPane rigthScrollPane = new JScrollPane();
////        rightPanel.setMinimumSize(new Dimension(25, 25));
////        rigthScrollPane.add(rightList);
//        rightPanel.add(rightList);
//
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightPanel);
//        splitPane.setDividerLocation(150);
//
//        mainFrame.getContentPane().add(splitPane);
//
//        mainFrame.setVisible(true);
//        mainFrame.setSize(600, 400);
    }
}
