package cn.client;

import cn.common.*;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

public class ScreenHall extends Screen {
    private class Room {
        public int id;
        public String name;
    }

    private Thread thread;

    private JPanel list = new JPanel();

    private JScrollPane scroll = new JScrollPane();

    private List<Room> rooms = new ArrayList<Room>();

    public ScreenHall(final Client client) {
        super(client, true);

        this.thread = new Thread(new Runnable() {
            public void run() {
                Agent agent = client.getAgent();
                try {
                    while ( client.running() ) {
                        agent.getGameList();
                        Thread.sleep(Configure.BEATING * 1000);
                    }
                } catch ( InterruptedException exception ) {
                    exception.printStackTrace();
                }
            }
        });
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public void doCreate() {
        this.scroll.setBounds(80, 60, 480, 240);
        this.scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.interfaces.add(this.scroll);
    }

    public void doDelete() {
        this.interfaces.remove(this.scroll);
    }

    public void doUpdate() {
        Data.Invition invition = this.agent.getIsInvited();

        this.updateGameRooms();

        if ( invition != null ) {
            this.updated(true);
            if ( this.agent.response(invition.id, this.showInvitionDialog(invition.identifier)) ) {
                this.thread.interrupt();
                this.manager.startNewScreen(new ScreenGame(this.client, invition.id, 1));
            }
        }
    }

    public void doRender() {
        int index;
        int width = ((Integer)UIManager.get("ScrollBar.width")).intValue();

        index = 0;
        width = width + (20 - width % 20);

        this.list = new JPanel();
        this.list.setLayout(null);
        for ( Room room : this.rooms ) {
            JButton button = new JButton();
            button.setText(room.name);
            button.setText(room.name);
            button.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        performInvite(event);
                    }
                }
            );
            button.setBounds((index % 4) * ((480 - width) / 4), (index / 4) * 60, ((480 - width) / 4), 60);
            this.list.add(button);
            index++;
        }
        this.list.setBounds(0, 0, 480 - width, (index / 4 + (index % 4 == 0 ? 0 : 1)) * 60);

        this.scroll.setViewportView(this.list);
    }

    public void doResume() {
    }

    public void doSuspend() {
    }

    public void doRestart() {
    }

    public void performInvite(ActionEvent event) {
        final Room room = this.rooms.get(Arrays.asList(this.list.getComponents()).indexOf(event.getSource()));
        final JDialog dialog = this.showInvitionResponseWaitingDialog();

        if ( this.agent.invite(room.id) ) {
            Logger.info("Accept!");
            this.thread.interrupt();
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            this.manager.startNewScreen(new ScreenGame(this.client, room.id, 1));
        } else {
            Logger.info("Reject!");
            dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            this.showInvitionResponseRejectDialog();
        }
    }

    private void updateGameRooms() {
        Map<Integer, String> list = this.agent.getUpdatedList();

        if ( list != null ) {
            this.rooms = new ArrayList<Room>();
            for ( Map.Entry<Integer, String> entry : list.entrySet() ) {
                Room room = new Room();
                room.id = entry.getKey();
                room.name = entry.getValue();
                this.rooms.add(room);
            }
            this.manager.updated(true);
        }
    }
}
