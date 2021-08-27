package cauc;

import Protocol.BasicProtocol;
import Protocol.Util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MyListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Font font = new Font("ubuntu", Font.BOLD, 15);

        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        label.setText(Util.getPlainText((BasicProtocol) value));

        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(font);
        return label;
    }
}
