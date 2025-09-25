/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas;

import com.mycompany.gestiondenominas.laboral.DatosNoCorrectosException;
import com.mycompany.gestiondenominas.laboral.Empleado;
import com.mycompany.gestiondenominas.laboral.Nomina;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aleja
 */
public class CalculaNominasFusion {

    
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
            System.out.println("Empleados leidos desde la base de datos");

            //Modificaciones en memoria
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

            //Genera copia de seguridad en el archivo de texto
            guardarEmpleadosEnArchivo(empleados, "empleados.txt");

            //Genera un archivo binario para guardar los suerdos como copia de seguridad
            escribirSalariosBinario(empleados);

            //Mostrar resultados por consola
            escribe(empleados);

        } catch (SQLException e) {
            /*En caso de que de error al conectar con la base de datos, como no sabemos si el error puede ser por error de conexion o porque la base de datos esta vacia, llamaremos al metodo manejaProgramaConFichero
            *  que volvera a intentar conectar con la base de datos, si conecta con ella y detecta que la base de datos esta vacia, realizara los procesos deseados de actualizacion y guardado en el backup, en este caso en los ficheros .txt y .bat
            */
            System.err.println("Error de base de datos: " + e.getMessage());

            manejaProgramaConFichero();

        } catch (IOException e) {
            System.err.println("Error de E/S (archivos): " + e.getMessage());
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
        
        //Consulta SQL para obtener todos los campos de la tabla empleados
        String sql = "SELECT nombre, dni, sexo, categoria, anyos FROM empleados";
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet results = stmt.executeQuery()) {

            while (results.next()) {
                //Extraemos cada campo guardandolo en una variable
                String nombre = results.getString("nombre");
                String dni = results.getString("dni");
                char sexo = results.getString("sexo").charAt(0);
                int categoria = results.getInt("categoria");
                int anyos = results.getInt("anyos");

                //Creamos un nuevo objeto Empleado con los datos guardados previamente y lo añadimos a una lista la cual sera devuelta una vez completada
                Empleado emp = new Empleado(nombre, dni, sexo, categoria, anyos);
                empleados.add(emp);
                System.out.println("Empleado leído de BD: " + nombre);
            }
        }
        return empleados;  //Devolvemos la lista empleados completa
    }

    // Guardar cambios de empleados en la base de datos
    private static void guardarCambiosEnBD(Connection conn, List<Empleado> empleados) throws SQLException {
        
        //Sentencia SQL para actualizar la categoria y los años trabajados de un empleado usando su dni
        String sql = "UPDATE empleados SET categoria = ?, anyos = ? WHERE dni = ?";
        try (PreparedStatement preparedS = conn.prepareStatement(sql)) {
            for (Empleado e : empleados) {
                preparedS.setInt(1, e.getCategoria());
                preparedS.setInt(2, e.getAnyos());
                preparedS.setString(3, e.getDni());
                preparedS.addBatch(); // Para mayor eficiencia ya que mejora el rendimiento
            }
            int[] resultados = preparedS.executeBatch(); //Envia al servidor todo de una vez, con el executeUpdate seria muy lento si quisiese guardar muchos cambios de muchos clientes
            System.out.println(resultados.length + " empleados actualizados en la base de datos.");
        }
    }

    // Guardar sueldos en la tabla nominas
    private static void guardarSueldosEnBD(Connection conn, List<Empleado> empleados) throws SQLException, DatosNoCorrectosException {
        Nomina n = new Nomina();
        // Primero, obtenemos el id del empleado por DNI para relacionarlo en la tabla nominas e insertamos un nuevo registro en la nomina
        String getIdSql = "SELECT id FROM empleados WHERE dni = ?";
        String insertSql = "INSERT INTO nominas (empleado_id, sueldo) VALUES (?, ?)";

        try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql); PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            for (Empleado e : empleados) {
                double sueldo = n.sueldo(e);  //Calculamos el sueldo del empleado

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

    //AQUI EMPIEZAN LOS METODOS DE LECTURA Y ESCRITURA EN ARCHIVOS:
    private static List<Empleado> leerEmpleadosDesdeArchivo(String empleadosArchivo) throws IOException, DatosNoCorrectosException {
        List<Empleado> empleados = new ArrayList<>();
        File archivoTexto = new File(empleadosArchivo);

        if (!archivoTexto.exists()) {
            throw new FileNotFoundException("Archivo " + empleadosArchivo + " no encontrado.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivoTexto))) {
            String linea;
            int lineaNum = 0;

            while ((linea = br.readLine()) != null) { 
                lineaNum++;
                String[] partes = linea.split(",");
                //Dividimos la linea por las comas para obtener los campos y comprobamos que haya 5 campos almenos
                if (partes.length >= 5) {
                    try {
                        String nombre = partes[0].trim();
                        String dni = partes[1].trim();
                        char sexo = partes[2].trim().charAt(0);  // Primer carácter del sexo
                        int categoria = Integer.parseInt(partes[3].trim());
                        int anosTrabajados = Integer.parseInt(partes[4].trim());

                        // Creamos un nuevo empleado y lo añadimos a la lista
                        Empleado emp = new Empleado(nombre, dni, sexo, categoria, anosTrabajados);
                        empleados.add(emp);

                        System.out.println("Empleado leído: " + nombre);

                    } catch (NumberFormatException ex) {
                        System.err.println("Error de conversión en línea " + lineaNum + ": " + linea);
                    } catch (DatosNoCorrectosException ex) {
                        System.err.println("Datos no válidos en línea " + lineaNum + ": " + ex.getMessage());
                    }
                } else {
                    System.err.println("Formato incorrecto en línea " + lineaNum + ": " + linea);
                }
            }
        }
        return empleados; // Devolvemos la lista completa de empleados

    }

    private static void guardarEmpleadosEnArchivo(List<Empleado> empleados, String empleadostxt) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(empleadostxt))) {
            for (Empleado e : empleados) {
                // Escribimos en formato: nombre,dni,sexo,años,categoría
                pw.printf("%s,%s,%c,%d,%d%n",
                        e.getNombre(),
                        e.getDni(),
                        e.getSexo(),
                        e.getCategoria(),
                        e.getAnyos());
            }
        }
        System.out.println("Empleados guardados en: " + empleadostxt);
    }

    private static void escribirSalariosBinario(List<Empleado> empleados) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("sueldos.dat"))) {
            Nomina n = new Nomina();  // Instancia para calcular sueldos

            System.out.println("\nEscribiendo sueldos en archivo binario 'sueldos.dat'...");

            // Recorremos cada empleado
            for (Empleado e : empleados) {
                double sueldo = n.sueldo(e);  // Calculamos sueldo

                // Escribimos en el archivo binario:
                dos.writeUTF(e.getDni());     // DNI como cadena (UTF)
                dos.writeDouble(sueldo);      // Sueldo como número decimal (double)

                System.out.printf("  -> %s: %.2f €\n", e.getDni(), sueldo);
            }

            System.out.println("Archivo binario 'sueldos.dat' generado correctamente.");
        }
    }

    //EN CASO DE ERROR CON LA BASE DE DATOS, SE REALIZARA LO MISMO PERO COGIENDO LOS DATOS DESDE EL ARCHIVO DE TEXTO QUE ES NUESTRO BACKUP:
    private static void manejaProgramaConFichero() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión a la base de datos establecida.");

            List<Empleado> empleados = leerEmpleadosDesdeArchivo("empleados.txt");

            //Modificaciones en memoria
            for (Empleado emp : empleados) {
                if (emp.getNombre().equals("James Cosling")) {
                    emp.setCategoria(9);  // Actualizamos su categoría a 9
                    System.out.println("Categoría de " + emp.getNombre() + " actualizada a 9.");
                } else if (emp.getNombre().equals("Ada Lovelace")) {
                    emp.incrAnyo();        // Incrementamos sus años trabajados en 1
                    System.out.println("Años trabajados de " + emp.getNombre() + " incrementados");
                }
            }

            //Guardar los cambios en la Base de Datos
            guardarCambiosEnBD(con, empleados);
            System.out.println("Base de datos actualizada con los cambios.");

            //Guardar los sueldos en la tabla nominas
            guardarSueldosEnBD(con, empleados);
            System.out.println("Sueldos almacenados en la base de datos.");

            //Genera copia de seguridad en el archivo de texto
            guardarEmpleadosEnArchivo(empleados, "empleados.txt");

            //Genera un archivo binario para guardar los suerdos como copia de seguridad
            escribirSalariosBinario(empleados);

            //Mostrar resultados por consola
            escribe(empleados);
        } catch (IOException | DatosNoCorrectosException ex) {
            System.err.println("Error: " + ex.getMessage());
        } catch (SQLException ex) {
            System.err.println("Error de base de datos, no conecto: " + ex.getMessage());
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
