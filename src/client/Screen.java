package cn.client;

import cn.common.*;
import java.awt.FlowLayout;
import java.util.Stack;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

abstract public class Screen {
    private boolean updated;

    protected Agent agent;

    protected Client client;

    protected Manager manager;

    protected Interface interfaces;

    public Screen(final Client client, Boolean connected) {
        this.client = client;
        this.manager = client.getManager();
        this.interfaces = client.getInterface();

        if ( connected ) {
            this.agent = client.getAgent();
        } else {
            this.agent = null;
        }

        this.updated = true;
    }

    public void create() {
        this.doCreate();
        this.updated = true;
        this.update();
    }

    public void delete() {
        this.doDelete();
        this.updated = true;
    }

    public void update() {
        this.doUpdate();

        if ( this.agent != null && this.agent.disconnected() ) {
            this.showDisconnectionDialog();
            this.client.running(false);
        }
    }

    public void render() {
        this.doRender();
    }

    public void resume() {
        this.doResume();
        this.updated = true;
    }

    public void suspend() {
        this.doSuspend();
        this.updated = true;
    }

    public void restart() {
        this.doRestart();
        this.updated = true;
    }

    abstract public void doCreate();
    abstract public void doDelete();
    abstract public void doUpdate();
    abstract public void doRender();
    abstract public void doResume();
    abstract public void doSuspend();
    abstract public void doRestart();

    final public boolean updated(boolean value) {
        return this.updated = value;
    }

    final public boolean updated() {
        return this.updated;
    }

    final public void showDisconnectionDialog() {
        JOptionPane.showMessageDialog(
            this.interfaces.getMainFrame(),
            Configure.TEXT_DISCONNECTION_MESSAGE,
            Configure.TEXT_DISCONNECTION_TITLE,
            JOptionPane.PLAIN_MESSAGE
        );
    }

    final public JDialog showInvitionResponseWaitingDialog() {
        final JDialog dialog = new JDialog(
            this.interfaces.getMainFrame(),
            Configure.TEXT_WAITING_TITLE,
            null
        );
    
        final JLabel label = new JLabel(Configure.TEXT_WAITING_MESSAGE);

        dialog.setLayout(new FlowLayout());
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);

        dialog.getContentPane().add(label);
        dialog.pack();
        dialog.setVisible(true);

        return dialog;
    }

    final public void showInvitionResponseRejectDialog() {
        JOptionPane.showMessageDialog(
            this.interfaces.getMainFrame(),
            Configure.TEXT_RESPONSE_MESSAGE,
            Configure.TEXT_RESPONSE_TITLE,
            JOptionPane.PLAIN_MESSAGE
        );
    }

    final public boolean showInvitionDialog(String name) {
        final Object[] options = {
            Configure.TEXT_YES,
            Configure.TEXT_NO
        };

        final int response = JOptionPane.showOptionDialog(
            this.interfaces.getMainFrame(),
            String.format(Configure.TEXT_INVITION_MESSAGE, name),
            Configure.TEXT_INVITION_TITLE,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if ( response == 0 ) return true;
        return false;
    }
}
