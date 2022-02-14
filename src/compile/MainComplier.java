package compile;

import frame.OutText;
import frame.ReadText;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * @description This is the main run window
 */
@SuppressWarnings("all")
public class MainComplier extends JFrame {

    //UI components, run windows with some buttons
    JButton jButton1 = new JButton("lexical analysis");
    JButton jButton2 = new JButton("empty");
    JButton jButton3 = new JButton("Grammar 1");
    JButton jButton4 = new JButton("Grammar 2");
    JButton jButton5 = new JButton("Grammar 3");
    JButton jButton6 = new JButton("Grammar 4");
    JLabel inLabel = new JLabel("File to Analyze");
    JLabel outLabel = new JLabel("Analysis Results");
    ReadText readText = new ReadText(7, 63);//input area
    OutText outText = new OutText(30, 65);//output area

    //For subclass inheritance
    public MainComplier(ReadText readText, OutText outText) throws HeadlessException {
        this.readText = readText;
        this.outText = outText;
    }
    //event listener
    public void initListener() {
        ActListener actListener = new ActListener();
        jButton1.addActionListener(actListener);
        jButton2.addActionListener(actListener);
        jButton3.addActionListener(actListener);
        jButton4.addActionListener(actListener);
        jButton5.addActionListener(actListener);
        jButton6.addActionListener(actListener);
    }
    //Build event listeners
    class ActListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jButton1) {
                new Lexer(readText, outText).Analysis();
            } else if (e.getSource() == jButton2) {
                readText.setText("");
                outText.setText("");
            } else if (e.getSource() == jButton3) {
               new Parser(readText, outText,1).Main();
            }
            else if (e.getSource() == jButton4) {
                new Parser(readText, outText,2).Main();
            }
            else if (e.getSource() == jButton5) {
                new Parser(readText, outText,3).Main();
            }
            else if (e.getSource() == jButton6) {
                new Parser(readText, outText,4).Main();
            }
        }
    }
    //Main window
    public MainComplier() throws IOException {
        super("lexical analyzer");
        setSize(650, 720);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));//center alignment
        this.add(this.inLabel);
        this.add(this.readText);
        this.add(this.outLabel);
        this.add(this.outText);
        this.add(this.jButton1);
        this.add(this.jButton3);
        this.add(this.jButton4);
        this.add(this.jButton5);
        this.add(this.jButton6);
        this.add(this.jButton2);
        initListener();
        this.setVisible(true);//Settings window to enter text
    }
}
