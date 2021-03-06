import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable {

    private ServerGUI gui;
    private ServerSocket server;

    public Server(ServerGUI gui) {
        this.gui = gui;
    }

    public void terminate() {
        try {
            for (ServerConnection conn : gui.app.clients) {
                if (conn != null) {
                    conn.sendPacket(
                            new Packet(Command.LOGOUT, conn.getPlayer().getPlayerId(), "Serwer został wyłączony."));
                    conn.closeConnection();
                }
            }
            server.close();
            gui.addLog("Wszystkie połączenie zostały zakończone.");
        } catch (IOException e) {
        }
    }

    public void run() {
        try {
            server = new ServerSocket(new Integer(gui.port.getText()));
            gui.addLog("Serwer uruchomiony na porcie: " + server.getLocalPort());
            gui.addLog("Maksymalna pojemność serwera to " + gui.app.clients.size() + " miejsc.");

            while (Common.isStatusContainsFlag(Status.RUNNING)) {
                Socket socket = server.accept();
                (new Thread(new ServerConnection(socket, gui))).start();
                //new ServerConnection(socket, gui).start();
            }
        } catch (SocketException e) {
        } catch (Exception e) {
            gui.addLog(e.toString());
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                gui.addLog(e.toString());
            }
        }
        gui.addLog("Serwer został zatrzymany.");
    }
}