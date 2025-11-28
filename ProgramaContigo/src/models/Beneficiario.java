package models;
import interfaces.Verificable;
import interfaces.Reportable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Beneficiario extends Persona implements Verificable, Reportable {
    private boolean tieneCertificadoDiscapacidad;
    private boolean tieneOtrosIngresos;
    private String clasificacionEconomica;
    private String region;
    private double pensionRecibida;
    private Certificado certificadoMedico;
    private LocalDate fechaInscripcion;
    private boolean[] padronesRecibidos; // Array para rastrear qué padrones ha cobrado

    public Beneficiario(String dni, String nombre, String apellido,
                        boolean tieneCertificado, boolean tieneIngresos,
                        String clasificacion, String region) {
        super(dni, nombre, apellido);
        this.tieneCertificadoDiscapacidad = tieneCertificado;
        this.tieneOtrosIngresos = tieneIngresos;
        this.clasificacionEconomica = clasificacion;
        this.region = region;
        this.pensionRecibida = 0.0;
        this.fechaInscripcion = LocalDate.now();
        this.padronesRecibidos = new boolean[6]; // 6 padrones anuales
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("Beneficiario: " + getNombreCompleto() +
                " - DNI: " + dni +
                " - Región: " + region +
                " - Fecha inscripción: " + fechaInscripcion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " - Pensión acumulada: S/" + pensionRecibida +
                " - Estado: " + (verificarElegibilidad() ? "Elegible" : "No elegible"));
    }

    @Override
    public boolean verificarElegibilidad() {
        boolean certificadoValido = tieneCertificadoDiscapacidad;

        if (certificadoMedico != null) {
            certificadoValido = certificadoMedico.verificarElegibilidad();
        }

        return certificadoValido &&
                !tieneOtrosIngresos &&
                (clasificacionEconomica.equals("pobre") ||
                        clasificacionEconomica.equals("pobre extremo"));
    }

    @Override
    public String generarReporte() {
        return "Beneficiario: " + getNombreCompleto() + "\n" +
                "DNI: " + dni + "\n" +
                "Región: " + region + "\n" +
                "Fecha de inscripción: " + fechaInscripcion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Clasificación: " + clasificacionEconomica + "\n" +
                "Tiene certificado: " + (tieneCertificadoDiscapacidad ? "Sí" : "No") + "\n" +
                "Tiene otros ingresos: " + (tieneOtrosIngresos ? "Sí" : "No") + "\n" +
                "Estado: " + (verificarElegibilidad() ? "Elegible" : "No elegible") + "\n" +
                "Pensión acumulada: S/" + pensionRecibida;
    }

    public void recibirPension(int numeroPadron) {
        if (verificarElegibilidad() && numeroPadron >= 1 && numeroPadron <= 6) {
            if (!padronesRecibidos[numeroPadron - 1]) {
                pensionRecibida += 300;
                padronesRecibidos[numeroPadron - 1] = true;
                System.out.println(nombre + " ha recibido S/300 del Padrón " + numeroPadron +
                        ". Total acumulado: S/" + pensionRecibida);
            } else {
                System.out.println("ERROR: El Padrón " + numeroPadron + " ya fue cobrado por " + nombre);
            }
        } else if (!verificarElegibilidad()) {
            System.out.println(nombre + " no cumple los requisitos para recibir la pensión.");
        } else {
            System.out.println("Número de padrón inválido: " + numeroPadron);
        }
    }

    public boolean puedeRecibirPadron(int numeroPadron, LocalDate fechaPago) {
        // Verificar si está inscrito antes de la fecha de pago
        return fechaInscripcion.isBefore(fechaPago) || fechaInscripcion.isEqual(fechaPago);
    }

    public int obtenerProximoPadronACobrar() {
        // Definir fechas de pago de 2025
        LocalDate[] fechasPago = {
                LocalDate.of(2025, 2, 21),  // Padrón I
                LocalDate.of(2025, 4, 25),  // Padrón II
                LocalDate.of(2025, 6, 20),  // Padrón III
                LocalDate.of(2025, 8, 22),  // Padrón IV
                LocalDate.of(2025, 10, 24), // Padrón V
                LocalDate.of(2025, 12, 5)   // Padrón VI
        };

        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            // Si puede cobrar este padrón y no lo ha cobrado aún
            if (puedeRecibirPadron(i + 1, fechasPago[i]) &&
                    !padronesRecibidos[i] &&
                    hoy.isBefore(fechasPago[i])) {
                return i + 1;
            }
        }
        return -1; // No hay próximo padrón disponible
    }

    public String generarCronogramaPagos() {
        StringBuilder cronograma = new StringBuilder();

        cronograma.append("=== CRONOGRAMA DE PAGOS 2025 ===\n");
        cronograma.append("Beneficiario: ").append(getNombreCompleto()).append("\n");
        cronograma.append("DNI: ").append(dni).append("\n");
        cronograma.append("Fecha de inscripción: ").append(fechaInscripcion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        cronograma.append("Fecha de generación: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");

        // Definir fechas y nombres de padrones
        String[] nombresPadrones = {
                "Padrón I (enero-febrero): 21 de febrero",
                "Padrón II (marzo-abril): 25 de abril",
                "Padrón III (mayo-junio): 20 de junio",
                "Padrón IV (julio-agosto): 22 de agosto",
                "Padrón V (septiembre-octubre): 24 de octubre",
                "Padrón VI (noviembre-diciembre): 5 de diciembre"
        };

        LocalDate[] fechasPago = {
                LocalDate.of(2025, 2, 21),  // Padrón I
                LocalDate.of(2025, 4, 25),  // Padrón II
                LocalDate.of(2025, 6, 20),  // Padrón III
                LocalDate.of(2025, 8, 22),  // Padrón IV
                LocalDate.of(2025, 10, 24), // Padrón V
                LocalDate.of(2025, 12, 5)   // Padrón VI
        };

        int proximoPadron = obtenerProximoPadronACobrar();
        boolean hayInscripcionTardia = false;

        for (int i = 0; i < 6; i++) {
            cronograma.append(nombresPadrones[i]);

            if (padronesRecibidos[i]) {
                cronograma.append(" (Ya cobrado)");
            } else if (!puedeRecibirPadron(i + 1, fechasPago[i])) {
                cronograma.append(" (No cobrado - inscripción tardía)");
                hayInscripcionTardia = true;
            } else if (i + 1 == proximoPadron) {
                cronograma.append(" (Próximo pago - Primer padrón a cobrar)");
            } else if (LocalDate.now().isAfter(fechasPago[i])) {
                cronograma.append(" (Perdido - fecha vencida)");
            } else {
                cronograma.append(" (Disponible)");
            }

            cronograma.append("\n");
        }

        if (hayInscripcionTardia) {
            cronograma.append("\nNota: Los pagos marcados como 'inscripción tardía' no se pueden cobrar debido a que ");
            cronograma.append("la inscripción se realizó después de las fechas de pago correspondientes.");
        }

        return cronograma.toString();
    }

    public void asignarCertificado(Certificado certificado) {
        if (certificado.getDniPaciente().equals(this.dni)) {
            this.certificadoMedico = certificado;
            this.tieneCertificadoDiscapacidad = true;
            System.out.println("Certificado asignado correctamente a " + getNombreCompleto());
        } else {
            System.out.println("Error: El certificado no corresponde a este beneficiario");
        }
    }

    // RF3 - Métodos de actualización de información
    public void actualizarClasificacion(String nuevaClasificacion) {
        String clasificacionAnterior = this.clasificacionEconomica;
        this.clasificacionEconomica = nuevaClasificacion;

        System.out.println("Clasificación socioeconómica actualizada:");
        System.out.println("Anterior: " + clasificacionAnterior);
        System.out.println("Nueva: " + nuevaClasificacion);

        if (!verificarElegibilidad()) {
            System.out.println("ADVERTENCIA: El beneficiario ya no cumple los requisitos de elegibilidad");
        }
    }

    public void actualizarIngresos(boolean tieneIngresos) {
        boolean estadoAnterior = this.tieneOtrosIngresos;
        this.tieneOtrosIngresos = tieneIngresos;

        System.out.println("Estado de otros ingresos actualizado:");
        System.out.println("Anterior: " + (estadoAnterior ? "Sí tiene" : "No tiene"));
        System.out.println("Nuevo: " + (tieneIngresos ? "Sí tiene" : "No tiene"));

        if (!verificarElegibilidad()) {
            System.out.println("ADVERTENCIA: El beneficiario ya no cumple los requisitos de elegibilidad");
        }
    }

    public void actualizarRegion(String nuevaRegion) {
        String regionAnterior = this.region;
        this.region = nuevaRegion;

        System.out.println("Región actualizada:");
        System.out.println("Anterior: " + regionAnterior);
        System.out.println("Nueva: " + nuevaRegion);
    }

    public void setPensionRecibida(double pensionRecibida) {
        this.pensionRecibida = pensionRecibida;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public void setPadronesRecibidos(boolean[] padronesRecibidos) {
        this.padronesRecibidos = padronesRecibidos;
    }

    // Getter adicional para el nombre (heredado de Persona)
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    // Getters
    public double getPensionRecibida() {
        return pensionRecibida;
    }

    public String getClasificacionEconomica() {
        return clasificacionEconomica;
    }

    public String getRegion() {
        return region;
    }

    public boolean isTieneCertificadoDiscapacidad() {
        return tieneCertificadoDiscapacidad;
    }

    public boolean isTieneOtrosIngresos() {
        return tieneOtrosIngresos;
    }

    public Certificado getCertificadoMedico() {
        return certificadoMedico;
    }

    public String getDni() {
        return dni;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public boolean[] getPadronesRecibidos() {
        return padronesRecibidos;
    }
    // En Beneficiario.java, asegúrate de tener este método:
    public void setTieneCertificadoDiscapacidad(boolean tieneCertificadoDiscapacidad) {
        this.tieneCertificadoDiscapacidad = tieneCertificadoDiscapacidad;
    }

    public boolean haCobradoPadron(int numeroPadron) {
        if (numeroPadron >= 1 && numeroPadron <= 6) {
            return padronesRecibidos[numeroPadron - 1];
        }
        return false;
    }
}