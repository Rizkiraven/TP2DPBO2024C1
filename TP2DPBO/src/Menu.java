import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.sql.*;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(480, 560);
        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);
        // isi window
        window.setContentPane(window.mainPanel);
        // ubah warna background
        window.getContentPane().setBackground(Color.white);
        // tampilkan window
        window.setVisible(true);
        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    private Database database;

    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JTextField umurField;
    private JComboBox nilaiComboBox;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JLabel umurLabel;
    private JLabel nilaiLabel;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        database = new Database();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        String[] nilaiData = {"", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "E"};
        nilaiComboBox.setModel(new DefaultComboBoxModel(nilaiData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(validateInput())
                {
                    if (selectedIndex == -1) {
                        insertData();
                    } else {
                        updateData();
                    }
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (selectedIndex >= 0)
                {
                    deleteData();
                }
            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedUmur= mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();
                String selectedNilai = mahasiswaTable.getModel().getValueAt(selectedIndex, 5).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                umurField.setText(selectedUmur);
                nilaiComboBox.setSelectedItem(selectedNilai);

                nimField.setEnabled(false);
                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");
                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Umur", "Nilai"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try
        {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            int i = 0;
            while (resultSet.next())
            {
                Object[] row = new Object[6];

                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");
                row[4] = resultSet.getString("umur");
                row[5] = resultSet.getString("nilai");

                temp.addRow(row);
                i++;
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return temp;
    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String umur = umurField.getText();
        String nilai = nilaiComboBox.getSelectedItem().toString();

        if(nimAda(nim))
        {
            JOptionPane.showMessageDialog(null, "Nim sudah terdaftar pada sistem", "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO mahasiswa VALUES (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + umur + "', '" + nilai + "');";
        database.insertUpdateDeleteQuery(sql);

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Insert berhasil!");
        JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
    }

    public void updateData() {
        // ambil data dari form
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String umur = umurField.getText();
        String nilai = nilaiComboBox.getSelectedItem().toString();

        String sql = "UPDATE mahasiswa SET nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', umur = '" + umur + "', nilai = '" + nilai + "' WHERE nim = '" + nim + "'";
        database.insertUpdateDeleteQuery(sql);

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Update Berhasil!");
        JOptionPane.showMessageDialog(null, "Data berhaasil diubah!");
    }

    public void deleteData() {
        String nim = nimField.getText();
        // Tambahkan konfirmasi sebelum penghapusan
        int choice = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // hapus data dari database
            String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Delete berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
        }
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        umurField.setText("");
        nilaiComboBox.setSelectedItem("");

        nimField.setEnabled(true);

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }

    private boolean validateInput()
    {
        if (nimField.getText().isEmpty() || namaField.getText().isEmpty() || jenisKelaminComboBox.getSelectedItem().toString().isEmpty() ||
                umurField.getText().isEmpty() || nilaiComboBox.getSelectedItem().toString().isEmpty())
        {
            JOptionPane.showMessageDialog(null, "Harap lengkapi semua inputan", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean nimAda(String nim)
    {
        try
        {
            ResultSet resultSet = database.selectQuery("SELECT COUNT(*) FROM mahasiswa WHERE nim = '" + nim + "'");
            if(resultSet.next())
            {
                int hitung = resultSet.getInt(1);
                return hitung > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
