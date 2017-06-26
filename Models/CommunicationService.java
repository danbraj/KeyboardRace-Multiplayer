package Models;

public class CommunicationService {

    public class Server {

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