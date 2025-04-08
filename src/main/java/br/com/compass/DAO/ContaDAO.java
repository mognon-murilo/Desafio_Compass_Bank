package br.com.compass.DAO;

import br.com.compass.Entity.Conta;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class ContaDAO {
    public Conta findById(Connection conn, int id) {
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT * FROM Conta WHERE Id = ?");
            st.setInt(1, id);
            rs = st.executeQuery();

            if (rs.next()) {
                Conta conta = new Conta();
                conta.setId(rs.getInt("Id"));
                conta.setNome(rs.getString("Nome"));
                conta.setCpf(rs.getString("Cpf"));
                conta.setTelefone(rs.getString("Telefone"));
                conta.setDataNascimento(rs.getString("data_nascimento"));
                conta.setTipoConta(rs.getString("tipo_conta"));
                conta.setSenhaHash(rs.getString("Senha_Hash"));
                conta.setSaldo(rs.getBigDecimal("saldo"));
                conta.setBloqueada(rs.getBoolean("bloqueada"));
                conta.setTentativasLogin(rs.getInt("tentativas_login"));
                return conta;
            }
            return null;

        } catch (SQLException e) {
            throw new dbException(e.getMessage());
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }
    public Conta findById(int id) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            return findById(conn, id);
        } finally {
            DB.closeConnection();
        }
    }



    public Conta findByCpf(Connection conn, String cpf) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT * FROM conta WHERE cpf = ?");
            st.setString(1, cpf);
            rs = st.executeQuery();

            if (rs.next()) {
                Conta conta = new Conta(
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("telefone"),
                        rs.getString("data_nascimento"),
                        rs.getString("tipo_conta"),
                        rs.getString("senha_hash")
                );
                conta.setId(rs.getInt("id"));
                conta.setSaldo(rs.getBigDecimal("saldo"));
                conta.setBloqueada(rs.getBoolean("bloqueada"));
                conta.setTentativasLogin(rs.getInt("tentativas_login"));
                return conta;
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

    public void inserir(Connection conn, Conta conta) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO conta (nome, cpf, telefone, data_nascimento, tipo_conta, senha_hash, saldo, bloqueada, tentativas_login) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

            st.setString(1, conta.getNome());
            st.setString(2, conta.getCpf());
            st.setString(3, conta.getTelefone());
            st.setString(4, conta.getDataNascimento());
            st.setString(5, conta.getTipoConta());
            st.setString(6, conta.getSenhaHash());
            st.setBigDecimal(7, conta.getSaldo());
            st.setBoolean(8, conta.isBloqueada());
            st.setInt(9, conta.getTentativasLogin());

            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB.closeStatement(st);
        }
    }

    public void atualizarSaldo(Connection conn, Conta conta) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement("UPDATE conta SET saldo = ? WHERE cpf = ?");
            st.setBigDecimal(1, conta.getSaldo());
            st.setString(2, conta.getCpf());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB.closeStatement(st);
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

