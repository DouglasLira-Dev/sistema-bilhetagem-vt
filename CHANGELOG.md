# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto segue [Versionamento Semântico](https://semver.org/lang/pt-BR/)
(`MAJOR.MINOR.PATCH`), com uma ressalva: como não há histórico de commits/tags
anterior a esta reconstrução, os números de versão abaixo são uma organização
lógica da evolução do projeto, não uma extração literal do Git. Ajuste-os se já
existirem tags divergentes no repositório.

Categorias usadas em cada versão: `Adicionado`, `Alterado`, `Corrigido`,
`Removido`, `Segurança`. Uma categoria só aparece na versão se houver algo
relevante para ela.

## [Não publicado]

Nenhuma mudança pendente no momento.

## [1.1.0] — 2026-07-14 — Documentação e padronização de autoria

### Adicionado
- `SECURITY.md`: política de segurança formal, cobrindo autenticação, RBAC,
  persistência, auditoria, limitações conhecidas e processo de disclosure
  responsável de vulnerabilidades.
- `CHANGELOG.md` (este arquivo).

### Alterado
- Padronizada a tag Javadoc `@author` em **30 arquivos** que ainda tinham
  placeholder genérico ou nenhuma tag, unificando para `Douglas Lira`
  (nome confirmado a partir do `LICENSE` do projeto):
  - **23 arquivos** com `@author [Seu Nome]`: `Main`, `ConexaoBD`,
    `LogAuditoriaDAO`, `LogAuditoriaDAOImpl`, `SolicitacaoDAOImpl`,
    `UsuarioDAOImpl`, `LogAuditoria`, `Solicitacao`, `Usuario`,
    `AuditoriaService`, `BancoUtil`, `CriptografiaUtil`, `ExcelUtil`,
    `GraficoUtil`, `MigracaoSenhaUtil`, `PrimeiroAcessoUtil`, `SessaoUtil`,
    `TelaAuditoria`, `TelaCadastro`, `TelaExportacao`, `TelaImportacao`,
    `TelaLogin`, `TelaRelatorio`.
  - **6 arquivos** com `@author Equipe de Desenvolvimento`: `SolicitacaoDAO`,
    `RateLimiterService`, `CpfUtil`, `TelaUsuarios`, `TelaPrincipal`,
    `CpfUtilTest`, `SessaoUtilTest`.
  - **1 arquivo** sem tag `@author` nenhuma: `UsuarioDAO`.
- Corrigida a `<url>` do projeto no `pom.xml`, que apontava para o placeholder
  `https://github.com/[seu-usuario]/...` e agora referencia o repositório real
  (`https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt`).
- `README.md` reescrito do zero: removida referência a uma pasta `controller/`
  que não existe mais na estrutura atual; adicionadas seção de segurança,
  tabela de perfis × permissões (RBAC), badges (CI, Java, licença), passo a
  passo de instalação/primeiro acesso, estrutura real do projeto e roadmap.

## [1.0.0] — Auditoria confiável e fechamento do RBAC

### Corrigido
- **Auditoria**: ações de criar/editar solicitação, importar e exportar dados
  só geram log **após a operação ser efetivamente concluída com sucesso**.
  Antes, cancelar uma tela ou uma importação sem nenhum registro válido ainda
  gerava uma entrada de auditoria (falso positivo).
- Padronizada a geração de ID (`SELECT last_insert_rowid()`) em todos os DAOs
  (`UsuarioDAOImpl`, `SolicitacaoDAOImpl`, `LogAuditoriaDAOImpl`), removendo o
  fallback obsoleto `"localhost"` como IP.
- Validação de CPF (dígitos verificadores) aplicada também na importação em
  massa via Excel, não apenas no cadastro manual — antes era possível importar
  CPFs inválidos por planilha.

### Adicionado
- Exportação de relatório mensal para Excel na tela de Relatórios (antes era
  um placeholder "em desenvolvimento", embora a função já existisse em
  `ExcelUtil`).
- Watchdog de expiração de sessão: um timer periódico (`javax.swing.Timer`)
  agora reverifica a sessão mesmo com a janela principal aberta e sem
  interação, em vez de checar apenas na abertura da tela.
- Auto-checagem de permissão (defesa em profundidade) nos construtores de
  `TelaImportacao`, `TelaExportacao` e `TelaRelatorio`, no mesmo padrão já
  usado por `TelaUsuarios` e `TelaAuditoria`.

## [0.4.0] — Correção crítica de primeiro acesso

### Corrigido
- **Crítico**: `PrimeiroAcessoUtil.verificarPrimeiroAcesso()` existia mas
  nunca era chamada — bancos novos ficavam sem nenhum usuário e sem forma de
  logar. Corrigido chamando o método em `Main.java` logo após a inicialização
  do banco.

## [0.3.0] — Gestão de usuários e senha segura no primeiro acesso

### Adicionado
- Tela de gerenciamento de usuários (`TelaUsuarios`): criar, editar,
  ativar/desativar usuários, com controle de acesso por permissão.
- `PrimeiroAcessoUtil`: geração de administrador com senha aleatória de 12
  caracteres no primeiro start, exibida uma única vez em tela, eliminando a
  necessidade de uma credencial padrão.
- `CpfUtil`: validação de CPF por dígito verificador.
- Testes automatizados para `UsuarioDAO`, `RateLimiterService`, `CpfUtil` e
  `SessaoUtil`.
- Dependência do Mockito (`mockito-core`, `mockito-junit-jupiter`) para
  testes.

### Removido
- Inserção automática de usuário `admin` com senha padrão no `schema.sql`.

### Segurança
- Eliminada a credencial padrão fixa, reduzindo a superfície de ataque em
  instalações novas não configuradas.

## [0.2.0] — Fundamentos de segurança (Fase 1)

### Adicionado
- Hash de senhas com BCrypt (`CriptografiaUtil`) e migração automática e
  transparente de senhas em formato legado (`MigracaoSenhaUtil`), aplicada no
  próximo login bem-sucedido.
- Controle de acesso por perfil (RBAC) aplicado nas telas principais, com
  quatro perfis (`Administrador`, `Gerente`, `Operador`, `Consultor`).
- Rate limiting de tentativas de login (`RateLimiterService`), bloqueando
  temporariamente novas tentativas após um número definido de falhas
  consecutivas.
- Log de auditoria (`AuditoriaService`, `LogAuditoriaDAO`) para ações
  sensíveis do sistema (login/logout, criação/edição/exclusão de solicitação,
  importação/exportação, gestão de usuários).
- Pipeline de CI (GitHub Actions) rodando `mvn test` a cada push/pull
  request.

### Segurança
- Início da postura de segurança do projeto: senhas deixam de ser
  armazenadas em texto plano; consultas SQL já usavam `PreparedStatement`
  parametrizado em todos os DAOs.

## [0.1.0] — Versão inicial

### Adicionado
- Cadastro, edição, consulta e exclusão lógica (`soft delete`) de
  solicitações de vale transporte.
- Importação e exportação de dados via planilha Excel (Apache POI).
- Relatórios mensais com gráficos (JFreeChart).
- Persistência local em SQLite.

[Não publicado]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v0.4.0...v1.0.0
[0.4.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/releases/tag/v0.1.0