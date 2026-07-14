# Política de Segurança

Este documento descreve as medidas de segurança implementadas no Sistema de Bilhetagem VT e como reportar uma vulnerabilidade.

## Medidas implementadas

### Autenticação e credenciais

- Senhas armazenadas com **BCrypt** (`CriptografiaUtil`), nunca em texto plano.
- Migração automática e transparente: hashes de um formato legado (se existentes) são migrados para BCrypt no próximo login bem-sucedido, sem exigir ação do usuário.
- Nenhuma credencial padrão é inserida no banco. Em uma instalação nova (sem usuários), `PrimeiroAcessoUtil` cria um administrador com senha aleatória de 12 caracteres, exibida uma única vez em tela — nunca gravada em log ou arquivo.
- **Rate limiting** (`RateLimiterService`): após um número definido de tentativas de login malsucedidas consecutivas para o mesmo usuário, novas tentativas são bloqueadas temporariamente.

### Controle de acesso (RBAC)

- Quatro perfis (`Administrador`, `Gerente`, `Operador`, `Consultor`), cada um com um conjunto fixo de permissões (`Usuario.Permissao`), definidos em código (`PERMISSOES` em `Usuario.java`) — não editáveis via UI, o que evita escalonamento de privilégio por dado malformado.
- Cada tela sensível verifica a permissão do usuário **duas vezes**: (1) o item de menu/botão que a abre só fica habilitado se o usuário tiver a permissão, e (2) a própria tela reverifica no construtor e lança `SecurityException` se a checagem falhar — mesmo que o primeiro ponto de verificação seja contornado.
- Sessão com expiração por tempo. Um `javax.swing.Timer` reverifica periodicamente se a sessão expirou, mesmo com a janela principal aberta e sem interação do usuário — a verificação não depende de reabrir a tela.

### Persistência e integridade dos dados

- Todas as consultas SQL usam `PreparedStatement` com parâmetros — nenhuma concatenação de string em SQL em nenhum DAO.
- Exclusão lógica (`soft delete`) nas solicitações: registros excluídos permanecem no banco com `deleted_at` preenchido, e todas as consultas de leitura e atualização filtram por `deleted_at IS NULL`.

### Auditoria

- Toda ação sensível (criação, edição, exclusão de solicitação; login/logout; importação/exportação de dados; gestão de usuários) é registrada em `logs_auditoria`, com usuário, ação, entidade afetada, detalhes e host de origem.
- O log só é gravado **após a operação ser efetivamente concluída com sucesso** — cancelar uma ação ou uma importação que falhou integralmente não gera uma entrada de auditoria enganosa.
- Como esta é uma aplicação desktop (sem requisições HTTP), o "IP de origem" registrado é o hostname + endereço IP da máquina local, resolvido via `InetAddress.getLocalHost()`.

## Limitações conhecidas

Sendo transparente sobre o que este projeto **não** cobre hoje:

- É uma aplicação desktop de uso local/interno — não foi desenhada para exposição direta à internet.
- O banco SQLite não é criptografado em repouso; a segurança física da máquina onde roda é uma premissa.
- Não há criptografia em trânsito (não aplicável — não há tráfego de rede além do acesso ao arquivo local do banco).

## Reportando uma vulnerabilidade

Se você encontrar uma vulnerabilidade de segurança neste projeto, por favor **não abra uma issue pública**. Em vez disso, entre em contato diretamente através do perfil do GitHub [DouglasLira-Dev](https://github.com/DouglasLira-Dev) ou por e-mail, se disponível no perfil. Descreva:

- O passo a passo para reproduzir o problema.
- O impacto potencial.
- Sugestões de correção, se tiver.

Todo relato será analisado e uma correção será priorizada conforme a severidade.