package cn.server;

import cn.common.*;

public class Data {
    public int id;

    public int wins;

    public int lost;

    public boolean busy;

    public boolean ready;

    public boolean surrender;

    public int location;

    public String name;

    public Boolean continued;

    Data() {
        this.id = this.hashCode();
        this.ready = false;
        this.initializePlayerData();
    }

    public void initializePlayerData() {
        this.wins = 0;
        this.lost = 0;
        this.resetPlayerData();
    }

    public void resetPlayerData() {
        this.busy = false;
        this.location = 9;
        this.surrender = false;
        this.continued = null;
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

    public int getPlayerWins() {
        return this.wins;
    }

    public int getPlayerLost() {
        return this.lost;
    }

    public void setPlayerBusy(boolean busy) {
        this.busy = busy;
    }

    public void increaseWins() {
        this.wins++;
    }

    public void increaseLost() {
        this.lost++;
    }
}
