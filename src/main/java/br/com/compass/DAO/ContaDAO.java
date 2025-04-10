package br.com.compass.DAO;

import br.com.compass.Entity.Conta;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContaDAO {

    private Conta mapResultSetToConta(ResultSet rs) throws SQLException {
        Conta conta = new Conta();
        conta.setId(rs.getInt("Id"));
        conta.setNome(rs.getString("Nome"));
        conta.setCpf(rs.getString("Cpf"));
        conta.setTelefone(rs.getString("Telefone"));
        conta.setDataNascimento(rs.getString("data_nascimento"));
        conta.setTipoConta(rs.getString("tipo_conta"));
        conta.setSenhaHash(rs.getString("senha_hash"));
        conta.setSaldo(rs.getBigDecimal("saldo"));
        conta.setBloqueada(rs.getBoolean("bloqueada"));
        return conta;
    }

    public Conta findById(Connection conn, int id) {
        String sql = "SELECT * FROM Conta WHERE Id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? mapResultSetToConta(rs) : null;
            }
        } catch (SQLException e) {
            throw new dbException("Erro ao buscar conta por ID: " + e.getMessage());
        }
    }

    public Conta findById(int id) {
        try (Connection conn = DB.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Conta findByCpf(Connection conn, String cpf) {
        String sql = "SELECT * FROM conta WHERE cpf = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, cpf);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? mapResultSetToConta(rs) : null;
            }
        } catch (SQLException e) {
            throw new dbException("Erro ao buscar conta por CPF: " + e.getMessage());
        }
    }

    public List<Conta> buscarContasBloqueadas(Connection conn) {
        List<Conta> contas = new ArrayList<>();
        String sql = "SELECT * FROM conta WHERE bloqueada = true";
        try (PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                contas.add(mapResultSetToConta(rs));
            }
        } catch (SQLException e) {
            throw new dbException("Erro ao buscar contas bloqueadas: " + e.getMessage());
        }
        return contas;
    }

    public void desbloquearConta(Connection conn, String cpf) {
        String sql = "UPDATE conta SET bloqueada = false WHERE cpf = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, cpf);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new dbException("Erro ao desbloquear conta: " + e.getMessage());
        }
    }

    public void inserir(Connection conn, Conta conta) {
        String sql = """
            INSERT INTO conta 
            (nome, cpf, telefone, data_nascimento, tipo_conta, senha_hash, saldo, bloqueada)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, conta.getNome());
            st.setString(2, conta.getCpf());
            st.setString(3, conta.getTelefone());
            st.setDate(4, Date.valueOf(conta.getDataNascimento()));
            st.setString(5, conta.getTipoConta());
            st.setString(6, conta.getSenhaHash());
            st.setBigDecimal(7, conta.getSaldo());
            st.setBoolean(8, conta.isBloqueada());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new dbException("Erro ao inserir conta: " + e.getMessage());
        }
    }

    public void atualizarSaldo(Connection conn, Conta conta) {
        String sql = "UPDATE conta SET saldo = ? WHERE cpf = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setBigDecimal(1, conta.getSaldo());
            st.setString(2, conta.getCpf());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new dbException("Erro ao atualizar saldo: " + e.getMessage());
        }
    }

    public void depositar(Connection conn, Conta conta, BigDecimal valor) {
        try {
            conta.depositar(valor);
            atualizarSaldo(conn, conta);
        } catch (IllegalArgumentException e) {
            System.out.println("Erro no depósito: " + e.getMessage());
        }
    }
    public void bloquearConta(Connection conn, String cpf) {
        String sql = "UPDATE conta SET bloqueada = true WHERE cpf = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new dbException(e.getMessage());
        }
    }

    public boolean sacar(Connection conn, Conta conta, BigDecimal valor) {
        try {
            conta.sacar(valor);
            atualizarSaldo(conn, conta);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Erro no saque: " + e.getMessage());
            return false;
        }
    }
}


