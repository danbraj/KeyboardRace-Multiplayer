package Models;

public class CommunicationService {

    static public class Server {

        private JTextArea logs;
        private Packet packet = null;

        public Server(JTextArea logs) {
            this.logs = logs;
        }

        public void setPacket(Packet packet) {
            this.packet = packet;
        }

        public boolean tryLogonToServer(ArrayList<Connection> clients) {
            
        }

        private void addMessage(String content) {
            logs.append(content + "\n");
            logs.setCaretPosition(logs.getDocument().getLength());
        }
    }

    static public class Client {

        private Packet packet;

        public Client(Packet packet) {
            this.packet = packet;
        }

        public int logoutClient(int status) {

            return status & ~Consts.CONNECTED;
        }

        public String showMessageIfExists() {

            String message = packet.getString();
            if (message != null && !message.isEmpty())
                return message;
            else
                return "";
        }

        public void startGame() {

        }
    }
}