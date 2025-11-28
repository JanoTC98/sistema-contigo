package models;

import interfaces.Verificable;
import interfaces.Reportable;

public class Autorizado extends Persona implements Verificable, Reportable {
    private String dniBeneficiario;
    private String parentesco;
    private boolean autorizado;

    public Autorizado(String dni, String nombre, String apellido,
                      String dniBeneficiario, String parentesco) {
        super(dni, nombre, apellido);
        this.dniBeneficiario = dniBeneficiario;
        this.parentesco = parentesco;
        this.autorizado = true; // Por defecto se crea como autorizado
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("Autorizado: " + getNombreCompleto() +
                " - DNI: " + dni +
                " - Beneficiario: " + dniBeneficiario +
                " - Parentesco: " + parentesco +
                " - Estado: " + (autorizado ? "Autorizado" : "No autorizado"));
    }

    @Override
    public boolean verificarElegibilidad() {
        // Un autorizado es elegible si está marcado como autorizado
        // y el parentesco es válido
        return autorizado && esParentescoValido();
    }

    public boolean esParentescoValido() {
        // Validar parentescos permitidos según normativa
        String[] parentescosValidos = {"hijo", "hija", "esposo", "esposa",
                "padre", "madre", "hermano", "hermana",
                "tutor", "tutora"};

        for (String parentescoValido : parentescosValidos) {
            if (parentesco.toLowerCase().contains(parentescoValido)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String generarReporte() {
        return "=== REPORTE DE AUTORIZADO ===\n" +
                "Nombre: " + getNombreCompleto() + "\n" +
                "DNI: " + dni + "\n" +
                "DNI del Beneficiario: " + dniBeneficiario + "\n" +
                "Parentesco: " + parentesco + "\n" +
                "Estado de autorización: " + (autorizado ? "Autorizado" : "No autorizado") + "\n" +
                "Parentesco válido: " + (esParentescoValido() ? "Sí" : "No") + "\n" +
                "Elegible para cobro: " + (verificarElegibilidad() ? "Sí" : "No");
    }

    public String getDniBeneficiario() {
        return dniBeneficiario;
    }

    public String getParentesco() {
        return parentesco;
    }

    public boolean isAutorizado() {
        return autorizado;
    }

    // Setters
    public void setAutorizado(boolean autorizado) {
        this.autorizado = autorizado;
    }

    public void setParentesco(String parentesco) {
        this.parentesco = parentesco;
    }
}