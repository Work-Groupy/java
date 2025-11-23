# Work-Group API (Java / Spring Boot)

API REST para gerenciamento de usuários (cadastro, listagem, paginação, atualização seletiva, login e verificação de existência de e‑mail), com suporte a:
- Persistência via Spring Data JPA (Oracle)
- Validação (Jakarta Bean Validation + Hibernate Validator)
- HATEOAS (endpoint introdutório com links navegáveis)
- Cache (Caffeine + anotações Spring Cache)
- Criptografia de senhas (PasswordEncoder – BCrypt recomendado)
- Tratamento global de exceções
- Upload/armazenamento de binários simples (imagens/perfil/currículo via `byte[]`)

---

## Sumário

1. Arquitetura e Fluxo  
2. Tecnologias  
3. Estrutura de Pacotes  
4. Modelo de Dados: User  
5. Validações  
6. Cache (Estratégia)  
7. Endpoints e Exemplos (curl)  
8. HATEOAS (Endpoint raiz `/user`)  
9. Paginação  
10. Login e Segurança de Senha  
11. Atualização Seletiva (`PUT /user/update/{id}`)  
12. Exceções e Formatos de Erro  
13. Configuração de Banco (Oracle)  
14. Build & Execução  
15. Dependências Principais (`pom.xml`)  
16. Como Testar Localmente  
17. Extensões Futuras Sugeridas  
18. Contribuição  
19. Licença (placeholder)  
20. CRUD de Funcionários (Postman)  
21. Anexos: Exemplos de Corpo de Requisição  
22. Observações Importantes  
23. Perguntas Frequentes (FAQ)  
24. Contato  

---

## 1. Arquitetura e Fluxo

Camadas principais identificadas:
```
Controller (UserController) -> Service (UserService) -> Repository (UserRepository) -> Oracle DB
                                   |-> PasswordEncoder (hash)
                                   |-> Cache (Caffeine)
HATEOAS: IntroAssembler gera links navegáveis
GlobalExceptionHandler: padroniza erros
```

Fluxo de criação de usuário:
1. Controller recebe JSON (`POST /user/create`)
2. Validação (@Valid) dos campos
3. Service aplica hash na senha e salva via Repository
4. Cache é invalidado para listas/páginas
5. Retorna `200 OK` com objeto persistido

---

## 2. Tecnologias

| Função              | Biblioteca |
|---------------------|------------|
| Framework Web       | Spring Boot 3.5.7 |
| Persistência        | Spring Data JPA + Hibernate |
| Banco de Dados      | Oracle (driver `ojdbc11`) |
| Validação           | Jakarta Validation + Hibernate Validator |
| HATEOAS             | spring-boot-starter-hateoas |
| Cache               | spring-boot-starter-cache + Caffeine |
| Criptografia        | spring-security-crypto (PasswordEncoder) |
| DTO / Boilerplate   | Lombok |
| Testes              | spring-boot-starter-test |
| Dev Reload          | spring-boot-devtools |

---

## 3. Estrutura de Pacotes

```
br.com.fiap.workgroup
 ├── controllers        (UserController, FuncionarioController)
 ├── services           (UserService)
 ├── repositories       (UserRepository, FuncionarioRepository)
 ├── models             (User, Funcionario)
 ├── dtos               (IntroDTO, LoginDTO, LoginResponseDTO)
 ├── hateoas            (IntroAssembler)
 └── exceptions         (GlobalExceptionHandler)
```

---

## 4. Modelo de Dados: User

Tabela: `T_WG_USER`

Campos:
| Campo (Java)  | Coluna DB         | Tipo DB     | Descrição |
|---------------|-------------------|-------------|-----------|
| id            | ID_USER           | NUMBER (seq)| Identificador (Sequence `T_WG_USER_SEQ`) |
| name          | NM_USER           | VARCHAR     | Nome do usuário |
| email         | ID_EMAIL          | VARCHAR     | E-mail único |
| password      | CD_SENHA          | VARCHAR     | Hash da senha |
| created_at    | DT_CRIADA         | DATE        | Timestamp de criação (inserido automaticamente) |
| profile       | IMG_PERFIL        | BLOB        | Byte array (imagem/perfil) |
| resume        | IMG_CURRICULO     | BLOB        | Byte array (currículo/documento) |
| bio           | DS_BIOGRAFIA      | VARCHAR(100)| Biografia curta |

---

## 5. Validações

Aplicadas via anotações em `User` e `LoginDTO`:

| Campo     | Regra |
|-----------|-------|
| name      | `@NotBlank` |
| email     | `@NotBlank` + regex email |
| password  | `@NotBlank` + regex forte (mín. 8 chars, maiúscula, minúscula, dígito, especial) |

Regex senha:
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}\[\]|:;\"'<>,.?/])[A-Za-z\d!@#$%^&*()_+\-={}\[\]|:;\"'<>,.?/]{8,}$
```

Erros de validação retornam `400 Bad Request` com mapa `field -> mensagem`.

---

## 6. Cache (Estratégia)

Anotações em `UserService`:

| Método              | Comportamento |
|---------------------|---------------|
| findAllPageable     | `@Cacheable("users_page")` |
| findAll             | `@Cacheable("users")` |
| findById(id)        | `@Cacheable(value="user", key="#id")` |
| create / update / delete | `@CacheEvict` limpa `users`, `user`, `users_page` |

Efeito: leituras repetidas são servidas do cache; qualquer mutação invalida entradas para garantir consistência.

---

## 7. Endpoints e Exemplos (curl)

Base URL padrão (local): `http://localhost:8080`

### 7.1 GET /user
Retorna DTO introdutório com links (HATEOAS).
```
curl -X GET http://localhost:8080/user
```
Resposta (exemplo):
```json
{
  "title": "Workgroup API",
  "message": "Welcome to the Workgroup API! Use the provided links to navigate through the available resources.",
  "_links": {
    "listar-usuarios": { "href": "http://localhost:8080/user/all" },
    "listar-usuarios-pelo-id": { "href": "http://localhost:8080/user/{id}" },
    "cadastrar-usuarios": { "href": "http://localhost:8080/user/create" },
    "atualizar-usuarios": { "href": "http://localhost:8080/user/update/{id}" },
    "deletar-usuarios": { "href": "http://localhost:8080/user/delete/{id}" }
  }
}
```

### 7.2 POST /user/create
Cria usuário (senha é automaticamente hasheada).
```
curl -X POST http://localhost:8080/user/create \
  -H "Content-Type: application/json" \
  -d '{
        "name":"Alice",
        "email":"alice@example.com",
        "password":"Str0ng@Senha",
        "bio": "Desenvolvedora"
      }'
```
Resposta:
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "password": "$2a$10$...",
  "created_at": "2025-11-22",
  "profile": null,
  "resume": null,
  "bio": "Desenvolvedora"
}
```

### 7.3 GET /user/all
```
curl http://localhost:8080/user/all
```

### 7.4 GET /user/page?page=0&size=10&sort=name,asc
```
curl "http://localhost:8080/user/page?page=0&size=5&sort=name,asc"
```
Resposta (exemplo):
```json
{
  "content": [ { "id":1,"name":"Alice","email":"alice@example.com" } ],
  "pageable": { "pageNumber":0,"pageSize":5 },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "size": 5,
  "number": 0
}
```

### 7.5 GET /user/{id}
```
curl http://localhost:8080/user/1
```

### 7.6 PUT /user/update/{id}
Atualização seletiva.
```
curl -X PUT http://localhost:8080/user/update/1 \
  -H "Content-Type: application/json" \
  -d '{ "bio": "Bio atualizada", "password": "NovaStr0ng@Senha" }'
```

### 7.7 DELETE /user/delete/{id}
```
curl -X DELETE http://localhost:8080/user/delete/1
```
Resposta:
```
"User deleted successfully!"
```

### 7.8 POST /user/login
```
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{ "email":"alice@example.com", "password":"Str0ng@Senha" }'
```
Resposta:
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com"
}
```

### 7.9 GET /user/exists?email=alice@example.com
```
curl "http://localhost:8080/user/exists?email=alice@example.com"
```
Resposta:
```json
true
```

---

## 8. HATEOAS (IntroAssembler)

`IntroAssembler` constrói `_links` usando `linkTo(methodOn(...))`.
Rels em português:
- listar-usuarios
- listar-usuarios-pelo-id
- cadastrar-usuarios
- atualizar-usuarios
- deletar-usuarios

---

## 9. Paginação

Endpoint: `GET /user/page`

| Parâmetro | Exemplo | Descrição         |
|-----------|---------|------------------|
| page      | 0       | Índice (0-based) |
| size      | 20      | Tamanho da página |
| sort      | name,asc| Campo + direção  |

---

## 10. Login e Segurança de Senha

- Hash aplicado em `create` e `update` quando senha não é vazia.
- Comparação: `passwordEncoder.matches(raw, hashed)`.

Exemplo bean:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
}
```

---

## 11. Atualização Seletiva

`PUT /user/update/{id}`:
- Atualiza somente campos presentes e não nulos.
- Lobs só substituídos se enviados.
- Senha re-hasheada se enviada.

---

## 12. Exceções e Formatos de Erro

| Caso                                | Status | Corpo (exemplo) |
|-------------------------------------|--------|-----------------|
| MethodArgumentNotValidException     | 400    | `{ "campo": "mensagem" }` |
| HttpMessageNotReadableException     | 400    | `"Invalid JSON format. Please check your request body."` |
| NotFoundException (Spring ChangeSet)| 404    | Mensagem |
| Exception (genérica)                | 500    | `"An unexpected error occurred."` |

---

## 13. Configuração de Banco (Oracle)

`application.properties`:
```
spring.datasource.url=jdbc:oracle:thin:@//HOST:PORT/SERVICE
spring.datasource.username=USUARIO
spring.datasource.password=SENHA
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

spring.cache.type=caffeine
```

---

## 14. Build & Execução

Build:
```
./mvnw clean package
```

Run:
```
./mvnw spring-boot:run
# ou
java -jar target/workgroup-0.0.1-SNAPSHOT.jar
```

---

## 15. Dependências Principais (Resumo do pom.xml)

```xml
<dependency> spring-boot-starter-data-jpa </dependency>
<dependency> spring-boot-starter-web </dependency>
<dependency> jakarta.validation-api </dependency>
<dependency> hibernate-validator + hibernate-validator-cdi </dependency>
<dependency> ojdbc11 (runtime) </dependency>
<dependency> spring-boot-starter-hateoas </dependency>
<dependency> spring-boot-starter-cache </dependency>
<dependency> caffeine </dependency>
<dependency> spring-security-crypto </dependency>
<dependency> lombok </dependency>
<dependency> spring-boot-starter-test </dependency>
<dependency> spring-boot-devtools </dependency>
```

---

## 16. Como Testar Localmente

1. Suba Oracle ou adapte para H2.
2. Configure `application.properties`.
3. Inicie aplicação.
4. Fluxo: criar usuário → listar → paginar → login → atualizar → deletar.
5. Validar comportamento de cache.
6. Criar testes unitários/integrados conforme sugerido.

---

## 17. Extensões Futuras Sugeridas

| Tema                | Sugestão |
|---------------------|----------|
| Autenticação JWT    | Security + emissão de token |
| Padronização erros  | Exceções custom + RFC 7807 |
| Observabilidade     | Actuator + métricas |
| Documentação API    | springdoc-openapi |
| Upload Imagens      | S3 / MinIO |
| Migrações DB        | Flyway |
| Rate limiting       | Bucket4j / Resilience4j |
| Melhor cache        | TTL + métricas |
| DTOs específicos    | Remover `password` das respostas |
| Tratamento de LOB   | Endpoints multipart dedicados |

---

## 18. Contribuição

1. Branch: `git checkout -b feat/nova-funcionalidade`
2. Implementar.
3. Build: `./mvnw clean verify`
4. Pull Request com objetivo, mudanças, testes, impacto.

Commits:
```
feat(user): adiciona endpoint de alteração de avatar
fix(user): corrige validação de e-mail
```

---

## 19. Licença

Ainda não definida. Adicionar arquivo `LICENSE` (ex.: MIT / Apache 2.0).

---

## 20. CRUD de Funcionários (Postman)

A entidade `Funcionario`:
```json
{
  "id": 1,
  "nome": "Fulano de Tal",
  "cargo": "Desenvolvedor",
  "salario": 7500.0
}
```

Base URL: `http://localhost:8080/funcionarios`

### 20.1 Criar Funcionário (POST /funcionarios)

Postman:
- Método: POST
- URL: `http://localhost:8080/funcionarios`
- Body (raw JSON):
```json
{
  "nome": "Maria Souza",
  "cargo": "Analista",
  "salario": 6800.0
}
```

Resposta (exemplo):
```json
{
  "id": 1,
  "nome": "Maria Souza",
  "cargo": "Analista",
  "salario": 6800.0
}
```

Curl:
```
curl -X POST http://localhost:8080/funcionarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Souza","cargo":"Analista","salario":6800.0}'
```

### 20.2 Listar Todos (GET /funcionarios)

```
curl http://localhost:8080/funcionarios
```
Resposta:
```json
[
  {
    "id": 1,
    "nome": "Maria Souza",
    "cargo": "Analista",
    "salario": 6800.0
  },
  {
    "id": 2,
    "nome": "João Silva",
    "cargo": "Dev Backend",
    "salario": 8000.0
  }
]
```

### 20.3 Buscar por ID (GET /funcionarios/{id})

```
curl http://localhost:8080/funcionarios/1
```

Erro se não existir: atualmente lança `RuntimeException` (retorno HTTP 500). Sugestão: substituir por exceção custom com `@ResponseStatus(HttpStatus.NOT_FOUND)`.

### 20.4 Atualizar Funcionário (PUT /funcionarios/{id})

PUT substitui todos os campos (não é parcial).
```
curl -X PUT http://localhost:8080/funcionarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria S. Souza","cargo":"Analista Sênior","salario":7800.0}'
```

Body no Postman:
```json
{
  "nome": "Maria S. Souza",
  "cargo": "Analista Sênior",
  "salario": 7800.0
}
```

Observação: Para suportar atualização parcial futura, implementar `PATCH` ou lógica que ignore campos `null`.

### 20.5 Remover Funcionário (DELETE /funcionarios/{id})

```
curl -X DELETE http://localhost:8080/funcionarios/1
```

Resposta atual: corpo vazio (status 200 ou 204 dependendo da configuração). Recomenda-se retornar mensagem ou usar 204.

### 20.6 Boas Práticas Futuras

- Adicionar validações:
  ```java
  @NotBlank private String nome;
  @NotBlank private String cargo;
  @PositiveOrZero private double salario;
  ```
- Padronizar resposta de erro 404.
- Introduzir DTO (ex.: FuncionarioResponseDTO).
- Testes: `@WebMvcTest(FuncionarioController.class)` + mocks.
- Paginação futura: criar endpoint `GET /funcionarios/page?page=0&size=10`.
- Cache se necessário (similar ao usuário).

### 20.7 Exemplo de Collection Postman

```
Workgroup API
 └── Funcionarios
      ├── Create Funcionário
      ├── List Funcionários
      ├── Get Funcionário por ID
      ├── Update Funcionário
      └── Delete Funcionário
```

Usar variável `{{baseUrl}}` = `http://localhost:8080`.

---

## 21. Anexos: Exemplos de Corpo de Requisição

### Usuário Completo (Create)
```json
{
  "name": "Maria Silva",
  "email": "maria.silva@example.com",
  "password": "Fort3@Senha",
  "bio": "Analista de sistemas",
  "profile": "BASE64-OPCIONAL",
  "resume": "BASE64-OPCIONAL"
}
```

### Atualização Seletiva (PUT User)
```json
{
  "bio": "Nova bio",
  "password": "Nov4@Senha"
}
```

---

## 22. Observações Importantes

- Senha não deve ser logada em texto puro.
- Retorno do create de usuário inclui hash — considerar ocultar em DTO.
- Exceções genéricas → 500; padronizar com objeto estruturado.
- Funcionários ainda sem validação/camada service/cache.
- Adicionar testes e DTOs melhora manutenção.

---

## 23. Perguntas Frequentes (FAQ)

1. Por que a senha não autentica após atualização?  
   - Verifique regex e presença do bean `PasswordEncoder`.

2. Como adicionar TTL ao cache?  
   - Propriedades:
     ```
     spring.cache.cache-names=users,users_page,user
     spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m
     ```

3. Como evitar expor hash de senha?  
   - Usar DTO de resposta sem `password`.

4. Posso usar PATCH para funcionário?  
   - Sim. Criar `@PatchMapping` e aplicar merge parcial (ignorar `null`).

---

## 24. Contato

Use Issues do repositório para bugs e sugestões.

---

Bom desenvolvimento!
