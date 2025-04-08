package br.com.compass.DAO;

import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Transacao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {


    private ContaDAO contaDAO = new ContaDAO();

    public void transferir(Connection conn, Conta contaOrigem, int idContaDestino, BigDecimal valor) {
        ContaDAO contaDAO = new ContaDAO();

        try {
            conn.setAutoCommit(false);

            if (contaOrigem.getId() == idContaDestino) {
                System.out.println("Não é possível transferir para a mesma conta.");
                return;
            }

            Conta contaDestino = contaDAO.findById(conn, idContaDestino);

            if (contaDestino == null) {
                System.out.println("Conta de destino não encontrada.");
                return;
            }

            if (contaOrigem.getSaldo().compareTo(valor) < 0) {
                System.out.println("Saldo insuficiente.");
                return;
            }


            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
            contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

            contaDAO.atualizarSaldo(conn, contaOrigem);
            contaDAO.atualizarSaldo(conn, contaDestino);


            String sql = "INSERT INTO transacoes (conta_origem_id, conta_destino_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, contaOrigem.getId());
                stmt.setInt(2, idContaDestino);
                stmt.setString(3, "TRANSFERENCIA");
                stmt.setBigDecimal(4, valor);
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

                stmt.executeUpdate();
            }

            conn.commit();
            System.out.println("Transferência realizada com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}