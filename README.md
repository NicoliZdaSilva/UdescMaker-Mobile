# UDESC MAKER Mobile

Aplicativo React Native/Expo e API Spring Boot para consultar o catálogo UDESC MAKER e publicar projetos no repositório do site. A credencial do GitHub existe somente no backend; o aplicativo conhece apenas a URL pública da API.

## Arquitetura

O backend preserva uma separação em camadas:

- `controller`: contratos HTTP de projetos, taxonomia e assets do catálogo local;
- `service`: filtros, ordenações, relacionados, normalização e fluxo de publicação;
- `repository`: SPI `ProjetoRepository`, leitor de diretório local, leitor GitHub com cache e fixture exclusiva de teste;
- `markdown`: parser SnakeYAML seguro e serializer determinístico do contrato Astro;
- `integration/git`: SPI de escrita e implementação da Git Data API do GitHub;
- `domain`, `dto`, `taxonomy`, `validation` e `exception`: modelo, contratos, validações e erros consistentes;
- `frontend`: aplicativo Expo/TypeScript, com cliente HTTP centralizado e navegação entre início, cadastro, detalhe, exploração e resultados.

Não há banco de dados. O Markdown do repositório web é a fonte de verdade. `publicadoEm` usa a data do servidor e a resposta da publicação usa o instante exato informado pelo commit do GitHub.

## Requisitos

- Java 21;
- Maven 3.9+ (ou o wrapper `./mvnw`);
- Node.js 22.13+ e npm 10+ (requisito do Expo SDK 57);
- Expo CLI por meio de `npx expo`;
- para publicação real, token fine-grained do GitHub com acesso ao repositório configurado e permissão **Contents: Read and write**. Metadata read é implícita.

## Backend

Copie `.env.example` apenas como referência e exporte as variáveis necessárias no ambiente. O Spring não carrega `.env` automaticamente.

```bash
cd UdescMaker-Mobile
./mvnw spring-boot:run
```

Por padrão, a API usa `UDESCMAKER_CATALOG_MODE=local` e lê `../udescmaker/src/content/projects`. O caminho é relativo ao diretório de execução e pode ser alterado sem recompilar. Assets relativos são expostos de forma segura em `/api/catalogo-assets/{slug}/{arquivo}`.

Para ler o catálogo público do GitHub, configure:

```bash
export UDESCMAKER_CATALOG_MODE=github
./mvnw spring-boot:run
```

Esse modo consulta uma árvore do repositório, baixa cada `index.md`, converte referências relativas em URLs `raw.githubusercontent.com` e mantém cache com TTL. Uma publicação bem-sucedida invalida o cache.

Endpoints principais:

- `GET /api/taxonomia`;
- `GET /api/projetos`;
- `GET /api/projetos/destaques`;
- `GET /api/projetos/{slug}`;
- `POST /api/projetos`;
- `GET /api/catalogo-assets/{slug}/{arquivo}` no modo local.

A listagem aceita `busca`, `tags` (repetido ou separado por vírgula), `categoria`, `dificuldade`, `idade`, `duracaoMaxima`, `ordenacao=recentes|duracao|dificuldade` e `limite` de 1 a 100. O alias legado `idadeMinima` continua aceito. Para `idade=10`, são retornados projetos com `idadeMinima <= 10`.

Relacionados são calculados no backend: 5 pontos por categoria comum, 3 por tag comum, desempate pela data mais recente, sem o próprio projeto, sem pontuação zero e no máximo três cards.

## Publicação multipart

`POST /api/projetos` recebe `multipart/form-data`:

- `projeto`: JSON `application/json` obrigatório;
- `capa`: um arquivo de imagem obrigatório;
- `galeria`: zero ou mais arquivos de imagem;
- `passosImagens`: zero ou mais imagens de passos;
- `baixaveis`: zero ou mais documentos;
- `arquivos`: zero ou mais arquivos complementares.

JSON da parte `projeto`:

```json
{
  "titulo": "Nome do projeto",
  "resumo": "Resumo entre 12 e 180 caracteres.",
  "autor": { "nome": "Pessoa Autora", "github": "usuario-opcional" },
  "dificuldade": "iniciante",
  "idadeMinima": 10,
  "duracaoMinutos": 90,
  "categorias": ["educacao"],
  "tags": ["maker"],
  "videoYoutube": "https://youtu.be/dQw4w9WgXcQ",
  "capaAlt": "Descrição acessível da capa",
  "descricaoLonga": "Markdown que ficará depois do frontmatter.",
  "galeria": [{ "alt": "Descrição acessível", "arquivoIndice": 0 }],
  "materiais": ["Material"],
  "ferramentas": ["Ferramenta"],
  "passos": [{ "titulo": "Montagem", "corpo": "Descrição do passo", "imagemArquivoIndice": 0 }],
  "dicas": [{ "tom": "warning", "texto": "Cuidado importante" }],
  "baixaveis": [{ "rotulo": "Manual", "tipo": "pdf", "arquivoIndice": 0 }],
  "arquivos": [{ "rotulo": "Modelo", "tipo": "stl", "arquivoIndice": 0 }]
}
```

Cada índice é zero-based dentro da lista multipart homônima. Cada arquivo deve ser usado exatamente uma vez, o que torna a associação independente do nome original. A capa não usa índice.

Em sucesso, a API responde HTTP 201 com `slug`, `caminho`, `shaCommit`, `urlCommit`, `urlProjeto`, `publicadoEm` e uma mensagem sobre a atualização assíncrona do GitHub Pages. Slug existente responde 409; validação, Markdown, arquivo, configuração, autenticação e conflito de branch usam um corpo uniforme com `codigo`, `mensagem`, `campos`, `timestamp` e `path`.

## Como a integração GitHub funciona

A publicação usa exclusivamente a Git Data API:

1. lê o HEAD da branch configurada e sua árvore;
2. confirma que `<projects-path>/<slug>/index.md` não existe;
3. cria blobs para o Markdown e todos os anexos;
4. cria uma árvore baseada na árvore atual;
5. cria um único commit com pai igual ao HEAD lido;
6. atualiza a referência com `force: false`;
7. se a branch mudou, repete todo o fluxo uma vez; um segundo conflito retorna 409.

Blobs ou commits não referenciados podem existir temporariamente se a rede falhar, mas a branch nunca aponta para uma publicação parcial. O owner, repositório, branch e caminho vêm apenas da configuração do servidor; nenhum deles é aceito do cliente.

Para habilitar escrita real, configure `UDESCMAKER_GITHUB_TOKEN` e somente então `UDESCMAKER_GITHUB_PUBLISH_ENABLED=true`. Com a flag falsa, o endpoint retorna indisponibilidade configurada sem escrever. O perfil `test` instala um gateway que sempre proíbe publicação real.

Nunca coloque o token em `EXPO_PUBLIC_*`, no aplicativo, em `application.yaml`, em commits ou logs.

## Front-end mobile

```bash
cd UdescMaker-Mobile/frontend
cp .env.example .env
npm ci
npx expo start
```

Configure apenas:

```dotenv
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080/api
```

No emulador Android, `10.0.2.2` aponta para o host. Em dispositivo físico, use o IP acessível da máquina na rede local. Nenhuma credencial deve estar no `.env` do Expo.

## Testes e validações

```bash
cd UdescMaker-Mobile
./mvnw test
./mvnw clean verify

cd frontend
npm ci
npm run typecheck
npm test
```

Os testes backend usam repositórios/gateways falsos e o perfil `test`; não fazem rede nem escrevem no GitHub. Eles cobrem parser e campos opcionais, serializer e round-trip, taxonomias, resumo de 180 caracteres, slug, sanitização, filtros e ordenações, relacionados 5/3, duplicidade, publicação atômica, multipart e erros.

## Frontmatter gerado

Cada projeto é gravado em `src/content/projects/<slug>/index.md`; anexos ficam na mesma pasta e são referenciados como `./arquivo.ext`. O serializer gera UTF-8, IDs de taxonomia, data `YYYY-MM-DD`, `destaque: false`, `relacionados: []`, `videoYoutube` e coloca a descrição longa somente no corpo Markdown. Antes de enviar, o backend parseia novamente o conteúdo gerado.
