package dao;

import config.DatabaseConnection;
import models.Beneficiario;
import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la gestión de beneficiarios en base de datos
 */
public class BeneficiarioDAO {
    private Connection connection;

    public BeneficiarioDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Beneficiario beneficiario) {
        String sql = "INSERT INTO beneficiarios (dni, nombre, apellido, tiene_certificado_discapacidad, " +
                "tiene_otros_ingresos, clasificacion_economica, region, pension_recibida, fecha_inscripcion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, beneficiario.getDni());
            stmt.setString(2, beneficiario.getNombre());
            stmt.setString(3, beneficiario.getApellido());
            stmt.setBoolean(4, beneficiario.isTieneCertificadoDiscapacidad());
            stmt.setBoolean(5, beneficiario.isTieneOtrosIngresos());
            stmt.setString(6, beneficiario.getClasificacionEconomica());
            stmt.setString(7, beneficiario.getRegion());
            stmt.setDouble(8, beneficiario.getPensionRecibida());
            stmt.setDate(9, Date.valueOf(beneficiario.getFechaInscripcion()));

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar beneficiario: " + e.getMessage());
            return false;
        }
    }

    public Beneficiario buscarPorDni(String dni) {
        String sql = "SELECT * FROM beneficiarios WHERE dni = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Beneficiario beneficiario = new Beneficiario(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getBoolean("tiene_certificado_discapacidad"),
                        rs.getBoolean("tiene_otros_ingresos"),
                        rs.getString("clasificacion_economica"),
                        rs.getString("region")
                );

                // Establecer valores adicionales
                beneficiario.setPensionRecibida(rs.getDouble("pension_recibida"));
                beneficiario.setFechaInscripcion(rs.getDate("fecha_inscripcion").toLocalDate());

                // Establecer padrones cobrados
                boolean[] padrones = new boolean[6];
                padrones[0] = rs.getBoolean("padron_1_cobrado");
                padrones[1] = rs.getBoolean("padron_2_cobrado");
                padrones[2] = rs.getBoolean("padron_3_cobrado");
                padrones[3] = rs.getBoolean("padron_4_cobrado");
                padrones[4] = rs.getBoolean("padron_5_cobrado");
                padrones[5] = rs.getBoolean("padron_6_cobrado");
                beneficiario.setPadronesRecibidos(padrones);

                return beneficiario;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar beneficiario: " + e.getMessage());
        }

        return null;
    }



    public List<Beneficiario> obtenerTodos() {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        String sql = "SELECT * FROM v_beneficiarios_activos";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Beneficiario beneficiario = new Beneficiario(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getBoolean("tiene_certificado_discapacidad"),
                        rs.getBoolean("tiene_otros_ingresos"),
                        rs.getString("clasificacion_economica"),
                        rs.getString("region")
                );

                beneficiario.setPensionRecibida(rs.getDouble("pension_recibida"));
                beneficiario.setFechaInscripcion(rs.getDate("fecha_inscripcion").toLocalDate());

                boolean[] padrones = new boolean[6];
                padrones[0] = rs.getBoolean("padron_1_cobrado");
                padrones[1] = rs.getBoolean("padron_2_cobrado");
                padrones[2] = rs.getBoolean("padron_3_cobrado");
                padrones[3] = rs.getBoolean("padron_4_cobrado");
                padrones[4] = rs.getBoolean("padron_5_cobrado");
                padrones[5] = rs.getBoolean("padron_6_cobrado");
                beneficiario.setPadronesRecibidos(padrones);

                beneficiarios.add(beneficiario);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener beneficiarios: " + e.getMessage());
        }

        return beneficiarios;
    }

    public boolean actualizar(Beneficiario beneficiario) {
        String sql = "UPDATE beneficiarios SET nombre = ?, apellido = ?, " +
                "tiene_certificado_discapacidad = ?, tiene_otros_ingresos = ?, " +
                "clasificacion_economica = ?, region = ?, pension_recibida = ?, " +
                "padron_1_cobrado = ?, padron_2_cobrado = ?, padron_3_cobrado = ?, " +
                "padron_4_cobrado = ?, padron_5_cobrado = ?, padron_6_cobrado = ? " +
                "WHERE dni = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, beneficiario.getNombre());
            stmt.setString(2, beneficiario.getApellido());
            stmt.setBoolean(3, beneficiario.isTieneCertificadoDiscapacidad());
            stmt.setBoolean(4, beneficiario.isTieneOtrosIngresos());
            stmt.setString(5, beneficiario.getClasificacionEconomica());
            stmt.setString(6, beneficiario.getRegion());
            stmt.setDouble(7, beneficiario.getPensionRecibida());

            boolean[] padrones = beneficiario.getPadronesRecibidos();
            for (int i = 0; i < 6; i++) {
                stmt.setBoolean(8 + i, padrones[i]);
            }
            stmt.setString(14, beneficiario.getDni());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar beneficiario: " + e.getMessage());
            return false;
        }
    }

    public List<Beneficiario> buscarPorRegion(String region) {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        String sql = "SELECT * FROM beneficiarios WHERE region = ? ORDER BY apellido, nombre";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, region);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Beneficiario beneficiario = new Beneficiario(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getBoolean("tiene_certificado_discapacidad"),
                        rs.getBoolean("tiene_otros_ingresos"),
                        rs.getString("clasificacion_economica"),
                        rs.getString("region")
                );

                beneficiario.setPensionRecibida(rs.getDouble("pension_recibida"));
                beneficiario.setFechaInscripcion(rs.getDate("fecha_inscripcion").toLocalDate());

                beneficiarios.add(beneficiario);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar por región: " + e.getMessage());
        }

        return beneficiarios;
    }


    public boolean registrarPago(String dni, int numeroPadron) {
        String sql = "INSERT INTO pagos_realizados (dni_beneficiario, numero_padron, monto) VALUES (?, ?, 300.00)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, dni);
            stmt.setInt(2, numeroPadron);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar pago: " + e.getMessage());
            return false;
        }
    }

    public boolean procesarPago(String dni, int numeroPadron) {
        String sql = "CALL sp_procesar_pago(?, ?, @resultado)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dni);
            stmt.setInt(2, numeroPadron);
            stmt.execute();

            Statement stmt2 = connection.createStatement();
            ResultSet rs = stmt2.executeQuery("SELECT @resultado");

            if (rs.next()) {
                String resultado = rs.getString(1);
                System.out.println("Resultado del pago: " + resultado);
                return resultado.contains("EXITOSAMENTE");
            }
        } catch (SQLException e) {
            System.err.println("Error al procesar pago: " + e.getMessage());
            // Si falla el procedimiento, usar el método directo
            return registrarPago(dni, numeroPadron);
        }

        return false;
    }


    public boolean marcarComoFallecido(String dni) {
        String sql = "UPDATE beneficiarios SET activo = false WHERE dni = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dni);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Beneficiario> obtenerFallecidos() {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        String sql = "SELECT * FROM v_beneficiarios_inactivos";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Beneficiario beneficiario = new Beneficiario(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getBoolean("tiene_certificado_discapacidad"),
                        rs.getBoolean("tiene_otros_ingresos"),
                        rs.getString("clasificacion_economica"),
                        rs.getString("region")
                );

                beneficiario.setPensionRecibida(rs.getDouble("pension_recibida"));
                beneficiario.setFechaInscripcion(rs.getDate("fecha_inscripcion").toLocalDate());

                beneficiarios.add(beneficiario);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener beneficiarios fallecidos: " + e.getMessage());
        }

        return beneficiarios;
    }

}