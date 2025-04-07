package br.com.compass;

import br.com.compass.DAO.ContaDAO;
import br.com.compass.DAO.UsuarioDAO;
import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import br.com.compass.DAO.DB;
import br.com.compass.DAO.dbException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;


public class App {
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final ContaDAO contaDAO = new ContaDAO();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            mainMenu(scanner);
        } finally {
            DB.closeConnection();
            scanner.close();
            System.out.println("Application closed.");
        }
    }

    public static void registrarUsuario(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();

            System.out.println("Nome: ");
            String nome = scanner.nextLine();
            System.out.println("CPF: ");
            String cpf = scanner.nextLine();
            System.out.println("Telefone: ");
            String telefone = scanner.nextLine();
            System.out.println("Data de Nascimento: ");
            String dataNascimento = scanner.nextLine();
            System.out.println("Tipo de Conta (Corrente/Poupanca): ");
            String tipoConta = scanner.nextLine();
            System.out.println("Senha: ");
            String senha = scanner.nextLine();

            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            Usuario usuario = new Usuario(nome, cpf, senhaHash, telefone, LocalDate.parse(dataNascimento));
            usuarioDAO.create(conn, usuario);

            Conta conta = new Conta(nome, cpf, telefone, dataNascimento, tipoConta, senhaHash);
            contaDAO.inserir(conn, conta);

            System.out.println("Usuário e conta criados com sucesso!");

        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }


    public static void loginUsuario(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();

            System.out.println("===== Login =====");
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();

            Usuario usuario = usuarioDAO.findByCpf(conn, cpf);

            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            int tentativas = 0;
            while (tentativas < 3) {
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                if (BCrypt.checkpw(senha, usuario.getSenhaHash())) {
                    System.out.println("Login efetuado com sucesso! Bem-vindo, " + usuario.getNome() + "!");

                    Conta conta = contaDAO.findByCpf(conn, cpf);
                    if (conta == null) {
                        System.out.println("Conta não encontrada para o CPF informado.");
                        return;
                    }

                    bankMenu(scanner, conta);
                    return;
                } else {
                    tentativas++;
                    System.out.println("Senha incorreta. Tentativa " + tentativas + "/3.");
                }
            }
            System.out.println("Conta bloqueada. Procure um gerente para desbloquear.");

        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }

    public static void mainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("========= Main Menu =========");
            System.out.println("|| 1. Login                ||");
            System.out.println("|| 2. Account Opening      ||");
            System.out.println("|| 0. Exit                 ||");
            System.out.println("=============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer

            switch (option) {
                case 1:
                    loginUsuario(scanner);
                    break;
                case 2:
                    registrarUsuario(scanner);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    public static void bankMenu(Scanner scanner, Conta conta) {
        boolean running = true;

        while (running) {
            System.out.println("========= Bank Menu =========");
            System.out.println("|| 1. Deposit              ||");
            System.out.println("|| 2. Withdraw             ||");
            System.out.println("|| 3. Check Balance        ||");
            System.out.println("|| 0. Exit                 ||");
            System.out.println("=============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer


            Connection conn = null;
            try {
                conn = DB.getConnection();
                switch (option) {
                    case 1:
                        System.out.print("Valor para depósito: ");
                        BigDecimal valorDeposito = scanner.nextBigDecimal();
                        scanner.nextLine();
                        contaDAO.depositar(conn, conta, valorDeposito);
                        System.out.println("Depósito realizado com sucesso.");
                        break;
                    case 2:
                        System.out.print("Valor para saque: ");
                        BigDecimal valorSaque = scanner.nextBigDecimal();
                        scanner.nextLine();
                        if (contaDAO.sacar(conn, conta, valorSaque)) {
                            System.out.println("Saque realizado com sucesso.");
                        } else {
                            System.out.println("Saque não realizado: saldo insuficiente.");
                        }
                        break;
                    case 3:
                        System.out.println("Saldo atual: " + conta.getSaldo());
                        break;
                    case 0:
                        System.out.println("Saindo do menu bancário...");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option! Please try again.");
                }
            } finally {
                DB.closeConnection();
            }
        }
    }
}
