package br.com.compass;

import br.com.compass.DAO.UsuarioDAO;
import br.com.compass.Entity.Usuario;
import br.com.compass.util.HashUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Scanner;

public class App {

    public static void dbconnect(){
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            conn = db.DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery("select * from usuario");
            while (rs.next()){
                System.out.println(rs.getInt("id") + ", " + rs.getString("nome"));
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        finally {
            db.DB.closeResultSet(rs);
            db.DB.closeStatement(st);
            db.DB.closeConnection();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        dbconnect();
        mainMenu(scanner);
        scanner.close();
        System.out.println("Application closed");
    }

    public static void registrarUsuario(Scanner scanner) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        System.out.println("===== Register New Account =====");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("CPF: ");
        String cpf = scanner.nextLine();

        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();

        System.out.print("Data de Nascimento (YYYY-MM-DD): ");
        LocalDate dataNascimento = LocalDate.parse(scanner.nextLine());

        System.out.print("Senha: ");
        String senha = scanner.nextLine();


        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

        Usuario usuario = new Usuario(0,nome, cpf, senhaHash, telefone, dataNascimento);
        usuarioDAO.create(usuario);
    }

    public static void loginUsuario(Scanner scanner) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        System.out.println("===== Login =====");
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();

        Usuario usuario = usuarioDAO.findByCpf(cpf);

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
                return;
            } else {
                tentativas++;
                System.out.println("Senha incorreta. Tentativa " + tentativas + "/3.");
            }
        }
        System.out.println("Conta bloqueada. Procure um gerente para desbloquear.");
    }

    public static void mainMenu(Scanner scanner) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        boolean running = true;

        while (running) {
            System.out.println("========= Main Menu =========");
            System.out.println("|| 1. Login                ||");
            System.out.println("|| 2. Account Opening      ||");
            System.out.println("|| 0. Exit                 ||");
            System.out.println("=============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

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

    public static void bankMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("========= Bank Menu =========");
            System.out.println("|| 1. Deposit              ||");
            System.out.println("|| 2. Withdraw             ||");
            System.out.println("|| 3. Check Balance        ||");
            System.out.println("|| 4. Transfer             ||");
            System.out.println("|| 5. Bank Statement       ||");
            System.out.println("|| 0. Exit                 ||");
            System.out.println("=============================");
            System.out.print("Choose an option: ");

            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.println("Deposit.");
                    break;
                case 2:
                    System.out.println("Withdraw.");
                    break;
                case 3:
                    System.out.println("Check Balance.");
                    break;
                case 4:
                    System.out.println("Transfer.");
                    break;
                case 5:
                    System.out.println("Bank Statement.");
                    break;
                case 0:
                    System.out.println("Exiting...");
                    running = false;
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
}


