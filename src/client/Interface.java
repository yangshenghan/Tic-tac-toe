package cn.client;

import cn.common.*;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Interface {
    private JFrame mainframe;

    public Interface(final Client client, final Manager manager) {
        final Interface interfaces = this;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch ( ClassNotFoundException exception ) {
            exception.printStackTrace();
        } catch ( InstantiationException exception ) {
            exception.printStackTrace();
        } catch ( IllegalAccessException exception ) {
            exception.printStackTrace();
        } catch ( UnsupportedLookAndFeelException exception ) {
            exception.printStackTrace();
        }

        this.mainframe = new JFrame();
        this.mainframe.setTitle(Configure.TITLE);
        this.mainframe.setLayout(null);
        this.mainframe.setSize(640, 360);
        this.mainframe.setResizable(false);
        this.mainframe.setLocationRelativeTo(null);
        this.mainframe.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    client.running(false);
                }
            }
        );
        this.mainframe.setVisible(true);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                while ( client.running() ) {
                    if ( manager.updated() ) {
                        manager.render();
                        interfaces.refresh();
                        manager.updated(false);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public JFrame getMainFrame() {
        return this.mainframe;
    }

    public void refresh() {
        this.mainframe.validate();
        this.mainframe.repaint();
    }

    public void add(Component component) {
        this.mainframe.getContentPane().add(component);
    }

    public void remove(Component component) {
        this.mainframe.getContentPane().remove(component);
    }
}
