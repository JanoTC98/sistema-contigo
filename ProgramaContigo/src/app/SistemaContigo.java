package app;
import java.util.Scanner;
import java.time.LocalDate;
import models.*;
import services.SistemaGestion;

/**
 * Clase principal optimizada del Sistema CONTIGO con cronograma de pagos
 */
public class SistemaContigo {
    private static final Scanner scanner = new Scanner(System.in);
    private static final SistemaGestion sistema = new SistemaGestion();
    private static final String[] CLASIFICACIONES = {"pobre extremo", "pobre", "no pobre"};
    private static final String[] GRADOS_DISCAPACIDAD = {"severa", "moderada", "leve"};
    private static final String[] NOMBRES_PADRONES = {
            "Padrón I (enero-febrero): 21 de febrero",
            "Padrón II (marzo-abril): 25 de abril",
            "Padrón III (mayo-junio): 20 de junio",
            "Padrón IV (julio-agosto): 22 de agosto",
            "Padrón V (septiembre-octubre): 24 de octubre",
            "Padrón VI (noviembre-diciembre): 5 de diciembre"
    };

    public static void main(String[] args) {
        System.out.println("=== SISTEMA CONTIGO - GESTIÓN DE BENEFICIARIOS ===");

        int opcion;
        do {
            mostrarMenu();
            opcion = scanner.nextInt();
            scanner.nextLine();

            ejecutarOpcion(opcion);
        } while (opcion != 0);

        scanner.close();
    }

    private static void mostrarMenu() {
        System.out.println("""
            
            === MENÚ PRINCIPAL ===
            1. Registrar Beneficiario
            2. Registrar Autorizado
            3. Asignar Certificado Médico
            4. Verificar Elegibilidad
            5. Procesar Padrón de Pagos
            6. Generar Cronograma de Pago
            7. Generar Reportes
            8. Ver Alertas
            9. Actualizar Información
            10. Eliminar un beneficiario por fallecimiento
            0. Salir""");
        System.out.print("Seleccione una opción: ");
    }

    private static void ejecutarOpcion(int opcion) {
        switch (opcion) {
            case 1 -> registrarBeneficiario();
            case 2 -> registrarAutorizado();
            case 3 -> asignarCertificado();
            case 4 -> verificarElegibilidad();
            case 5 -> procesarPadronPagos();
            case 6 -> generarCronogramaPago();
            case 7 -> generarReportes();
            case 8 -> mostrarAlertas();
            case 9 -> actualizarInformacion();
            case 10 -> eliminarPorFallecimiento();
            case 0 -> System.out.println("Saliendo del sistema...");
            default -> System.out.println("Opción inválida");
        }
    }

    private static void registrarBeneficiario() {
        System.out.println("\n=== REGISTRO DE BENEFICIARIO ===");

        String dni = leerString("DNI: ");
        if (sistema.buscarBeneficiario(dni) != null) {
            System.out.println("ERROR: Ya existe un beneficiario con ese DNI");
            return;
        }

        String nombre = leerString("Nombre: ");
        String apellido = leerString("Apellido: ");
        boolean tieneCertificado = leerBoolean("¿Tiene certificado de discapacidad? (s/n): ");
        boolean tieneIngresos = leerBoolean("¿Tiene otros ingresos? (s/n): ");
        String clasificacion = seleccionarOpcion("Clasificación socioeconómica:", CLASIFICACIONES);
        String region = leerString("Región: ");

        Beneficiario beneficiario = new Beneficiario(dni, nombre, apellido,
                tieneCertificado, tieneIngresos, clasificacion, region);

        sistema.registrarBeneficiario(beneficiario);
        System.out.println("Beneficiario registrado exitosamente");
        System.out.println("Fecha de inscripción: " + LocalDate.now());
    }

    private static void registrarAutorizado() {
        System.out.println("\n=== REGISTRO DE AUTORIZADO ===");

        String dniAutorizado = leerString("DNI del autorizado: ");
        String nombre = leerString("Nombre: ");
        String apellido = leerString("Apellido: ");
        String dniBeneficiario = leerString("DNI del beneficiario: ");

        if (sistema.buscarBeneficiario(dniBeneficiario) == null) {
            System.out.println("ERROR: No existe beneficiario con ese DNI");
            return;
        }

        String parentesco = leerString("Parentesco: ");

        Autorizado autorizado = new Autorizado(dniAutorizado, nombre, apellido,
                dniBeneficiario, parentesco);

        sistema.registrarAutorizado(autorizado);
        System.out.println("Autorizado registrado exitosamente");
    }

    private static void asignarCertificado() {
        System.out.println("\n=== ASIGNAR CERTIFICADO MÉDICO ===");

        String codigo = leerString("Código del certificado: ");
        String dniPaciente = leerString("DNI del paciente: ");

        Beneficiario beneficiario = sistema.buscarBeneficiario(dniPaciente);
        if (beneficiario == null) {
            System.out.println("ERROR: No existe beneficiario con ese DNI");
            return;
        }

        if (sistema.buscarCertificado(dniPaciente) != null &&
                !leerBoolean("ADVERTENCIA: Ya existe un certificado. ¿Desea reemplazarlo? (s/n): ")) {
            System.out.println("Operación cancelada");
            return;
        }

        String grado = seleccionarOpcion("Grado de discapacidad:", GRADOS_DISCAPACIDAD);
        boolean vigente = leerBoolean("¿Está vigente? (s/n): ");

        Certificado certificado = new Certificado(codigo, dniPaciente, grado, vigente);
        beneficiario.asignarCertificado(certificado);

        if (sistema.registrarCertificado(certificado)) {
            if (!beneficiario.isTieneCertificadoDiscapacidad()) {
                beneficiario.setTieneCertificadoDiscapacidad(true);
                sistema.actualizarBeneficiario(beneficiario);
            }

            System.out.printf("""
                Certificado asignado exitosamente
                Código: %s
                Paciente: %s
                Grado: %s
                Vigente: %s
                %s
                """, codigo, beneficiario.getNombreCompleto(), grado,
                    vigente ? "Sí" : "No",
                    certificado.verificarElegibilidad() ?
                            "Este certificado CALIFICA para el programa" :
                            "Este certificado NO califica para el programa");
        } else {
            System.out.println("ERROR: Falló al guardar en base de datos");
        }
    }

    private static void verificarElegibilidad() {
        System.out.println("\n=== VERIFICACIÓN DE ELEGIBILIDAD ===");

        Beneficiario beneficiario = buscarBeneficiarioParaOperacion();
        if (beneficiario == null) return;

        System.out.println("\nRevisando eligibilidad de " + beneficiario.getNombreCompleto());

        if (beneficiario.verificarElegibilidad()) {
            System.out.println("""
                ✓ El beneficiario CUMPLE con todos los requisitos
                  - Certificado de discapacidad severa: OK
                  - Sin otros ingresos: OK
                  - Clasificación socioeconómica: OK""");
        } else {
            System.out.println("✗ El beneficiario NO cumple con los requisitos");
        }
    }

    private static void procesarPadronPagos() {
        System.out.println("\n=== PROCESAMIENTO DE PADRÓN INDIVIDUAL ===");

        Beneficiario beneficiario = buscarBeneficiarioParaOperacion();
        if (beneficiario == null) return;

        if (!beneficiario.verificarElegibilidad()) {
            System.out.println("ERROR: El beneficiario no cumple los requisitos de elegibilidad");
            return;
        }

        System.out.printf("Beneficiario: %s\nDNI: %s\n",
                beneficiario.getNombreCompleto(), beneficiario.getDni());

        int proximoPadron = beneficiario.obtenerProximoPadronACobrar();

        if (proximoPadron == -1) {
            mostrarEstadoPadrones(beneficiario);
            return;
        }

        System.out.printf("""
            
            Padrón disponible para cobrar:
            %s
            Monto: S/300
            """, NOMBRES_PADRONES[proximoPadron - 1]);

        if (leerBoolean("\n¿Procesar pago? (s/n): ")) {
            beneficiario.recibirPension(proximoPadron);
            boolean exito = sistema.procesarPago(beneficiario.getDni(), proximoPadron);

            System.out.println(exito ?
                    "Pago procesado exitosamente y guardado en base de datos." :
                    "ADVERTENCIA: Pago procesado en memoria pero falló al guardar en BD.");
        } else {
            System.out.println("Pago cancelado.");
        }
    }

    private static void mostrarEstadoPadrones(Beneficiario beneficiario) {
        System.out.println("No hay padrones disponibles para cobrar.\n\nEstado de padrones:");
        String[] estadoPadrones = {
                "Padrón I (enero-febrero)", "Padrón II (marzo-abril)", "Padrón III (mayo-junio)",
                "Padrón IV (julio-agosto)", "Padrón V (septiembre-octubre)", "Padrón VI (noviembre-diciembre)"
        };

        for (int i = 0; i < 6; i++) {
            System.out.printf("%s: %s\n", estadoPadrones[i],
                    beneficiario.haCobradoPadron(i + 1) ? "Ya cobrado" : "No disponible");
        }
    }

    private static void generarCronogramaPago() {
        System.out.println("\n=== GENERAR CRONOGRAMA DE PAGO ===");

        Beneficiario beneficiario = buscarBeneficiarioParaOperacion();
        if (beneficiario != null) {
            System.out.println("\n" + beneficiario.generarCronogramaPagos());
        }
    }

    private static void generarReportes() {
        System.out.println("""
            
            === GENERACIÓN DE REPORTES ===
            1. Reporte general de beneficiarios
            2. Reporte de autorizados
            3. Reporte por región
            4. Reporte de no elegibles
            5. Reporte de beneficiarios fallecidos""");
        System.out.print("Seleccione tipo de reporte: ");

        int tipo = scanner.nextInt();
        scanner.nextLine();

        switch (tipo) {
            case 1 -> sistema.generarReporteBeneficiarios();
            case 2 -> sistema.generarReporteAutorizados();
            case 3 -> sistema.generarReportePorRegion(leerString("Región: "));
            case 4 -> sistema.generarReporteNoElegibles();
            case 5 -> sistema.generarReporteFallecidos();
            default -> System.out.println("Opción inválida");
        }
    }

    private static void mostrarAlertas() {
        System.out.println("\n=== ALERTAS DEL SISTEMA ===");
        sistema.generarAlertas();
    }

    private static void actualizarInformacion() {
        System.out.println("\n=== ACTUALIZAR INFORMACIÓN ===");

        Beneficiario beneficiario = buscarBeneficiarioParaOperacion();
        if (beneficiario == null) return;

        System.out.println("""
        ¿Qué desea actualizar?
        1. Clasificación socioeconómica
        2. Estado de otros ingresos
        3. Región""");
        System.out.print("Seleccione: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        boolean actualizado = false;

        switch (opcion) {
            case 1 -> {
                String nueva = seleccionarOpcion("Nueva clasificación:", CLASIFICACIONES);
                beneficiario.actualizarClasificacion(nueva);
                actualizado = true;
            }
            case 2 -> {
                boolean ingresos = leerBoolean("¿Tiene otros ingresos? (s/n): ");
                beneficiario.actualizarIngresos(ingresos);
                actualizado = true;
            }
            case 3 -> {
                String region = leerString("Nueva región: ");
                beneficiario.actualizarRegion(region);
                actualizado = true;
            }
            default -> {
                System.out.println("Opción inválida");
                return;
            }
        }

        // Guardar los cambios en la base de datos
        if (actualizado) {
            boolean exitoGuardado = sistema.actualizarBeneficiario(beneficiario);

            if (exitoGuardado) {
                System.out.println("Los cambios se han guardado exitosamente en la base de datos");
            } else {
                System.out.println("ERROR: Los cambios se realizaron en memoria pero no se pudieron guardar en la base de datos");
            }
        }
    }

    // Métodos auxiliares para reducir duplicación
    private static String leerString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static boolean leerBoolean(String prompt) {
        return leerString(prompt).toLowerCase().startsWith("s");
    }

    private static void eliminarPorFallecimiento() {
        System.out.println("\n=== ELIMINAR POR FALLECIMIENTO ===");

        String dni = leerString("DNI del beneficiario fallecido: ");

        if (sistema.eliminarPorFallecimiento(dni)) {
            System.out.println("Beneficiario marcado como fallecido exitosamente");
        } else {
            System.out.println("ERROR: No se pudo procesar la eliminación");
        }
    }

    private static String seleccionarOpcion(String titulo, String[] opciones) {
        System.out.println(titulo);
        for (int i = 0; i < opciones.length; i++) {
            System.out.printf("%d. %s\n", i + 1,
                    opciones[i].substring(0, 1).toUpperCase() + opciones[i].substring(1));
        }
        System.out.print("Seleccione (1-" + opciones.length + "): ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        return opcion > 0 && opcion <= opciones.length ?
                opciones[opcion - 1] : opciones[opciones.length - 1];
    }

    private static Beneficiario buscarBeneficiarioParaOperacion() {
        String dni = leerString("DNI del beneficiario: ");
        Beneficiario beneficiario = sistema.buscarBeneficiario(dni);

        if (beneficiario == null) {
            System.out.println("ERROR: No existe beneficiario con ese DNI");
        }

        return beneficiario;
    }
}