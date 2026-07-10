# UDESC MAKER Mobile

Aplicativo React Native/Expo que consulta o catálogo real da API Spring Boot e publica novos projetos no repositório do site por meio do backend. O aplicativo **não** acessa o GitHub diretamente e nunca recebe token de publicação.

## Arquitetura

- Expo SDK 57, React Native 0.86, React 19.2 e TypeScript estrito.
- React Navigation com um único `native-stack` para as cinco telas: início, cadastro, detalhes, exploração e resultados.
- Cliente HTTP baseado em `fetch`, centralizado em `src/services/`, com timeout de 20 segundos para consultas e 5 minutos para publicação com anexos, normalização de DTOs e erros consistentes.
- Formulário com React Hook Form + Zod, listas dinâmicas e associação determinística de arquivos aos metadados.
- Seleção nativa com Expo ImagePicker e DocumentPicker.
- Imagens via Expo Image, inclusive capas SVG já existentes no catálogo.
- Vídeo validado e incorporado somente por `youtube-nocookie.com`.
- Nome da pessoa autora e GitHub opcional armazenados localmente com AsyncStorage; falhas desse armazenamento não alteram uma publicação já concluída e não há autenticação simulada.
- Tema próprio em `src/theme/`, inspirado nos protótipos e no site UDESC MAKER.

Estrutura principal:

```text
frontend/
  src/
    components/   componentes visuais, estados e listas dinâmicas
    hooks/        carregamento assíncrono e invalidação do catálogo
    navigation/   stack e tipos de rotas
    screens/      as cinco telas obrigatórias
    services/     cliente HTTP, DTOs, normalização e multipart
    theme/        cores, espaçamento, raios e sombras
    types/        contratos da API e do formulário
    utils/        YouTube, arquivos, filtros, links e formatação
    validation/   schema de publicação
```

## Requisitos

- Node.js 22.13 ou superior (requisito do Expo SDK 57).
- npm 10 ou superior.
- Expo Go compatível com SDK 57 ou um development build.
- Backend Spring Boot em execução e acessível pelo dispositivo.

Para builds nativos locais também são necessários Android Studio/Android SDK ou macOS com Xcode. Eles não são necessários para typecheck e testes unitários.

## Configuração

Copie `.env.example` para `.env` e ajuste somente a URL pública da API:

```dotenv
EXPO_PUBLIC_API_URL=http://localhost:8080/api
```

- Android Emulator normalmente acessa o host por `http://10.0.2.2:8080/api`.
- Dispositivo físico deve usar o IP da máquina na rede local, por exemplo `http://192.168.1.20:8080/api`.
- O backend precisa liberar essa origem na configuração de CORS.
- Nunca adicione `UDESCMAKER_GITHUB_TOKEN`, senha ou qualquer segredo a uma variável `EXPO_PUBLIC_*`.

## Instalação e execução

Na pasta `UdescMaker-Mobile/frontend`:

```bash
npm ci
npm run start
```

Atalhos:

```bash
npm run android
npm run ios
npm run web
```

O destino web é útil para desenvolvimento, mas Android/iOS continuam sendo os alvos principais.

## Contrato consumido

Consultas:

```text
GET /api/projetos?busca=&tags=tag1,tag2&categoria=educacao&dificuldade=iniciante&idade=10&duracaoMaxima=90&ordenacao=recentes&limite=6
GET /api/projetos/{slug}
GET /api/taxonomia
```

As ordenações aceitas são `recentes`, `duracao` e `dificuldade`. O detalhe traz `videoYoutube` opcional para conteúdos antigos e `relacionados` como resumos prontos; o aplicativo não recalcula relevância.

Publicação:

```text
POST /api/projetos
Content-Type: multipart/form-data
```

Partes:

- `projeto`: JSON com os metadados;
- `capa`: imagem obrigatória;
- `galeria`: imagens repetidas;
- `passosImagens`: imagens de passos repetidas;
- `baixaveis`: PDFs/DOCs/ZIPs repetidos;
- `arquivos`: arquivos complementares repetidos.

Cada item do JSON utiliza `arquivoIndice` ou `imagemArquivoIndice`, começando em zero dentro da lista multipart homônima. Não defina manualmente o boundary de `Content-Type`; React Native o gera ao enviar o `FormData`.

Após uma publicação bem-sucedida, o catálogo local é invalidado, a confirmação mostra o slug e informa que o GitHub Pages pode levar alguns instantes para atualizar. Erros recuperáveis preservam o formulário preenchido; no envio inválido, as seções com problemas são abertas e os campos destacados ficam acompanhados de um resumo acessível no topo.

## Validações e segurança

- título com pelo menos 4 caracteres;
- resumo de 12 a 180 caracteres, com contador visual;
- autor, categoria, tag, dificuldade, idade, duração positiva, vídeo do YouTube, capa e texto alternativo obrigatórios;
- somente URLs HTTPS nos formatos `youtube.com/watch`, `youtu.be/<id>` e `youtube.com/shorts/<id>` são aceitas; o embed é gerado internamente em `youtube-nocookie.com` após validação do ID;
- links de arquivos aceitam somente HTTP/HTTPS;
- clique duplicado em Publicar é bloqueado;
- o backend continua sendo a autoridade para extensões, tamanho, slug, path traversal e credenciais GitHub.

## Testes

```bash
npm run typecheck
npm test
```

Os testes cobrem validação obrigatória e limite do resumo, paths dos erros de anexos, URLs HTTPS permitidas do YouTube, listas dinâmicas, filtros/apagar filtros, ordenação enviada à API, timeout de publicação, associação e nomes das partes multipart, descarte de respostas assíncronas obsoletas, estados de loading/erro/vazio e renderização condicional de vídeo/relacionados. Todas as chamadas de publicação são testadas localmente, sem GitHub real.
