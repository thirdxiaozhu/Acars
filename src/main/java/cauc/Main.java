package cauc;

import Protocol.BasicProtocol;
import Protocol.DownlinkProtocol;
import Protocol.UplinkProtocol;
import Protocol.Util;
import com.formdev.flatlaf.FlatIntelliJLaf;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.net.UnknownHostException;
import java.security.Security;

/**
 * @author jiaxv
 */
public class Main {

    private static void createGUI() throws UnknownHostException {
        FlatIntelliJLaf.install();
        //窗口标题
        JFrame frame = new JFrame("地面站DSP");
        //构建窗口
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        //在屏幕中间显示
        frame.setLocationRelativeTo(null);
        //禁止调整大小
        frame.setResizable(false);
    }

    /**
     * 主函数
     * @param args
     */
    public static void main(String[] args) {
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
