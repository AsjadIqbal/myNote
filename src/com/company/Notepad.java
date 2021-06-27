package com.company;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;

public class Notepad {

    //Main components of the GUI
    JFrame mainFrame;
    JTextArea textArea;
    JLabel statusLabel;

    String fileName = "Untitled";
    String appName = "myNote";

    //Object reference to file operations class
    FileOperations fileOp;

    //Edit menu items
    private JMenuItem cutMI, copyMI, deleteMI, findMI,
            findNextMI, replaceMI, gotoMI, selectAllMI, undoMI, redoMI;

    protected UndoManager undoManager = new UndoManager();

    JColorChooser bgColorChooser=null;
    JColorChooser fgColorChooser=null;
    JDialog bgDialog=null;
    JDialog fgDialog=null;



    //Constructor method
    public Notepad(){
        fileOp = new FileOperations(this);
        prepareGUI();
    }

    //Method to setup major GUI components
    private void prepareGUI(){
        mainFrame = new JFrame(fileName + " - " + appName);
        textArea = new JTextArea(30,70);
        statusLabel = new JLabel("||    Ln 1, Col 1", JLabel.RIGHT);

        mainFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        mainFrame.add(statusLabel, BorderLayout.SOUTH);
        mainFrame.add(new JLabel(" "), BorderLayout.EAST);
        mainFrame.add(new JLabel(" "), BorderLayout.WEST);

        mainFrame.setJMenuBar(createMenuBar());
        createPopupMenu();

        //Adding cursor status to Status Bar
        textArea.addCaretListener(e -> {
            int ln=0, col=0, pos;
            try{
                pos = textArea.getCaretPosition();
                ln = textArea.getLineOfOffset(pos);
                col = textArea.getLineStartOffset(ln);
            }catch (Exception ex){ex.printStackTrace();}
            if(textArea.getText().length()==0){
                ln=0;
                col=0;
            }
            statusLabel.setText("||    Ln " + (ln+1) + ", Col "+ (col+1));
        });

        //Updating save status of document
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fileOp.saved = false;
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                fileOp.saved = false;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                fileOp.saved = false;
            }
        };
        textArea.getDocument().addDocumentListener(docListener);

        textArea.getDocument().addUndoableEditListener(new MyUndoableEditListener());

        mainFrame.pack();
        mainFrame.setLocation(500, 50);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(!fileOp.confirmSave()){
                    int op = JOptionPane.showConfirmDialog(
                            mainFrame,
                            "There are unsaved changes in your document." +
                                    " Are you sure you want to exit?");
                    if(op == JOptionPane.YES_OPTION)
                        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
                else mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        mainFrame.setVisible(true);
    }

    //Method to create menu bar.
    private JMenuBar createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        JMenu fileM = new JMenu("File");
        fileM.setMnemonic(KeyEvent.VK_F);
        JMenu editM = new JMenu("Edit");
        editM.setMnemonic(KeyEvent.VK_E);
        JMenu formatM = new JMenu("Format");
        formatM.setMnemonic(KeyEvent.VK_O);
        JMenu viewM = new JMenu("View");
        viewM.setMnemonic(KeyEvent.VK_V);
        JMenu helpM = new JMenu("Help");
        helpM.setMnemonic(KeyEvent.VK_H);

        MenuItemListener miListener = new MenuItemListener();

        JMenuItem tempMI;

        //Creating File menu
        createMenuItem("New", fileM, miListener, KeyEvent.VK_N, KeyEvent.VK_N);
        createMenuItem("Open", fileM, miListener, KeyEvent.VK_O, KeyEvent.VK_O);
        createMenuItem("Save", fileM, miListener, KeyEvent.VK_S, KeyEvent.VK_S);
        createMenuItem("Save As", fileM, miListener, KeyEvent.VK_A);
        fileM.addSeparator();
        tempMI = createMenuItem("Page Setup", fileM, miListener, KeyEvent.VK_U);
        tempMI.setEnabled(false);
        createMenuItem("Print", fileM, miListener, KeyEvent.VK_P, KeyEvent.VK_P);
        fileM.addSeparator();
        createMenuItem("Exit", fileM, miListener, KeyEvent.VK_X);

        //Creating Edit menu
        undoMI = createMenuItem("Undo", editM, miListener, KeyEvent.VK_U, KeyEvent.VK_Z);
        undoMI.setEnabled(false);
        redoMI = createMenuItem("Redo", editM, miListener, KeyEvent.VK_R);
        redoMI.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK |
                        InputEvent.SHIFT_DOWN_MASK));
        redoMI.setEnabled(false);
        editM.addSeparator();
        cutMI = createMenuItem("Cut", editM, miListener, KeyEvent.VK_T, KeyEvent.VK_X);
        copyMI = createMenuItem("Copy", editM, miListener, KeyEvent.VK_C, KeyEvent.VK_C);
        createMenuItem("Paste", editM, miListener, KeyEvent.VK_P, KeyEvent.VK_V);
        deleteMI = createMenuItem("Delete", editM, miListener, KeyEvent.VK_L);
        deleteMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        editM.addSeparator();
        findMI = createMenuItem("Find", editM, miListener, KeyEvent.VK_F, KeyEvent.VK_F);
        findNextMI = createMenuItem("Find Next", editM, miListener, KeyEvent.VK_N);
        replaceMI = createMenuItem("Replace", editM, miListener, KeyEvent.VK_R);
        gotoMI = createMenuItem("Go to", editM, miListener, KeyEvent.VK_G);
        editM.addSeparator();
        selectAllMI = createMenuItem("Select All", editM, miListener, KeyEvent.VK_A, KeyEvent.VK_A);
        createMenuItem("Time/Date", editM, miListener, KeyEvent.VK_T);

        //Creating Format menu
        JCheckBoxMenuItem wordWrapMI = new JCheckBoxMenuItem("Word-wrap", false);
        wordWrapMI.setActionCommand("WordWrap");
        wordWrapMI.addActionListener(miListener);
        formatM.add(wordWrapMI);
        createMenuItem("Font", formatM, miListener, KeyEvent.VK_F);
        formatM.addSeparator();
        createMenuItem("Text Color", formatM, miListener, KeyEvent.VK_T);
        createMenuItem("BG Color", formatM, miListener, KeyEvent.VK_B);
        JMenu themeSubMenu = new JMenu("Theme");
        JRadioButtonMenuItem lightThemeMI = new JRadioButtonMenuItem("Light", true);
        lightThemeMI.setActionCommand("lightTheme");
        lightThemeMI.addActionListener(miListener);
        JRadioButtonMenuItem darkThemeMI = new JRadioButtonMenuItem("Dark", false);
        darkThemeMI.setActionCommand("darkTheme");
        darkThemeMI.addActionListener(miListener);
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeMI);
        themeGroup.add(darkThemeMI);
        themeSubMenu.add(lightThemeMI);
        themeSubMenu.add(darkThemeMI);
        formatM.add(themeSubMenu);

        //Creating View menu
        JCheckBoxMenuItem statusMI = new JCheckBoxMenuItem("View Status Bar", true);
        statusMI.setActionCommand("viewStat");
        statusMI.addActionListener(miListener);
        viewM.add(statusMI);

        //Creating Help menu
        createMenuItem("Help Topic", helpM, miListener, KeyEvent.VK_H);
        createMenuItem("About myNote", helpM, miListener, KeyEvent.VK_A);

        //Only enables certain menu items when text area is not empty and/or selected.
        MenuListener editListener = new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if(textArea.getText().length()==0) {
                    findMI.setEnabled(false);
                    findNextMI.setEnabled(false);
                    selectAllMI.setEnabled(false);
                    replaceMI.setEnabled(false);
                    gotoMI.setEnabled(false);
                    undoMI.setEnabled(false);
                    redoMI.setEnabled(false);
                } else{
                    findMI.setEnabled(true);
                    findNextMI.setEnabled(true);
                    selectAllMI.setEnabled(true);
                    replaceMI.setEnabled(true);
                    gotoMI.setEnabled(true);
                    undoMI.setEnabled(true);
                    redoMI.setEnabled(true);
                }
                if(textArea.getSelectionStart()==textArea.getSelectionEnd()) {
                    cutMI.setEnabled(false);
                    copyMI.setEnabled(false);
                    deleteMI.setEnabled(false);
                } else {
                    cutMI.setEnabled(true);
                    copyMI.setEnabled(true);
                    deleteMI.setEnabled(true);
                }
            }
            @Override
            public void menuDeselected(MenuEvent e) {}
            @Override
            public void menuCanceled(MenuEvent e) {}
        };
        editM.addMenuListener(editListener);

        menuBar.add(fileM);
        menuBar.add(editM);
        menuBar.add(formatM);
        menuBar.add(viewM);
        menuBar.add(helpM);

        mainFrame.setVisible(true);
        return menuBar;
    }

    //Method to create menu items without shortcut.
    private JMenuItem createMenuItem(String name, JMenu toMenu, ActionListener miListener, int key){
        JMenuItem mItem = new JMenuItem(name, key);
        mItem.setActionCommand(name);
        mItem.addActionListener(miListener);
        toMenu.add(mItem);
        return mItem;
    }

    //Method to create menu items with shortcut.
    private JMenuItem createMenuItem(String name, JMenu toMenu, ActionListener miListener,
                                     int key, int accKey){
        JMenuItem mItem = new JMenuItem(name, key);
        mItem.setActionCommand(name);
        mItem.addActionListener(miListener);
        mItem.setAccelerator(KeyStroke.getKeyStroke(accKey, InputEvent.CTRL_DOWN_MASK));
        toMenu.add(mItem);
        return mItem;
    }

    //Method to create Right-Click popup menu.
    private void createPopupMenu() {
        final JPopupMenu popEdit = new JPopupMenu("Edit");

        JMenuItem cutPopMI = new JMenuItem("Cut");
        cutPopMI.setActionCommand("Cut");

        JMenuItem copyPopMI = new JMenuItem("Copy");
        copyPopMI.setActionCommand("Copy");

        JMenuItem pastePopMI = new JMenuItem("Paste");
        pastePopMI.setActionCommand("Paste");

        JMenuItem selectAllPopMI = new JMenuItem("Select All");
        selectAllPopMI.setActionCommand("Select All");

        JMenuItem deletePopMI = new JMenuItem("Delete");
        deletePopMI.setActionCommand("Delete");

        MenuItemListener miListener = new MenuItemListener();

        cutPopMI.addActionListener(miListener);
        copyPopMI.addActionListener(miListener);
        pastePopMI.addActionListener(miListener);
        selectAllPopMI.addActionListener(miListener);
        deletePopMI.addActionListener(miListener);

        popEdit.add(cutPopMI);
        popEdit.add(copyPopMI);
        popEdit.add(pastePopMI);
        popEdit.add(selectAllPopMI);
        popEdit.add(deletePopMI);

        textArea.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e))
                    popEdit.show(mainFrame, e.getX(), e.getY());
            }
        });
        textArea.add(popEdit);
        //mainFrame.add(popEdit);
        mainFrame.setVisible(true);
    }


    private void goTo(){
        int ln;
        try {
            ln = textArea.getLineOfOffset(textArea.getCaretPosition())+1;
            String lnStr = JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter line number:",
                    "" + ln);
            if(lnStr == null) return;
            ln = Integer.parseInt(lnStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void bgColorDialog(){
        if(bgColorChooser == null) bgColorChooser = new JColorChooser();
        if(bgDialog == null)
            bgDialog = JColorChooser.createDialog(
                    mainFrame,
                    "Set text color...",
                    false,
                    bgColorChooser,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            textArea.setBackground(bgColorChooser.getColor());
                        }
                    }, null);
        bgDialog.setVisible(true);
    }
    private void fgColorDialog(){
        if(fgColorChooser == null) fgColorChooser = new JColorChooser();
        if(fgDialog == null)
            fgDialog = JColorChooser.createDialog(
                    mainFrame,
                    "Set pad color...",
                    false,
                    fgColorChooser,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            textArea.setForeground(fgColorChooser.getColor());
                        }
                    }, null);
        fgDialog.setVisible(true);
    }


    //Driver Function
    public static void main(String[] args) {
        new Notepad();
    }

    private class MyUndoableEditListener implements UndoableEditListener{

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());
        }
    }

    //This is where the magic happens (and also in FileOperations)
    private class MenuItemListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                //File menu actions
                case "New" -> fileOp.newFile();
                case "Open" -> fileOp.openFile();
                case "Save" -> fileOp.saveThisFile();
                case "Save As" -> fileOp.saveAsFile();
                case "Exit" -> {
                    if(fileOp.confirmSave())
                        System.exit(0);
                }

                //Edit menu actions
                case "Print" -> JOptionPane.showMessageDialog(
                        mainFrame,
                        "No printer could be detected.",
                        "Printer not found",
                        JOptionPane.INFORMATION_MESSAGE);
                case "Cut" -> textArea.cut();
                case "Copy" -> textArea.copy();
                case "Paste" -> textArea.paste();
                case "Delete" -> textArea.replaceSelection("");
                case "Select All" -> textArea.selectAll();
                //TODO Undo and Redo functionality to be added.
                case "Undo" -> {
                    if(undoManager.canUndo()) undoManager.undo();
                }
                case "Redo" -> {
                    if(undoManager.canRedo()) undoManager.redo();
                }
                case "Find", "Help Topic", "Font", "Replace", "Find Next" -> {}
                case "Go to" -> {
                    if(textArea.getText().length()==0) return;
                    goTo();
                }

                //Format menu actions
                case "WordWrap" -> {
                    JCheckBoxMenuItem temp = (JCheckBoxMenuItem) e.getSource();
                    textArea.setLineWrap(temp.isSelected());
                }
                case "Text Color" -> fgColorDialog();
                case "BG Color" -> bgColorDialog();
                case "Time/Date" -> textArea.insert(new Date().toString(), textArea.getSelectionStart());
                case "lightTheme" -> {
                    textArea.setBackground(Color.WHITE);
                    textArea.setForeground(Color.BLACK);
                }
                case "darkTheme" -> {
                    textArea.setForeground(Color.LIGHT_GRAY);
                    textArea.setBackground(Color.DARK_GRAY);
                }
                //TODO More actions to be added for menus.

                //View menu actions
                case "viewStat" -> {
                    JCheckBoxMenuItem temp = (JCheckBoxMenuItem) e.getSource();
                    statusLabel.setVisible(temp.isSelected());
                }

                //About menu actions
                case "About myNote" -> JOptionPane.showMessageDialog(
                        mainFrame,
                        "<html>This project has been created as my first work on AWT and Swing.<br>" +
                                "Project is still under development and improvement.<br>" +
                                "--Developed by Asjad Iqbal--<html>",
                        "About myNote",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        }

    }
    //Created by Asjad Iqbal
}

