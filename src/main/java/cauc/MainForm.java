package cauc;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainForm {
    public JPanel mainPanel;
    private JButton DSPButton;
    private JButton CMUButton;
    private JButton MethodButton;
    private JButton AboutButton;
    private JLabel icon;

    public MainForm() {
        initPanel();
    }

    private void initPanel() {
        icon.setIcon(new ImageIcon("src/main/resources/图片1.png"));

        DSPButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("地面站DSP");
                DSP_MainForm dspMainForm = new DSP_MainForm();
                //构建窗口
                frame.setContentPane(dspMainForm.mainPanel);
                //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        //super.windowClosing(e);
                        DSPButton.setEnabled(true);
                        try {
                            dspMainForm.serverListener.closeConnection();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //在屏幕中间显示
                frame.setLocationRelativeTo(null);
                //禁止调整大小
                frame.setResizable(false);
                DSPButton.setEnabled(false);
            }
        });

        CMUButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("机载CMU");
                CMU_MainForm cmuMainForm = new CMU_MainForm();
                //构建窗口
                frame.setContentPane(cmuMainForm.mainPanel);
                //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        CMUButton.setEnabled(true);
                        if(cmuMainForm.client != null) {
                            cmuMainForm.client.closeConnection();
                        }
                    }
                });
                frame.pack();
                frame.setVisible(true);
                frame.setLayout(null);
                //在屏幕中间显示
                frame.setLocationRelativeTo(null);
                //禁止调整大小
                frame.setResizable(false);
                CMUButton.setEnabled(false);
            }
        });


    }
}
