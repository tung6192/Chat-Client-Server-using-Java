package Assignment.Assignment3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by dinhtungtp on 3/13/2017.
 */
public class Client implements Runnable
{
    BufferedReader reader;
    PrintWriter writer;
    JTextArea displayArea;
    JButton connectBtn, disconnectBtn, sendBtn;
    JRadioButton hostRBtn, guestRBtn;
    JLabel notiLable;
    JTextField commentField, nameField, hostField, portField;

    public void run() {
        JFrame frame = new JFrame("Simple TCP Chat");

        // 1. Connection panel
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.ipady = 10;
        c.insets = new Insets(5,10,0,0);
        c.anchor = GridBagConstraints.LINE_END;

        // 1.1 Label Field
        c.gridx = 0;
        c.gridy = 0;
        JLabel nameLabel = new JLabel("Name");
        panel1.add(nameLabel, c);

        c.gridy++;
        JLabel hostLabel = new JLabel("Host");
        panel1.add(hostLabel, c);

        c.gridy++;
        JLabel portLabel = new JLabel("Port");
        panel1.add(portLabel, c);

        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 1;
        c.gridy=0;
        nameField = new JTextField(12);
        panel1.add(nameField, c);

        c.gridy++;
        hostField = new JTextField(12);
        hostField.setText("127.0.0.1");
        panel1.add(hostField, c);

        c.gridy++;
        portField = new JTextField(12);
        portField.setText("5555");
        panel1.add(portField, c);

        // 1.2.Radio button
        c.gridx = 0;
        c.gridy = 3;
        ButtonGroup group = new ButtonGroup();
        hostRBtn = new JRadioButton("Host");
        hostRBtn.setMnemonic(0);
        hostRBtn.setSelected(true);
        group.add(hostRBtn);
        panel1.add(hostRBtn, c);

        c.gridx++;
        guestRBtn = new JRadioButton("Guest");
        guestRBtn.setMnemonic(1);
        group.add(guestRBtn);
        panel1.add(guestRBtn, c);

        // 1.3.Button
        c.gridx = 0;
        c.gridy = 6;
        connectBtn = new JButton("Connect");
        panel1.add(connectBtn, c);

        c.gridx++;
        disconnectBtn = new JButton("Disconnect");
        disconnectBtn.setEnabled(false);
        panel1.add(disconnectBtn, c);

        frame.add(panel1, BorderLayout.WEST);

        // 1.4.Add behavior
        hostRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setEditable(true);
            }
        });
        guestRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.setText("");
                nameField.setEditable(false);
            }
        });

        connectBtn.addActionListener(new ActionListener() {
            String name;
            @Override
            public void actionPerformed(ActionEvent e) {
                name = "";
                if (nameField.isEditable() && nameField.getText().equals("")){
                    JOptionPane.showMessageDialog(null,"You must provide your username", "Missing username", JOptionPane.ERROR_MESSAGE);
                    nameField.requestFocus();
                } else if (nameField.isEditable()){
                    name = nameField.getText();
                } else {
                    int randomNum = (int) (Math.random() * 10000);
                    name = "Guest [" + randomNum + "]";
                }

                // check username uniqueness
                boolean isUnique = true;
                for (int i = 0; i < Server.clientOutputStreams.size(); i++) {
                    String name = Server.clientOutputStreams.get(i).getName();
                    if (nameField.getText().equals(name)){
                        JOptionPane.showMessageDialog(null,"Username has been used. Please provide different name", "Username error", JOptionPane.ERROR_MESSAGE);
                        isUnique = false;
                        break;
                    }
                }

                if (isUnique && !name.equals("")){
                    String host = hostField.getText();
                    int port = 0;
                    try {
                        port = Integer.parseInt(portField.getText());
                        runConnection(name, host, port);
                    } catch (NumberFormatException ei){
                        JOptionPane.showMessageDialog(null,"Please provide a valid port", "Invalid port", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        disconnectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hostRBtn.setSelected(true);
                hostRBtn.setEnabled(true);
                guestRBtn.setEnabled(true);
                nameField.setText("");
                nameField.setEditable(true);
                hostField.setText("");
                hostField.setEditable(true);
                portField.setText("");
                portField.setEditable(true);
                connectBtn.setEnabled(true);
                disconnectBtn.setEnabled(false);
                notiLable.setText("Disconnected");
                notiLable.setForeground(Color.red);

            }
        });

        // 2. Chat panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        displayArea = new JTextArea(17,45);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        panel2.add(scrollPane);

        // 2.1 message panel
        JPanel msgPanel = new JPanel();
        commentField = new JTextField(33);
        Font font1 = new Font("SansSerif", Font.PLAIN, 17);
        commentField.setFont(font1);
        msgPanel.add(commentField);
        sendBtn = new JButton("Send");
        sendBtn.setEnabled(false);
        msgPanel.add(sendBtn);

        panel2.add(msgPanel);

        frame.add(panel2, BorderLayout.EAST);

        // 3. Notification panel
        JPanel panel3 = new JPanel();
        panel3.setBorder(BorderFactory.createMatteBorder(2,0,0,0,Color.BLUE));
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel status = new JLabel("Status:");
        panel3.add(status);
        notiLable = new JLabel("Disconnected");
        notiLable.setForeground(Color.red);
        panel3.add(notiLable);
        frame.add(panel3, BorderLayout.SOUTH);


        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void runConnection(String userName, String host, int port) {
        try{
            Socket socket = new Socket(host, port);
            displayArea.append("... Server has been connected\n");

            // change GUI
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            hostRBtn.setEnabled(false);
            guestRBtn.setEnabled(false);
            notiLable.setText("Connected");
            notiLable.setForeground(Color.green);
            sendBtn.setEnabled(true);
            nameField.setText(userName);
            nameField.setEditable(false);
            hostField.setEditable(false);
            portField.setEditable(false);

            // in & out stream
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(userName);
            writer.println("... Welcome " + userName + " joined the chat room");

            // create reader thread
            Thread t1 = new Thread(new inputReader());
            t1.start();

            // send message
            sendBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String msgout = commentField.getText();
                    if (msgout.equals("")){
                        return;
                    } else {
                        writer.println(userName + ": " + msgout);
                        commentField.setText("");
                        commentField.requestFocus();
                    }
                }
            });

            commentField.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    return;
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER){
                        sendBtn.setSelected(true);
                        String msgout = commentField.getText();
                        if (msgout.equals("")){
                            return;
                        } else {
                            writer.println(userName + ": " + msgout);
                            commentField.setText("");
                            commentField.requestFocus();
                        }

                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    return;
                }
            });

            // disconnect
            disconnectBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        writer.println("... "+userName + " has left the chat room");
                        System.out.println(userName + " disconnected");
                        // remove stream
                        for (int i = 0; i < Server.clientOutputStreams.size(); i++) {
                            String name = Server.clientOutputStreams.get(i).getName();
                            if (name.equals(userName)){
                                Server.clientOutputStreams.remove(i);
                                break;
                            }
                        }
                        System.out.println(Server.clientOutputStreams.size());
                        writer.close();
                        reader.close();

                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        } catch (UnknownHostException e) {
            displayArea.append("... Unable to connect. Please check your IP address\n");
            e.printStackTrace();
        } catch (IOException e) {
            displayArea.append("... Unable to connect to Server\n");
            e.printStackTrace();
        }
    }

    private class inputReader implements Runnable {
        String msgin = null;

        @Override
        public void run() {
            try{
                while ((msgin = reader.readLine()) != null){
                    displayArea.append(msgin + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
