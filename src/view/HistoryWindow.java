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

public class HistoryWindow extends JFrame {

    private JButton btnBack, btnDel, btnRed;
    private static JTable tableHistory;
    private DBWorker dbWorker;

    public HistoryWindow() {
        super("История");
        dbWorker = new DBWorker();
        setSize(800, 400);
        this.setLocationRelativeTo(null); // Окно по центру экрана
        init();
        updateBoard();
    }

    private void init() {
        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("История", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] columnNames = {"ID", "ФИО", "Паспорт", "Дата и время создания"};
        Object[][] historyData = {};
        tableHistory = new JTable(historyData, columnNames);
        JScrollPane scrollPane = new JScrollPane(tableHistory);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnBack = new JButton("Назад");
        this.btnBack.setBackground(Color.GRAY);
        this.btnBack.setForeground(Color.WHITE);
        buttonPanel.add(btnBack);

        btnDel = new JButton("Удалить");
        this.btnDel.setBackground(Color.GRAY);
        this.btnDel.setForeground(Color.WHITE);
        buttonPanel.add(btnDel);

        btnRed = new JButton("Редактировать");
        this.btnRed.setBackground(Color.GRAY);
        this.btnRed.setForeground(Color.WHITE);
        buttonPanel.add(btnRed);

        add(buttonPanel, BorderLayout.SOUTH);
        addListeners();

        setVisible(true);
    }

    protected static void updateBoard() {
        DBWorker dbWorker = new DBWorker();
        try {
            ResultSet resultSet = dbWorker.getAllFromDB("History");

            // Создание модели таблицы и заполнение её данными из ResultSet
            String[] columnNames = {"ID", "ФИО", "Паспорт", "Дата и время создания"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Integer.class : String.class;
                }
            };

            // Проверка наличия данных в ResultSet
            if (!resultSet.isBeforeFirst()) {
                System.out.println("No data returned from the query.");
            }

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String FIO = resultSet.getString("fio");
                String passport = resultSet.getString("passport");
                String timeStamp = resultSet.getString("timeStamp");
                model.addRow(new Object[]{id, FIO, passport, timeStamp});
            }

            // Создание и настройка сортировщика
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
            tableHistory.setRowSorter(sorter);
            tableHistory.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addListeners() {
        btnBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Закрываем окно при нажатии кнопки
            }
        });
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sanatoriumIdStr = JOptionPane.showInputDialog(HistoryWindow.this, "Введите ID события для удаления:");
                if (sanatoriumIdStr != null && !sanatoriumIdStr.trim().isEmpty()) {
                    try {
                        int sanatoriumId = Integer.parseInt(sanatoriumIdStr.trim());
                        dbWorker.deleteFromDBById(sanatoriumId, "History");
                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(HistoryWindow.this, "Ошибка при удалении события: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        btnRed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String IdStr = JOptionPane.showInputDialog(HistoryWindow.this, "Введите ID события для редактирования:");
                if (IdStr != null && !IdStr.trim().isEmpty()) {
                    try {
                        int historyId = Integer.parseInt(IdStr.trim());
                        String idInTables = DBWorker.getIdInTablesById(historyId);

                        if (idInTables != null) {
                            // Вызываем метод для работы с полученным idInTables
                            DBWorker.processIdInTables(idInTables);
                        } else {
                            JOptionPane.showMessageDialog(HistoryWindow.this, "История с таким ID не найдена", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }

                        updateBoard();
                    } catch (NumberFormatException | SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(HistoryWindow.this, "Ошибка при редактировании события: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

    }
}
