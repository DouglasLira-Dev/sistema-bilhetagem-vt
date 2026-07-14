# Sistema de Bilhetagem VT

[![CI](https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/actions/workflows/ci.yml/badge.svg)](https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![License](https://img.shields.io/badge/license-MIT-blue)
![Status](https://img.shields.io/badge/status-ativo-brightgreen)

Sistema desktop em Java para gerenciamento de solicitações de vale transporte (adesão, renúncia e alteração), desenvolvido para uso administrativo real. Nasceu como uma ferramenta interna e evoluiu para um projeto com controle de acesso por perfil, trilha de auditoria e cobertura de testes automatizados.

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Segurança](#segurança)
- [Tecnologias](#tecnologias)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como executar](#como-executar)
- [Primeiro acesso](#primeiro-acesso)
- [Perfis e permissões](#perfis-e-permissões)
- [Testes](#testes)
- [Roadmap](#roadmap)
- [Licença](#licença)

## Sobre o projeto

O objetivo é centralizar e facilitar a gestão de solicitações de vale transporte, permitindo:

- Cadastro manual de solicitações, com validação de CPF (dígitos verificadores).
- Importação em massa via planilha Excel (`.xlsx`), com validação linha a linha e relatório de erros.
- Consulta e filtro por nome, matrícula, CPF, mês de referência e tipo de solicitação.
- Relatórios mensais com gráficos (barras e pizza) e exportação para Excel.
- Exportação de dados completos ou resumidos.
- Gestão de usuários e perfis de acesso.
- Trilha de auditoria completa (quem fez o quê, quando, e de onde).

## Funcionalidades

| Módulo | Descrição |
|---|---|
| Cadastro | Criação e edição de solicitações, com validação de CPF e campos obrigatórios |
| Importação | Leitura de planilhas Excel com validação por linha e contagem de sucessos/erros |
| Exportação | Exportação para Excel (dados completos ou relatório resumido) |
| Relatórios | Consolidação mensal com gráficos e exportação do relatório |
| Usuários | CRUD de usuários, atribuição de perfil, ativação/desativação |
| Auditoria | Log de toda ação sensível (criação, edição, exclusão, login/logout, import/export), com host de origem |

## Segurança

Este projeto tem uma trilha de remediação de segurança documentada e aplicada de ponta a ponta — não é apenas login e senha:

- **Senhas com BCrypt** (`CriptografiaUtil`), com migração automática e transparente de hashes legados no primeiro login pós-atualização.
- **Controle de acesso por perfil (RBAC)** aplicado de forma consistente: cada tela sensível (`TelaUsuarios`, `TelaAuditoria`, `TelaImportacao`, `TelaExportacao`, `TelaRelatorio`) verifica a permissão do usuário logado tanto na abertura quanto em cada ação — não depende apenas do menu estar desabilitado.
- **Primeiro acesso seguro**: nenhuma credencial padrão é criada no banco. No primeiro start sem nenhum usuário cadastrado, o sistema gera um administrador com senha aleatória de 12 caracteres, exibida uma única vez.
- **Rate limiting no login** (`RateLimiterService`): bloqueio temporário após tentativas de login malsucedidas consecutivas.
- **Expiração de sessão ativa**: um watchdog (`javax.swing.Timer`) reverifica periodicamente se a sessão expirou, mesmo com a janela aberta e sem interação — não depende de reabrir a tela.
- **Auditoria confiável**: toda ação só é registrada após a operação ser efetivamente persistida (cancelar um cadastro ou uma importação sem sucesso não gera um log falso-positivo).
- **SQL parametrizado** em 100% das consultas (nenhuma concatenação de string em SQL).
- **Exclusão lógica (soft delete)** nas solicitações, com auditoria preservada mesmo após exclusão.

Para detalhes completos e como reportar uma vulnerabilidade, veja [SECURITY.md](SECURITY.md).

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem e runtime |
| Swing | Interface gráfica desktop |
| SQLite (`sqlite-jdbc`) | Persistência local |
| Apache POI | Leitura/escrita de planilhas Excel |
| JFreeChart | Gráficos nos relatórios |
| jBCrypt | Hash de senhas |
| Log4j2 | Logging estruturado |
| JUnit 5 + Mockito | Testes automatizados |
| Maven | Build e gestão de dependências |
| GitHub Actions | Integração contínua (compilação + testes a cada push/PR) |

## Estrutura do projeto

```
sistema-bilhetagem-vt/
├── .github/workflows/ci.yml       # Pipeline de CI (build + testes)
├── src/
│   ├── main/
│   │   ├── java/com/bilhetagem/
│   │   │   ├── Main.java          # Ponto de entrada
│   │   │   ├── dao/               # Acesso a dados (SQLite, SQL parametrizado)
│   │   │   ├── model/             # Entidades e enums (Perfil, Permissao)
│   │   │   ├── service/           # Regras de negócio (auditoria, rate limit)
│   │   │   ├── util/              # Criptografia, CPF, Excel, sessão, gráficos
│   │   │   └── view/              # Telas Swing
│   │   └── resources/
│   │       ├── database/schema.sql
│   │       └── log4j2.xml
│   └── test/java/com/bilhetagem/  # Testes JUnit 5 + Mockito
├── pom.xml
├── SECURITY.md
├── CHANGELOG.md
└── LICENSE
```

## Como executar

Pré-requisitos: JDK 17+ e Maven.

```bash
git clone https://github.com/DouglasLira-Dev/sistema-bilhetagem-vt.git
cd sistema-bilhetagem-vt
mvn clean package
java -jar target/sistema-bilhetagem-vt-1.0.0-SNAPSHOT.jar
```

O banco SQLite é criado automaticamente (pasta `data/`) na primeira execução, junto com o schema.

## Primeiro acesso

Como não existe usuário/senha padrão no banco, na primeira execução (banco vazio) o sistema:

1. Detecta que não há nenhum usuário cadastrado.
2. Cria um usuário `admin` com senha aleatória de 12 caracteres.
3. Exibe essa senha uma única vez, em um popup — anote antes de fechar.
4. A partir daí, use a tela **Gerenciar Usuários** (perfil Administrador) para criar os demais usuários e definir seus perfis.

## Perfis e permissões

| Permissão | Administrador | Gerente | Operador | Consultor |
|---|:---:|:---:|:---:|:---:|
| Cadastrar solicitação | ✅ | ✅ | ✅ | ❌ |
| Editar solicitação | ✅ | ✅ | ✅ | ❌ |
| Excluir solicitação | ✅ | ❌ | ❌ | ❌ |
| Consultar solicitação | ✅ | ✅ | ✅ | ✅ |
| Importar dados (Excel) | ✅ | ✅ | ❌ | ❌ |
| Exportar dados (Excel) | ✅ | ✅ | ❌ | ❌ |
| Gerar relatórios | ✅ | ✅ | ❌ | ❌ |
| Ver auditoria | ✅ | ✅ | ❌ | ❌ |
| Gerenciar usuários | ✅ | ❌ | ❌ | ❌ |

## Testes

```bash
mvn test
```

Cobertura atual: autenticação e migração de senha (`UsuarioDAOTest`), regras de negócio de solicitações (`SolicitacaoDAOTest`), rate limiting (`RateLimiterServiceTest`), validação de CPF (`CpfUtilTest`) e controle de sessão/RBAC (`SessaoUtilTest`). Os testes rodam automaticamente a cada `push`/`pull request` via GitHub Actions.

## Roadmap

- [ ] Exibir nome do usuário (em vez do ID) na tela de auditoria.
- [ ] Pool de conexões (ex: HikariCP) caso o acesso ao banco deixe de ser estritamente sequencial.
- [ ] Internacionalização da interface (hoje 100% em pt-BR).

## Licença

Distribuído sob a licença MIT — veja [LICENSE](LICENSE) para detalhes.

## Autor

**Douglas Lira** — [GitHub](https://github.com/DouglasLira-Dev)