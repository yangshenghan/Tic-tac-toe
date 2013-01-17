package cn.server;

import cn.common.*;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class Agent implements Runnable {
    private boolean connected;

    private Data data;

    private Server server;

    private Socket socket;

    private DataOutputStream sender;

    private DataInputStream receiver;

    public Agent(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.socket.setSoTimeout(0);

        this.data = new Data();
        this.sender = new DataOutputStream(socket.getOutputStream());
        this.receiver = new DataInputStream(socket.getInputStream());

        this.connected = false;
    }

    public void run() {
        try {
            char operation;

            while ( this.disconnected() ) {}

            while ( (operation = this.receiver.readChar()) != Packet.LEAVE ) {
                switch ( operation ) {
                    case Packet.LIST :
                        {
                            this.sendGameList();
                            break;
                        }
                    case Packet.INVITE :
                        {
                            this.sendInvite();
                            break;
                        }
                    case Packet.ACCEPT :
                        {
                            this.sendResponse(true);
                            break;
                        }
                    case Packet.REJECT :
                        {
                            this.sendResponse(false);
                            break;
                        }
                    case Packet.RETRIVE :
                        {
                            this.sendPlayerData();
                            break;
                        }
                    case Packet.MOVE :
                        {
                            this.retrivePlayerMovement();
                            break;
                        }
                    case Packet.SURRENDER :
                        {
                            this.retrivePlayerSurrender();
                            break;
                        }
                    case Packet.CONTINUE :
                        {
                            this.retrivePlayerContinue();
                            break;
                        }
                    case Packet.READY :
                        {
                            this.retrivePlayerReady();
                            break;
                        }
                    case Packet.BEAT :
                        {
                            this.sendNotify();
                            break;
                        }
                    default :
                        Logger.warning("Unknown packet.");
                }
            }
        } catch ( SocketException exception ) {
            Logger.log("The player: " + this.data.getIdentifier() + " had force closed connection.");
        } catch ( IOException exception ) {
            exception.printStackTrace();
        } finally {
            this.closeSocketConnection();
        }
    }

    public void connect() {
        if ( this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.CONNECT);
                this.sender.writeChar(Packet.VERSION);
                this.sender.writeInt(this.data.getAgentId());
                if ( this.receiver.readChar() == Packet.CONFIRM ) {
                    this.data.name = this.receiver.readUTF();
                    this.connected = true;
                    this.socket.setSoTimeout(Configure.TIMEOUT * 1000);
                    Logger.log("Player " + this.data.getIdentifier() + " joined.");
                } else {
                    this.closeSocketConnection();
                }
            } catch ( IOException exception ) {
                exception.printStackTrace();
                this.closeSocketConnection();
            }
        }
    }

    public void disconnect() {
        if ( ! this.disconnected() ) {
            try {
                this.connected = false;
                this.sender.writeChar(Packet.DISCONNECT);
                if ( this.receiver.readChar() != Packet.CONFIRM ) {
                    Logger.error("Incorrect packet.");
                    return;
                }
            } catch ( IOException exception ) {
                exception.printStackTrace();
            } finally {
                this.closeSocketConnection();
            }
        }

        Logger.log("The player: " + this.data.getIdentifier() + " lefted.");
    }

    public Data getAgentData() {
        return this.data;
    }

    public void sendGameList() throws IOException {
        if ( ! this.data.busy ) {
            List<Agent> list = new ArrayList<Agent>();
            for ( Agent agent : this.server.getAgentCollection() ) {
                if ( ! agent.data.busy && agent.data.getAgentId() != this.data.getAgentId() ) {
                    list.add(agent);
                }
            }

            this.sender.writeChar(Packet.LIST);
            this.sender.writeInt(list.size());
            for ( Agent agent : list ) {
                this.sender.writeInt(agent.data.getAgentId());
                this.sender.writeUTF(agent.data.getPlayerName());
            }

            Logger.log("Sent " + this.data.getIdentifier() + ": Gmae list with size " + list.size() + ".");
        }
    }

    public void sendInvite() throws IOException {
        Agent agent = this.server.getAgent(this.receiver.readInt());
        if ( agent.data.busy ) {
            this.sender.writeChar(Packet.REJECT);
            this.data.busy = false;
            Logger.log("Sent " + this.data.getIdentifier() + ": REJECT.");
        } else {
            agent.sender.writeChar(Packet.INVITE);
            agent.sender.writeInt(this.data.getAgentId());
            agent.sender.writeUTF(this.data.getPlayerName());
            agent.data.busy = true;
            this.data.busy = true;
            Logger.log("Sent " + agent.data.getIdentifier() + ": INVITE.");
        }
    }

    public void sendResponse(boolean answer) throws IOException {
        final Agent agent = this.server.getAgent(this.receiver.readInt());
        if ( answer ) {
            agent.sender.writeChar(Packet.ACCEPT);
            Game.startNewGame(this, agent);
        } else {
            agent.sender.writeChar(Packet.REJECT);
            agent.data.busy = false;
            this.data.busy = false;
        }
        Logger.log("Sent " + agent.data.getIdentifier() + ": ACCEPT or REJECT (" + answer + ").");
    }

    public void sendWaiting(boolean waiting) throws IOException {
        this.sender.writeChar(Packet.WAITING);
        this.sender.writeBoolean(waiting);
        this.data.location = 9;
        Logger.log("Sent " + this.data.getIdentifier() + ": WAITING (" + waiting + ").");
    }

    public void sendWinner() throws IOException {
        this.sender.writeChar(Packet.WINNER);
        Logger.log("Sent " + this.data.getIdentifier() + ": WINNER.");
    }

    public void sendLoser() throws IOException {
        this.sender.writeChar(Packet.LOSER);
        Logger.log("Sent " + this.data.getIdentifier() + ": LOSER.");
    }

    public void sendDraw() throws IOException {
        this.sender.writeChar(Packet.DRAW);
        Logger.log("Sent " + this.data.getIdentifier() + ": DRAW.");
    }

    public void sendContinue(boolean answer) throws IOException {
        this.sender.writeChar(Packet.CONTINUE);
        this.sender.writeBoolean(answer);
        Logger.log("Sent " + this.data.getIdentifier() + ": CONTINUE (" + answer + ").");
    }

    public void sendNotify() throws IOException {
        this.sender.writeChar(Packet.NOTIFY);
        Logger.log("Sent " + this.data.getIdentifier() + ": NOTIFY.");
    }

    public void sendRefresh(byte[] board) throws IOException {
        this.sender.writeChar(Packet.REFRESH);
        for ( int index = 0 ; index < 9 ; ++index ) {
            this.sender.writeByte(board[index]);
        }
        Logger.log("Sent " + this.data.getIdentifier() + ": REFRESH.");
    }

    public void sendPlayerData() throws IOException {
        final Agent agent = this.server.getAgent(this.receiver.readInt());

        this.sender.writeChar(Packet.RETRIVE);
        this.sender.writeInt(agent.data.getAgentId());
        this.sender.writeUTF(agent.data.getPlayerName());
        this.sender.writeInt(agent.data.getPlayerWins());
        this.sender.writeInt(agent.data.getPlayerLost());

        Logger.log("Sent " + this.data.getIdentifier() + ": RETRIVE (data of " + agent.data.getPlayerName() + ").");
    }

    public synchronized boolean getPlayerSurrender() {
        return this.data.surrender;
    }

    public synchronized int getPlayerMovement() {
        return this.data.location;
    }

    public synchronized Boolean getPlayerContinue() {
        return this.data.continued;
    }

    public boolean disconnected() {
        return ! this.connected || this.socket.isClosed();
    }

    private synchronized void retrivePlayerMovement() throws IOException {
        this.data.location = this.receiver.readInt();
        notify();
        Logger.log("Received: MOVE.");
    }

    public synchronized void retrivePlayerSurrender() throws IOException {
        this.data.surrender = true;
        notify();
        Logger.log("Received: SURRENDER.");
    }

    public synchronized void retrivePlayerContinue() throws IOException {
        this.data.continued = this.receiver.readBoolean();
        notify();
        Logger.log("Received: CONTINUE.");
    }

    public synchronized void retrivePlayerReady() throws IOException {
        this.data.ready = true;
        notify();
        Logger.log("Received: READY.");
    }

    private void closeSocketConnection() {
        if ( ! this.socket.isClosed() ) {
            try {
                this.connected = false;
                this.receiver.close();
                this.sender.close();
                this.socket.close();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
        }
    }
}
