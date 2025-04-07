package br.com.compass.DAO;

import br.com.compass.Entity.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    public List<Usuario> findAll(){
        List<Usuario> lista = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = db.DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM Usuario");

            while(rs.next()){
                Usuario usuario = new Usuario(
                        rs.getInt("Id"),
                        rs.getString("Nome"),
                        rs.getString("Cpf"),
                        rs.getString("Senha"),
                        rs.getString("telefone"),
                        rs.getDate("DataNascimento").toLocalDate()

                );
                lista.add(usuario);
            }
            return lista;
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            db.DB.closeResultSet(rs);
            db.DB.closeStatement(st);
            db.DB.closeConnection();
        }
    }
}
