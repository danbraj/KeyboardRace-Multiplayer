package Models;

import java.io.Serializable;

public class Player implements Serializable {

    public boolean isReady;
    int playerId = -1;
    String nick = "?";
    int actionPoints; // nieużywana

    public Player(int id) {
        this.playerId = id;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public boolean toggleAndGetReady() {
        this.isReady = !this.isReady;
        return this.isReady;
    }

    public void setUnready() {
        this.isReady = false;
    }
}