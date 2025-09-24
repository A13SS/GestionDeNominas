/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas.laboral;

/**
 *
 * @author Alejandro Fernandez Escribano
 * Clase que recoge en una tabla tods los sueldos base y con el metodo sueldo calcula el
 * sueldo base de cada empleado
 */
public class Nomina {

    private static final int SUELDO_BASE[] = {50000, 70000, 90000, 110000, 130000, 150000, 170000, 190000, 210000, 230000};
    
    /**
     * Metodo que calcula y devuelve el sueldo base
     * @param e {@link Empleado}
     * @return 
     */
    public double sueldo(Empleado e) {
        double sueldo = 0;
        sueldo = SUELDO_BASE[e.getCategoria() - 1] + (5000 * e.anyos);
        return sueldo;
    }
}
