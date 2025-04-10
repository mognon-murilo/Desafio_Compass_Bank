package br.com.compass;

import br.com.compass.DAO.ContaDAO;
import br.com.compass.DAO.DB;
import br.com.compass.DAO.TransacaoDAO;
import br.com.compass.DAO.UsuarioDAO;
import br.com.compass.DAO.dbException;
import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class App {

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final ContaDAO contaDAO = new ContaDAO();
    private static final TransacaoDAO transacaoDAO = new TransacaoDAO();
    private static final Conta conta = new Conta();
    private static final Usuario usuario = new Usuario();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = null;
        try {
            // Inicializa o gerente padrão se não existir
            conn = DB.getConnection();
            usuarioDAO.inicializarGerente(conn);
        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
        try {
            mainMenu(scanner);
        } finally {
            scanner.close();
            System.out.println("Application closed.");
        }
    }


    public static void mainMenu(Scanner scanner) {
        boolean running = true;

        while (running) {
            System.out.println("\n========= Main Menu =========");
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
    private static void desbloquearContasBloqueadas(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            List<Conta> contasBloqueadas = contaDAO.buscarContasBloqueadas(conn);

            if (contasBloqueadas.isEmpty()) {
                System.out.println("Não há contas bloqueadas no momento.");
                return;
            }

            System.out.println("Contas bloqueadas:");
            for (int i = 0; i < contasBloqueadas.size(); i++) {
                Conta c = contasBloqueadas.get(i);
                System.out.printf("[%d] CPF: %s | Nome: %s ",
                        i + 1, c.getCpf(), c.getNome());
            }

            System.out.print("Escolha o número da conta a ser desbloqueada (ou 0 para cancelar): ");
            int escolha = scanner.nextInt();
            scanner.nextLine(); // limpar buffer

            if (escolha > 0 && escolha <= contasBloqueadas.size()) {
                Conta contaSelecionada = contasBloqueadas.get(escolha - 1);
                contaDAO.desbloquearConta(conn, contaSelecionada.getCpf());
                System.out.println("Conta desbloqueada com sucesso!");
            } else if (escolha == 0) {
                System.out.println("Operação cancelada.");
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (dbException e) {
            throw new dbException("Erro ao tentar desbloquear contas: " + e.getMessage());
        } finally {
            DB.closeConnection();
        }
    }


    // Registro de usuário (CLIENTE) acessível do menu público
    public static void registrarUsuario(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();

            System.out.println("\n===== Account Opening =====");
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();
            System.out.print("Telefone: ");
            String telefone = scanner.nextLine();
            System.out.print("Data de Nascimento (yyyy-MM-dd): ");
            String dataNascimento = scanner.nextLine();
            System.out.print("Tipo de Conta (Corrente/Poupanca): ");
            String tipoConta = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            // Para registros via menu público, o tipo é fixado como CLIENTE
            String tipo = "CLIENTE";
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            Usuario usuario = new Usuario(nome, cpf, senhaHash, telefone, LocalDate.parse(dataNascimento), tipo);
            usuarioDAO.create(conn, usuario);

            // Cria a conta associada ao usuário
            Conta conta = new Conta(nome, cpf, telefone, LocalDate.parse(dataNascimento), tipoConta, senhaHash);
            contaDAO.inserir(conn, conta);

            System.out.println("Usuário e conta criados com sucesso!");

        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }

    // Login: autentica e direciona para menu específico baseado no tipo de usuário
    public static void loginUsuario(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            System.out.println("\n===== Login =====");
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();

            Usuario usuario = usuarioDAO.findByCpf(conn, cpf);
            if (usuario == null) {
                System.out.println("Usuário não encontrado.");
                return;
            }

            // Verificação apenas se NÃO for gerente
            if (!usuario.getTipo().equalsIgnoreCase("GERENTE")) {
                Conta conta = contaDAO.findByCpf(conn, cpf);
                if (conta == null) {
                    System.out.println("Conta não encontrada.");
                    return;
                }

                if (conta.isBloqueada()) {
                    System.out.println("Sua conta está bloqueada. Procure um gerente para desbloquear.");
                    return;
                }
            }

            int tentativas = 0;
            while (tentativas < 3) {
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                if (BCrypt.checkpw(senha, usuario.getSenhaHash())) {
                    System.out.println("Login efetuado com sucesso! Bem-vindo, " + usuario.getNome() + "!");

                    if (usuario.getTipo().equalsIgnoreCase("GERENTE")) {
                        gerenteMenu(scanner, usuario);
                    } else {
                        clienteMenu(scanner, usuario);
                    }
                    return;
                } else {
                    tentativas++;
                    System.out.println("Senha incorreta. Tentativa " + tentativas + "/3.");
                }
            }

            System.out.println("Conta bloqueada. Procure um gerente para desbloquear.");

            // Bloqueia apenas se NÃO for gerente
            if (!usuario.getTipo().equalsIgnoreCase("GERENTE")) {
                contaDAO.bloquearConta(conn, cpf);
            }

        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }


    // Menu para usuários do tipo CLIENTE (operações bancárias comuns)
    public static void clienteMenu(Scanner scanner, Usuario usuarioLogado) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            Conta conta = contaDAO.findByCpf(conn, usuarioLogado.getCpf());
            if (conta == null) {
                System.out.println("Conta não encontrada para o CPF informado.");
                return;
            }
            bankMenu(scanner, conta);
        } finally {
            DB.closeConnection();
        }
    }
    public static void solicitarEstorno(Scanner scanner, Usuario usuarioLogado) {
        Connection conn = null;

        try {
            conn = DB.getConnection();

            System.out.println("===== Solicitar Estorno =====");
            System.out.print("Informe o ID da transação que deseja estornar: ");
            int idTransacao = Integer.parseInt(scanner.nextLine());

            transacaoDAO.solicitarEstorno(conn, idTransacao, usuarioLogado.getCpf());

        } catch (dbException e) {
            throw new dbException("Erro: " + e.getMessage());
        } finally {
            DB.closeConnection();
        }
    }


    // Menu para usuários do tipo GERENTE (inclui opções de operações bancárias e registrar novos gerentes)
    public static void gerenteMenu(Scanner scanner, Usuario gerenteLogado) {
        boolean running = true;
        while (running) {
            System.out.println("\n========= Gerente Menu =========");
            System.out.println("|| 1. Bank Operations         ||");
            System.out.println("|| 2. Register New Manager    ||");
            System.out.println("|| 3. Account Opening         ||");
            System.out.println("|| 4. Unblock Accounts         ||");
            System.out.println("|| 0. Logout                  ||");
            System.out.print("Escolha uma opção: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // Limpar buffer

            switch (option) {
                case 1:
                    // Para operações bancárias, utiliza a conta associada ao gerente
                    Connection conn = null;
                    try {
                        conn = DB.getConnection();
                        Conta conta = contaDAO.findByCpf(conn, gerenteLogado.getCpf());
                        if (conta != null) {
                            bankMenu(scanner, conta);
                        } else {
                            System.out.println("Conta não encontrada para o CPF do gerente.");
                        }
                    } finally {
                        DB.closeConnection();
                    }
                    break;
                case 2:
                    registrarGerente(scanner, gerenteLogado);
                    break;
                case 3:
                    criarContaParaGerente(scanner, gerenteLogado);
                    break;
                case 4:

                    desbloquearContasBloqueadas(scanner);
                    break;
                case 0:
                    running = false;
                    System.out.println("Fazendo logout do menu gerente...");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    public static void bankMenu(Scanner scanner, Conta conta) {
        boolean running = true;
        while (running) {
            System.out.println("\n========= Bank Menu =========");
            System.out.println("|| 1. Deposit              ||");
            System.out.println("|| 2. Withdraw             ||");
            System.out.println("|| 3. Check Balance        ||");
            System.out.println("|| 4. Transfer             ||");
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
                    case 4:
                        System.out.print("Digite o ID da conta destino: ");
                        int idContaDestino = scanner.nextInt();
                        System.out.print("Digite o valor para transferir: ");
                        BigDecimal valorTransferencia = scanner.nextBigDecimal();
                        scanner.nextLine();
                        transacaoDAO.transferir(conn, conta, idContaDestino, valorTransferencia);
                        // Atualiza a conta logada com os dados do banco
                        conta = contaDAO.findById(conn, conta.getId());
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


    public static void registrarGerente(Scanner scanner, Usuario gerenteLogado) {
        if (!gerenteLogado.getCpf().equals("00000000000")) {
            System.out.println("Acesso negado: somente o Gerente Principal podem registrar novos gerentes.");
            return;
        }
        Connection conn = null;
        try {
            conn = DB.getConnection();

            System.out.println("\n===== Register New Manager =====");
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("CPF: ");
            String cpf = scanner.nextLine();
            System.out.print("Telefone: ");
            String telefone = scanner.nextLine();
            System.out.print("Data de Nascimento (yyyy-MM-dd): ");
            String dataNascimento = scanner.nextLine();
            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            String tipo = "GERENTE";
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            Usuario novoGerente = new Usuario(nome, cpf, senhaHash, telefone, LocalDate.parse(dataNascimento), tipo);
            usuarioDAO.create(conn, novoGerente);


            System.out.println("Novo gerente registrado com sucesso!");
        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }
    private static void criarContaParaGerente(Scanner scanner, Usuario gerenteLogado) {
        Connection conn = null;

        try {
            conn = DB.getConnection();

            // Verifica se o gerente já possui conta
            Conta contaExistente = contaDAO.findByCpf(conn, gerenteLogado.getCpf());
            if (contaExistente != null) {
                System.out.println("Você já possui uma conta cadastrada.");
                return;
            }

            System.out.println("\n===== Criar Conta Bancária =====");
            System.out.print("Tipo de Conta (Corrente/Poupanca): ");
            String tipoConta = scanner.nextLine();
            System.out.print("Digite sua senha para confirmar: ");
            String senhaDigitada = scanner.nextLine();


            if (!BCrypt.checkpw(senhaDigitada, gerenteLogado.getSenhaHash())) {
                System.out.println("Senha incorreta! Conta não foi criada.");
                return;
            }

            Conta novaConta = new Conta(
                    gerenteLogado.getNome(),
                    gerenteLogado.getCpf(),
                    gerenteLogado.getTelefone(),
                    gerenteLogado.getDataNascimento(),
                    tipoConta,
                    gerenteLogado.getSenhaHash()
            );

            contaDAO.inserir(conn, novaConta);
            System.out.println("Conta criada com sucesso!");

        } catch (dbException e) {
            throw new dbException("Erro ao criar conta: " + e.getMessage());
        } finally {
            DB.closeConnection();
        }
    }
}
