import java.net.Socket;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            // Membuat socket klien dan terhubung ke server di alamat IP dan port tertentu
            Socket clientSocket = new Socket("localhost", 8080);

            // Mendapatkan output stream untuk mengirim data ke server
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // Memasukkan nama klien
            System.out.println("Masukkan nama Anda: ");
            Scanner scanner = new Scanner(System.in);
            String clientName = scanner.nextLine();

            // Kirimkan nama klien ke server
            writer.println(clientName);

            // Menerima pesan selamat datang dari server
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String welcomeMessage = reader.readLine();
            System.out.println(welcomeMessage);

            // Thread untuk menerima pesan dari server
            Thread receiveThread = new ClientReceiveThread(clientSocket);
            receiveThread.start();

            // Mengirim pesan ke server
            while (true) {
                System.out.println("Ketik pesan Anda: ");
                String message = scanner.nextLine();
                writer.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientReceiveThread extends Thread {
    private Socket clientSocket;

    public ClientReceiveThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            // Menerima dan menampilkan pesan dari server
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}