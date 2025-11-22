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
 ├── controllers        (UserController)
 ├── services           (UserService)
 ├── repositories       (UserRepository)
 ├── models             (User)
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
Lista todos os usuários (cacheado).
```
curl http://localhost:8080/user/all
```

### 7.4 GET /user/page?page=0&size=10&sort=name,asc
Paginação (Spring Data Pageable).
```
curl "http://localhost:8080/user/page?page=0&size=5&sort=name,asc"
```
Resposta (exemplo):
```json
{
  "content": [ { "id":1,"name":"Alice","email":"alice@example.com", ... } ],
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
Atualização seletiva (somente campos não nulos no body sobrescrevem).
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
Autentica por e‑mail/senha (comparação de hash).
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
Verifica existência (case insensitive).
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
Links possuem `rel` em português:
- listar-usuarios
- listar-usuarios-pelo-id
- cadastrar-usuarios
- atualizar-usuarios
- deletar-usuarios

Útil para clientes que seguem navegação dirigida por hipermídia.

---

## 9. Paginação

Endpoint: `GET /user/page`

Parâmetros suportados (padrão Spring):
| Parâmetro | Exemplo | Descrição |
|-----------|---------|-----------|
| page      | 0       | Índice (0-based) |
| size      | 20      | Tamanho da página |
| sort      | name,asc| Ordenação (campo, direção) |

---

## 10. Login e Segurança de Senha

- Hash aplicado em `create` e `update` quando senha não é vazia.
- Comparação: `passwordEncoder.matches(raw, hashed)`.
- Necessário declarar um bean `PasswordEncoder` (exemplo):

```java
// Exemplo de configuração (adicionar em classe @Configuration)
@Bean
public PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
}
```

Sem isso, a injeção (`@Autowired PasswordEncoder`) falhará.

---

## 11. Atualização Seletiva

`PUT /user/update/{id}`:
- Atualiza somente campos presentes e não nulos no corpo.
- Arrays `profile` e `resume` só substituídos se tamanho > 0.
- Senha re-hasheada se enviada (mesma política da criação).

Sugestão para atualizações parciais futuras: implementar `PATCH` com JSON Merge Patch ou JSON Patch. Atualmente o `PUT` já funciona como "partial update".

---

## 12. Exceções e Formatos de Erro

`GlobalExceptionHandler` cobre:

| Classe / Caso                          | Status | Corpo |
|----------------------------------------|--------|-------|
| `MethodArgumentNotValidException`      | 400    | `{ "field": "mensagem" , ... }` |
| `HttpMessageNotReadableException`      | 400    | `"Invalid JSON format. Please check your request body."` |
| `NotFoundException` (Spring ChangeSet) | 404    | Mensagem da exceção |
| Qualquer outra (`Exception`)           | 500    | `"An unexpected error occurred."` (stack trace no console) |

OBS: Em `UserService.findById` e outros throws é usada `RuntimeException("User not found...")`. Isto cairá no handler genérico como 500; recomendável padronizar para retornar 404 (ex. lançar `ResponseStatusException(HttpStatus.NOT_FOUND, ...)` ou criar exceção custom).

---

## 13. Configuração de Banco (Oracle)

`application.properties` (exemplo):
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

Dependendo do ambiente de desenvolvimento, pode ser conveniente:
- Usar Oracle XE via container.
- Perfil `test` substituindo por H2 (apenas para testes rápidos; atenção às diferenças de comportamento de sequences).

---

## 14. Build & Execução

Build:
```
./mvnw clean package
```

Execução:
```
./mvnw spring-boot:run
# ou
java -jar target/workgroup-0.0.1-SNAPSHOT.jar
```

Porta padrão: `8080`.

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
<dependency> lombok (optional + annotationProcessor) </dependency>
<dependency> spring-boot-starter-test (test) </dependency>
<dependency> spring-boot-devtools (runtime optional) </dependency>
```

---

## 16. Como Testar Localmente

1. Garanta Oracle acessível (ou adapte para H2).
2. Configure `application.properties`.
3. Inicie a aplicação.
4. Teste fluxo completo:
   - Criar usuário
   - Verificar `/user/all`
   - Paginar `/user/page?page=0&size=2`
   - Login
   - Atualizar senha
   - Apagar usuário
5. Inspecione logs para validação de cache (uso pode ser observado adicionando log nas operações).

Testes automatizados não foram inspecionados (nenhum teste listado). Sugestão:
- Criar testes unitários para `UserService` (mock `UserRepository` + `PasswordEncoder`).
- Testes de integração com banco (Testcontainers Oracle ou H2 com script adaptado).
- Teste de controller usando `@WebMvcTest`.

---

## 17. Extensões Futuras Sugeridas

| Tema                | Ação |
|---------------------|------|
| Autenticação JWT    | Introduzir `spring-boot-starter-security`, gerar token no login |
| Padronização erros  | Substituir `RuntimeException` por exceções custom + status coerentes |
| Observabilidade     | Adicionar Actuator + métricas |
| Documentação API    | Integrar `springdoc-openapi-starter-webmvc-ui` |
| Upload Imagens      | Converter `byte[]` para armazenamento externo (S3, MinIO) |
| Migrações DB        | Flyway para versionamento de esquema |
| Rate limiting       | Adicionar Bucket4j / Resilience4j |
| Melhor cache        | TTL, métricas e limpeza seletiva |
| DTOs específicos    | Expor modelos sem campo `password` (já não expõe? – login sim) |
| Tratamento de LOB   | Endpoints específicos para upload/download (multipart) |

---

## 18. Contribuição

1. Crie branch:
```
git checkout -b feat/nova-funcionalidade
```
2. Altere / adicione código.
3. Garanta build:
```
./mvnw clean verify
```
4. Abra Pull Request descrevendo:
   - Objetivo
   - Mudanças técnicas
   - Como testar
   - Impacto em config

Padrão de commit sugerido:
```
feat(user): adiciona endpoint de alteração de avatar
fix(user): corrige validação de e-mail
```

---

## 19. Licença

Ainda não definida no `pom.xml` (tags estão vazias). Recomenda-se adicionar arquivo `LICENSE` (ex.: MIT ou Apache 2.0) e preencher metadados.

Exemplo trecho MIT:
```
Permission is hereby granted, free of charge, to any person obtaining a copy...
```

---

## Anexos: Exemplos de Corpo de Requisição

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

### Atualização Seletiva (PUT)
```json
{
  "bio": "Nova bio",
  "password": "Nov4@Senha"
}
```

Se enviar campos nulos ou ausentes: não serão alterados.

---

## Observações Importantes

- Senha nunca deve ser logada em texto puro.
- Retorno do create inclui `password` (hash). Para segurança, considere omitir no futuro (DTO de resposta).
- Exceções genéricas retornam 500 com mensagem vaga – ideal padronizar usando objeto JSON estruturado.
- HATEOAS: alguns links usam `methodOn(...).getById(null)` – para evitar `null`, pode-se criar link template ou ajustar assembler.

---

## Perguntas Frequentes (FAQ)

1. Por que minha senha não autentica após atualização?  
   - Verifique se o regex de senha foi atendido e se o hash foi gerado (config do `PasswordEncoder`).

2. Como adicionar TTL ao cache?  
   - Configure Caffeine via `CacheCustomizer` ou propriedades:  
     `spring.cache.cache-names=users,users_page,user`  
     `spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m`

3. Como evitar expor o hash?  
   - Criar DTO de resposta sem `password` e mapear antes de retornar.

---

## Contato

Use Issues do repositório para bugs e sugestões.

---

Se desejar, posso fornecer:
- Arquivo de configuração de segurança inicial (JWT)
- Esquema OpenAPI
- Exemplo de Dockerfile / Docker Compose para Oracle XE

Solicite conforme necessidade.

Bom desenvolvimento!
