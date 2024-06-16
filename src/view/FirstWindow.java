package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import DB.DBWorker;

public class FirstWindow extends JFrame {
    private JButton btn_login;
    private JLabel jLabel_hello, jLabel_login, jLabel_password;
    private JTextField tf_login;
    private JPasswordField pf_password;

    public FirstWindow() {
        super("Вход");
        setSize(300, 200); // Размер окна
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // Закрытие окна
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
    }

    public void init() {

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);


        jLabel_hello = new JLabel("Турагентство Самойленко", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(jLabel_hello, gbc);


        jLabel_login = new JLabel("Логин");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(jLabel_login, gbc);

        tf_login = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(tf_login, gbc);

        jLabel_password = new JLabel("Пароль");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(jLabel_password, gbc);

        pf_password = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(pf_password, gbc);

        btn_login = new JButton("Вход");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        this.btn_login.setBackground(Color.GRAY);
        this.btn_login.setForeground(Color.WHITE);
        gbc.anchor = GridBagConstraints.CENTER;
        add(btn_login, gbc);

        addListeners();

        setVisible(true);
    }

    private void addListeners() {
        btn_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String login = tf_login.getText();
                char [] password = pf_password.getPassword();
                login(login, new String(password));
                dispose();
            }
        });
    }

    private void login(String login, String password) {

        if (DBWorker.checkCredentials(login, password)) {
            JOptionPane.showMessageDialog(this, "Успешно");
            new MainWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Неверный логин или пароль");
            new FirstWindow();
        }

    }
}
