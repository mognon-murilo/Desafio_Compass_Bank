package br.com.compass.DAO;

import br.com.compass.Entity.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;

public class UsuarioDAO {
    public void inicializarGerente(Connection conn) {
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            // Verifica se já existe algum gerente
            st = conn.prepareStatement("SELECT * FROM usuario WHERE tipo = ?");
            st.setString(1, "GERENTE");
            rs = st.executeQuery();

            if (!rs.next()) { // Nenhum gerente encontrado
                System.out.println("Nenhum gerente encontrado. Criando gerente padrão...");


                String nome = "Gerente Principal";
                String cpf = "00000000000";  // CPF padrão (único)
                String telefone = "0000000000";
                LocalDate dataNascimento = LocalDate.of(1970, 1, 1);
                String senha = "gerente123";
                String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
                String tipo = "GERENTE";

                Usuario gerente = new Usuario(nome, cpf, senhaHash, telefone, dataNascimento, tipo);

                // Insere o gerente no banco
                create(conn, gerente);
                System.out.println("Gerente padrão criado com sucesso.");
            }
        } catch (SQLException e) {
            throw new dbException(e.getMessage());
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

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
                        rs.getDate("data_nascimento").toLocalDate(),
                        rs.getString("tipo")
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
        String sql = "INSERT INTO Usuario (nome, cpf, senha_hash, telefone, data_nascimento,tipo) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, usuario.getNome());
            st.setString(2, usuario.getCpf());
            st.setString(3, usuario.getSenhaHash());
            st.setString(4, usuario.getTelefone());
            st.setDate(5, Date.valueOf(usuario.getDataNascimento()));
                    st.setString(6, usuario.getTipo());


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


