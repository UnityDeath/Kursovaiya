package view;

import DB.DBWorker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CreatingWindow extends JFrame {
    private JButton saveButton;
    private int totalCost = 0;
    private List<JTable> tables;
    private StringBuilder packageDescription;
    private String timeStamp;
    private String FIO;
    private String passport;
    private String IDs;

    public CreatingWindow(List<JTable> tables, int totalCost, String FIO, String passport) {
        this.tables = tables;
        this.FIO = FIO;
        this.passport = passport;
        this.totalCost = totalCost;
        setTitle("Creating Package");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        timeStamp = dateFormat.format(new Date());

        packageDescription = new StringBuilder("Путевка " + timeStamp + "     Клиент: " + FIO + "     Паспорт клиента: " + passport + "\n\nПакет услуг: билет, отель, экскурсии, санаторий\n\n");

        for (JTable table : tables) {
            String tableName = getTableName(table);
            packageDescription.append(tableName).append(":\n");
            packageDescription.append(getTableData(table)).append("\n");
        }

        packageDescription.append("Общая стоимость: ").append(totalCost).append("\n");

        textArea.setText(packageDescription.toString());

        saveButton = new JButton("Сохранить путевку");
        this.saveButton.setBackground(Color.GRAY);
        this.saveButton.setForeground(Color.WHITE);

        JPanel panelBtnSouth = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelBtnSouth.add(saveButton);

        addListeners();

        add(panelBtnSouth, BorderLayout.SOUTH);
        setVisible(true);

        // Extract IDs and set IDs variable
        this.IDs = extractIDs(tables);
    }

    private void addListeners() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePackage();
            }
        });
    }

    private String getTableName(JTable table) {
        String tableName = table.getName();
        if (tableName == null) {
            tableName = "Unknown Table";
        }
        return tableName;
    }

    private String getTableData(JTable table) {
        StringBuilder tableData = new StringBuilder();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                tableData.append(model.getColumnName(j)).append(": ");
                tableData.append(model.getValueAt(i, j)).append(" ");
            }
            tableData.append("\n");
        }
        return tableData.toString();
    }

    private String extractIDs(List<JTable> tables) {
        StringBuilder idsBuilder = new StringBuilder();

        for (JTable table : tables) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int rowCount = model.getRowCount();
            if (rowCount == 0) {
                idsBuilder.append("0 ");
            } else {
                for (int i = 0; i < rowCount; i++) {
                    idsBuilder.append(model.getValueAt(i, 0)).append(",");
                }
                idsBuilder.setLength(idsBuilder.length() - 1);  // Remove the trailing comma
                idsBuilder.append(" ");
            }
        }

        idsBuilder.setLength(idsBuilder.length() - 1);  // Remove the trailing space
        return idsBuilder.toString();
    }

    private void savePackage() {
        // Создаем строку для добавления в таблицу History
        Object[] historyValues = {null, FIO, passport, timeStamp, IDs};

        // Вызываем метод addToDB() для добавления данных в таблицу History
        DBWorker dbWorker = new DBWorker();
        dbWorker.addToDB("History", historyValues);

        // Отображаем диалоговое окно для выбора папки сохранения путевки
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = fileChooser.getSelectedFile();
            String fileName = generateFileName(timeStamp);
            File outputFile = new File(selectedFolder, fileName);
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(packageDescription.toString());
                JOptionPane.showMessageDialog(this, "Путевка успешно сохранена");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при сохранении путевки");
            }
        }
    }

    private String generateFileName(String timeStamp) {
        return "Package_" + timeStamp + ".txt";
    }
}
