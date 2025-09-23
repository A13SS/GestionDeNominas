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

public class CalculaNominas {

    public static void main(String[] args) {
        try {
            Empleado emple1 = new Empleado("James Cosling", "32000032G", 'M', 4, 7);
            Empleado emple2 = new Empleado("Ada Lovelace", "32000031R", 'F');

            escribe(emple1, emple2);

            emple1.setCategoria(9);
            emple2.incrAnyo();

            escribe(emple1, emple2);
        } catch (DatosNoCorrectosException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void escribe(Empleado emple1, Empleado emple2) {
        Nomina n = new Nomina();
        emple1.imprime();
        System.out.println("sueldo: " + n.sueldo(emple1));
        emple2.imprime();
        System.out.println("sueldo: " + n.sueldo(emple2));
    }
}
