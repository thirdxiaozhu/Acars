package cauc;

import Protocol.DesktopApi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainForm {
    public JPanel mainPanel;
    private JButton DSPButton;
    private JButton CMUButton;
    private JButton methodButton;
    private JButton aboutButton;
    private JLabel icon;
    private ImageIcon imageIcon = new ImageIcon("src/main/resources/79912bf08255554390402759a50d45b3.png");

    public MainForm() {
        initPanel();
    }

    private void initPanel() {
        //icon.setIcon(new ImageIcon("src/main/resources/图片1.png"));

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
                frame.setIconImage(imageIcon.getImage().getScaledInstance(80, 80,Image.SCALE_DEFAULT));
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
                        if (cmuMainForm.client != null) {
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
                frame.setIconImage(imageIcon.getImage().getScaledInstance(80, 80,Image.SCALE_DEFAULT));
                CMUButton.setEnabled(false);
            }
        });

        methodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DesktopApi.browse(new URI("http://www.goosenest.xyz"));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });

        aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("关于");
                About about = new About();
                //构建窗口
                frame.setContentPane(about.mainDialog);
                //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                //在屏幕中间显示
                frame.setLocationRelativeTo(null);
                //禁止调整大小
                frame.setResizable(false);
                frame.setSize(400, 200);
                frame.setIconImage(imageIcon.getImage().getScaledInstance(80, 80,Image.SCALE_DEFAULT));
            }
        });
    }
}
