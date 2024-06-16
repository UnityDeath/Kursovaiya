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

public class ExcursionsWindow extends JFrame {

    private static JTable tableExcursions;
    private JButton btnBack, btnAddExcursion, btnAddTo, btnDel;
    private DBWorker dbWorker;
    private MainWindow mainWindow;

    public ExcursionsWindow(MainWindow mainWindow) {
        super("Экскурсии");
        setSize(800, 400);
        this.mainWindow = mainWindow;
        dbWorker = new DBWorker();
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
        updateBoard();
    }

    public void init() {

        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("Экскурсии", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] TicketNames = {"id", "leaving_city", "leaving_addres", "arriving_place", "depature_time", "arriving_time", "cost"};
        Object[][] TicketsData = {
                {"", "", "", "", "", "", ""},
                {"", "", "", "", "", "", ""}

        };
        tableExcursions = new JTable(TicketsData, TicketNames);
        JScrollPane scrollPane = new JScrollPane(tableExcursions);
        add(scrollPane, BorderLayout.CENTER);

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnBack = new JButton("Назад");
        this.btnBack.setBackground(Color.GRAY);
        this.btnBack.setForeground(Color.WHITE);
        buttonPanel.add(btnBack);

        btnAddExcursion = new JButton("Добавить экскурсию");
        this.btnAddExcursion.setBackground(Color.GRAY);
        this.btnAddExcursion.setForeground(Color.WHITE);
        buttonPanel.add(btnAddExcursion);

        btnAddTo = new JButton("Добавить в путевку");
        this.btnAddTo.setBackground(Color.GRAY);
        this.btnAddTo.setForeground(Color.WHITE);
        buttonPanel.add(btnAddTo);

        btnDel = new JButton("Удалить экскурсию");
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
        btnAddExcursion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTicketDialog();
            }
        });
        btnAddTo.addActionListener(e -> {
            String excursionIdStr = JOptionPane.showInputDialog(this, "Введите ID экскурсии:");
            if (excursionIdStr != null && !excursionIdStr.trim().isEmpty()) {
                try {
                    int ticketId = Integer.parseInt(excursionIdStr.trim());
                    ResultSet resultSet = dbWorker.getFromDBById(ticketId, "Excursions");
                    if (resultSet.next()) {
                        Object[] rowData = {
                                resultSet.getInt("id"),
                                resultSet.getString("leaving_city"),
                                resultSet.getString("leaving_addres"),
                                resultSet.getString("arriving_place"),
                                resultSet.getString("depature_time"),
                                resultSet.getString("arriving_time"),
                                resultSet.getInt("cost")
                        };
                        mainWindow.addRowToTableExcursions(rowData);
                    } else {
                        JOptionPane.showMessageDialog(this, "Excursion ID not found");
                    }
                } catch (NumberFormatException | SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error retrieving excursion data");
                }
            }
        });
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sanatoriumIdStr = JOptionPane.showInputDialog(ExcursionsWindow.this, "Введите ID экскурсии для удаления:");
                if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                    try {
                        int sanatoriumId = Integer.parseInt(sanatoriumIdStr.trim());
                        dbWorker.deleteFromDBById(sanatoriumId, "Excursions");
                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(ExcursionsWindow.this, "Ошибка при удалении экскурсии: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    protected static void updateBoard() {
        DBWorker dbWorker = new DBWorker();
        try {
            ResultSet resultSet = dbWorker.getAllFromDB("Excursions");

            // Создание модели таблицы и заполнение её данными из ResultSet
            String[] columnNames = {"ID", "Город отправки", "Адрес отправки", "Место посещения", "Время отправки", "Время прибытия", "Стоимость"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 0:
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
                String leaving_city = resultSet.getString("leaving_city");
                String leaving_addres = resultSet.getString("leaving_addres");
                String arriving_place = resultSet.getString("arriving_place");
                String depature_time = resultSet.getString("depature_time");
                String arriving_time = resultSet.getString("arriving_time");
                int cost = resultSet.getInt("cost");
                model.addRow(new Object[]{id, leaving_city, leaving_addres, arriving_place, depature_time, arriving_time, cost});
            }

            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tableExcursions.setRowSorter(sorter);
            tableExcursions.setModel(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addTicketDialog() {
        JTextField leavingCityField = new JTextField(10);
        JTextField leavingAddressField = new JTextField(10);
        JTextField arrivingPlaceField = new JTextField(10);
        JTextField depatureTimeField = new JTextField(10);
        JTextField arrivingTimeField = new JTextField(10);
        JTextField costField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Город отправки:"));
        panel.add(leavingCityField);
        panel.add(new JLabel("Адрес отправки:"));
        panel.add(leavingAddressField);
        panel.add(new JLabel("Место посещения:"));
        panel.add(arrivingPlaceField);
        panel.add(new JLabel("Время отправки:"));
        panel.add(depatureTimeField);
        panel.add(new JLabel("Время прибытия:"));
        panel.add(arrivingTimeField);
        panel.add(new JLabel("Стоимость:"));
        panel.add(costField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Добавление экскурсии", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String leavingCity = leavingCityField.getText();
                String leavingAddress = leavingAddressField.getText();
                String arrivingPlace = arrivingPlaceField.getText();
                String depatureTime = depatureTimeField.getText();
                String arrivingTime = arrivingTimeField.getText();
                int cost = Integer.parseInt(costField.getText());

                Object[] rowData = {null, leavingCity, leavingAddress, arrivingPlace, depatureTime, arrivingTime, cost};
                dbWorker.addToDB("Excursions", rowData);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении экскурсии: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        updateBoard();
    }

}
