package com.github.shannah.jdeployhellocommands;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JdeployHelloCommands extends JFrame {

    public JdeployHelloCommands() {
        setTitle("Hello World");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Hello World", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        add(label);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            SwingUtilities.invokeLater(() -> {
                JdeployHelloCommands frame = new JdeployHelloCommands();
                frame.setVisible(true);
            });
        } else {
            System.out.println(args[0]);

            if (args.length >= 2) {
                try {
                    int exitCode = Integer.parseInt(args[1]);
                    System.exit(exitCode);
                } catch (NumberFormatException e) {
                    System.err.println("Error: Second argument must be an integer");
                    System.exit(1);
                }
            }
        }
    }
}
