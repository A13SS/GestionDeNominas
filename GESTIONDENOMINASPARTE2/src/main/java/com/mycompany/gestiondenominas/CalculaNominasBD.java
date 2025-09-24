/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas;

/**
 *
 * @author aleja
 */
import com.mycompany.gestiondenominas.laboral.DatosNoCorrectosException;
import com.mycompany.gestiondenominas.laboral.Empleado;
import com.mycompany.gestiondenominas.laboral.Nomina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CalculaNominasBD {
    // Configuración de la base de datos
    private static final String URL = "jdbc:mariadb://localhost:3306/gestion_nominas";
    private static final String USER = "root"; 
    private static final String PASSWORD = "123456";
    
    public static void main(String[] args) {
        Connection con = null;
        try {
            
            //Conectar a la base de datos
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión a la base de datos establecida.");

            //Leer empleados desde la base de datos
            List<Empleado> empleados = leerEmpleadosDesdeBD(con);

            //Aplicar modificaciones en memoria
            for (Empleado e : empleados) {
                if (e.getNombre().equals("James Cosling")) {
                    e.setCategoria(9);  // Actualizamos su categoría a 9
                    System.out.println("Categoría de " + e.getNombre() + " actualizada a 9.");
                } else if (e.getNombre().equals("Ada Lovelace")) {
                    e.incrAnyo();        // Incrementamos sus años trabajados en 1
                    System.out.println("Años trabajados de " + e.getNombre() + " incrementados");
                }
            }

            //Guardar los cambios en la Base de Datos
            guardarCambiosEnBD(con, empleados);
            System.out.println("Base de datos actualizada con los cambios.");

            //Guardar los sueldos en la tabla nominas
            guardarSueldosEnBD(con, empleados);
            System.out.println("Sueldos almacenados en la base de datos.");

            //Mostrar resultados por consola
            escribe(empleados);

        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
        } catch (DatosNoCorrectosException e) {
            System.err.println("Error en datos: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.close();
                    System.out.println("Conexión cerrada.");
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la base de datos: " + e.getMessage());
                }
            }
        }
    }

    // Leer empleados desde la base de datos
    private static List<Empleado> leerEmpleadosDesdeBD(Connection conn) throws SQLException, DatosNoCorrectosException {
        List<Empleado> empleados = new ArrayList<>();
        String sql = "SELECT nombre, dni, sexo, categoria, anyos FROM empleados";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet results = stmt.executeQuery()) {

            while (results.next()) {
                String nombre = results.getString("nombre");
                String dni = results.getString("dni");
                char sexo = results.getString("sexo").charAt(0);
                int categoria = results.getInt("categoria");
                int anyos = results.getInt("anyos");

                Empleado emp = new Empleado(nombre, dni, sexo, categoria, anyos);
                empleados.add(emp);
                System.out.println("Empleado leído de BD: " + nombre);
            }
        }
        return empleados;
    }

    // Guardar cambios de empleados en la base de datos
    private static void guardarCambiosEnBD(Connection conn, List<Empleado> empleados) throws SQLException {
        String sql = "UPDATE empleados SET categoria = ?, anyos = ? WHERE dni = ?";
        try (PreparedStatement preparedS = conn.prepareStatement(sql)) {
            for (Empleado e : empleados) {
                 preparedS.setInt(1, e.getCategoria());
                 preparedS.setInt(2, e.getAnyos());
                 preparedS.setString(3, e.getDni());
                 preparedS.addBatch(); // Para mayor eficiencia ya que mejora el rendimiento
            }
            int[] resultados =  preparedS.executeBatch(); //Envia al servidor todo de una vez, con el executeUpdate seria muy lento si quisiese guardar muchos cambios de muchos clientes
            System.out.println(resultados.length + " empleados actualizados en la base de datos.");
        }
    }

    // Guardar sueldos en la tabla nominas
    private static void guardarSueldosEnBD(Connection conn, List<Empleado> empleados) throws SQLException, DatosNoCorrectosException {
        Nomina n = new Nomina();
        // Primero, obtenemos el id del empleado por DNI
        String getIdSql = "SELECT id FROM empleados WHERE dni = ?";
        String insertSql = "INSERT INTO nominas (empleado_id, sueldo) VALUES (?, ?)";

        try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            for (Empleado e : empleados) {
                double sueldo = n.sueldo(e);

                // Obtener id del empleado
                getIdStmt.setString(1, e.getDni());
                ResultSet rs = getIdStmt.executeQuery();
                if (rs.next()) {
                    int empleadoId = rs.getInt("id");

                    // Insertar sueldo
                    insertStmt.setInt(1, empleadoId);
                    insertStmt.setDouble(2, sueldo);
                    insertStmt.executeUpdate();

                    System.out.printf("Sueldo de %.2f € guardado en BD para %s\n", sueldo, e.getNombre());
                }
            }
        }
    }

    //Muestra por consola
    private static void escribe(List<Empleado> empleados) {
        Nomina n = new Nomina();
        System.out.println("\nEMPLEADOS Y SUS SUELDOS");
        for (Empleado e : empleados) {
            System.out.println(e);  // Imprime toString() del empleado
            System.out.printf("Sueldo calculado: %.2f €\n\n", n.sueldo(e));
        }
    }
}
