package models;

import java.util.ArrayList;
import java.util.List;
import interfaces.Reportable;

public class Padron implements Reportable {
    private int numeroPadron;
    private String periodoMeses;
    private List<Beneficiario> beneficiarios;

    public Padron(int numeroPadron, String periodoMeses) {
        this.numeroPadron = numeroPadron;
        this.periodoMeses = periodoMeses;
        this.beneficiarios = new ArrayList<>();
    }

    public void agregarBeneficiario(Beneficiario beneficiario) {
        if (beneficiario.verificarElegibilidad()) {
            beneficiarios.add(beneficiario);
        }
    }

    @Override
    public String generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("Padrón ").append(numeroPadron).append(" (").append(periodoMeses).append(")\n");
        reporte.append("Total de beneficiarios: ").append(beneficiarios.size()).append("\n");

        return reporte.toString();
    }

    public List<Beneficiario> getBeneficiarios() {
        return beneficiarios;
    }
}