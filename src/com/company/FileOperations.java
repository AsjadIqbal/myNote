package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.io.*;

public class FileOperations {

    Notepad note;

    boolean saved;
    boolean newFileFlag;
    String fileName;
    String appName = "myNote";

    File file;
    JFileChooser fChooser;


    FileOperations(Notepad note){
        this.note = note;

        saved = true;
        newFileFlag = true;
        fileName = "Untitled";
        file = new File("C:\\Desktop\\" + fileName);
        fChooser = new JFileChooser();
        fChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Java Source Codes(*.java)", "java"));
        fChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Text Files(*.txt)", "txt"));
        fChooser.setCurrentDirectory(new File("C:\\Desktop\\"));
    }


    //File save operations
    boolean saveFile(File temp){
        FileWriter writer = null;
        try {
            writer = new FileWriter(temp);
            writer.write(note.textArea.getText());
        } catch (IOException e) {
            updateStatus(temp, false);
            return false;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateStatus(temp, true);
        return true;
    }
    boolean saveThisFile(){
        if (!newFileFlag) return saveFile(file);
        return saveAsFile();
    }
    boolean saveAsFile(){
        File temp;
        fChooser.setDialogTitle("Save As...");
        fChooser.setApproveButtonText("Save");
        fChooser.setApproveButtonMnemonic(KeyEvent.VK_S);

        while(true) {
            if(fChooser.showSaveDialog(this.note.mainFrame)!=JFileChooser.APPROVE_OPTION)
                return false;
            temp = fChooser.getSelectedFile();
            if(!temp.exists()) break;
            int x = JOptionPane.showConfirmDialog(
                    this.note.mainFrame,
                    "<html>" +temp.getPath()+ " already exists. Do you wish to overwrite?<html>",
                    "Save As",
                    JOptionPane.YES_NO_OPTION);
            if(x == JOptionPane.YES_OPTION) break;
        }
        return saveFile(temp);
    }
    boolean confirmSave(){
        String alert = "<html>Changes have been made to the document.<br>"
                + "Would you like to save changes?<html>";
        if(!saved){
            int opt = JOptionPane.showConfirmDialog(
                    this.note.mainFrame,
                    alert,
                    appName,
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if(opt==JOptionPane.CANCEL_OPTION) return false;
            else if(opt == JOptionPane.YES_OPTION && !saveThisFile()) return false;
        }
        return true;
    }


    //File Open operations
    void openFile(){
        if(!confirmSave()) return;
        fChooser.setDialogTitle("Open file...");
        fChooser.setApproveButtonText("Open");

        File temp;
        while (true){
            if (fChooser.showOpenDialog(this.note.mainFrame)!=JFileChooser.APPROVE_OPTION)
                return;
            temp = fChooser.getSelectedFile();
            if(temp.exists()) break;
            JOptionPane.showMessageDialog(
                    this.note.mainFrame,
                    "<html>" +temp.getName()+ "<br>File not found.<html>",
                    "Open",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        this.note.textArea.setText("");

        if(!openFile(temp)){
            fileName = "Untitled";
            this.note.mainFrame.setTitle(fileName + " - " + appName);
        }
        if(!temp.canWrite()) newFileFlag = true;
    }
    boolean openFile(File temp){
        FileInputStream fis = null;
        BufferedReader br = null;
        try{
            fis = new FileInputStream(temp);
            br = new BufferedReader(new InputStreamReader(fis));
            String text = " ";
            while(text != null){
                text = br.readLine();
                if(text == null) break;
                this.note.textArea.append(text + "\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            updateStatus(temp, false);
            return false;
        } finally {
            try {
                br.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateStatus(temp, true);
        this.note.textArea.setCaretPosition(0);
        return true;
    }


    void updateStatus(File temp, boolean saved){
        if(saved){
            this.saved = true;
            fileName = temp.getName();
            if(!temp.canWrite()){
                fileName += "(Read Only)";
                newFileFlag = true;
            }
            file = temp;
            note.mainFrame.setTitle(fileName + " - " + appName);
            note.statusLabel.setText("File: " +temp.getPath()+ " saved/opened successfully.");
            newFileFlag = false;
        } else {
            note.statusLabel.setText("Failed to save/open: " +temp.getPath());
            note.mainFrame.setTitle(fileName + "* - " + appName);
        }
    }


    //New file operation
    void newFile(){
        if(!confirmSave()) return;

        this.note.textArea.setText("");
        fileName = "Untitled";
        file = new File(fileName);
        saved = true;
        newFileFlag = true;
        this.note.mainFrame.setTitle(fileName + " - " + appName);
    }

}
