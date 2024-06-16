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

public class HotelsWindow extends JFrame {

    private static JTable tableHotels;
    private JButton btnBack, btnAddHotel, btnAddTo, btnDel;
    private DBWorker dbWorker;
    private MainWindow mainWindow;

    public HotelsWindow(MainWindow mainWindow) {
        super("Отели");
        setSize(800, 400);
        this.mainWindow = mainWindow;
        dbWorker = new DBWorker();
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
        updateBoard();
    }

    public void init() {

        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("Отели", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] TicketNames = {"id", "name", "city", "adress", "cost_per_day", "rating"};
        Object[][] TicketsData = {
                {"", "", "", "", "", ""},
                {"", "", "", "", "", ""}

        };
        tableHotels = new JTable(TicketsData, TicketNames);
        JScrollPane scrollPane = new JScrollPane(tableHotels);
        add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnBack = new JButton("Назад");
        this.btnBack.setBackground(Color.GRAY);
        this.btnBack.setForeground(Color.WHITE);
        buttonPanel.add(btnBack);

        btnAddHotel = new JButton("Добавить отель");
        this.btnAddHotel.setBackground(Color.GRAY);
        this.btnAddHotel.setForeground(Color.WHITE);
        buttonPanel.add(btnAddHotel);

        btnAddTo = new JButton("Добавить в путевку");
        this.btnAddTo.setBackground(Color.GRAY);
        this.btnAddTo.setForeground(Color.WHITE);
        buttonPanel.add(btnAddTo);

        btnDel = new JButton("Удалить отель");
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
        btnAddHotel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTicketDialog();
            }
        });
        btnAddTo.addActionListener(e -> {
            String hotelIdStr = JOptionPane.showInputDialog(this, "Введите ID отеля:");
            int daysCount = Integer.parseInt(JOptionPane.showInputDialog(this, "Введите количество дней для проживания:"));
            if (hotelIdStr != null && !hotelIdStr.trim().isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(hotelIdStr.trim());
                    ResultSet resultSet = dbWorker.getFromDBById(ticketId, "Hotel");
                    if (resultSet.next()) {
                        Object[] rowData = {
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getString("city"),
                                resultSet.getString("adress"),
                                resultSet.getInt("cost_per_day") * daysCount,
                                resultSet.getInt("rating")
                        };
                        mainWindow.addRowToTableHotels(rowData);
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
                String sanatoriumIdStr = JOptionPane.showInputDialog(HotelsWindow.this, "Введите ID отеля для удаления:");
                if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                    try {
                        int sanatoriumId = Integer.parseInt(sanatoriumIdStr.trim());
                        dbWorker.deleteFromDBById(sanatoriumId, "Hotel");
                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(HotelsWindow.this, "Ошибка при удалении отеля: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    protected void updateBoard() {
        DBWorker dbWorker = new DBWorker();
        try {
            ResultSet resultSet = dbWorker.getAllFromDB("Hotel");

            // Создание модели таблицы и заполнение её данными из ResultSet
            String[] columnNames = {"ID", "Название", "Город", "Адрес", "Цена за сутки", "Рейтинг"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
                        case 4:
                        case 5:
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
                int cost_per_day = resultSet.getInt("cost_per_day");
                int rating = resultSet.getInt("rating");
                model.addRow(new Object[]{id, name, city, adress, cost_per_day, rating});
            }

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tableHotels.setRowSorter(sorter);
            tableHotels.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addTicketDialog() {
        JTextField nameField = new JTextField(10);
        JTextField cityField = new JTextField(10);
        JTextField addressField = new JTextField(10);
        JTextField costPerDayField = new JTextField(10);
        JTextField ratingField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Название отеля:"));
        panel.add(nameField);
        panel.add(new JLabel("Город:"));
        panel.add(cityField);
        panel.add(new JLabel("Адрес:"));
        panel.add(addressField);
        panel.add(new JLabel("Цена за сутки:"));
        panel.add(costPerDayField);
        panel.add(new JLabel("Рейтинг:"));
        panel.add(ratingField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавление отеля", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String city = cityField.getText();
                String address = addressField.getText();
                int costPerDay = Integer.parseInt(costPerDayField.getText());
                int rating = Integer.parseInt(ratingField.getText());

                Object[] rowData = {null, name, city, address, costPerDay, rating};
                dbWorker.addToDB("Hotel", rowData);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении отеля: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        updateBoard();
    }


}
