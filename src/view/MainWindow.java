package view;

import DB.DBWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private static int totalCost = 0;
    private static JTable tableTickets, tableHotels, tableExcursions, tableSanatorium;
    private JButton btnTickets, btnHotels, btnSanatorium, btnExcursions;
    private JButton btnCreate, btnHelp, btnDelete;
    private static JLabel totalCostLabel;
    private JPanel panel;

    public MainWindow() {
        super("Турагентство");
        setSize(1000, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        init();
    }

    public void init() {
        setLayout(new BorderLayout());

        JLabel jLabel_title = new JLabel("Турагентство", SwingConstants.CENTER);
        add(jLabel_title, BorderLayout.NORTH);

        String[] TicketNames = {"ID", "Номер", "Откуда", "Куда", "Время вылета", "Дата", "Тип", "Цена"};
        DefaultTableModel ticketsModel = new DefaultTableModel(new Object[][]{}, TicketNames);
        tableTickets = new JTable(ticketsModel);

        String[] HotelNames = {"ID", "Название", "Город", "Адрес", "Цена", "Рейтинг"};
        DefaultTableModel hotelsModel = new DefaultTableModel(new Object[][]{}, HotelNames);
        tableHotels = new JTable(hotelsModel);

        String[] ExcursionsNames = {"ID", "Город отправки", "Адрес отправки", "Место", "Время отправки", "Время прибытия", "Цена"};
        DefaultTableModel excursionsModel = new DefaultTableModel(new Object[][]{}, ExcursionsNames);
        tableExcursions = new JTable(excursionsModel);

        String[] SanatoriumNames = {"ID", "Название", "Город", "Адрес", "Цена", "Рейтинг", "Количество дней"};
        DefaultTableModel sanatoriumModel = new DefaultTableModel(new Object[][]{}, SanatoriumNames);
        tableSanatorium = new JTable(sanatoriumModel);

        panel = new JPanel(new GridLayout(4, 1, 15, 15));
        JPanel panelBtn = new JPanel(new GridLayout(4, 1, 15, 15));
        JPanel panelBtnSouth = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        btnTickets = new JButton("Билеты");
        this.btnTickets.setBackground(Color.GRAY);
        this.btnTickets.setForeground(Color.WHITE);

        btnHotels = new JButton("Отели");
        this.btnHotels.setBackground(Color.GRAY);
        this.btnHotels.setForeground(Color.WHITE);

        btnExcursions = new JButton("Экскурсии");
        this.btnExcursions.setBackground(Color.GRAY);
        this.btnExcursions.setForeground(Color.WHITE);

        btnSanatorium = new JButton("Санатории");
        this.btnSanatorium.setBackground(Color.GRAY);
        this.btnSanatorium.setForeground(Color.WHITE);

        panelBtn.add(btnTickets);
        panelBtn.add(btnHotels);
        panelBtn.add(btnExcursions);
        panelBtn.add(btnSanatorium);

        btnCreate = new JButton("Создание путевки");
        this.btnCreate.setBackground(Color.GRAY);
        this.btnCreate.setForeground(Color.WHITE);

        btnHelp = new JButton("История");
        this.btnHelp.setBackground(Color.GRAY);
        this.btnHelp.setForeground(Color.WHITE);

        btnDelete = new JButton("Удалить");
        this.btnDelete.setBackground(Color.GRAY);
        this.btnDelete.setForeground(Color.WHITE);

        panelBtnSouth.add(btnCreate);
        panelBtnSouth.add(btnHelp);
        panelBtnSouth.add(btnDelete);

        panel.add(new JScrollPane(tableTickets));
        panel.add(new JScrollPane(tableHotels));
        panel.add(new JScrollPane(tableExcursions));
        panel.add(new JScrollPane(tableSanatorium));
        tableTickets.setName("Билеты");
        tableHotels.setName("Отели");
        tableExcursions.setName("Экскурсии");
        tableSanatorium.setName("Санатории");

        totalCostLabel = new JLabel("Общая стоимость: 0");
        panelBtnSouth.add(totalCostLabel);

        add(panel, BorderLayout.CENTER);
        add(panelBtnSouth, BorderLayout.SOUTH);
        add(panelBtn, BorderLayout.WEST);

        addListeners();

        setVisible(true);
    }

    private void addListeners() {
        btnTickets.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new TicketsWindow(MainWindow.this);
            }
        });
        btnHotels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HotelsWindow(MainWindow.this);
            }
        });
        btnExcursions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ExcursionsWindow(MainWindow.this);
            }
        });
        btnSanatorium.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SanatoriumsWindow(MainWindow.this);
            }
        });
        btnHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HistoryWindow();
            }
        });

        btnCreate.addActionListener(e -> {
            String FIO = JOptionPane.showInputDialog(this, "Введите ФИО клиента:");
            if (FIO == null) return;
            String passport = JOptionPane.showInputDialog(this, "Введите паспорт клиента:");
            if (passport == null) return;
            else {
                List<JTable> tables = new ArrayList<>();
                tables.add(tableTickets);
                tables.add(tableHotels);
                tables.add(tableExcursions);
                tables.add(tableSanatorium);

                new CreatingWindow(tables, totalCost, FIO, passport);
            }
        });

        btnDelete.addActionListener(e -> {
            JTable selectedTable = getSelectedTable();
            if (selectedTable != null) {
                DefaultTableModel model = (DefaultTableModel) selectedTable.getModel();
                int[] selectedRows = selectedTable.getSelectedRows();
                // Удаляем строки в обратном порядке, чтобы корректно обрабатывать индексы
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    model.removeRow(selectedRows[i]);
                }
                updateTotalCost();
            }
        });
    }

    private JTable getSelectedTable() {
        Component[] components = panel.getComponents();  // Search within the panel containing tables
        for (Component component : components) {
            if (component instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) component;
                JViewport viewport = scrollPane.getViewport();
                if (viewport.getView() instanceof JTable) {
                    JTable table = (JTable) viewport.getView();
                    if (table.getSelectedRowCount() > 0) {
                        return table;
                    }
                }
            }
        }
        return null;
    }

    public static void addRowToTableTickets(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) tableTickets.getModel();
        model.addRow(rowData);
        updateTotalCost();
    }

    public static void addRowToTableHotels(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) tableHotels.getModel();
        model.addRow(rowData);
        updateTotalCost();
    }

    public static void addRowToTableExcursions(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) tableExcursions.getModel();
        model.addRow(rowData);
        updateTotalCost();
    }

    public static void addRowToTableSanatoriums(Object[] rowData) {
        DefaultTableModel model = (DefaultTableModel) tableSanatorium.getModel();
        model.addRow(rowData);
        updateTotalCost();
    }

    private static void updateTotalCost() {
        totalCost = 0;
        totalCost += calculateTableCost(tableTickets, 7);  // столбец цены для билетов
        totalCost += calculateTableCost(tableHotels, 4);   // столбец цены для отелей
        totalCost += calculateTableCost(tableExcursions, 6); // столбец цены для экскурсий
        totalCost += calculateTableCost(tableSanatorium, 4); // столбец цены для санаториев

        totalCostLabel.setText("Общая стоимость: " + totalCost);
    }

    private static int calculateTableCost(JTable table, int priceColumnIndex) {
        int total = 0;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object value = model.getValueAt(i, priceColumnIndex);
            if (value instanceof Number) {
                total += ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    total += Integer.parseInt((String) value);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return total;
    }
}
