package models;
import interfaces.Verificable;

public class Certificado implements Verificable {
    private String codigo;
    private String dniPaciente;
    private String gradoDiscapacidad;
    private boolean vigente;

    public Certificado(String codigo, String dniPaciente, String gradoDiscapacidad, boolean vigente) {
        this.codigo = codigo;
        this.dniPaciente = dniPaciente;
        this.gradoDiscapacidad = gradoDiscapacidad;
        this.vigente = vigente;
    }

    @Override
    public boolean verificarElegibilidad() {
        // Un certificado es elegible si está vigente y es de discapacidad severa
        return vigente && gradoDiscapacidad.equalsIgnoreCase("severa");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDniPaciente() {
        return dniPaciente;
    }

    public String getGradoDiscapacidad() {
        return gradoDiscapacidad;
    }

    public boolean isVigente() {
        return vigente;
    }

}