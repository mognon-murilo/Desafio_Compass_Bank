package br.com.compass.DAO;

import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Transacao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    private ContaDAO contaDAO = new ContaDAO();

    // Método de transferência
    public void transferir(Connection conn, Conta contaOrigem, int idContaDestino, BigDecimal valor) throws SQLException {
        try {
            conn.setAutoCommit(false); // Inicia transação manual

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

            // Atualiza saldo em memória
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
            contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

            // Atualiza saldo no banco usando DAO
            contaDAO.atualizarSaldo(conn, contaOrigem);
            contaDAO.atualizarSaldo(conn, contaDestino);

            // Registra transação
            String sql = "INSERT INTO transacoes (conta_origem_id, conta_destino_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, contaOrigem.getId());
                stmt.setInt(2, idContaDestino);
                stmt.setString(3, "TRANSFERENCIA");
                stmt.setBigDecimal(4, valor);
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();
            }

            conn.commit(); // Finaliza transação
            System.out.println("Transferência realizada com sucesso.");

        } catch (SQLException e) {
            conn.rollback(); // Desfaz alterações no banco
            throw e; // Propaga o erro
        }
    }
    public void solicitarEstorno(Connection conn, int idTransacao, String cpfSolicitante, String motivoEstorno) throws SQLException {
        // Verifica se transação existe
        Transacao transacao = findById(conn, idTransacao);
        if (transacao == null) {
            System.out.println("Transação não encontrada.");
            return;
        }

        // Verifica se já existe estorno aprovado ou pendente
        if ("APROVADO".equalsIgnoreCase(transacao.getStatusEstorno())) {
            System.out.println("Essa transação já teve estorno aprovado e não pode ser solicitado novamente.");
            return;
        }
        if ("PENDENTE".equalsIgnoreCase(transacao.getStatusEstorno())) {
            System.out.println("Já existe um estorno pendente para essa transação.");
            return;
        }

        String sql = "UPDATE transacoes SET status_estorno = 'PENDENTE', motivo_solicitacao_estorno = ?, data_solicitacao_estorno = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, motivoEstorno);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idTransacao);
            stmt.executeUpdate();
            System.out.println("Solicitação de estorno realizada com sucesso.");
        }
    }

    // Aprovar Estorno
    public void aprovarEstorno(Connection conn, int idTransacao) throws SQLException {
        // Buscar a transação completa
        Transacao transacao = findById(conn, idTransacao);

        if (transacao == null) {
            System.out.println("Transação não encontrada.");
            return;
        }

        // Atualizar status da transação
        String sqlUpdateTransacao = "UPDATE transacoes SET status_estorno = 'APROVADO', data_resposta_estorno = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateTransacao)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, idTransacao);
            stmt.executeUpdate();
        }

        // Devolver o saldo para a conta de origem
        Conta contaOrigem = transacao.getContaOrigem();
        contaOrigem.setSaldo(contaOrigem.getSaldo().add(transacao.getValor()));
        contaDAO.atualizarSaldo(conn, contaOrigem);

        System.out.println("Estorno aprovado e saldo devolvido com sucesso.");
    }

    // Recusar Estorno
    public void recusarEstorno(Connection conn, int idTransacao, String motivoRecusa) throws SQLException {
        String sql = "UPDATE transacoes SET status_estorno = 'RECUSADO', motivo_recusa_estorno = ?, data_resposta_estorno = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, motivoRecusa);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idTransacao);
            stmt.executeUpdate();
        }

        System.out.println("Estorno recusado com sucesso.");
    }
    public List<Transacao> buscarExtratoPorConta(Connection conn, Conta conta) throws SQLException {
        List<Transacao> lista = new ArrayList<>();

        String sql = """
        SELECT * FROM transacoes 
        WHERE conta_origem_id = ? OR conta_destino_id = ? 
        ORDER BY data_hora DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conta.getId());
            stmt.setInt(2, conta.getId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(instantiateTransacao(conn, rs));
            }
        }
        return lista;
    }


    // Buscar transação por ID
    public Transacao findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM transacoes WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return instantiateTransacao(conn, rs);
            }
        }
        return null;
    }
    public void exportarExtratoCSV(List<Transacao> transacoes, String diretorio) {
        File pasta = new File(diretorio);
        if (!pasta.exists()) {
            pasta.mkdirs(); // cria pasta se não existir
        }

        int numeroArquivo = 1;
        File arquivoCSV;

        // Procura um nome disponível (extrato1, extrato2, extrato3...)
        do {
            arquivoCSV = new File(diretorio + "/extrato" + numeroArquivo + ".csv");
            numeroArquivo++;
        } while (arquivoCSV.exists());

        try (FileWriter writer = new FileWriter(arquivoCSV)) {
            writer.append("Tipo,Valor,DataHora\n");

            for (Transacao t : transacoes) {
                writer.append(t.getTipo() + "," + t.getValor() + "," + t.getDataHora() + "\n");
            }

            System.out.println("Extrato exportado com sucesso: " + arquivoCSV.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao exportar extrato CSV: " + e.getMessage());
        }
    }


    // Listar todas as transações
    public List<Transacao> buscarTransacoesPendentesEstorno(Connection conn) throws SQLException {
        List<Transacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM transacoes WHERE status_estorno = 'PENDENTE'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(instantiateTransacao(conn, rs));
            }
        }
        return lista;
    }

    // Montar objeto Transacao a partir do ResultSet
    private Transacao instantiateTransacao(Connection conn, ResultSet rs) throws SQLException {
        Transacao t = new Transacao();

        t.setId(rs.getInt("id"));
        t.setContaOrigem(contaDAO.findById(conn, rs.getInt("conta_origem_id")));
        t.setContaDestino(contaDAO.findById(conn, rs.getInt("conta_destino_id")));
        t.setTipo(rs.getString("tipo"));
        t.setValor(rs.getBigDecimal("valor"));
        t.setDataHora(rs.getTimestamp("data_hora").toLocalDateTime());
        t.setStatusEstorno(rs.getString("status_estorno"));
        t.setDataSolicitacaoEstorno(rs.getTimestamp("data_solicitacao_estorno") != null
                ? rs.getTimestamp("data_solicitacao_estorno").toLocalDateTime() : null);

        t.setMotivo_solicitacao_estorno(rs.getString("motivo_solicitacao_estorno"));
        t.setMotivo_recusa_estorno(rs.getString("motivo_recusa_estorno"));

        t.setDataRespostaEstorno(rs.getTimestamp("data_resposta_estorno") != null
                ? rs.getTimestamp("data_resposta_estorno").toLocalDateTime() : null);

        return t;
    }
}
