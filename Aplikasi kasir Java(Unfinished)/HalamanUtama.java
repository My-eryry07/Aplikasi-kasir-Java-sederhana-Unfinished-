/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package pbo2_2024;

import java.awt.BorderLayout;
import java.awt.print.PrinterException;
import java.sql.Connection; 
import java.beans.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;


/**
 *
 * @author User
 */
public class HalamanUtama extends javax.swing.JFrame {
private int jumlah = 0;
private Connection con;
private PreparedStatement stat;
private ResultSet rs;   
koneksi dbKoneksi = new koneksi();
 

    /**
     * Creates new form HalamanUtama
     */
    public HalamanUtama() {
         dbKoneksi.connect();
        initComponents();
    }
private void cetakResi(double subtotal, double uangDibayar, double kembalian) {
   
    resiArea.setEditable(false); 
    
   
    resiArea.setText(""); 

    
    resiArea.append("===== Resi Pembayaran =====\n");
    resiArea.append("ID Pelanggan: " + txtIdcustomer.getText() + "\n");
    resiArea.append("Nama Pelanggan: " + txtNamacustomer.getText() + "\n");
    resiArea.append("Nomor Meja: " + Pilihmeja.getSelectedItem().toString() + "\n\n");
    DefaultTableModel model = (DefaultTableModel) TableMenu.getModel();
    resiArea.append("Pesanan:\n");
    for (int i = 0; i < model.getRowCount(); i++) {
        String namaMenu = model.getValueAt(i, 3).toString();
        int qty = Integer.parseInt(model.getValueAt(i, 5).toString());
        double harga = Double.parseDouble(model.getValueAt(i, 4).toString());
        double totalHarga = harga * qty;
        resiArea.append(namaMenu + " x" + qty + " - Rp" + harga + " = Rp" + totalHarga + "\n");
    }
    resiArea.append("\nSubtotal: Rp" + subtotal + "\n");
    resiArea.append("Dibayar: Rp" + uangDibayar + "\n");
    resiArea.append("Kembalian: Rp" + kembalian + "\n");
    resiArea.append("===========================\n");
    resiArea.append("Terima kasih telah berkunjung!");
    
}

private void hitungTotalHarga() {
    double totalHarga = 0;
    DefaultTableModel model = (DefaultTableModel) TableMenu.getModel();

    
    for (int i = 0; i < model.getRowCount(); i++) {
        double harga = (double) model.getValueAt(i, 6); 
        totalHarga += harga;
    }
    
    
    txtSubtotal.setText(String.valueOf(totalHarga));
}

 private void LoadMenu() {
    Connection con = dbKoneksi.connect();
    String query = "SELECT nama_menu FROM menus";
    try {
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (Pilihmenu == null) {
            System.out.println("PilihMenu adalah null!");
        } else {
            while (rs.next()) {
                Pilihmenu.addItem(rs.getString("nama_menu"));
            }
        }
    } catch (Exception e) {
        System.out.println("Gagal mengambil data menu: " + e.getMessage());
    }
}

  private void kurangiStok(String namaMenu, int qty) {
 con = dbKoneksi.connect(); 
    String query = "UPDATE menus SET stok = stok - ? WHERE nama_menu = ?";
    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, qty);
        ps.setString(2, namaMenu);
        ps.executeUpdate();
    } catch (Exception e) {
        System.out.println("Gagal mengurangi stok: " + e.getMessage());
    }
}

  
    private int getIdMenuByName(String namaMenu) {
        con = dbKoneksi.connect(); 
    String query = "SELECT id_menu FROM menus WHERE nama_menu = ?";
    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, namaMenu);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_menu");
        }
    } catch (Exception e) {
        System.out.println("Gagal mendapatkan ID menu: " + e.getMessage());
    }
    return -1;
}

        


        
private void prosesTransaksi() {
    con = dbKoneksi.connect();
    double subtotal = 0;
    double uangDibayar;
    double kembalian;

    try {
        DefaultTableModel model = (DefaultTableModel) TableMenu.getModel();

       
        for (int i = 0; i < model.getRowCount(); i++) {
            String randomId = model.getValueAt(i, 0) != null ? model.getValueAt(i, 0).toString() : "0";  
            String namaMenu = model.getValueAt(i, 3) != null ? model.getValueAt(i, 3).toString() : "";
            int qty = model.getValueAt(i, 5) != null ? Integer.parseInt(model.getValueAt(i, 5).toString()) : 0;
            double harga = model.getValueAt(i, 4) != null ? Double.parseDouble(model.getValueAt(i, 4).toString()) : 0.0;
            double totalHarga = harga * qty;
            subtotal += totalHarga;

            
            if (!cekStok(namaMenu, qty)) {
                JOptionPane.showMessageDialog(this, "Stok untuk " + namaMenu + " tidak mencukupi.");
                return;
            }

           
            kurangiStok(namaMenu, qty);

            String query = "INSERT INTO transactions (id_customer, id_menu, qty, total_harga, tanggal) VALUES (?, ?, ?, ?, CURDATE())";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, randomId); 
            ps.setInt(2, getIdMenuByName(namaMenu));
            ps.setInt(3, qty); 
            ps.setDouble(4, totalHarga);
            ps.executeUpdate();
        }

      
        txtSubtotal.setText(String.valueOf(subtotal));

    
        try {
            uangDibayar = Double.parseDouble(txtBayar.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah yang dibayarkan tidak valid!");
            return;
        }

        
        kembalian = uangDibayar - subtotal;

        
        if (kembalian < 0) {
            JOptionPane.showMessageDialog(this, "Uang yang dibayarkan tidak cukup!");
            return;
        }

       
        jLabelKembalian.setText(String.valueOf(kembalian));

        
        JOptionPane.showMessageDialog(this, "Pembayaran berhasil! Total: " + subtotal + ", Kembalian: " + kembalian);

       
        cetakResi(subtotal, uangDibayar, kembalian);

    } catch (Exception e) {
        System.out.println("Gagal memproses transaksi: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close(); 
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}





private boolean cekStok(String namaMenu, int qty) {
    con = dbKoneksi.connect(); 
    String query = "SELECT stok FROM menus WHERE nama_menu = ?";
    try (PreparedStatement ps = con.prepareStatement(query)) {
        ps.setString(1, namaMenu);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int stok = rs.getInt("stok");
                return stok >= qty; 
            }
        }
    } catch (Exception e) {
        System.out.println("Gagal memeriksa stok: " + e.getMessage());
    } finally {
       
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
    return false;
}

  
    private double getHargaFromMenu(String namaMenu) {
    con = dbKoneksi.connect(); 
    double harga = 0.0;

    String query = "SELECT harga FROM menus WHERE nama_menu = ?";
    try (PreparedStatement ps = con.prepareStatement(query)) {
        ps.setString(1, namaMenu); 
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            harga = rs.getDouble("harga"); 
        } else {
            System.out.println("Menu tidak ditemukan: " + namaMenu);
        }
    } catch (SQLException e) {
        System.out.println("Gagal mengambil harga dari database: " + e.getMessage());
    } finally {
      
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
    
    return harga; 
}


  private void tambahKeTabelPesanan() {
        String randomId = generateRandomId();
    txtIdcustomer.setText(randomId);
        String namaCustomer = txtNamacustomer.getText().trim();
        String meja = Pilihmeja.getSelectedItem().toString();
        String namaMenu = Pilihmenu.getSelectedItem().toString();
        
        
        if (txtQty.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah pesanan!");
            return;
        }
        
        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!");
            return;
        }

        
        if (!cekStok(namaMenu, qty)) {
            JOptionPane.showMessageDialog(this, "Stok tidak mencukupi untuk menu ini.");
            return;
        }

     
        double harga = getHargaFromMenu(namaMenu);
        double totalHarga = harga * qty;

      
        if ( namaCustomer.isEmpty() || meja.isEmpty() || namaMenu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pastikan semua data sudah diisi dengan benar.");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) TableMenu.getModel();
        model.addRow(new Object[]{randomId, namaCustomer, meja, namaMenu, harga, qty, totalHarga});
        System.out.println("Menambahkan ke tabel: " + randomId + ", " + namaCustomer + ", " + meja + ", " + namaMenu + ", " + harga + ", " + qty + ", " + totalHarga);
        
        
        kurangiStok(namaMenu, qty);

        
        hitungTotalHarga(); 
    }


private String generateRandomId() {
    
    Random random = new Random();
    int idNumber = random.nextInt(10000);
    return "CUST-" + String.format("%04d", idNumber);
}

private void tampilkanLaporanTransaksi() {
    Connection con = dbKoneksi.connect();  
    String query = "SELECT id_transaction, id_customer, tanggal, total_harga FROM transactions";
    
    try {
        PreparedStatement ps = con.prepareStatement(query);  
        ResultSet rs = ps.executeQuery();  

        // Membuat model tabel untuk menyusun kolom
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID Transaksi");
        model.addColumn("ID Customer");
        model.addColumn("Tanggal");
        model.addColumn("Total Harga");

       
        boolean dataFound = false;   
        while (rs.next()) {
            Object[] row = new Object[4];
            row[0] = rs.getString("id_transaction");  
            row[1] = rs.getString("id_customer"); 
            row[2] = rs.getString("tanggal");  
            row[3] = rs.getDouble("total_harga");  

            model.addRow(row);  
            dataFound = true;  
        }

       
        if (!dataFound) {
            JOptionPane.showMessageDialog(this, "Tidak ada transaksi yang ditemukan.");
        }

        
        TableLaporan.setModel(model);  

     
        JScrollPane scrollPane = new JScrollPane(TableLaporan);

         
        jpLaporanPenjualan.removeAll();  
        jpLaporanPenjualan.setLayout(new BorderLayout()); 
        jpLaporanPenjualan.add(scrollPane, BorderLayout.CENTER);  
        jpLaporanPenjualan.revalidate(); 
        jpLaporanPenjualan.repaint();    

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menampilkan laporan transaksi: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();  
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}


private void TampilDanEditStokMenu() {
    Connection con = dbKoneksi.connect();  
    String query = "SELECT id_menu, nama_menu, harga, stok FROM menus";

    try {
        PreparedStatement ps = con.prepareStatement(query);  
        ResultSet rs = ps.executeQuery(); 

        // Membuat model tabel untuk menyusun kolom
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID Menu");
        model.addColumn("Nama Menu");
        model.addColumn("Harga");
        model.addColumn("Stok");

      
        boolean dataFound = false;
        while (rs.next()) {
            Object[] row = new Object[4];
            row[0] = rs.getString("id_menu");
            row[1] = rs.getString("nama_menu");
            row[2] = rs.getString("harga");
            row[3] = rs.getObject("stok");  

            model.addRow(row); 
            dataFound = true;
        }

        // Cek jika data ditemukan, jika tidak beri peringatan
        if (!dataFound) {
            JOptionPane.showMessageDialog(this, "Tidak ada menu yang ditemukan.");
        }

        TableStok.setModel(model); // Mengatur model tabel baru untuk TableStok

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menampilkan stok menu: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close(); // Menutup koneksi database
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}


//TOMBOL INSERT UPDATE DELETE DAN SEACRH START
private void insertData() {
    Connection con = dbKoneksi.connect();
    String query = "INSERT INTO menus (id_menu, nama_menu, harga, stok) VALUES (?, ?, ?, ?)";

    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, txtStokidmenu.getText());
        ps.setString(2, txtStoknamamenu.getText());
        ps.setDouble(3, Double.parseDouble(txtStokhargamenu.getText()));
        ps.setInt(4, Integer.parseInt(txtStokMenu.getText()));

        ps.executeUpdate();
        JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan.");
        TampilDanEditStokMenu(); 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menambahkan data: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}

private void updateData() {
    Connection con = dbKoneksi.connect();
    String query = "UPDATE menus SET nama_menu = ?, harga = ?, stok = ? WHERE id_menu = ?";

    try {
        PreparedStatement ps = con.prepareStatement(query);
       ps.setString(1, txtStoknamamenu.getText());
        ps.setDouble(2, Double.parseDouble(txtStokhargamenu.getText()));
        ps.setInt(3, Integer.parseInt(txtStokMenu.getText()));
        ps.setString(4, txtStokidmenu.getText());

        ps.executeUpdate();
        JOptionPane.showMessageDialog(this, "Data berhasil diperbarui.");
        TampilDanEditStokMenu(); // Refresh tabel
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memperbarui data: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
    
}
private void deleteData() {
    Connection con = dbKoneksi.connect();
    String query = "DELETE FROM menus WHERE id_menu = ?";

    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, txtStokidmenu.getText());

        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
            TampilDanEditStokMenu(); 
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}

private void searchData() {
    Connection con = dbKoneksi.connect();
    String query = "SELECT * FROM menus WHERE id_menu = ?";

    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, txtStokidmenu.getText());
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            txtStoknamamenu.setText(rs.getString("nama_menu"));
            txtStokhargamenu.setText(rs.getString("harga"));
            txtStokMenu.setText(rs.getString("stok"));
            JOptionPane.showMessageDialog(this, "Data ditemukan.");
        } else {
            JOptionPane.showMessageDialog(this, "Data tidak ditemukan.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mencari data: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
private void clearFields() {
    txtStokidmenu.setText("");    
    txtStoknamamenu.setText("");    
    txtStokhargamenu.setText("");       
    txtStokMenu.setText("");          
}

//TOMBOL INSERT UPDATE DELETE DAN SEACRH END



//CETAK LAPORAN  HARIAN START
    private void tampilkanPendapatanHarianLengkap(String tanggal) {
    Connection con = dbKoneksi.connect();
    String query = "SELECT id_menu, total_harga, SUM(qty) AS total_terjual, SUM(total_harga * qty) AS total_pendapatan " +
                   "FROM transactions " +
                   "WHERE DATE(tanggal) = ? " +
                   "GROUP BY id_menu, total_harga";

    try {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, tanggal); 
        
        ResultSet rs = ps.executeQuery();

         
        StringBuilder laporan = new StringBuilder();
        laporan.append("Laporan Pendapatan Harian\n");
        laporan.append("Tanggal: ").append(tanggal).append("\n\n");
        laporan.append(String.format("%-15s %-12s %-10s %-15s\n", "Nama Menu", "Harga", "Terjual", "Pendapatan"));
        laporan.append("-------------------------------------------------------------\n");

        double totalPendapatanHarian = 0;
        boolean dataFound = false;

        while (rs.next()) {
            String namaMenu = rs.getString("id_menu");
            double harga = rs.getDouble("total_harga");
            int totalTerjual = rs.getInt("total_terjual");
            double totalPendapatan = rs.getDouble("total_pendapatan");

             
            laporan.append(String.format("%-15s %-12.2f %-10d %-15.2f\n", namaMenu, harga, totalTerjual, totalPendapatan));
            
            totalPendapatanHarian += totalPendapatan;
            dataFound = true;
        }

         
        laporan.append("-------------------------------------------------------------\n");
        laporan.append(String.format("%-15s %-12s %-10s %-15.2f\n", "Total", "", "", totalPendapatanHarian));

        if (!dataFound) {
            JOptionPane.showMessageDialog(this, "Tidak ada data pendapatan untuk tanggal ini.");
            return;
        }

        laporanArea.setText(laporan.toString()); // Menampilkan laporan pada JTextArea bernama laporanArea
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menampilkan pendapatan harian: " + e.getMessage());
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("Gagal menutup koneksi: " + e.getMessage());
        }
    }
}


//CETAK LAPORAN  HARIAN END
    
    
    private void cetakLaporanPendapatanHarian() {
    try {
        boolean printed = laporanArea.print(); 
        if (printed) {
            JOptionPane.showMessageDialog(this, "Laporan berhasil dicetak.");
        } else {
            JOptionPane.showMessageDialog(this, "Pencetakan laporan dibatalkan.");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + e.getMessage());
    }
}
    private void logout() {
    int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?", "Logout", JOptionPane.YES_NO_OPTION);
    if (confirm == JOptionPane.YES_OPTION) {
        
        this.dispose();
        
       
        HalamanLogin halamanLogin = new HalamanLogin();
        halamanLogin.setVisible(true);
    }
}
    /**
     * This method is called from ++++++within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        frameUtama = new javax.swing.JPanel();
        jpMenu = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jpIsi = new javax.swing.JPanel();
        menuMakanan = new javax.swing.JPanel();
        txtIdcustomer = new javax.swing.JTextField();
        txtNamacustomer = new javax.swing.JTextField();
        Pilihmeja = new javax.swing.JComboBox<>();
        txtQty = new javax.swing.JTextField();
        btnTmbhutama = new javax.swing.JButton();
        txtBayar = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        Pilihmenu = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TableMenu = new javax.swing.JTable();
        txtSubtotal = new javax.swing.JTextField();
        jLabelKembalian = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        btnCetakresi = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        resiArea = new javax.swing.JTextArea();
        jpPenjualan = new javax.swing.JPanel();
        jpLaporanPenjualan = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableLaporan = new javax.swing.JTable();
        jpStok = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TableStok = new javax.swing.JTable();
        btnInsert = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnstokresert = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        txtStokidmenu = new javax.swing.JTextField();
        txtStoknamamenu = new javax.swing.JTextField();
        txtStokhargamenu = new javax.swing.JTextField();
        txtStokMenu = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnSearch = new javax.swing.JButton();
        jpLaporan = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        laporanArea = new javax.swing.JTextArea();
        txtPilihTanggal = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        btnSearchTanggal = new javax.swing.JButton();
        btnCetakLaporan = new javax.swing.JButton();
        jpSetting = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();

        jLabel6.setText("jLabel6");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(456, 492));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        frameUtama.setBackground(new java.awt.Color(111, 78, 55));
        frameUtama.setMaximumSize(new java.awt.Dimension(800, 800));

        jpMenu.setBackground(new java.awt.Color(241, 229, 209));
        jpMenu.setMaximumSize(new java.awt.Dimension(438, 93));

        jButton1.setBackground(new java.awt.Color(111, 78, 55));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UTS/coffee-machine.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(111, 78, 55));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UTS/profile.png"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(111, 78, 55));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UTS/shop.png"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(111, 78, 55));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UTS/economy (1).png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(111, 78, 55));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UTS/gear (1).png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpMenuLayout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(jpMenuLayout);
        jpMenuLayout.setHorizontalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jpMenuLayout.setVerticalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpIsi.setBackground(new java.awt.Color(241, 229, 209));

        menuMakanan.setBackground(new java.awt.Color(241, 229, 209));
        menuMakanan.setForeground(new java.awt.Color(111, 78, 55));
        menuMakanan.setPreferredSize(new java.awt.Dimension(456, 393));

        txtIdcustomer.setEditable(false);
        txtIdcustomer.setBackground(new java.awt.Color(111, 78, 55));
        txtIdcustomer.setFont(new java.awt.Font("Poppins Medium", 1, 12)); // NOI18N
        txtIdcustomer.setForeground(new java.awt.Color(241, 229, 209));
        txtIdcustomer.setBorder(null);
        txtIdcustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdcustomerActionPerformed(evt);
            }
        });

        txtNamacustomer.setBackground(new java.awt.Color(111, 78, 55));
        txtNamacustomer.setForeground(new java.awt.Color(241, 229, 209));
        txtNamacustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNamacustomerActionPerformed(evt);
            }
        });

        Pilihmeja.setBackground(new java.awt.Color(111, 78, 55));
        Pilihmeja.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        Pilihmeja.setForeground(new java.awt.Color(241, 229, 209));
        Pilihmeja.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "===Pilih Meja===", "1", "2", "3", "4", " " }));

        txtQty.setBackground(new java.awt.Color(111, 78, 55));
        txtQty.setForeground(new java.awt.Color(241, 229, 209));
        txtQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQtyActionPerformed(evt);
            }
        });

        btnTmbhutama.setBackground(new java.awt.Color(111, 78, 55));
        btnTmbhutama.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        btnTmbhutama.setForeground(new java.awt.Color(241, 229, 209));
        btnTmbhutama.setText("Tambah");
        btnTmbhutama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTmbhutamaActionPerformed(evt);
            }
        });

        txtBayar.setBackground(new java.awt.Color(111, 78, 55));
        txtBayar.setForeground(new java.awt.Color(241, 229, 209));
        txtBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBayarActionPerformed(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(111, 78, 55));
        jButton9.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        jButton9.setForeground(new java.awt.Color(241, 229, 209));
        jButton9.setText("Bayar");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        Pilihmenu.setBackground(new java.awt.Color(111, 78, 55));
        Pilihmenu.setEditable(true);
        Pilihmenu.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        Pilihmenu.setForeground(new java.awt.Color(241, 229, 209));
        Pilihmenu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "=Pilih Menu=", "Espresso", "Latte", "Cappuccino", "Mocha", "Americano", "Chocolate" }));
        Pilihmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PilihmenuActionPerformed(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(111, 78, 55));
        jLabel7.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(111, 78, 55));
        jLabel7.setText("ID_Customer         :");

        jLabel8.setBackground(new java.awt.Color(111, 78, 55));
        jLabel8.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(111, 78, 55));
        jLabel8.setText("Qty                           :");

        jLabel9.setBackground(new java.awt.Color(111, 78, 55));
        jLabel9.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(111, 78, 55));
        jLabel9.setText("Nama_Customer :");

        jLabel10.setBackground(new java.awt.Color(111, 78, 55));
        jLabel10.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(111, 78, 55));
        jLabel10.setText("Menu                        :");

        TableMenu.setBackground(new java.awt.Color(111, 78, 55));
        TableMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        TableMenu.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        TableMenu.setForeground(new java.awt.Color(241, 229, 209));
        TableMenu.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Nama", "Meja", "Menu", "Harga", "Jumlah", "Total Harga"
            }
        ));
        TableMenu.setSelectionBackground(new java.awt.Color(169, 144, 126));
        jScrollPane3.setViewportView(TableMenu);

        txtSubtotal.setBackground(new java.awt.Color(111, 78, 55));
        txtSubtotal.setForeground(new java.awt.Color(241, 229, 209));
        txtSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSubtotalActionPerformed(evt);
            }
        });

        jLabelKembalian.setBackground(new java.awt.Color(111, 78, 55));
        jLabelKembalian.setFont(new java.awt.Font("Poppins Medium", 1, 12)); // NOI18N

        jLabel13.setBackground(new java.awt.Color(111, 78, 55));
        jLabel13.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(111, 78, 55));
        jLabel13.setText("Sub Total :");

        jLabel14.setBackground(new java.awt.Color(111, 78, 55));
        jLabel14.setFont(new java.awt.Font("Poppins Medium", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(111, 78, 55));
        jLabel14.setText("kembalian :");

        jButton6.setBackground(new java.awt.Color(111, 78, 55));
        jButton6.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        jButton6.setForeground(new java.awt.Color(241, 229, 209));
        jButton6.setText("Reset");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        btnCetakresi.setBackground(new java.awt.Color(111, 78, 55));
        btnCetakresi.setFont(new java.awt.Font("Poppins Medium", 0, 12)); // NOI18N
        btnCetakresi.setForeground(new java.awt.Color(241, 229, 209));
        btnCetakresi.setText("Cetak Resi");
        btnCetakresi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakresiActionPerformed(evt);
            }
        });

        resiArea.setColumns(20);
        resiArea.setRows(5);
        jScrollPane4.setViewportView(resiArea);

        javax.swing.GroupLayout menuMakananLayout = new javax.swing.GroupLayout(menuMakanan);
        menuMakanan.setLayout(menuMakananLayout);
        menuMakananLayout.setHorizontalGroup(
            menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuMakananLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(menuMakananLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addGap(27, 27, 27)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(menuMakananLayout.createSequentialGroup()
                                .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(menuMakananLayout.createSequentialGroup()
                                .addComponent(jLabelKembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCetakresi)))
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(menuMakananLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(txtBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(28, 28, 28))
                            .addGroup(menuMakananLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jButton9)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(menuMakananLayout.createSequentialGroup()
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(menuMakananLayout.createSequentialGroup()
                                .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(menuMakananLayout.createSequentialGroup()
                                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel10)
                                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(txtNamacustomer, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtIdcustomer, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(Pilihmenu, 0, 109, Short.MAX_VALUE)))
                                    .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, menuMakananLayout.createSequentialGroup()
                                            .addComponent(Pilihmeja, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(btnTmbhutama, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, menuMakananLayout.createSequentialGroup()
                                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        menuMakananLayout.setVerticalGroup(
            menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuMakananLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(menuMakananLayout.createSequentialGroup()
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdcustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(txtNamacustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Pilihmenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Pilihmeja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTmbhutama)))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(menuMakananLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton9)
                        .addComponent(btnCetakresi))
                    .addComponent(jLabel14)
                    .addComponent(jLabelKembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jpPenjualan.setBackground(new java.awt.Color(241, 229, 209));
        jpPenjualan.setPreferredSize(new java.awt.Dimension(456, 393));

        jLabel1.setFont(new java.awt.Font("Poppins Medium", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(111, 78, 55));
        jLabel1.setText("LAPORAN PENJUALAN");

        TableLaporan.setBackground(new java.awt.Color(111, 78, 55));
        TableLaporan.setFont(new java.awt.Font("Poppins Medium", 0, 14)); // NOI18N
        TableLaporan.setForeground(new java.awt.Color(241, 229, 209));
        TableLaporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID Transaksi", "ID Customer", "Tanggal", "Harga"
            }
        ));
        TableLaporan.setMaximumSize(new java.awt.Dimension(300, 80));
        jScrollPane1.setViewportView(TableLaporan);

        javax.swing.GroupLayout jpLaporanPenjualanLayout = new javax.swing.GroupLayout(jpLaporanPenjualan);
        jpLaporanPenjualan.setLayout(jpLaporanPenjualanLayout);
        jpLaporanPenjualanLayout.setHorizontalGroup(
            jpLaporanPenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpLaporanPenjualanLayout.createSequentialGroup()
                .addGap(114, 114, 114)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(127, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
        );
        jpLaporanPenjualanLayout.setVerticalGroup(
            jpLaporanPenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpLaporanPenjualanLayout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jpPenjualanLayout = new javax.swing.GroupLayout(jpPenjualan);
        jpPenjualan.setLayout(jpPenjualanLayout);
        jpPenjualanLayout.setHorizontalGroup(
            jpPenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpLaporanPenjualan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jpPenjualanLayout.setVerticalGroup(
            jpPenjualanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpLaporanPenjualan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jpStok.setBackground(new java.awt.Color(241, 229, 209));
        jpStok.setPreferredSize(new java.awt.Dimension(456, 393));

        TableStok.setBackground(new java.awt.Color(111, 78, 55));
        TableStok.setForeground(new java.awt.Color(241, 229, 209));
        TableStok.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID Menu", "Nama Menu", "Harga Menu", "Stok"
            }
        ));
        jScrollPane5.setViewportView(TableStok);

        btnInsert.setBackground(new java.awt.Color(111, 78, 55));
        btnInsert.setForeground(new java.awt.Color(241, 229, 209));
        btnInsert.setText("INSERT");
        btnInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertActionPerformed(evt);
            }
        });

        btnDelete.setBackground(new java.awt.Color(111, 78, 55));
        btnDelete.setForeground(new java.awt.Color(241, 229, 209));
        btnDelete.setText("DELETE");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnstokresert.setBackground(new java.awt.Color(111, 78, 55));
        btnstokresert.setForeground(new java.awt.Color(241, 229, 209));
        btnstokresert.setText("RESET");
        btnstokresert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnstokresertActionPerformed(evt);
            }
        });

        btnUpdate.setBackground(new java.awt.Color(111, 78, 55));
        btnUpdate.setForeground(new java.awt.Color(241, 229, 209));
        btnUpdate.setText("UPDATE");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        txtStokidmenu.setBackground(new java.awt.Color(111, 78, 55));
        txtStokidmenu.setForeground(new java.awt.Color(241, 229, 209));

        txtStoknamamenu.setBackground(new java.awt.Color(111, 78, 55));
        txtStoknamamenu.setForeground(new java.awt.Color(241, 229, 209));

        txtStokhargamenu.setBackground(new java.awt.Color(111, 78, 55));
        txtStokhargamenu.setForeground(new java.awt.Color(241, 229, 209));

        txtStokMenu.setBackground(new java.awt.Color(111, 78, 55));
        txtStokMenu.setForeground(new java.awt.Color(241, 229, 209));
        txtStokMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStokMenuActionPerformed(evt);
            }
        });

        jLabel2.setBackground(new java.awt.Color(111, 78, 55));
        jLabel2.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(111, 78, 55));
        jLabel2.setText("Stok                :");

        jLabel3.setBackground(new java.awt.Color(111, 78, 55));
        jLabel3.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(111, 78, 55));
        jLabel3.setText("Harga Menu :");

        jLabel4.setBackground(new java.awt.Color(111, 78, 55));
        jLabel4.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(111, 78, 55));
        jLabel4.setText("Nama menu :");

        jLabel5.setBackground(new java.awt.Color(111, 78, 55));
        jLabel5.setFont(new java.awt.Font("Poppins Medium", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(111, 78, 55));
        jLabel5.setText("ID Menu          :");

        btnSearch.setBackground(new java.awt.Color(111, 78, 55));
        btnSearch.setForeground(new java.awt.Color(241, 229, 209));
        btnSearch.setText("SEACRH");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpStokLayout = new javax.swing.GroupLayout(jpStok);
        jpStok.setLayout(jpStokLayout);
        jpStokLayout.setHorizontalGroup(
            jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpStokLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtStokhargamenu)
                        .addComponent(txtStokidmenu)
                        .addComponent(txtStokMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))
                    .addComponent(txtStoknamamenu, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jpStokLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnUpdate)
                .addGap(18, 18, 18)
                .addComponent(btnDelete)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(18, 18, 18)
                .addComponent(btnstokresert)
                .addGap(27, 27, 27))
            .addGroup(jpStokLayout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jpStokLayout.setVerticalGroup(
            jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpStokLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStokidmenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStoknamamenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtStokhargamenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtStokMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnstokresert, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jpLaporan.setBackground(new java.awt.Color(241, 229, 209));
        jpLaporan.setPreferredSize(new java.awt.Dimension(456, 393));

        laporanArea.setColumns(20);
        laporanArea.setRows(5);
        jScrollPane6.setViewportView(laporanArea);

        txtPilihTanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPilihTanggalActionPerformed(evt);
            }
        });

        jLabel11.setForeground(new java.awt.Color(111, 78, 55));
        jLabel11.setText("Pilih Tanggal :");

        btnSearchTanggal.setText("SEARCH");
        btnSearchTanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchTanggalActionPerformed(evt);
            }
        });

        btnCetakLaporan.setText("CETAK LAPORAN");
        btnCetakLaporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakLaporanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpLaporanLayout = new javax.swing.GroupLayout(jpLaporan);
        jpLaporan.setLayout(jpLaporanLayout);
        jpLaporanLayout.setHorizontalGroup(
            jpLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpLaporanLayout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
            .addGroup(jpLaporanLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jpLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpLaporanLayout.createSequentialGroup()
                        .addComponent(btnSearchTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCetakLaporan))
                    .addComponent(txtPilihTanggal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpLaporanLayout.setVerticalGroup(
            jpLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpLaporanLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(jpLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPilihTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jpLaporanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearchTanggal)
                    .addComponent(btnCetakLaporan))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jpSetting.setBackground(new java.awt.Color(241, 229, 209));
        jpSetting.setPreferredSize(new java.awt.Dimension(456, 393));

        jButton7.setBackground(new java.awt.Color(241, 229, 209));
        jButton7.setForeground(new java.awt.Color(241, 229, 209));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pbo2_2024/logout.png"))); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpSettingLayout = new javax.swing.GroupLayout(jpSetting);
        jpSetting.setLayout(jpSettingLayout);
        jpSettingLayout.setHorizontalGroup(
            jpSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpSettingLayout.createSequentialGroup()
                .addGap(0, 386, Short.MAX_VALUE)
                .addComponent(jButton7))
        );
        jpSettingLayout.setVerticalGroup(
            jpSettingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpSettingLayout.createSequentialGroup()
                .addContainerGap(332, Short.MAX_VALUE)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jpIsiLayout = new javax.swing.GroupLayout(jpIsi);
        jpIsi.setLayout(jpIsiLayout);
        jpIsiLayout.setHorizontalGroup(
            jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(menuMakanan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpSetting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jpIsiLayout.setVerticalGroup(
            jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(menuMakanan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpPenjualan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpLaporan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addGroup(jpIsiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jpIsiLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jpSetting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout frameUtamaLayout = new javax.swing.GroupLayout(frameUtama);
        frameUtama.setLayout(frameUtamaLayout);
        frameUtamaLayout.setHorizontalGroup(
            frameUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jpIsi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        frameUtamaLayout.setVerticalGroup(
            frameUtamaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(frameUtamaLayout.createSequentialGroup()
                .addComponent(jpMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jpIsi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(frameUtama, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
jpIsi.removeAll();
jpIsi.repaint();
jpIsi.revalidate();

jpIsi.add(menuMakanan);
jpIsi.repaint();
jpIsi.revalidate();
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  
jpIsi.removeAll();
jpIsi.repaint();
jpIsi.revalidate();

jpIsi.add(jpPenjualan);
jpIsi.repaint();
jpIsi.revalidate(); 
tampilkanLaporanTransaksi();
// TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
   TampilDanEditStokMenu();
        jpIsi.removeAll();
jpIsi.repaint();
jpIsi.revalidate();

jpIsi.add(jpStok);
jpIsi.repaint();
jpIsi.revalidate();
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
  jpIsi.removeAll();
jpIsi.repaint();
jpIsi.revalidate();

jpIsi.add(jpLaporan);
jpIsi.repaint();
jpIsi.revalidate();        
// TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
  jpIsi.removeAll();
jpIsi.repaint();
jpIsi.revalidate();

jpIsi.add(jpSetting);
jpIsi.repaint();
jpIsi.revalidate();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void txtBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBayarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBayarActionPerformed

    private void txtIdcustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdcustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdcustomerActionPerformed

    private void txtQtyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQtyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQtyActionPerformed

    private void PilihmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PilihmenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PilihmenuActionPerformed

    private void btnTmbhutamaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTmbhutamaActionPerformed
 tambahKeTabelPesanan();
 LoadMenu();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTmbhutamaActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed

        prosesTransaksi();
      
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    private void txtSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubtotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSubtotalActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
    DefaultTableModel model = (DefaultTableModel) TableMenu.getModel();
    model.setRowCount(0); 

    //
    txtSubtotal.setText(""); 
    txtBayar.setText(""); 
    txtQty.setText("");
    txtNamacustomer.setText(""); 
    Pilihmenu.setSelectedIndex(0); 
    Pilihmeja.setSelectedIndex(0); 
    jLabelKembalian.setText(""); 
    txtIdcustomer.setText("");
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void btnCetakresiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakresiActionPerformed
 
     try {
            
            double subtotal = Double.parseDouble(txtSubtotal.getText());
            double uangDibayar = Double.parseDouble(txtBayar.getText());
            double kembalian = Double.parseDouble(jLabelKembalian.getText());
            
          
            cetakResi(subtotal, uangDibayar, kembalian);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Pastikan subtotal, uang dibayar, dan kembalian sudah terisi dengan benar!");
        }
     
      int pilihanCetak = JOptionPane.showConfirmDialog(this, "Apakah Anda ingin mencetak resi ini?", "Cetak Resi", JOptionPane.YES_NO_OPTION);
    if (pilihanCetak == JOptionPane.YES_OPTION) {
        try {
            resiArea.print();
        } catch (PrinterException e) {
            System.out.println("Gagal mencetak resi: " + e.getMessage());
        }
    }
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCetakresiActionPerformed

    private void txtStokMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStokMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStokMenuActionPerformed

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
insertData();        // TODO add your handling code here:
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
 updateData();        // TODO add your handling code here:
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
deleteData();        // TODO add your handling code here:
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnstokresertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnstokresertActionPerformed
     clearFields();   // TODO add your handling code here:
    }//GEN-LAST:event_btnstokresertActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed

     searchData();  // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtPilihTanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPilihTanggalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPilihTanggalActionPerformed

    private void btnSearchTanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchTanggalActionPerformed
String tanggalTerpilih = txtPilihTanggal.getText(); // Mengambil tanggal dari JTextField
tampilkanPendapatanHarianLengkap(tanggalTerpilih);        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchTanggalActionPerformed

    private void btnCetakLaporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakLaporanActionPerformed
cetakLaporanPendapatanHarian();        // TODO add your handling code here:
    }//GEN-LAST:event_btnCetakLaporanActionPerformed

    private void txtNamacustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNamacustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNamacustomerActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
logout();      
    // Tutup halaman utama
    this.dispose(); // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HalamanUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HalamanUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HalamanUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HalamanUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HalamanUtama().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> Pilihmeja;
    private javax.swing.JComboBox<String> Pilihmenu;
    private javax.swing.JTable TableLaporan;
    private javax.swing.JTable TableMenu;
    private javax.swing.JTable TableStok;
    private javax.swing.JButton btnCetakLaporan;
    private javax.swing.JButton btnCetakresi;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnInsert;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSearchTanggal;
    private javax.swing.JButton btnTmbhutama;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnstokresert;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel frameUtama;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelKembalian;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable jTable2;
    private javax.swing.JPanel jpIsi;
    private javax.swing.JPanel jpLaporan;
    private javax.swing.JPanel jpLaporanPenjualan;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JPanel jpPenjualan;
    private javax.swing.JPanel jpSetting;
    private javax.swing.JPanel jpStok;
    private javax.swing.JTextArea laporanArea;
    private javax.swing.JPanel menuMakanan;
    private javax.swing.JTextArea resiArea;
    private javax.swing.JTextField txtBayar;
    private javax.swing.JTextField txtIdcustomer;
    private javax.swing.JTextField txtNamacustomer;
    private javax.swing.JTextField txtPilihTanggal;
    private javax.swing.JTextField txtQty;
    private javax.swing.JTextField txtStokMenu;
    private javax.swing.JTextField txtStokhargamenu;
    private javax.swing.JTextField txtStokidmenu;
    private javax.swing.JTextField txtStoknamamenu;
    private javax.swing.JTextField txtSubtotal;
    // End of variables declaration//GEN-END:variables
}
