package cauc;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.net.UnknownHostException;
import java.security.Security;

/**
 * @author jiaxv
 */
public class Main {

    private static void createGUI() throws UnknownHostException {
        ImageIcon imageIcon = new ImageIcon("src/main/resources/79912bf08255554390402759a50d45b3.png");
        FlatIntelliJLaf.install();
        //窗口标题
        JFrame frame = new JFrame("启动软件");
        //构建窗口
        //frame.setContentPane(new DSP_MainForm().mainPanel);
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        //frame.setLayout(null);
        //在屏幕中间显示
        frame.setLocationRelativeTo(null);
        //禁止调整大小
        frame.setResizable(false);
        frame.setSize(600, 300);
        frame.setIconImage(imageIcon.getImage().getScaledInstance(80, 80, Image.SCALE_DEFAULT));
    }

    /**
     * 主函数
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println((char) 20);
        Security.addProvider(new BouncyCastleProvider());

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    createGUI();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
