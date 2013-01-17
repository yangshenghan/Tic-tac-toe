package cn.client;

import cn.common.*;
import java.util.HashMap;

public class Data {
    final public class Invition {
        public int id;
        public String name;
        public String identifier;
    };

    public int id;

    public char state;

    public boolean freshed;

    public byte[] board;

    public String name;

    public Boolean movement;

    public Boolean response;

    public Boolean continued;

    public Invition invition;

    public Agent.Player player;

    public HashMap<Integer, String> players;

    Data() {
        this.board = new byte[9];
        this.initializePlayerData();
    }

    public Invition CreateNewInvition(int id, String name) {
        this.invition = new Invition();
        this.invition.id = id;
        this.invition.name = name;
        this.invition.identifier = name + "<" + id + ">";
        return this.invition;
    }

    public void initializePlayerData() {
        this.state = 0;
        this.freshed = false;
        this.movement = false;
        this.response = null;
        this.invition = null;
        this.continued = null;
        this.player = null;
        this.players = null;
        for ( int index = 0 ; index < 9 ; ++index ) {
            this.board[0] = 0;
        }
    }

    public int getAgentId() {
        return this.id;
    }

    public String getPlayerName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.name + "<" + this.id + ">";
    }
}
