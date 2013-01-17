package cn.client;

import cn.common.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;

public class ScreenReadme extends Screen {
    private JButton back = new JButton();

    private JTextPane readme = new JTextPane();

    private JScrollPane scroll = new JScrollPane();

    public ScreenReadme(final Client client) {
        super(client, false);
    }

    public void doCreate() {
        this.scroll.setBounds(80, 60, 480, 240);

        this.readme.setEditable(false);
        this.readme.setContentType("text/html");
        this.readme.setText(Configure.TEXT_README_CONTENT);
        this.readme.setBounds(0, 0, 480, 200);
        this.readme.setCaretPosition(0);

        this.back.setText(Configure.TEXT_BACK);
        this.back.setToolTipText(Configure.TEXT_BACK_TIPS);
        this.back.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    performBack(event);
                }
            }
        );
        this.back.setBounds(440, 300, 120, 30);

        this.scroll.getViewport().add(this.readme);

        this.interfaces.add(this.scroll);
        this.interfaces.add(this.back);
    }

    public void doDelete() {
        this.interfaces.remove(this.back);
        this.interfaces.remove(this.scroll);
    }

    public void doUpdate() {
    }

    public void doRender() {
    }

    public void doResume() {
    }

    public void doSuspend() {
    }

    public void doRestart() {
    }

    public void performBack(ActionEvent event) {
        this.manager.startNewScreen(new ScreenTitle(this.client));
    }
}
