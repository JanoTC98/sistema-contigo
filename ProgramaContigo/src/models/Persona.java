package models;

public abstract class Persona {
    protected String dni;
    protected String nombre;
    protected String apellido;

    public Persona(String dni, String nombre, String apellido) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Metodo abstracto que deberán implementar las clases hijas
    public abstract void mostrarInformacion();

    // Métodos concretos compartidos por todas las personas
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    // Getters
    public String getDni() {
        return dni;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    // Setters para permitir actualizaciones
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
}