package cn.client;

import cn.common.*;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class Agent implements Runnable {
    public class Player {
        public int id;
        public String name;
        public int wins;
        public int lost;
    };

    private boolean connected;

    private Thread thread;

    private Client client;

    private Socket socket;

    private Data data;

    private DataOutputStream sender;

    private DataInputStream receiver;

    public Agent(final Socket socket, final Client client) throws IOException {
        this.socket = socket;
        this.client = client;
        this.data = new Data();
        this.sender = new DataOutputStream(socket.getOutputStream());
        this.receiver = new DataInputStream(socket.getInputStream());
        this.connected = false;
    }

    public void run() {
        try {
            char operation;

            while ( this.disconnected() ) {}

            while ( (operation = this.receiver.readChar()) != Packet.DISCONNECT ) {
                switch ( operation ) {
                    case Packet.LIST :
                        {
                            this.retriveGameList();
                            break;
                        }
                    case Packet.INVITE :
                        {
                            this.receiveInvitionResponse();
                            break;
                        }
                    case Packet.RETRIVE :
                        {
                            this.retrivePlayerData();
                            break;
                        }
                    case Packet.ACCEPT :
                        {
                            this.retriveInvitionResponse(true);
                            break;
                        }
                    case Packet.REJECT :
                        {
                            this.retriveInvitionResponse(false);
                            break;
                        }
                    case Packet.WAITING :
                        {
                            this.retriveMovementState();
                            break;
                        }
                    case Packet.WINNER :
                        {
                            this.retriveGameState(Packet.WINNER);
                            break;
                        }
                    case Packet.LOSER :
                        {
                            this.retriveGameState(Packet.LOSER);
                            break;
                        }
                    case Packet.DRAW :
                        {
                            this.retriveGameState(Packet.DRAW);
                            break;
                        }
                    case Packet.CONTINUE :
                        {
                            this.retriveGameContinue();
                            break;
                        }
                    case Packet.REFRESH :
                        {
                            this.retriveGameBoard();
                            break;
                        }
                    case Packet.NOTIFY :
                        {
                            this.retriveServerNotify();
                            break;
                        }
                    default :
                        Logger.warning("Unknown packet.");
                }
            }
        } catch ( SocketException exception ) {
            Logger.info("Stop waiting packets.");
        } catch ( IOException exception ) {
            exception.printStackTrace();
        } finally {
            this.closeSocketConnection();
        }
    }

    public void connect(String name) {
        if ( this.disconnected() ) {
            try {
                this.thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            while ( client.running() ) {
                                beating();
                                Thread.sleep(Configure.BEATING * 1000);
                            }
                        } catch ( InterruptedException exception ) {
                            exception.printStackTrace();
                        }
                    }
                });

                if ( this.receiver.readChar() != Packet.CONNECT ) {
                    Logger.error("Incorrect packet.");
                    this.disconnect();
                    return;
                }

                if ( this.receiver.readChar() != Packet.VERSION ) {
                    Logger.error("The version is not compatible.");
                    this.disconnect();
                    return;
                }

                this.data.id = this.receiver.readInt();
                this.data.name = name;
                this.sender.writeChar(Packet.CONFIRM);
                this.sender.writeUTF(name);
                this.connected = true;

                this.thread.setDaemon(true);
                while ( ! this.thread.isAlive() ) {
                    this.thread.start();
                }
            } catch ( IOException exception ) {
                exception.printStackTrace();
                this.closeSocketConnection();
            }

            Logger.log("Get identifier " + this.data.getIdentifier() + " from the server.");
        }
    }

    public void disconnect() {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.CONFIRM);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            } finally {
                this.closeSocketConnection();
            }

            Logger.log("Disconnect from the server.");
        }
        this.connected = false;
    }

    public Data getAgentData() {
        return this.data;
    }

    public synchronized HashMap<Integer, String> getGameList() {
        if ( ! this.disconnected() ) {
            try {
                this.data.players = null;
                this.sender.writeChar(Packet.LIST);
                while ( this.data.players == null ) {
                    wait();
                }
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: getGameList.");
        }
        return this.data.players;
    }

    public synchronized Player getPlayerData(int id) {
        if ( ! this.disconnected() ) {
            try {
                this.data.player = null;
                this.sender.writeChar(Packet.RETRIVE);
                this.sender.writeInt(id);
                while ( this.data.player == null ) {
                    wait();
                }
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: getPlayerData.");
        }
        return this.data.player;
    }

    public synchronized void beating() {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.BEAT);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: BEAT.");
        }
        notify();
    }

    public synchronized boolean invite(int id) {
        if ( ! this.disconnected() ) {
            try {
                this.data.response = null;
                this.sender.writeChar(Packet.INVITE);
                this.sender.writeInt(id);
                while ( this.data.response == null ) {
                    wait();
                }
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: Invite player <" + id + ">.");
        }
        return this.data.response;
    }

    public synchronized boolean response(int id, boolean answer) {
        if ( ! this.disconnected() ) {
            try {
                this.data.invition = null;
                if ( answer ) {
                    this.sender.writeChar(Packet.ACCEPT);
                } else {
                    this.sender.writeChar(Packet.REJECT);
                }
                this.sender.writeInt(id);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: ACCEPT or REJECT to player <" + id + ">.");
        }
        return answer;
    }

    public synchronized void move(int location) {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.MOVE);
                this.sender.writeInt(location);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: MOVE.");
        }
    }

    public synchronized void ready() {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.READY);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: READY.");
        }
    }

    public synchronized boolean continued(boolean answer) {
        if ( ! this.disconnected() ) {
            try {
                this.data.continued = null;
                this.sender.writeChar(Packet.CONTINUE);
                this.sender.writeBoolean(answer);
                while ( this.data.continued == null ) {
                    wait();
                }
            } catch ( InterruptedException exception ) {
                exception.printStackTrace();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: CONTINUE.");
        }
        return this.data.continued;
    }

    public synchronized void surrender() {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.SURRENDER);
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: SURRENDER.");
        }
    }

    public synchronized HashMap<Integer, String> getUpdatedList() {
        if ( this.data.players != null ) {
            final HashMap<Integer, String> players = this.data.players;
            this.data.players = null;
            return players;
        }
        return null;
    }

    public synchronized Data.Invition getIsInvited() {
        return this.data.invition;
    }

    public synchronized Boolean getMovementState() {
        return this.data.movement;
    }

    public synchronized char getGameState() {
        return this.data.state;
    }

    public synchronized boolean getIsFreshed() {
        return this.data.freshed;
    }

    public synchronized byte getGameBoard(int index) {
        this.data.freshed = false;
        return this.data.board[index];
    }

    public synchronized Boolean getPlayerContinue() {
        return this.data.continued;
    }

    public void leave() {
        if ( ! this.disconnected() ) {
            try {
                this.sender.writeChar(Packet.LEAVE);
                this.disconnect();
            } catch ( IOException exception ) {
                exception.printStackTrace();
            }
            Logger.log("Sent: Leave.");
        }
    }

    public boolean disconnected() {
        return ! this.connected || this.socket.isClosed();
    }

    private synchronized void retriveGameList() throws IOException {
        int size = this.receiver.readInt();
        this.data.players = new HashMap<Integer, String>();
        for ( int count = 0 ; count < size ; ++count ) {
            int id = this.receiver.readInt();
            String name = this.receiver.readUTF();
            this.data.players.put((Integer)id, name);
        }
        notify();
        Logger.log("Received: LIST.");
    }

    private synchronized void receiveInvitionResponse() throws IOException {
        int id = this.receiver.readInt();
        String name = this.receiver.readUTF();
        this.data.invition = this.data.CreateNewInvition(id, name);
        notify();
        Logger.log("Received: INVITE from " + name + "<" + id + ">.");
    }

    private synchronized void retriveInvitionResponse(boolean answer) throws IOException {
        this.data.response = answer;
        notify();
        Logger.log("Received: ACCEPT or REJECT.");
    }

    private synchronized void retriveMovementState() throws IOException {
        this.data.movement = ! this.receiver.readBoolean();
        notify();
        Logger.log("Received: WAITING<" + this.data.movement + ">.");
    }

    private synchronized void retriveGameState(char state) throws IOException {
        this.data.state = state;
        notify();
        Logger.log("Received: WINNER or LOSER or DRAW.");
    }

    private synchronized void retriveGameContinue() throws IOException {
        this.data.continued = this.receiver.readBoolean();
        notify();
        Logger.log("Received: CONTINUE.");
    }

    private synchronized void retriveGameBoard() throws IOException {
        for ( int index = 0 ; index < 9 ; ++index ) {
            this.data.board[index] = this.receiver.readByte();
        }
        this.data.freshed = true;
        notify();
        Logger.log("Received: REFRESH.");
    }

    private synchronized void retrivePlayerData() throws IOException {
        this.data.player = new Player();
        this.data.player.id = this.receiver.readInt();
        this.data.player.name = this.receiver.readUTF();
        this.data.player.wins = this.receiver.readInt();
        this.data.player.lost = this.receiver.readInt();
        notify();
        Logger.log("Received: REFRESH.");
    }

    private synchronized void retriveServerNotify() throws IOException {
        notify();
        Logger.log("Received: NOTIFY.");
    }

    private void closeSocketConnection() {
        if ( ! this.socket.isClosed() ) {
            try {
                this.thread.interrupt();
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
