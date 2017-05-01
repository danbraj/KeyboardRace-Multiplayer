import java.io.Serializable;

class Player implements Serializable {
    int idPlayer = -1;
    String nick = "---";
    boolean isReady = false;
    int numberOfWins = 0;
    int actionPoints = 0;

    public void setId(int id) {
        this.idPlayer = id;
    }

    public int getId() {
        return this.idPlayer;
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
}