package br.com.compass.DAO;

import br.com.compass.Entity.Usuario;
import br.com.compass.util.HashUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    public Usuario findByCpf(String cpf) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = db.DB.getConnection();
            st = conn.prepareStatement("SELECT * FROM Usuario WHERE cpf = ?");
            st.setString(1, cpf);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("senha_hash"),
                        rs.getString("telefone"),
                        rs.getDate("data_nascimento").toLocalDate()
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            db.DB.closeResultSet(rs);
            db.DB.closeStatement(st);
            db.DB.closeConnection();
        }
    }
    public void create(Usuario usuario) {
        String sql = "INSERT INTO Usuario (nome, cpf, senha_hash, telefone, data_nascimento) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.DB.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, usuario.getNome());
            st.setString(2, usuario.getCpf());
            st.setString(3, usuario.getSenhaHash());
            st.setString(4, usuario.getTelefone());
            st.setDate(5, Date.valueOf(usuario.getDataNascimento()));

            st.executeUpdate();
            System.out.println("Conta criada com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Usuario login(String cpf, String senha) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = db.DB.getConnection();
            st = conn.prepareStatement("SELECT * FROM Usuario WHERE cpf = ?");
            st.setString(1, cpf);
            rs = st.executeQuery();

            if (rs.next()) {
                String senhaHash = rs.getString("senha_hash");

                if (HashUtil.verifyPassword(senha, senhaHash)) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            senhaHash,
                            rs.getString("telefone"),
                            rs.getDate("data_nascimento").toLocalDate()
                    );
                }
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            db.DB.closeResultSet(rs);
            db.DB.closeStatement(st);
            db.DB.closeConnection();
        }
    }
}

