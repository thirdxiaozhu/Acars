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
        JFrame frame = new JFrame("地面站DSP"); //窗口标题
        frame.setContentPane(new MainForm().mainPanel); //构建窗口
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null); //在屏幕中间显示
        //frame.setAlwaysOnTop(true); //永远处于最上方
        frame.setResizable(false); //禁止调整大小
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
