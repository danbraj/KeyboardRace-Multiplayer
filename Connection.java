import java.net.Socket;
import java.net.UnknownHostException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Connection {

    protected Socket socket;
    protected ObjectInputStream ois;
    protected ObjectOutputStream oos;

    public Connection(Socket socket) {
        this.socket = socket;
        this.declarationStreams();
    }

    private void declarationStreams() {
        try {
            oos = new ObjectOutputStream(this.socket.getOutputStream());
            ois = new ObjectInputStream(this.socket.getInputStream());
            oos.flush();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendPacket(Packet packet) {
        this.sendPacket(packet, false);
    }

    public void sendPacket(Packet packet, boolean reset) {
        try {
            if (reset)
                oos.reset();
            oos.writeObject(packet);
            oos.flush();
        } catch (IOException e) {
        }
    }

    public void sendPacketAgain(Packet packet) {
        try {
            oos.writeUnshared(packet);
            oos.flush();
        } catch (IOException e) {
        }
    }

    public Packet receivePacket() {
        try {
            return (Packet) ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void closeConnection() {
        try {
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException e) {
        }
    }
}