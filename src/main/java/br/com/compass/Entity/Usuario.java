package br.com.compass.Entity;

import java.time.LocalDate;

public class Usuario {
    private int id;
    private String nome;
    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private String senhaHash;
    private String tipo;

    public Usuario(String nome, String cpf, String senhaHash, String telefone, LocalDate dataNascimento,String tipo) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
    }
    public Usuario(int id, String nome, String cpf, String senhaHash, String telefone, LocalDate dataNascimento,String tipo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.senhaHash = senhaHash;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
        this.tipo = tipo;
    }
    public Usuario(int id) {
        this.id = id;
    }
    public Usuario(){

    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}