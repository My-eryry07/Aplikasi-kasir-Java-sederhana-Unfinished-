/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pbo2_2024;

public class aplikasiKasir {
    public static void main(String[] args) {
        koneksi dbKoneksi = new koneksi();
        dbKoneksi.connect(); 

        
        if (dbKoneksi.getConnection() != null) {
          
            System.out.println("Operasi database dapat dilakukan.");
        }
    }
}
