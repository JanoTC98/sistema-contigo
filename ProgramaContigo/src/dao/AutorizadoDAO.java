package dao;

import config.DatabaseConnection;
import models.Autorizado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutorizadoDAO {
    private Connection connection;

    public AutorizadoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean insertar(Autorizado autorizado) {
        // Validar usando el metodo de la propia clase Autorizado
        if (!autorizado.esParentescoValido()) {
            System.err.println("Error: Parentesco inválido - " + autorizado.getParentesco());
            return false;
        }

        String sql = "INSERT INTO autorizados (dni, nombre, apellido, dni_beneficiario, parentesco, autorizado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, autorizado.getDni());
            stmt.setString(2, autorizado.getNombre());
            stmt.setString(3, autorizado.getApellido());
            stmt.setString(4, autorizado.getDniBeneficiario());
            stmt.setString(5, autorizado.getParentesco());
            stmt.setBoolean(6, autorizado.isAutorizado());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar autorizado: " + e.getMessage());
            return false;
        }
    }

    public Autorizado buscarPorDni(String dni) {
        String sql = "SELECT * FROM autorizados WHERE dni = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dni);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Autorizado autorizado = new Autorizado(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni_beneficiario"),
                        rs.getString("parentesco")
                );
                autorizado.setAutorizado(rs.getBoolean("autorizado"));
                return autorizado;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar autorizado: " + e.getMessage());
        }

        return null;
    }

    public List<Autorizado> obtenerTodos() {
        List<Autorizado> autorizados = new ArrayList<>();
        String sql = "SELECT * FROM autorizados ORDER BY apellido, nombre";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Autorizado autorizado = new Autorizado(
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni_beneficiario"),
                        rs.getString("parentesco")
                );
                autorizado.setAutorizado(rs.getBoolean("autorizado"));
                autorizados.add(autorizado);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener autorizados: " + e.getMessage());
        }

        return autorizados;
    }

}