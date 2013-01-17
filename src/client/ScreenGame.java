package cn.client;

import cn.common.*;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

final class Canvas extends JPanel {
    private byte[] board;

    public Canvas() {
        super();
        this.board = new byte[9];
    }

    public void paint(Graphics graphics) {
        super.paint(graphics);

        Graphics2D graphics2d = (Graphics2D)graphics;
        // Color color = graphics2d.getColor();
        graphics2d.fillRect(80, 105, 320, 10);
        graphics2d.fillRect(80, 215, 320, 10);
        graphics2d.fillRect(180, 5, 10, 320);
        graphics2d.fillRect(290, 5, 10, 320);

        for ( int index = 0 ; index < 9 ; ++index ) {
            byte mark = this.board[index];
            if ( mark == Configure.CIRCLE ) {
                this.drawCircle(graphics2d, index);
            } else if ( mark == Configure.CROSS ) {
                this.drawCross(graphics2d, index);
            }
        }
    }

    public void markCircle(int location) {
        this.board[location] = Configure.CIRCLE;
    }

    public void markCross(int location) {
        this.board[location] = Configure.CROSS;
    }

    private void drawCircle(Graphics2D graphics2d, int location) {
        Point point = this.calculateDrawLocation(location);
        graphics2d.drawOval(point.x, point.y, 90, 90);
    }

    private void drawCross(Graphics2D graphics2d, int location) {
        Point point = this.calculateDrawLocation(location);
        graphics2d.drawLine(point.x, point.y, point.x + 90, point.y + 90);
        graphics2d.drawLine(point.x + 90, point.y, point.x, point.y + 90);
    }

    private Point calculateDrawLocation(int location) {
        return new Point((location % 3) * 110 + 85, (location / 3) * 110 + 10);
    }
}

public class ScreenGame extends Screen {
    private int id;

    private int round;

    private JLabel status = new JLabel(Configure.TEXT_WAITING);

    private Canvas canvas = new Canvas();

    private JPanel information = new JPanel();

    public ScreenGame(final Client client, int id, int round) {
        super(client, true);
        this.id = id;
        this.round = round;
        this.agent.getAgentData().initializePlayerData();
    }

    public void doCreate() {
        this.agent.ready();
        while ( this.agent.getPlayerContinue() == null ) {}
        if ( this.agent.getPlayerContinue() == true ) {
            JLabel rounds = new JLabel(Configure.TEXT_ROUND + this.round);
            JButton surrender = new JButton(Configure.TEXT_SURRENDER);

            rounds.setBounds(0, 145, 160, 40);

            surrender.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        performSurrender(event);
                    }
                }
            );
            surrender.setBounds(70, 145, 80, 40);

            this.status.setBounds(160, 0, 160, 40);

            this.canvas.setLayout(null);
            this.canvas.addMouseListener(
                new MouseListener() {
                    public void mouseClicked(MouseEvent event) {
                    }
                    public void mousePressed(MouseEvent event) {
                    }
                    public void mouseReleased(MouseEvent event) {
                        performCanvasClick(event);
                    }
                    public void mouseEntered(MouseEvent event) {
                    }
                    public void mouseExited(MouseEvent event) {
                    }
                }
            );
            this.canvas.setBounds(0, 0, 480, 360);

            this.information.setLayout(null);
            this.information.add(rounds);
            this.information.add(surrender);
            this.information.add(this.drawPlayerInformation(this.agent.getPlayerData(this.agent.getAgentData().getAgentId()), 0, 0));
            this.information.add(this.drawPlayerInformation(this.agent.getPlayerData(this.id), 0, 190));
            this.information.setBounds(480, 0, 160, 360);

            this.interfaces.add(this.status);
            this.interfaces.add(this.canvas);
            this.interfaces.add(this.information);
        } else {
            this.manager.startNewScreen(new ScreenHall(this.client));
        }
    }

    public void doDelete() {
        this.interfaces.remove(this.status);
        this.interfaces.remove(this.canvas);
        this.interfaces.remove(this.information);
    }

    public void doUpdate() {
        if ( this.agent.getIsFreshed() ) this.manager.updated(true);

        if ( this.agent.getMovementState() ) {
            this.status.setVisible(false);
        } else {
            this.status.setVisible(true);
        }

        switch ( this.agent.getGameState() ) {
            case Packet.WINNER :
                {
                    this.manager.startNewScreen(new ScreenWinner(this.client, this.id, this.round));
                    break;
                }
            case Packet.LOSER :
                {
                    this.manager.startNewScreen(new ScreenLoser(this.client, this.id, this.round));
                    break;
                }
            case Packet.DRAW :
                {
                    this.manager.startNewScreen(new ScreenDraw(this.client, this.id, this.round));
                    break;
                }
            default :
                {
                    break;
                }
        }
    }

    public void doRender() {
        for ( int index = 0 ; index < 9 ; ++index ) {
            byte mark = this.agent.getGameBoard(index);
            if ( mark == Configure.CIRCLE ) {
                this.canvas.markCircle(index);
            } else if ( mark == Configure.CROSS ) {
                this.canvas.markCross(index);
            }
        }
    }

    public void doResume() {
    }

    public void doSuspend() {
    }

    public void doRestart() {
    }

    public void performSurrender(ActionEvent event) {
        this.agent.surrender();
    }

    public void performCanvasClick(MouseEvent event) {
        if ( this.agent.getMovementState() && event.getButton() == MouseEvent.BUTTON1 ) {
            int x = event.getX() - 80;
            int y = event.getY() - 20;
            if ( x >= 0 && x <= 320 && y >= 0 && y <= 320 ) {
                int location = ((x / 110) + (y / 110) * 3);
                this.agent.move(location);
            }
        }
    }

    private JPanel drawPlayerInformation(Agent.Player data, int x, int y) {
        JPanel panel = new JPanel();
        JLabel name = new JLabel(data.name);
        JLabel wins = new JLabel(Configure.TEXT_WINS + data.wins);
        JLabel lost = new JLabel(Configure.TEXT_LOST + data.lost);

        name.setBounds(0, 0, 160, 30);
        wins.setBounds(0, 30, 160, 30);
        lost.setBounds(0, 60, 160, 30);

        panel.setLayout(null);
        panel.add(name);
        panel.add(wins);
        panel.add(lost);
        panel.setBounds(x, y, 160, 140);

        return panel;
    }
}
