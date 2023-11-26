import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final int PORT = 8888;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private ServerSocketChannel serverSocketChannel;
    private Map<SocketChannel, String> clientMap = new HashMap<>();

    public Server() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));

            System.out.println("Server started on port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                SocketChannel clientChannel = serverSocketChannel.accept();
                clientChannel.configureBlocking(true); // Set to blocking mode

                String clientName = getClientName(clientChannel);
                clientMap.put(clientChannel, clientName);

                broadcast(clientName + " has joined the chat");
                sendWelcomeMessage(clientChannel);

                new Thread(() -> {
                    try {
                        while (true) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int bytesRead = clientChannel.read(buffer);

                            if (bytesRead > 0) {
                                buffer.flip();
                                CharBuffer charBuffer = CHARSET.decode(buffer);
                                String message = charBuffer.toString();
                                String formattedMessage = clientName + " " + message;

                                // Display message on the server console
                                System.out.println(formattedMessage);

                                // Broadcast message to other clients
                                broadcastToOthers(clientChannel, formattedMessage);
                            } else {
                                // Client has disconnected
                                clientChannel.close();
                                clientMap.remove(clientChannel);
                                broadcast(clientName + " has left the chat");
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast message to all clients except the sender
    private void broadcastToOthers(SocketChannel senderChannel, String message) throws IOException {
        for (SocketChannel channel : clientMap.keySet()) {
            if (channel != senderChannel) {
                channel.write(CHARSET.encode(message));
            }
        }
    }

    private String getClientName(SocketChannel clientChannel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer);
        buffer.flip();
        return CHARSET.decode(buffer).toString().trim();
    }

    private void sendWelcomeMessage(SocketChannel clientChannel) throws IOException {
        String welcomeMessage = "Welcome to the chat, enter your messages!";
        clientChannel.write(CHARSET.encode(welcomeMessage));
    }

    private void broadcast(String message) throws IOException {
        for (SocketChannel channel : clientMap.keySet()) {
            channel.write(CHARSET.encode(message));
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}