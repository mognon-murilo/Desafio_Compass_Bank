package br.com.compass.DAO;

import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Transacao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TransacaoDAO {

    public List<Transacao> findAll() {
        List<Transacao> list = new ArrayList<>();
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            conn = db.DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("SELECT * FROM Transacao");

            while (rs.next()) {
                Conta contaOrigem = new Conta(rs.getInt("ContaOrigemId"));  // só o ID
                Conta contaDestino = new Conta(rs.getInt("ContaDestinoId")); // só o ID

                Transacao transacao = new Transacao(
                        contaDestino,
                        contaOrigem,
                        rs.getTimestamp("DataHora").toLocalDateTime(),
                        rs.getInt("Id"),
                        rs.getString("Tipo"),
                        rs.getBigDecimal("Valor")
                );

                list.add(transacao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.DB.closeResultSet(rs);
            db.DB.closeStatement(st);
            db.DB.closeConnection();
        }

        return list;
    }
}