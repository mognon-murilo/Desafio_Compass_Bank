package br.com.compass.DAO;

import br.com.compass.Entity.Usuario;

import java.sql.*;

public class UsuarioDAO {

    public Usuario findByCpf(Connection conn, String cpf) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
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
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    public void create(Connection conn, Usuario usuario) {
        String sql = "INSERT INTO Usuario (nome, cpf, senha_hash, telefone, data_nascimento) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, usuario.getNome());
            st.setString(2, usuario.getCpf());
            st.setString(3, usuario.getSenhaHash());
            st.setString(4, usuario.getTelefone());
            st.setDate(5, Date.valueOf(usuario.getDataNascimento()));

            st.executeUpdate();

            rs = st.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }


}


