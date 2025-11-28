package services;

import dao.*;
import models.*;
import interfaces.Reportable;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SistemaGestion implements Reportable {
    private final BeneficiarioDAO beneficiarioDAO;
    private final AutorizadoDAO autorizadoDAO;
    private final CertificadoDAO certificadoDAO;

    public SistemaGestion() {
        this.beneficiarioDAO = new BeneficiarioDAO();
        this.autorizadoDAO = new AutorizadoDAO();
        this.certificadoDAO = new CertificadoDAO();
    }

    // RF1 - Registro de beneficiarios
    public boolean registrarBeneficiario(Beneficiario beneficiario) {
        return beneficiarioDAO.insertar(beneficiario);
    }

    // RF5 - Gestión de autorizaciones
    public boolean registrarAutorizado(Autorizado autorizado) {
        return autorizadoDAO.insertar(autorizado);
    }

    public boolean registrarCertificado(Certificado certificado) {
        return certificadoDAO.insertar(certificado);
    }

    // Métodos de búsqueda unificados
    public Beneficiario buscarBeneficiario(String dni) {
        return beneficiarioDAO.buscarPorDni(dni);
    }

    public Autorizado buscarAutorizado(String dni) {
        return autorizadoDAO.buscarPorDni(dni);
    }

    public Certificado buscarCertificado(String dniPaciente) {
        return certificadoDAO.buscarPorDniPaciente(dniPaciente);
    }

    public boolean actualizarBeneficiario(Beneficiario beneficiario) {
        return beneficiarioDAO.actualizar(beneficiario);
    }

    // RF6 - Procesamiento de pagos
    public boolean procesarPago(String dni, int numeroPadron) {
        return beneficiarioDAO.procesarPago(dni, numeroPadron);
    }


    // RF8 - Generación de reportes optimizada
    public void generarReporteBeneficiarios() {
        System.out.println("\n=== REPORTE GENERAL DE BENEFICIARIOS ===");
        List<Beneficiario> beneficiarios = beneficiarioDAO.obtenerTodos();

        Map<Boolean, List<Beneficiario>> elegibilidad = beneficiarios.stream()
                .collect(Collectors.partitioningBy(Beneficiario::verificarElegibilidad));

        System.out.printf("Total: %d | Elegibles: %d | No elegibles: %d\n",
                beneficiarios.size(),
                elegibilidad.get(true).size(),
                elegibilidad.get(false).size());

        beneficiarios.forEach(b -> {
            System.out.println(b.generarReporte());
            System.out.println("---");
        });
    }

    public boolean eliminarPorFallecimiento(String dni) {
        Beneficiario beneficiario = buscarBeneficiario(dni);
        if (beneficiario == null) {
            System.out.println("No se encontró al beneficiario con DNI: " + dni);
            return false;
        }
        boolean exito = beneficiarioDAO.marcarComoFallecido(dni);
        if (exito) {
            System.out.println("Beneficiario marcado como fallecido: " + beneficiario.getNombreCompleto());
        } else {
            System.out.println("No se pudo marcar como fallecido.");
        }
        return exito;
    }

    public void generarReporteAutorizados() {
        System.out.println("\n=== REPORTE DE AUTORIZADOS ===");
        List<Autorizado> autorizados = autorizadoDAO.obtenerTodos();
        System.out.println("Total de autorizados: " + autorizados.size());

        autorizados.forEach(a -> {
            System.out.println(a.generarReporte());
            System.out.println("---");
        });
    }

    public void generarReportePorRegion(String region) {
        System.out.printf("\n=== REPORTE POR REGIÓN: %s ===\n", region.toUpperCase());

        List<Beneficiario> beneficiariosRegion = beneficiarioDAO.buscarPorRegion(region);
        System.out.printf("Beneficiarios en %s: %d\n", region, beneficiariosRegion.size());

        beneficiariosRegion.forEach(b ->
                System.out.printf("- %s (DNI: %s) - Estado: %s\n",
                        b.getNombreCompleto(),
                        b.getDni(),
                        b.verificarElegibilidad() ? "Elegible" : "No elegible"));
    }

    public void generarReporteNoElegibles() {
        System.out.println("\n=== REPORTE DE NO ELEGIBLES ===");

        List<Beneficiario> noElegibles = beneficiarioDAO.obtenerTodos().stream()
                .filter(b -> !b.verificarElegibilidad())
                .toList();

        System.out.println("Total de beneficiarios no elegibles: " + noElegibles.size());

        noElegibles.forEach(b ->
                System.out.printf("- %s (DNI: %s) - Clasificación: %s\n",
                        b.getNombreCompleto(),
                        b.getDni(),
                        b.getClasificacionEconomica()));
    }

    // RF7 - Alertas optimizadas con método auxiliar
    public void generarAlertas() {
        System.out.println("=== ALERTAS CRÍTICAS ===");
        List<Beneficiario> beneficiarios = beneficiarioDAO.obtenerTodos();

        AlertaInfo[] alertas = {
                new AlertaInfo("sin certificado de discapacidad",
                        b -> !b.isTieneCertificadoDiscapacidad()),
                new AlertaInfo("con otros ingresos",
                        Beneficiario::isTieneOtrosIngresos),
                new AlertaInfo("con CSE incompatible",
                        b -> "no pobre".equals(b.getClasificacionEconomica()))
        };

        boolean hayAlertas = false;
        for (AlertaInfo alerta : alertas) {
            List<Beneficiario> casos = beneficiarios.stream()
                    .filter(alerta.condicion)
                    .toList();

            if (!casos.isEmpty()) {
                hayAlertas = true;
                System.out.printf("ALERTA: %d beneficiarios %s:\n", casos.size(), alerta.descripcion);
                casos.forEach(b -> System.out.printf("  - %s (DNI: %s)%s\n",
                        b.getNombreCompleto(),
                        b.getDni(),
                        alerta.descripcion.contains("CSE") ? " - CSE: " + b.getClasificacionEconomica() : ""));
            }
        }

        if (!hayAlertas) {
            System.out.println("No hay alertas críticas en el sistema");
        }
    }

    // Clase auxiliar para alertas
    private record AlertaInfo(String descripcion, Predicate<Beneficiario> condicion) {}

    // RF6 - Validación de incompatibilidades simplificada
    public void validarIncompatibilidades() {
        beneficiarioDAO.obtenerTodos().stream()
                .filter(b -> !b.verificarElegibilidad())
                .forEach(b -> System.out.printf("Beneficiario %s marcado como NO ELEGIBLE\n",
                        b.getNombreCompleto()));
    }

    // Verificación de certificados optimizada
    public void verificarCertificadosVigentes() {
        System.out.println("\n=== VERIFICACIÓN DE CERTIFICADOS ===");

        beneficiarioDAO.obtenerTodos().stream()
                .filter(Beneficiario::isTieneCertificadoDiscapacidad)
                .forEach(b -> {
                    Certificado cert = certificadoDAO.buscarPorDniPaciente(b.getDni());
                    if (cert == null) {
                        System.out.printf("%s tiene marcado certificado pero no se encuentra en BD\n",
                                b.getNombreCompleto());
                    } else if (!cert.isVigente()) {
                        System.out.printf("Certificado vencido para %s\n", b.getNombreCompleto());
                    }
                });
    }

    // Estadísticas avanzadas optimizadas
    public void generarEstadisticas() {
        System.out.println("\n=== ESTADÍSTICAS DEL SISTEMA ===");
        List<Beneficiario> beneficiarios = beneficiarioDAO.obtenerTodos();

        System.out.printf("Total beneficiarios: %d\nTotal autorizados: %d\n",
                beneficiarios.size(), autorizadoDAO.obtenerTodos().size());

        // Estadísticas por región
        System.out.println("\n--- Por Región ---");
        imprimirEstadisticas(beneficiarios, Beneficiario::getRegion);

        // Estadísticas por clasificación económica
        System.out.println("\n--- Por Clasificación Económica ---");
        imprimirEstadisticas(beneficiarios, Beneficiario::getClasificacionEconomica);

        // Estadísticas de elegibilidad
        long elegibles = beneficiarios.stream().filter(Beneficiario::verificarElegibilidad).count();
        System.out.printf("\n--- Elegibilidad ---\nElegibles: %d\nNo elegibles: %d\n",
                elegibles, beneficiarios.size() - elegibles);
    }

    // Metodo auxiliar para estadísticas
    private <T> void imprimirEstadisticas(List<Beneficiario> beneficiarios,
                                          java.util.function.Function<Beneficiario, T> clasificador) {
        beneficiarios.stream()
                .collect(Collectors.groupingBy(clasificador, Collectors.counting()))
                .forEach((clave, count) -> System.out.printf("%s: %d beneficiarios\n", clave, count));
    }

    @Override
    public String generarReporte() {
        List<Beneficiario> beneficiarios = beneficiarioDAO.obtenerTodos();
        long elegibles = beneficiarios.stream().filter(Beneficiario::verificarElegibilidad).count();

        return String.format("""
                === REPORTE GENERAL DEL SISTEMA ===
                Total de beneficiarios: %d
                Total de autorizados: %d
                Beneficiarios elegibles: %d
                Beneficiarios no elegibles: %d
                """,
                beneficiarios.size(),
                autorizadoDAO.obtenerTodos().size(),
                elegibles,
                beneficiarios.size() - elegibles);
    }

    // Getters optimizados
    public List<Beneficiario> getBeneficiarios() {
        return beneficiarioDAO.obtenerTodos();
    }

    public List<Autorizado> getAutorizados() {
        return autorizadoDAO.obtenerTodos();
    }

    public List<Beneficiario> getBeneficiariosPorRegion(String region) {
        return beneficiarioDAO.buscarPorRegion(region);
    }

    public void generarReporteFallecidos() {
        System.out.println("\n=== REPORTE DE BENEFICIARIOS FALLECIDOS ===");
        List<Beneficiario> fallecidos = beneficiarioDAO.obtenerFallecidos();
        System.out.printf("Total: %d\n", fallecidos.size());
        fallecidos.forEach(b -> System.out.printf("- %s (DNI: %s)\n", b.getNombreCompleto(), b.getDni()));
    }

}