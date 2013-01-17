package cn.server;

import cn.common.*;
import java.io.IOException;

public class Game extends Thread {
    private class Player {
        public Agent agent;
        public byte mark;
        public Player(Agent agent, byte mark) {
            this.agent = agent;
            this.mark = (byte)mark;
        }
    };

    private byte board[];

    private int current;

    private int setps;

    private Player players[];

    public Game(final Agent a, final Agent b) {
        this.board = new byte[9];
        this.players = new Player[2];
        this.players[0] = new Player(a, Configure.CIRCLE);
        this.players[1] = new Player(b, Configure.CROSS);
        this.current = 0;
        this.setps = 0;

        this.players[0].agent.getAgentData().resetPlayerData();
        this.players[1].agent.getAgentData().resetPlayerData();

        for ( int index = 0 ; index < 9 ; ++index ) {
            this.board[0] = 0;
        }

        this.players[0].agent.getAgentData().setPlayerBusy(true);
        this.players[1].agent.getAgentData().setPlayerBusy(true);

        try {
            this.players[0].agent.sendContinue(true);
            this.players[1].agent.sendContinue(true);
        } catch ( IOException exception ) {
            exception.printStackTrace();
        }
    }

    public static void startNewGame(final Agent a, final Agent b) {
        Game game = new Game(a, b);
        game.setDaemon(true);
        game.start();
    }

    public void run() {
        int location;

        try {
            while ( true ) {
                Boolean a = this.players[0].agent.getAgentData().ready;
                Boolean b = this.players[1].agent.getAgentData().ready;
                Logger.debug(a);
                Logger.debug(b);
                if ( a == true && b == true ) break;
            }

            this.players[0].agent.getAgentData().ready = false;
            this.players[1].agent.getAgentData().ready = false;

            this.players[0].agent.sendContinue(true);
            this.players[1].agent.sendContinue(true);

            this.players[0].agent.sendWaiting(true);
            this.players[1].agent.sendWaiting(true);

            while ( ! this.players[0].agent.disconnected() && ! this.players[1].agent.disconnected() ) {
                Agent agent = this.players[this.current].agent;

                this.players[0].agent.sendRefresh(this.board);
                this.players[1].agent.sendRefresh(this.board);

                agent.sendWaiting(false);
                if ( this.judgeGameOver() ) {
                    agent.sendLoser();
                    agent.getAgentData().increaseLost();
                    break;
                }

                do {
                    location = agent.getPlayerMovement();
                } while ( this.judgeInvalidMovement(location) && ! this.judgePlayerSurrender() );

                if ( this.judgePlayerSurrender() ) {
                    if ( this.players[this.current].agent.getPlayerSurrender() ) {
                        this.players[this.current].agent.sendLoser();
                        this.players[this.getOtherSide()].agent.sendWinner();
                    } else {
                        this.players[this.current].agent.sendWinner();
                        this.players[this.getOtherSide()].agent.sendLoser();
                    }
                    break;
                }

                this.setps++;
                this.board[location] = this.players[this.current].mark;
                this.current = this.getOtherSide();
                agent.sendWaiting(true);

                if ( this.setps == 9 ) {
                    this.players[0].agent.sendDraw();
                    this.players[1].agent.sendDraw();
                    break;
                }

                if ( this.judgeGameOver() ) {
                    agent.sendWinner();
                    agent.getAgentData().increaseWins();
                }
            }

            if ( this.players[0].agent.disconnected() ) {
                this.players[1].agent.sendWinner();
                this.players[1].agent.sendContinue(false);
            }

            if ( this.players[1].agent.disconnected() ) {
                this.players[0].agent.sendWinner();
                this.players[0].agent.sendContinue(false);
            }

            while ( ! this.players[0].agent.disconnected() && ! this.players[1].agent.disconnected() ) {
                Boolean a = this.players[0].agent.getPlayerContinue();
                Boolean b = this.players[1].agent.getPlayerContinue();

                if ( (a != null && a == false) || (b != null && b == false) ) {
                    this.players[0].agent.getAgentData().initializePlayerData();
                    this.players[1].agent.getAgentData().initializePlayerData();
                    this.players[0].agent.getAgentData().setPlayerBusy(false);
                    this.players[1].agent.getAgentData().setPlayerBusy(false);
                    this.players[0].agent.sendContinue(false);
                    this.players[1].agent.sendContinue(false);
                    break;
                }

                if ( a != null && a == true && b != null && b == true ) {
                    this.startNewGame(this.players[0].agent, this.players[1].agent);
                    break;
                }
            }
        } catch ( IOException exception ) {
            exception.printStackTrace();
        }
    }

    private boolean judgePlayerSurrender() {
        if ( this.players[0].agent.getPlayerSurrender() ) return true;
        if ( this.players[1].agent.getPlayerSurrender() ) return true;
        return false;
    }

    private boolean judgeInvalidMovement(int location) {
        if ( location < 0 || location > 8 ) return true;
        if ( this.board[location] == Configure.CIRCLE || this.board[location] == Configure.CROSS ) return true;
        return false;
    }

    private boolean judgeGameOver() {
        for ( int index = 0 ; index < 7 ; index += 3 ) {
            if ( this.board[index] != 0 && this.board[index] == this.board[index + 1] && this.board[index] == this.board[index + 2] ) return true;
        }

        for ( int index = 0 ; index < 3 ; index += 1 ) {
            if ( this.board[index] != 0 && this.board[index] == this.board[index + 3] && this.board[index] == this.board[index + 6] ) return true;
        }

        if ( this.board[0] != 0 && this.board[0] == this.board[4] && this.board[0] == this.board[8] ) return true;
        if ( this.board[2] != 0 && this.board[2] == this.board[4] && this.board[2] == this.board[6] ) return true;

        return false;
    }

    private int getOtherSide() {
        return this.current == 0 ? 1 : 0;
    }
}
