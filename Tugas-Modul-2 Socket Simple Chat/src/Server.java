import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.HashMap;

public class Server {
    public static HashMap<Socket, String> clients = new HashMap<>(); // Menyimpan klien dan nama mereka

    public static void main(String[] args) {
        try {
            // Membuat server socket dan mendengarkan koneksi dari klien di port tertentu
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server telah berjalan...");
            while (true) {
                // Menerima permintaan koneksi dari klien
                Socket clientSocket = serverSocket.accept();

                // Membaca nama klien
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientName = reader.readLine();

                // Menambahkan klien ke daftar klien yang terhubung
                clients.put(clientSocket, clientName);

                // Membuat thread untuk menangani koneksi dengan klien
                Thread clientThread = new ClientHandlerThread(clientSocket, clientName);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandlerThread extends Thread {
    private Socket clientSocket;
    private String clientName;

    public ClientHandlerThread(Socket clientSocket, String clientName) {
        this.clientSocket = clientSocket;
        this.clientName = clientName;
    }

    public void run() {
        try {
            // Mengirim pesan selamat datang ke klien
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("Selamat datang, " + clientName + "!");

            while (true) {
                // Menerima pesan dari klien
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = reader.readLine();

                // Kirim pesan ke semua klien yang terhubung
                for (Socket client : Server.clients.keySet()) {
                    if (client != clientSocket) {
                        writer = new PrintWriter(client.getOutputStream(), true);
                        writer.println(clientName + ": " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Hapus klien yang terputus dari daftar klien
            Server.clients.remove(clientSocket);
        }
    }
}