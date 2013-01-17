package cn.client;

import cn.common.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JDialog;

public class ScreenLoser extends Screen {
    private int id;

    private int round;

    private JLabel text = new JLabel(Configure.TEXT_LOSER);

    private JButton bcontinue = new JButton(Configure.TEXT_CONTINUE);

    private JButton bhall = new JButton(Configure.TEXT_HALL);

    public ScreenLoser(final Client client, int id, int round) {
        super(client, true);
        this.id = id;
        this.round = round;
    }

    public void doCreate() {
        this.text.setBounds(0, 0, 480, 360);

        this.bcontinue.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    performContinue(event);
                }
            }
        );
        this.bcontinue.setBounds(120, 240, 120, 40);

        this.bhall.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    performHall(event);
                }
            }
        );
        this.bhall.setBounds(240, 240, 120, 40);

        this.interfaces.add(this.text);
        this.interfaces.add(this.bcontinue);
        this.interfaces.add(this.bhall);
    }

    public void doDelete() {
        this.interfaces.remove(this.text);
        this.interfaces.remove(this.bcontinue);
        this.interfaces.remove(this.bhall);
    }

    public void doUpdate() {
        Boolean answer = this.agent.getPlayerContinue();
        if ( answer != null && answer == false ) {
            this.manager.startNewScreen(new ScreenHall(this.client));
        }
    }

    public void doRender() {
    }

    public void doResume() {
    }

    public void doSuspend() {
    }

    public void doRestart() {
    }

    public void performContinue(ActionEvent event) {
        final JDialog dialog = this.showInvitionResponseWaitingDialog();

        if ( this.agent.continued(true) ) {
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            this.manager.startNewScreen(new ScreenGame(this.client, this.id, this.round + 1));
        } else {
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            this.showInvitionResponseRejectDialog();
            this.manager.startNewScreen(new ScreenHall(this.client));
        }
    }

    public void performHall(ActionEvent event) {
        this.agent.continued(false);
        this.manager.startNewScreen(new ScreenHall(this.client));
    }
}
