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
import java.util.Comparator;

public class TicketsWindow extends JFrame {

    private static JTable tableTickets;
    private JButton btnBack, btnAddTicket, btnAddTo, btnDel;
    private DBWorker dbWorker;
    private MainWindow mainWindow;

    public TicketsWindow(MainWindow mainWindow) {
        super("Билеты");
        this.mainWindow = mainWindow;
        dbWorker = new DBWorker();
        setSize(800, 400);
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
        updateBoard();
    }

    public void init() {

        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("Билеты", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] TicketNames = {"id", "number", "leaving_city", "arriving_city", "depature_time", "date", "type", "cost"};
        Object[][] TicketsData = {
                {"", "", "", "", "", "", "", ""},
                {"", "", "", "", "", "", "", ""}
        };
        tableTickets = new JTable(TicketsData, TicketNames);
        JScrollPane scrollPane = new JScrollPane(tableTickets);
        add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnBack = new JButton("Назад");
        this.btnBack.setBackground(Color.GRAY);
        this.btnBack.setForeground(Color.WHITE);
        buttonPanel.add(btnBack);

        btnAddTicket = new JButton("Добавить билет");
        this.btnAddTicket.setBackground(Color.GRAY);
        this.btnAddTicket.setForeground(Color.WHITE);
        buttonPanel.add(btnAddTicket);

        btnAddTo = new JButton("Добавить в путевку");
        this.btnAddTo.setBackground(Color.GRAY);
        this.btnAddTo.setForeground(Color.WHITE);
        buttonPanel.add(btnAddTo);

        btnDel = new JButton("Удалить билет");
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
        btnAddTicket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTicketDialog();
            }
        });
        btnAddTo.addActionListener(e -> {
            String ticketIdStr = JOptionPane.showInputDialog(this, "Enter Ticket ID to add to main table:");
            if (ticketIdStr != null && !ticketIdStr.trim().isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(ticketIdStr.trim());
                    ResultSet resultSet = dbWorker.getFromDBById(ticketId, "Ticket");
                    if (resultSet.next()) {
                        Object[] rowData = {
                                resultSet.getInt("id"),
                                resultSet.getInt("number"),
                                resultSet.getString("leaving_city"),
                                resultSet.getString("arriving_city"),
                                resultSet.getString("depature_time"),
                                resultSet.getString("date"),
                                resultSet.getString("type"),
                                resultSet.getInt("cost")
                        };
                        mainWindow.addRowToTableTickets(rowData);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ticket ID not found");
                    }
                } catch (NumberFormatException | SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error retrieving ticket data");
                }
            }
        });
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sanatoriumIdStr = JOptionPane.showInputDialog(TicketsWindow.this, "Введите ID билета для удаления:");
                if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                    try {
                        int sanatoriumId = Integer.parseInt(sanatoriumIdStr.trim());
                        dbWorker.deleteFromDBById(sanatoriumId, "Ticket");
                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(TicketsWindow.this, "Ошибка при удалении билета: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    protected static void updateBoard() {
        DBWorker dbWorker = new DBWorker();
        try {
            ResultSet resultSet = dbWorker.getAllFromDB("Ticket");

            // Создание модели таблицы и заполнение её данными из ResultSet
            String[] columnNames = {"ID", "Номер", "Город отправки", "Город прибытия", "Время отправки", "Дата", "Тип", "Цена"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                        case 1:
                        case 7:
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
                int number = resultSet.getInt("number");
                String leaving_city = resultSet.getString("leaving_city");
                String arriving_city = resultSet.getString("arriving_city");
                String depature_time = resultSet.getString("depature_time");
                String date = resultSet.getString("date");
                String type = resultSet.getString("type");
                int cost = resultSet.getInt("cost");
                model.addRow(new Object[]{id, number, leaving_city, arriving_city, depature_time, date, type, cost});
            }

            // Создание и настройка сортировщика
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tableTickets.setRowSorter(sorter);
            tableTickets.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTicketDialog() {
        JTextField numberField = new JTextField(10);
        JTextField leavingCityField = new JTextField(10);
        JTextField arrivingCityField = new JTextField(10);
        JTextField depatureTimeField = new JTextField(10);
        JTextField dateField = new JTextField(10);
        JTextField typeField = new JTextField(10);
        JTextField costField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(7, 2));
        panel.add(new JLabel("Номер билета:"));
        panel.add(numberField);
        panel.add(new JLabel("Город отправки:"));
        panel.add(leavingCityField);
        panel.add(new JLabel("Город прибытия:"));
        panel.add(arrivingCityField);
        panel.add(new JLabel("Время отправки:"));
        panel.add(depatureTimeField);
        panel.add(new JLabel("Дата:"));
        panel.add(dateField);
        panel.add(new JLabel("Тип:"));
        panel.add(typeField);
        panel.add(new JLabel("Цена:"));
        panel.add(costField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавление билета", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int number = Integer.parseInt(numberField.getText());
                String leavingCity = leavingCityField.getText();
                String arrivingCity = arrivingCityField.getText();
                String depatureTime = depatureTimeField.getText();
                String date = dateField.getText();
                String type = typeField.getText();
                int cost = Integer.parseInt(costField.getText());

                Object[] rowData = {null, number, leavingCity, arrivingCity, depatureTime, date, type, cost};
                dbWorker.addToDB("Ticket", rowData);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении билета: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        updateBoard();
    }

}
