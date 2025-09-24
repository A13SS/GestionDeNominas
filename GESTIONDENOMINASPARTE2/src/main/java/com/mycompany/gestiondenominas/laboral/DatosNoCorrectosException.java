/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas.laboral;

/**
 *
 * @author AlejandroFernandez Escribano
 */
public class DatosNoCorrectosException extends Exception{
    /**
     * Excepcion que se usara durante el programa en caso de que algun dato no este
     * correcto
     * @param message 
     */
    public DatosNoCorrectosException(String message) {
        super(message);
    }
    
}
