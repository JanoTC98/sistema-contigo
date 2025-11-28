package dao;

import config.DatabaseConnection;
import models.Certificado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificadoDAO {
    private Connection connection;

    public CertificadoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Certificado certificado) {
        String sql = "INSERT INTO certificados (codigo, dni_paciente, grado_discapacidad, vigente) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, certificado.getCodigo());
            stmt.setString(2, certificado.getDniPaciente());
            stmt.setString(3, certificado.getGradoDiscapacidad());
            stmt.setBoolean(4, certificado.isVigente());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Certificado guardado en BD: " + certificado.getCodigo());
                return true;
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Error al insertar certificado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Metodo para actualizar certificado existente
    public boolean actualizar(Certificado certificado) {
        String sql = "UPDATE certificados SET grado_discapacidad = ?, vigente = ? " +
                "WHERE codigo = ? AND dni_paciente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, certificado.getGradoDiscapacidad());
            stmt.setBoolean(2, certificado.isVigente());
            stmt.setString(3, certificado.getCodigo());
            stmt.setString(4, certificado.getDniPaciente());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar certificado: " + e.getMessage());
            return false;
        }
    }

    public Certificado buscarPorDniPaciente(String dniPaciente) {
        String sql = "SELECT * FROM certificados WHERE dni_paciente = ? AND vigente = TRUE ORDER BY fecha_emision DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dniPaciente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Certificado(
                        rs.getString("codigo"),
                        rs.getString("dni_paciente"),
                        rs.getString("grado_discapacidad"),
                        rs.getBoolean("vigente")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar certificado por DNI: " + e.getMessage());
        }

        return null;
    }

    //Buscar por código
    public Certificado buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM certificados WHERE codigo = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Certificado(
                        rs.getString("codigo"),
                        rs.getString("dni_paciente"),
                        rs.getString("grado_discapacidad"),
                        rs.getBoolean("vigente")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar certificado por código: " + e.getMessage());
        }

        return null;
    }

    //Obtener todos los certificados
    public List<Certificado> obtenerTodos() {
        List<Certificado> certificados = new ArrayList<>();
        String sql = "SELECT * FROM certificados ORDER BY fecha_emision DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                certificados.add(new Certificado(
                        rs.getString("codigo"),
                        rs.getString("dni_paciente"),
                        rs.getString("grado_discapacidad"),
                        rs.getBoolean("vigente")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener certificados: " + e.getMessage());
        }

        return certificados;
    }

    //Marcar certificado como no vigente
    public boolean marcarComoNoVigente(String codigo) {
        String sql = "UPDATE certificados SET vigente = FALSE WHERE codigo = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al marcar certificado como no vigente: " + e.getMessage());
            return false;
        }
    }
}