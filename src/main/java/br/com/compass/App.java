package br.com.compass;

import br.com.compass.DAO.ContaDAO;
import br.com.compass.DAO.DB;
import br.com.compass.DAO.TransacaoDAO;
import br.com.compass.DAO.UsuarioDAO;
import br.com.compass.DAO.dbException;
import br.com.compass.Entity.Conta;
import br.com.compass.Entity.Transacao;
import br.com.compass.Entity.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

            int option = -1;
            try {
                option = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida! Digite um número.");
                continue;
            }

            switch (option) {
                case 1:
                    loginUsuario(scanner);
                    break;
                case 2:
                    registrarUsuario(scanner);
                    break;
                case 0:
                    running = false;
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
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

            // Nome
            String nome;
            while (true) {
                System.out.print("Nome: ");
                nome = scanner.nextLine();
                if (!nome.trim().isEmpty()) break;
                System.out.println("Entrada inválida! Insira um nome válido.");
            }

            // CPF
            String cpf;
            while (true) {
                System.out.print("CPF: ");
                cpf = scanner.nextLine();
                if (cpf.matches("\\d{11}")) break;
                System.out.println("Entrada inválida! Insira um CPF com 11 dígitos numéricos.");
            }

            // Telefone
            String telefone;
            while (true) {
                System.out.print("Telefone: ");
                telefone = scanner.nextLine();
                if (telefone.matches("\\d{10,11}")) break;
                System.out.println("Entrada inválida! Insira um número de telefone com 10 ou 11 dígitos.");
            }

            // Data de nascimento
            LocalDate dataNascimento = null;
            while (dataNascimento == null) {
                System.out.print("Data de Nascimento (yyyy-MM-dd): ");
                String dataInput = scanner.nextLine();
                try {
                    dataNascimento = LocalDate.parse(dataInput);
                } catch (Exception e) {
                    System.out.println("Entrada inválida! Insira a data no formato correto (yyyy-MM-dd).");
                }
            }

            // Tipo de conta
            String tipoConta;
            while (true) {
                System.out.print("Tipo de Conta (Corrente/Poupanca/Conta Salario): ");
                tipoConta = scanner.nextLine().trim().toUpperCase();
                if (tipoConta.equals("CORRENTE") || tipoConta.equals("POUPANCA") || tipoConta.equals("CONTA SALARIO")) break;
                System.out.println("Entrada inválida! Digite 'Corrente', 'Poupanca' ou 'Conta Salario'.");
            }

            // Senha
            String senha;
            while (true) {
                System.out.print("Senha: ");
                senha = scanner.nextLine();
                if (!senha.trim().isEmpty()) break;
                System.out.println("Entrada inválida! A senha não pode estar vazia.");
            }

            String tipo = "CLIENTE";
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            Usuario usuario = new Usuario(nome, cpf, senhaHash, telefone, dataNascimento, tipo);
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
    public void solicitarEstorno(Connection conn, int idTransacao, String motivoSolicitacao) throws SQLException {
        Transacao transacao = transacaoDAO.findById(conn, idTransacao);

        if (transacao == null) {
            System.out.println("Transação não encontrada.");
            return;
        }

        if (transacao.getStatusEstorno() != null && transacao.getStatusEstorno().equalsIgnoreCase("APROVADO")) {
            System.out.println("Estorno já foi aprovado. Não é possível solicitar novamente.");
            return;
        }

        String sql = "UPDATE transacoes SET status_estorno = 'PENDENTE', motivo_solicitacao_estorno = ?, data_solicitacao_estorno = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, motivoSolicitacao);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, idTransacao);
            stmt.executeUpdate();
        }

        System.out.println("Estorno solicitado com sucesso! Aguardando aprovação do gerente.");
    }


    // Menu de aprovação ou recusa de estornos (Gerente)
    private static void menuEstornosGerente(Scanner scanner) {
        Connection conn = null;
        try {
            conn = DB.getConnection();

            List<Transacao> estornosPendentes = transacaoDAO.buscarTransacoesPendentesEstorno(conn);

            if (estornosPendentes.isEmpty()) {
                System.out.println("Não há solicitações de estorno pendentes.");
                return;
            }

            System.out.println("\n===== Estornos Pendentes =====");
            for (Transacao t : estornosPendentes) {
                System.out.println("ID Transação: " + t.getId() +
                        " | Valor: " + t.getValor() +
                        " | Tipo: " + t.getTipo() +
                        " | Conta Origem ID: " + t.getContaOrigem().getId());
            }

            System.out.print("Digite o ID da transação para análise (ou 0 para cancelar): ");
            int idTransacao = scanner.nextInt();
            scanner.nextLine(); // limpar buffer

            if (idTransacao == 0) {
                System.out.println("Operação cancelada.");
                return;
            }

            Transacao transacaoSelecionada = transacaoDAO.findById(conn, idTransacao);

            if (transacaoSelecionada == null) {
                System.out.println("Transação não encontrada.");
                return;
            }

            System.out.println("1 - Aprovar Estorno");
            System.out.println("2 - Recusar Estorno");
            System.out.print("Escolha: ");
            int escolha = scanner.nextInt();
            scanner.nextLine(); // limpar buffer

            if (escolha == 1) {
                transacaoDAO.aprovarEstorno(conn, idTransacao);
            } else if (escolha == 2) {
                System.out.print("Digite o motivo da recusa: ");
                String motivo = scanner.nextLine();
                transacaoDAO.recusarEstorno(conn, idTransacao, motivo);
            } else {
                System.out.println("Opção inválida.");
            }

        } catch (SQLException e) {
            throw new dbException("Erro no menu de estornos: " + e.getMessage());
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
            System.out.println("|| 4. Unblock Accounts        ||");
            System.out.println("|| 5. Approve/Reject Reversal ||");
            System.out.println("|| 0. Logout                  ||");
            System.out.print("Escolha uma opção: ");

            int option = -1;
            try {
                option = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
                continue;
            }

            switch (option) {
                case 1 -> {
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
                }
                case 2 -> registrarGerente(scanner, gerenteLogado);
                case 3 -> registrarGerente(scanner, gerenteLogado); // CORRETO aqui!
                case 4 -> desbloquearContasBloqueadas(scanner);
                case 5 -> menuEstornosGerente(scanner);
                case 0 -> {
                    running = false;
                    System.out.println("Fazendo logout do menu gerente...");
                }
                default -> System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static BigDecimal lerValorMonetario(Scanner scanner, String mensagem) {
        BigDecimal valor = null;
        while (valor == null) {
            System.out.print(mensagem + " ");
            String entrada = scanner.nextLine().replace(",", ".");
            try {
                valor = new BigDecimal(entrada);
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Valor precisa ser positivo.");
                    valor = null;
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um valor numérico (ex: 100.50 ou 100,50).");
            }
        }
        return valor;
    }

    private static int lerInteiro(Scanner scanner, String mensagem) {
        Integer valor = null;
        while (valor == null) {
            System.out.print(mensagem + " ");
            String entrada = scanner.nextLine();
            try {
                valor = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número inteiro.");
            }
        }
        return valor;
    }


    public static void bankMenu(Scanner scanner, Conta conta) {
        boolean running = true;
        while (running) {
            System.out.println("\n========= Bank Menu =========");
            System.out.println("|| 1. Deposit                      ||");
            System.out.println("|| 2. Withdraw                     ||");
            System.out.println("|| 3. Check Balance                ||");
            System.out.println("|| 4. Transfer                     ||");
            System.out.println("|| 5. Request Reversal             ||");
            System.out.println("|| 6. View extract                 ||");
            System.out.println("|| 7. Export extract in csv file  ||");
            System.out.println("|| 0. Exit                         ||");
            System.out.println("====================================");
            System.out.print("Choose an option: ");

            int option;
            try {
                option = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida. Digite um número.");
                continue;
            }

            Connection conn = null;
            try {
                conn = DB.getConnection();
                switch (option) {
                    case 1: {
                        BigDecimal valorDeposito = lerValorMonetario(scanner, "Valor para depósito:");
                        contaDAO.depositar(conn, conta, valorDeposito);
                        System.out.println("Depósito realizado com sucesso.");
                        break;
                    }
                    case 2: {
                        BigDecimal valorSaque = lerValorMonetario(scanner, "Valor para saque:");
                        if (contaDAO.sacar(conn, conta, valorSaque)) {
                            System.out.println("Saque realizado com sucesso.");
                        } else {
                            System.out.println("Saque não realizado: saldo insuficiente.");
                        }
                        break;
                    }
                    case 3:
                        System.out.println("Saldo atual: " + conta.getSaldo());
                        break;
                    case 4: {
                        
                        if (conta.getTipoConta().equals("CONTA SALARIO")) {
                            System.out.println("Não é permitido realizar transferências de uma conta salário.");
                            break;
                        }

                        int idContaDestino = lerInteiro(scanner, "Digite o ID da conta destino:");
                        BigDecimal valorTransferencia = lerValorMonetario(scanner, "Digite o valor para transferir:");
                        transacaoDAO.transferir(conn, conta, idContaDestino, valorTransferencia);
                        conta = contaDAO.findById(conn, conta.getId());
                        break;
                    }
                    case 5: {
                        int idTransacao = lerInteiro(scanner, "Digite o ID da transação que deseja estornar:");
                        System.out.println("Digite o motivo do estorno:");
                        String motivoEstorno = scanner.nextLine();
                        transacaoDAO.solicitarEstorno(conn, idTransacao, conta.getCpf(), motivoEstorno);
                        break;
                    }
                    case 6: {
                        System.out.println("===== Extrato Bancário =====");
                        List<Transacao> extrato = transacaoDAO.buscarExtratoPorConta(conn, conta);
                        if (extrato.isEmpty()) {
                            System.out.println("Nenhuma transação encontrada.");
                        } else {
                            for (Transacao t : extrato) {
                                String tipo = t.getTipo();
                                String info = "";

                                if (tipo.equalsIgnoreCase("TRANSFERENCIA")) {
                                    if (t.getContaOrigem().getId() == conta.getId()) {
                                        info = "Transferência Enviada para Conta ID: " + t.getContaDestino().getId();
                                    } else {
                                        info = "Transferência Recebida de Conta ID: " + t.getContaOrigem().getId();
                                    }
                                } else if (tipo.equalsIgnoreCase("SAQUE")) {
                                    info = "Saque Realizado";
                                } else if (tipo.equalsIgnoreCase("DEPOSITO")) {
                                    info = "Depósito Realizado";
                                }

                                System.out.println(info +
                                        " | Valor: " + t.getValor() +
                                        " | Data: " + t.getDataHora());
                            }
                        }
                        break;
                    }
                    case 7: {
                        System.out.println("===== Exportar Extrato CSV =====");
                        List<Transacao> extratoExport = transacaoDAO.buscarExtratoPorConta(conn, conta);
                        if (extratoExport.isEmpty()) {
                            System.out.println("Nenhuma transação encontrada para exportar.");
                        } else {
                            transacaoDAO.exportarExtratoCSV(
                                    extratoExport,
                                    "C:/Desafio_compass/BankChallenge/src/main/java/csvfiles"
                            );
                        }
                        break;
                    }
                    case 0:
                        System.out.println("Saindo do menu bancário...");
                        running = false;
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                DB.closeConnection();
            }
        }
    }



    public static void registrarGerente(Scanner scanner, Usuario gerenteLogado) {
        if (!gerenteLogado.getCpf().equals("00000000000")) {
            System.out.println("Acesso negado: somente o Gerente Principal pode registrar novos gerentes.");
            return;
        }

        Connection conn = null;

        try {
            conn = DB.getConnection();

            System.out.println("\n===== Registrar Novo Gerente =====");

            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("CPF: ");
            String cpf = scanner.nextLine();

            System.out.print("Telefone: ");
            String telefone = scanner.nextLine();

            LocalDate dataNascimento = null;
            while (dataNascimento == null) {
                System.out.print("Data de Nascimento (yyyy-MM-dd): ");
                String data = scanner.nextLine();
                try {
                    dataNascimento = LocalDate.parse(data);
                } catch (Exception e) {
                    System.out.println("Data inválida! Use o formato yyyy-MM-dd.");
                }
            }

            System.out.print("Senha: ");
            String senha = scanner.nextLine();
            String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

            String tipo = "GERENTE";
            Usuario novoGerente = new Usuario(nome, cpf, senhaHash, telefone, dataNascimento, tipo);
            usuarioDAO.create(conn, novoGerente);

            System.out.println("Novo gerente registrado com sucesso!");

        } catch (dbException e) {
            e.printStackTrace();
        } finally {
            DB.closeConnection();
        }
    }

}
