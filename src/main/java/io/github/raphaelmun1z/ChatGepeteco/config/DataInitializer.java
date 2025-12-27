package io.github.raphaelmun1z.ChatGepeteco.config;

import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Papel;
import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Permissao;
import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.Admin;
import io.github.raphaelmun1z.ChatGepeteco.entities.usuario.UsuarioComum;
import io.github.raphaelmun1z.ChatGepeteco.repositories.UsuarioRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.autorizacao.PapelRepository;
import io.github.raphaelmun1z.ChatGepeteco.repositories.autorizacao.PermissaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissaoRepository permissaoRepository;
    private final PapelRepository papelRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        PermissaoRepository permissaoRepository,
        PapelRepository papelRepository,
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder) {
        this.permissaoRepository = permissaoRepository;
        this.papelRepository = papelRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    //@Transactional
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() > 0) {
            log.info("O banco de dados já contém dados. A inicialização não será executada.");
            return;
        }

        log.info("Iniciando a criação de dados (Admin + Usuário Comum)...");

        // -----------------------------------------------------
        // 1. CRIAR PERMISSÕES
        // -----------------------------------------------------
        log.info("Criando permissões...");

        Permissao pChatCriar = new Permissao(null, "CHAT_CRIAR");
        Permissao pChatLer = new Permissao(null, "CHAT_LER");
        Permissao pChatDeletar = new Permissao(null, "CHAT_DELETAR");
        Permissao pChatRenomear = new Permissao(null, "CHAT_RENOMEAR");
        Permissao pMensagemEnviar = new Permissao(null, "MENSAGEM_ENVIAR");
        Permissao pUsoIA = new Permissao(null, "USO_MODELO_IA");

        Permissao pAdminDashboard = new Permissao(null, "ADMIN_DASHBOARD_LER");
        Permissao pUsuarioListar = new Permissao(null, "USUARIO_LISTAR");
        Permissao pUsuarioBanir = new Permissao(null, "USUARIO_BANIR");
        Permissao pConfigSistema = new Permissao(null, "SISTEMA_CONFIGURAR");

        permissaoRepository.saveAll(List.of(
            pChatCriar, pChatLer, pChatDeletar, pChatRenomear, pMensagemEnviar, pUsoIA,
            pAdminDashboard, pUsuarioListar, pUsuarioBanir, pConfigSistema
        ));

        // -----------------------------------------------------
        // 2. CRIAR PAPÉIS (ROLES)
        // -----------------------------------------------------
        log.info("Criando papéis...");

        Papel papelAdmin = new Papel(null, "ROLE_ADMIN", Set.of(
            pChatCriar, pChatLer, pChatDeletar, pChatRenomear, pMensagemEnviar, pUsoIA,
            pAdminDashboard, pUsuarioListar, pUsuarioBanir, pConfigSistema
        ));

        Papel papelUser = new Papel(null, "ROLE_USER", Set.of(
            pChatCriar, pChatLer, pChatDeletar, pChatRenomear, pMensagemEnviar, pUsoIA
        ));

        papelRepository.saveAll(List.of(papelAdmin, papelUser));

        log.info("Criando usuários de exemplo...");
        String senhaPadrao = passwordEncoder.encode("123456");

        Admin admin = new Admin();
        admin.setNome("Administrador Supremo");
        admin.setEmail("admin@chat.com");
        admin.setSenha(senhaPadrao);
        admin.setPapel(papelAdmin);

        UsuarioComum comum = new UsuarioComum();
        comum.setNome("Usuário Padrão");
        comum.setEmail("usuario@chat.com");
        comum.setSenha(senhaPadrao);
        comum.setPapel(papelUser);
        comum.setDataCadastro(LocalDateTime.now());

        usuarioRepository.saveAll(List.of(admin, comum));

        log.info("----------------------------------------------------");
        log.info("DADOS INICIAIS CRIADOS COM SUCESSO!");
        log.info(" > Admin:   admin@chat.com   | Senha: 123456");
        log.info(" > Comum:   usuario@chat.com | Senha: 123456");
        log.info("----------------------------------------------------");
    }
}