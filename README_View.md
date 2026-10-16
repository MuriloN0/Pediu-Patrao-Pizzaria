# Pediu Patrão

Projeto acadêmico de um sistema de pedidos para pizzaria.

## Funcionalidades

- Autenticação com os perfis ADMIN, GERENTE e ATENDENTE.
- Administração de usuários e atribuição de perfis pelo ADMIN.
- Cadastro e atualização de clientes pelo ATENDENTE.
- Cadastro, edição e exclusão de produtos pelo ATENDENTE e GERENTE.
- Registro de pedidos com itens, quantidades, preços e total calculado no servidor.
- Controle de transições dos pedidos, entrada, saída e respectivos responsáveis.
- Descontos de até 20% e cancelamento com justificativa, exclusivos do GERENTE e permitidos antes da saída.
- Auditoria de pedidos e alterações de clientes persistida no MongoDB.
- Consulta de auditoria exclusiva para ADMIN e GERENTE.
- Proteção CSRF nas operações e encerramento de sessão por logout.

## Requisitos

- Java 21.
- Maven 3.9 ou o Maven Wrapper incluído no projeto.
- Docker Desktop iniciado para executar o MongoDB localmente.
- MongoDB Compass para consultar o banco.

## Executar localmente

### 1. Iniciar o MongoDB

Na primeira execução, use no PowerShell:

```powershell
docker run --name pediupatrao-mongo -p 27017:27017 -v pediupatrao-mongo-data:/data/db -d mongo:7
```

Se o container já existir:

```powershell
docker start pediupatrao-mongo
```

No Compass, conecte usando:

```text
mongodb://localhost:27017
```

O banco utilizado pela aplicação é `pizzaria`.

### 2. Iniciar a aplicação

Abra um terminal na pasta que contém o arquivo `pom.xml`:

```powershell
cd .\PediuPatrao
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.data.mongodb.uri=mongodb://localhost:27017/pizzaria --server.port=8081"
```

Para executar pelo IntelliJ, abra a classe `PizzariaPedidosApplication` e configure estes **Program arguments**:

```text
--spring.data.mongodb.uri=mongodb://localhost:27017/pizzaria --server.port=8081
```

Acesse: http://localhost:8081/login

### 3. Acesso inicial

Em um banco novo, o inicializador cria o usuário de demonstração:

- Usuário: `admin`
- Senha: `teste123`
- Perfil: `ADMIN`

Use o ADMIN para cadastrar usuários GERENTE e ATENDENTE.

## Permissões

| Operação | ADMIN | GERENTE | ATENDENTE |
| --- | --- | --- | --- |
| Administrar usuários e atribuir perfis | Sim | Não | Não |
| Consultar pedidos | Sim | Sim | Sim |
| Registrar pedidos | Não | Não | Sim |
| Alterar andamento dos pedidos | Não | Sim | Não |
| Aplicar desconto ou cancelar | Não | Sim | Não |
| Consultar clientes | Não | Sim | Sim |
| Cadastrar e atualizar clientes | Não | Não | Sim |
| Cadastrar, editar e excluir produtos | Não | Sim | Sim |
| Consultar auditoria | Sim | Sim | Não |

As permissões são verificadas no backend. Requisições de alteração também precisam de um token CSRF válido.

## Fluxo dos pedidos

```text
RECEBIDO → EM_PREPARACAO → PRONTO → SAIU_PARA_ENTREGA → FINALIZADO
                                ↘ RETIRADO         → FINALIZADO
```

O cancelamento é permitido ao GERENTE nos estados `RECEBIDO`, `EM_PREPARACAO` e `PRONTO`, mediante justificativa. Pedidos cancelados não podem receber novas alterações. O desconto é calculado sobre o subtotal dos itens e não se acumula com o percentual anterior.

As datas são armazenadas em UTC e exibidas na interface conforme o fuso do navegador.

## Organização

O código da aplicação está em `PediuPatrao/src/main`:

- `java/com/umc/pediupatrao/config`: autenticação, autorização e inicialização.
- `java/com/umc/pediupatrao/controller`: páginas e APIs.
- `java/com/umc/pediupatrao/dto`: dados de entrada das operações.
- `java/com/umc/pediupatrao/entity`: modelos persistidos.
- `java/com/umc/pediupatrao/repository`: acesso ao MongoDB.
- `java/com/umc/pediupatrao/service`: regras de negócio e auditoria.
- `resources/templates`: páginas Thymeleaf.

