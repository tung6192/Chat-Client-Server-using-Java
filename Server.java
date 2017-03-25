package Assignment.Assignment3;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * Created by dinhtungtp on 3/13/2017.
 */
public class Server implements Runnable{
    static ArrayList<OutStream> clientOutputStreams = new ArrayList<>();
    static ServerSocket serverSocket;
    private volatile boolean running = true;
    Connection connection;
    Statement stmt;
    String dbUrl, userName, password, dbName;

    public void run() {
        connectDatabase();

        try {
            System.out.println("Starting the server");
            if (serverSocket == null){
                serverSocket = new ServerSocket(5555);
            }

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Got a connection");

//                OutputStream out = clientSocket.getOutputStream();

                // create OutStream(name, OutputStream)
                BufferedReader firstReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String user = firstReader.readLine();
                OutputStream outputStream = clientSocket.getOutputStream();
                OutStream out = new OutStream(user, outputStream);
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
        }
    }

    private void connectDatabase() {
        try {
            // Get a connection to database
            dbUrl = "jdbc:mysql://localhost:3307/";
            userName = "root";
            password = "";
            connection = DriverManager.getConnection(dbUrl, userName, password);

            // Create statement
            stmt = connection.createStatement();

            // Check database exits
            dbName = "tungndcc00504x";

            boolean isExist = false;
            ResultSet dbResultSet = connection.getMetaData().getCatalogs();
            while (dbResultSet.next()){
                String database = dbResultSet.getString(1);
                if (database.equals(dbName)){
                    isExist = true;
                    break;
                }
            }
            if (!isExist){
                stmt.execute("CREATE DATABASE " + dbName);
                stmt.execute("USE " + dbName);
                stmt.execute("CREATE TABLE chat_message (" +
                        "ChatID int AUTO_INCREMENT PRIMARY KEY," +
                        "Sender varchar(255)," +
                        "Receiver varchar(255)," +
                        "Content tinytext," +
                        "ChatTime datetime DEFAULT CURRENT_TIMESTAMP" +
                        ");");

            }

            // update database connection
            dbUrl = dbUrl + dbName;
            connection = DriverManager.getConnection(dbUrl, userName, password);

        } catch (Exception e) {
            System.out.println("My local port has been changed to 3307, usually port for mysql is 3306. Update your username & password as well");
            e.printStackTrace();
        }

    }

    public void start() {
        running = true;
    }

    public void terminate(){
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            OutStream outStream = (OutStream) it.next();
            OutputStream outputStream = outStream.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println("... Server down. Please re-login later");
            writer.close();
        }
        running = false;
    }

    private class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket clientSocket;

        public ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String s = null;
            PreparedStatement preparedStmt;
            String message = "", sender="";
            try {
                while (running && (s = reader.readLine()) != null) {
                    // Get message, exclude name
                    List<String> textArray = new ArrayList<String>(Arrays.asList(s.split(":")));
                    // check if message is sent by sender or a server message
                    if (textArray.size()==2){
                        sender = textArray.get(0);
                        message = textArray.get(1);
                    }

                    // Print whole message to other client
                    Iterator it = clientOutputStreams.iterator();
                    while (it.hasNext()) {
                        OutStream outStream = (OutStream) it.next();
                        String receiver = outStream.getName();
                        OutputStream outputStream = outStream.getOutputStream();

                        // Send message to client
                        PrintWriter writer = new PrintWriter(outputStream, true);
                        writer.println(s);

                        // check if message is sent by sender or a server message && sender is not receiver
                        if (textArray.size()==2 && !receiver.equals(sender)) {
                            // Save message to server
                            try {

                                preparedStmt = connection.prepareStatement("INSERT INTO chat_message(Sender, Receiver, Content) Values(?,?,?)");
                                preparedStmt.setString(1, sender);
                                preparedStmt.setString(2, receiver);
                                preparedStmt.setString(3, message);
                                preparedStmt.executeUpdate();

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
