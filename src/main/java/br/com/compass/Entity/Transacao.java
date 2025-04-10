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
    private String motivoEstorno; // texto livre do usuário
    private LocalDateTime dataSolicitacaoEstorno;
    private LocalDateTime dataRespostaEstorno;

    public Transacao(Conta contaDestino, Conta contaOrigem, LocalDateTime dataHora, int id, String tipo, BigDecimal valor) {
        this.contaDestino = contaDestino;
        this.contaOrigem = contaOrigem;
        this.dataHora = dataHora;
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
    }

    public Conta getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(Conta contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Conta getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }
}