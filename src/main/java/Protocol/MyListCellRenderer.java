package Protocol;

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

        BasicProtocol protocol = (BasicProtocol)value;

        label.setText(protocol.getTime() + " " + Util.getPlainText(protocol));

        label.setHorizontalTextPosition(JLabel.RIGHT);
        label.setFont(font);
        return label;
    }
}
