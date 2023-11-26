import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    private static final int PORT = 8888;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Map<SocketChannel, String> clientMap = new HashMap<>();

    public Server() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started on port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                selector.select();

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        String clientName = getClientName(clientChannel);
        clientMap.put(clientChannel, clientName);

        broadcast(clientName + " has joined the chat");
        sendWelcomeMessage(clientChannel);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip();
            CharBuffer charBuffer = CHARSET.decode(buffer);
            String message = charBuffer.toString();
            String clientName = clientMap.get(clientChannel);
            String formattedMessage = clientName + " " + message;

            // Menampilkan pesan ke server console
            System.out.println(formattedMessage);

            // Menyebarkan pesan hanya kepada klien lainnya
            broadcastToOthers(clientChannel, formattedMessage);
        } else {
            // Client has disconnected
            String clientName = clientMap.get(clientChannel);
            clientChannel.close();
            clientMap.remove(clientChannel);
            broadcast(clientName + " has left the chat");
        }
    }

    // Menyebarkan pesan ke semua klien kecuali pengirim
    private void broadcastToOthers(SocketChannel senderChannel, String message) throws IOException {
        for (SocketChannel channel : clientMap.keySet()) {
            if (channel != senderChannel) {
                channel.write(CHARSET.encode(message));
            }
        }
    }

    private String getClientName(SocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            clientChannel.read(buffer);
            buffer.flip();
            return CHARSET.decode(buffer).toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
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