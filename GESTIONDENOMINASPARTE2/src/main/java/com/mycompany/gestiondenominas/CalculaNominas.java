/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas;

/**
 *
 * @author Alejandro Fernandez Escribano Clase principal con el metodo main y un
 * metodo escribe el cuel imprime todos los datos del empleado y su sueldo En
 * caso de error se captura la excepcion {@link DatosNoCorrectosException}
 */
import com.mycompany.gestiondenominas.laboral.DatosNoCorrectosException;
import com.mycompany.gestiondenominas.laboral.Empleado;
import com.mycompany.gestiondenominas.laboral.Nomina;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalculaNominas {

    public static void main(String[] args) {

        List<Empleado> empleados;
        try {

            empleados = leerEmpleadosDesdeArchivo("empleados.txt");

            for (Empleado e : empleados) {
                if (e.getNombre().equals("James Cosling")) {
                    e.setCategoria(9);  // Actualizamos su categoría a 9
                    System.out.println("Categoría de " + e.getNombre() + " actualizada a 9.");
                }else if (e.getNombre().equals("Ada Lovelace")) {
                    e.incrAnyo();        // Incrementamos sus años trabajados en 1
                    System.out.println("Años trabajados de " + e.getNombre() + " incrementados");
                }
            }

            guardarEmpleadosEnArchivo(empleados, "empleados.txt");
            System.out.println("Archivo 'empleados.txt' actualizado con los cambios.");

            escribirSalariosBinario(empleados);
            
            escribe(empleados);

        } catch (IOException e) {
            System.err.println("Error al leer/escribir archivos: " + e.getMessage());
        } catch (DatosNoCorrectosException e) {
            System.out.println(e.getMessage());
        }

    }

    private static List<Empleado> leerEmpleadosDesdeArchivo(String empleadosArchivo) throws IOException, DatosNoCorrectosException {
        List<Empleado> empleados = new ArrayList<>();
        File archivoTexto = new File(empleadosArchivo);

        if (!archivoTexto.exists()) {
            throw new FileNotFoundException("Archivo " + empleadosArchivo + " no encontrado.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivoTexto))) {
            String linea;
            int lineaNum = 0;

            while ((linea = br.readLine()) != null) { //isBlank se podria usar en vez de null para evitar los saltos de pagina o los espacios.
                lineaNum++;
                String[] partes = linea.split(",");
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

    private static void escribe(List<Empleado> empleados) {
        Nomina n = new Nomina();
        System.out.println("EMPLEADOS Y SUS SUELDOS");
        for (Empleado e : empleados) {
                System.out.println(e);  // Imprime toString() del empleado
                System.out.printf("Sueldo calculado: %.2f €\n\n", n.sueldo(e));  // Formateamos el sueldo con 2 decimales
            }
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
}
