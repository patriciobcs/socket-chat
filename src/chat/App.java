package chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.swing.*;

public class App {
    String title = "EchoChat";
    JFrame newFrame = new JFrame(title);
    JButton sendMessage;
    JTextField messageBox;
    JTextArea chatBox;
    JTextField usernameChooser;
    JCheckBox castChooser;
    JFrame preFrame;
    EchoClient client;
    String username;

    public EchoClient getClient() {
        return client;
    }
    public void setClient(EchoClient client) {
        this.client = client;
    }

    public void init() {
        newFrame.setVisible(false);
        preFrame = new JFrame(title);
        usernameChooser = new JTextField(15);
        castChooser = new JCheckBox();
        JLabel chooseUsernameLabel = new JLabel("Username");
        JButton enterServer = new JButton("Chat");
        enterServer.addActionListener(new enterServerButtonListener());
        JPanel prePanel = new JPanel(new GridBagLayout());
        JPanel castPanel = new JPanel(new GridBagLayout());

        GridBagConstraints preTop = new GridBagConstraints();
        preTop.anchor = GridBagConstraints.CENTER;
        preTop.insets = new Insets(0, 10, 0, 10);
        preTop.gridwidth = GridBagConstraints.REMAINDER;
        GridBagConstraints preBottom = new GridBagConstraints();
        preBottom.anchor = GridBagConstraints.CENTER;
        preBottom.insets = new Insets(0, 10, 0, 10);
        preBottom.fill = GridBagConstraints.HORIZONTAL;
        preBottom.gridwidth = GridBagConstraints.REMAINDER;

        prePanel.add(chooseUsernameLabel, preTop);
        prePanel.add(usernameChooser, preBottom);

        castPanel.add(castChooser, preTop);
        castPanel.add(enterServer, preBottom);
        preFrame.add(prePanel, BorderLayout.CENTER);
        preFrame.add(castPanel, BorderLayout.SOUTH);
        preFrame.setSize(300, 300);
        preFrame.setVisible(true);
    }

    public void display() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        messageBox = new JTextField(30);
        messageBox.requestFocusInWindow();

        sendMessage = new JButton("Send Message");
        sendMessage.addActionListener(new sendMessageButtonListener());

        chatBox = new JTextArea();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
        chatBox.setLineWrap(true);

        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.LINE_START;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 512.0D;
        left.weighty = 1.0D;

        GridBagConstraints right = new GridBagConstraints();
        right.insets = new Insets(0, 10, 0, 0);
        right.anchor = GridBagConstraints.LINE_END;
        right.fill = GridBagConstraints.NONE;
        right.weightx = 1.0D;
        right.weighty = 1.0D;

        southPanel.add(messageBox, left);
        southPanel.add(sendMessage, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        newFrame.add(mainPanel);
        newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newFrame.setSize(470, 300);
        newFrame.setVisible(true);
    }

    public void addMessage(String message) {
        message += "\n";
        chatBox.append(message);
        messageBox.setText("");
        messageBox.requestFocusInWindow();
    }

    class sendMessageButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (messageBox.getText().length() > 0) {
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                String message = date + " - " + client.getName() + ": " + messageBox.getText();
                addMessage(message);
                client.send(message);
            }
        }
    }

    class enterServerButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            username = usernameChooser.getText();
            if (username.length() < 1) username = UUID.randomUUID().toString().substring(0, 6);
            display();
            client.setName(username);
            System.out.println(username);
            try {
                if (castChooser.isSelected()) {
                    client.setHost("228.5.6.7");
                    client.runClientMulticast();
                } else {
                    client.runClientUnicast();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            preFrame.setVisible(false);
        }

    }
}