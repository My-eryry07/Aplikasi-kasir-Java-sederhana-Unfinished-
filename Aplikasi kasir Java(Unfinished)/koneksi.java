/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pbo2_2024;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author User
 */
public class koneksi {
    private String URL = "jdbc:mysql://127.0.0.1:3306/db_kasir"; 
    private String USERNAME = "root"; 
    private String PASSWORD = "";
    private Connection con;

    // Method untuk menghubungkan ke database
    public Connection connect() {
        try {
            con = DriverManager.getConnection(URL, USERNAME, PASSWORD); 
            System.out.println("Koneksi berhasil!");
        } catch (SQLException e) {
         
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }

   
    public Connection getConnection() {
        return con;
    }
}