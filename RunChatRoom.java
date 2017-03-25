package Assignment.Assignment3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.ServerSocket;

/**
 * Created by dinhtungtp on 3/16/2017.
 */
public class RunChatRoom
{
    JButton serverBtn, clientBtn;
    public static void main(String[] args) {
        RunChatRoom chatRoom = new RunChatRoom();
        chatRoom.buildGUI();
    }

    private void buildGUI() {
        // Server Frame
        JFrame serverFrame = new JFrame("Start Program");

        JPanel panel1 = new JPanel();
        // Instruction
        JLabel instruction = new JLabel("Welcome to Client-Server Chat Room");
        panel1.add(instruction, BorderLayout.NORTH);

        // Control panel
        serverBtn = new JButton("Start Server");
        serverBtn.setBackground(Color.green);
        clientBtn = new JButton("Initiate Client");
        clientBtn.setBackground(Color.green);
        panel1.add(serverBtn);
        panel1.add(clientBtn);

        serverFrame.add(panel1, BorderLayout.CENTER);

        // Start server
        startServer();

        // Initiate client
        initiateClient();

        // Show serverFrame
        serverFrame.setSize(250, 100);
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.setVisible(true);

    }

    private void startServer() {
        Server server = new Server();
        Thread serverThread = new Thread(server);
        serverThread.start();
        serverBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverBtn.getText().equals("Start Server")){
                    server.start();
                    serverBtn.setText("Stop Server");
                    serverBtn.setBackground(Color.red);
                    JOptionPane.showMessageDialog(null,"Server has been started", "Start Server", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    server.terminate();
                    System.out.println("Stop Server");
                    serverBtn.setText("Start Server");
                    serverBtn.setBackground(Color.green);
                    JOptionPane.showMessageDialog(null,"Server has been disconnected", "Stop Server", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    private void initiateClient() {
        clientBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread client = new Thread(new Client());
                client.start();
            }
        });
    }
}
