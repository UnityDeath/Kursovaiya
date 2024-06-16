package view;

import DB.DBWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SanatoriumsWindow extends JFrame {

    private static JTable tableSanatoriums;
    private JButton btnBack, btnAddSanatorium, btnAddTo, btnDel;
    private DBWorker dbWorker;
    private MainWindow mainWindow;

    public SanatoriumsWindow(MainWindow mainWindow) {
        super("Санатории");
        setSize(800, 400);
        this.mainWindow = mainWindow;
        dbWorker = new DBWorker();
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
        updateBoard();
    }

    public void init() {

        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("Санатории", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] TicketNames = {"id", "name", "city", "adress", "cost", "rating", "time"};
        Object[][] TicketsData = {
                {"", "", "", "", "", "", ""},
                {"", "", "", "", "", "", ""},
                {"", "", "", "", "", "", ""},
                {"", "", "", "", "", "", ""},
                {"", "", "", "", "", "", ""}
        };
        tableSanatoriums = new JTable(TicketsData, TicketNames);
        JScrollPane scrollPane = new JScrollPane(tableSanatoriums);
        add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnBack = new JButton("Назад");
        this.btnBack.setBackground(Color.GRAY);
        this.btnBack.setForeground(Color.WHITE);
        buttonPanel.add(btnBack);

        btnAddSanatorium = new JButton("Добавить санаторий");
        this.btnAddSanatorium.setBackground(Color.GRAY);
        this.btnAddSanatorium.setForeground(Color.WHITE);
        buttonPanel.add(btnAddSanatorium);

        btnAddTo = new JButton("Добавить в путевку");
        this.btnAddTo.setBackground(Color.GRAY);
        this.btnAddTo.setForeground(Color.WHITE);
        buttonPanel.add(btnAddTo);

        btnDel = new JButton("Удалить санаторий");
        this.btnDel.setBackground(Color.GRAY);
        this.btnDel.setForeground(Color.WHITE);
        buttonPanel.add(btnDel);

        add(buttonPanel, BorderLayout.SOUTH);

        addListeners();

        // Установка видимости окна
        setVisible(true);
    }

    private void addListeners() {
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Закрываем окно при нажатии кнопки
            }
        });
        btnAddSanatorium.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTicketDialog();
            }
        });
        btnAddTo.addActionListener(e -> {
            String sanatoriumIdStr = JOptionPane.showInputDialog(this, "Введите ID санатория:");
            if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(sanatoriumIdStr.trim());
                    ResultSet resultSet = dbWorker.getFromDBById(ticketId, "Sanatorium");
                    if (resultSet.next()) {
                        Object[] rowData = {
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("city"),
                                resultSet.getString("adress"),
                                resultSet.getInt("cost"),
                                resultSet.getInt("rating"),
                                resultSet.getString("time")
                        };
                        mainWindow.addRowToTableSanatoriums(rowData);
                    } else {
                        JOptionPane.showMessageDialog(this, "Sanatorium ID not found");
                    }
                } catch (NumberFormatException | SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error retrieving sanatorium data");
                }
            }
        });
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sanatoriumIdStr = JOptionPane.showInputDialog(SanatoriumsWindow.this, "Введите ID санатория для удаления:");
                if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                    try {
                        int sanatoriumId = Integer.parseInt(sanatoriumIdStr.trim());
                        dbWorker.deleteFromDBById(sanatoriumId, "Sanatorium");
                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(SanatoriumsWindow.this, "Ошибка при удалении санатория: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    protected static void updateBoard() {
        DBWorker dbWorker = new DBWorker();
        try {
            ResultSet resultSet = dbWorker.getAllFromDB("Sanatorium");

            String[] columnNames = {"ID", "Имя", "Город", "Адрес", "Стоимость", "Рейтинг", "Количество дней"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                        case 4:
                        case 5:
                        case 6:
                            return Integer.class;
                        default:
                            return String.class;
                    }
                }
            };

            // Проверка наличия данных в ResultSet
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No data returned from the query.");
            }

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String city = resultSet.getString("city");
                String adress = resultSet.getString("adress");
                int cost = resultSet.getInt("cost");
                int rating = resultSet.getInt("rating");
                String time = resultSet.getString("time");
                model.addRow(new Object[]{id, name, city, adress, cost, rating, time});
            }

            // Создание и настройка сортировщика
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tableSanatoriums.setRowSorter(sorter);
            tableSanatoriums.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTicketDialog() {
        JTextField nameField = new JTextField(10);
        JTextField cityField = new JTextField(10);
        JTextField addressField = new JTextField(10);
        JTextField costField = new JTextField(10);
        JTextField ratingField = new JTextField(10);
        JTextField timeField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(7, 2));
        panel.add(new JLabel("Имя санатория:"));
        panel.add(nameField);
        panel.add(new JLabel("Город:"));
        panel.add(cityField);
        panel.add(new JLabel("Адрес:"));
        panel.add(addressField);
        panel.add(new JLabel("Стоимость:"));
        panel.add(costField);
        panel.add(new JLabel("Рейтинг:"));
        panel.add(ratingField);
        panel.add(new JLabel("Количество дней:"));
        panel.add(timeField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавление санатория", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String city = cityField.getText();
                String address = addressField.getText();
                int cost = Integer.parseInt(costField.getText());
                int rating = Integer.parseInt(ratingField.getText());
                String time = timeField.getText();

                Object[] rowData = {null, name, city, address, cost, rating, time};
                dbWorker.addToDB("Sanatorium", rowData);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении санатория: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        updateBoard();
    }


}
