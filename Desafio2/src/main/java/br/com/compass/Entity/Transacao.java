package br.com.compass.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {
    private int id;
    private Conta contaOrigem;
    private Conta contaDestino;
    private String tipo;
    private BigDecimal valor;
    private LocalDateTime dataHora;
    private String statusEstorno; // PENDENTE, APROVADO, RECUSADO
    private String motivo_solicitacao_estorno;
    private String motivo_recusa_estorno;// texto livre do usuário
    private LocalDateTime dataSolicitacaoEstorno;
    private LocalDateTime dataRespostaEstorno;

    // Construtor Completo
    public Transacao(int id, Conta contaOrigem, Conta contaDestino, String tipo, BigDecimal valor, LocalDateTime dataHora,
                     String statusEstorno, String motivo_solicitacao_estorno,String motivo_recusa_estorno, LocalDateTime dataSolicitacaoEstorno, LocalDateTime dataRespostaEstorno) {
        this.id = id;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
        this.statusEstorno = statusEstorno;
        this.motivo_solicitacao_estorno = motivo_solicitacao_estorno;
        this.motivo_recusa_estorno = motivo_recusa_estorno;
        this.dataSolicitacaoEstorno = dataSolicitacaoEstorno;
        this.dataRespostaEstorno = dataRespostaEstorno;
    }

    // Construtor Resumido (transferência ou depósito)
    public Transacao(Conta contaOrigem, Conta contaDestino, String tipo, BigDecimal valor, LocalDateTime dataHora) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }
    public Transacao(){

    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Conta getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(Conta contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public String getMotivo_recusa_estorno() {
        return motivo_recusa_estorno;
    }

    public void setMotivo_recusa_estorno(String motivo_recusa_estorno) {
        this.motivo_recusa_estorno = motivo_recusa_estorno;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatusEstorno() {
        return statusEstorno;
    }

    public void setStatusEstorno(String statusEstorno) {
        this.statusEstorno = statusEstorno;
    }

    public String getMotivo_solicitacao_estorno() {
        return motivo_solicitacao_estorno;
    }

    public void setMotivo_solicitacao_estorno(String motivo_solicitacao_estorno) {
        this.motivo_solicitacao_estorno = motivo_solicitacao_estorno;
    }

    public LocalDateTime getDataSolicitacaoEstorno() {
        return dataSolicitacaoEstorno;
    }

    public void setDataSolicitacaoEstorno(LocalDateTime dataSolicitacaoEstorno) {
        this.dataSolicitacaoEstorno = dataSolicitacaoEstorno;
    }

    public LocalDateTime getDataRespostaEstorno() {
        return dataRespostaEstorno;
    }

    public void setDataRespostaEstorno(LocalDateTime dataRespostaEstorno) {
        this.dataRespostaEstorno = dataRespostaEstorno;
    }
}
