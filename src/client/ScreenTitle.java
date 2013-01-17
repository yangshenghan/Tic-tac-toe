package cn.client;

import cn.common.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;

public class ScreenTitle extends Screen {
    private JTextField name = new JTextField(24);

    private JButton login = new JButton();

    private JButton readme = new JButton();

    public ScreenTitle(final Client client) {
        super(client, false);
    }

    public void doCreate() {
        this.name.setText(Configure.TEXT_DEFAULT_NAME);
        this.name.setToolTipText(Configure.TEXT_NAME_TIPS);
        this.name.setBounds(195, 180, 250, 30);

        this.login.setText(Configure.TEXT_LOGIN);
        this.login.setToolTipText(Configure.TEXT_LOGIN_TIPS);
        this.login.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    performLogin(event);
                }
            }
        );
        this.login.setBounds(325, 210, 120, 30);

        this.readme.setText(Configure.TEXT_README);
        this.readme.setToolTipText(Configure.TEXT_README_TIPS);
        this.readme.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    performReadme(event);
                }
            }
        );
        this.readme.setBounds(195, 210, 120, 30);

        this.interfaces.add(this.name);
        this.interfaces.add(this.login);
        this.interfaces.add(this.readme);
    }

    public void doDelete() {
        this.interfaces.remove(this.name);
        this.interfaces.remove(this.login);
        this.interfaces.remove(this.readme);
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

    public void performLogin(ActionEvent event) {
        Agent agent = this.client.getAgent();

        if ( agent != null ) agent.connect(this.name.getText());

        if ( agent != null && ! agent.disconnected() ) {
            this.manager.startNewScreen(new ScreenHall(this.client));
        } else {
            Logger.error(Configure.TEXT_CANNOT_CONNECT);
            this.showDisconnectionDialog();
        }
    }

    public void performReadme(ActionEvent event) {
        this.manager.startNewScreen(new ScreenReadme(this.client));
    }
}
