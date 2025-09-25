/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.gestiondenominas.laboral;

/**
 *
 * @author Alejandro Fernandez Escribano
 * 
 * La clase Persona pertenece al paquete laboral y en esta clase se describen los atributos de la persona.
 */
public class Persona {

    public String nombre;
    public String dni;
    public char sexo;

    /**
     * Constructor que recoge todos los atributos de persona
     * @param nombre String
     * @param dni String
     * @param sexo char
     */
    public Persona(String nombre, String dni, char sexo) {
        this.nombre = nombre;
        this.dni = dni;
        this.sexo = sexo;
    }

    /**
     * Constructor que recoge solo los atributos nombre y sexo
     * @param nombre String
     * @param sexo char
     */
    public Persona(String nombre, char sexo) {
        this.nombre = nombre;
        this.sexo = sexo;
    }

    /**
     * Para modificar el valor del dni de una persona
     * @param dni String
     */
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    /**
     * Presenta por pantalla el nombre y el dni
     * @param dni String
     * @param nombre String
     */
    public void Imprime(String dni, String nombre){
        System.out.println("Nombre: " + nombre + ", Dni: " + dni);
    }   
}
