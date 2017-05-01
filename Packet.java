import java.io.Serializable;
import java.util.ArrayList;

class Packet implements Serializable {

    Command command;
    String parameter;
    
    public Packet(Command command) {
        this.command = command;
    }

    public Packet(Command command, String parameter) {
        this.command = command;
        this.parameter = parameter;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getParameter() {
        return this.parameter;
    }
}