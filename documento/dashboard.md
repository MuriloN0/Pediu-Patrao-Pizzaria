# Dashboard administrativo do Pediu Patrão

## Objetivo

O dashboard ajuda o responsável pela pizzaria a acompanhar as vendas e a situação dos pedidos. A página reúne os principais números da operação e permite consultar períodos diferentes sem sair do sistema.

## Requisitos funcionais

| Código | O que o painel faz |
| --- | --- |
| RF01 — Indicadores gerais de vendas | Mostra o valor líquido das vendas, o número de pedidos finalizados e o ticket médio. |
| RF02 — Análise de vendas | Apresenta um gráfico de barras com o valor líquido por dia. Os dias sem vendas aparecem com valor zero. |
| RF03 — Situação dos pedidos | Mostra o total de pedidos no período e a quantidade em cada status. |
| RF04 — Produtos mais vendidos | Lista até cinco produtos, ordenados pela quantidade de unidades vendidas em pedidos finalizados. |
| RF05 — Filtros de consulta | Permite selecionar a data inicial e a final. O filtro vale para toda a página. |
| RF06 — Integração com a API | O painel busca os dados na API, que consulta os pedidos no MongoDB e devolve os resultados em JSON. |
| RF07 — Atualização e tratamento de dados | A página carrega os dados ao abrir e refaz a consulta ao clicar em **Atualizar**. Também mostra mensagens de carregamento, período vazio e erro. |

## Requisitos não funcionais

- **Segurança.** Apenas usuários com perfil `ADMIN` ou `GERENTE` podem abrir o painel e consultar a API. A permissão é verificada no servidor.
- **Precisão dos valores.** Os cálculos monetários são feitos no backend com `BigDecimal`. O ticket médio é arredondado para duas casas decimais.
- **Usabilidade.** A interface usa AdminLTE e Chart.js, apresenta as datas no formato brasileiro e os valores em reais.
- **Limite da consulta.** Cada consulta aceita até 366 dias. O filtro é aplicado no MongoDB antes do cálculo dos indicadores.

## Como os números são calculados

O período considera a **data de entrada do pedido**, no fuso `America/Sao_Paulo`. As duas datas do filtro são inclusivas. A data de entrada foi escolhida porque o sistema registra a entrada e a saída do pedido, mas não guarda uma data específica de finalização.

A tabela de situação inclui todos os pedidos do período. Para o valor líquido e o ranking, entram apenas os pedidos `FINALIZADO`. O valor líquido usa o total registrado no pedido, já com desconto. O ticket médio divide esse valor pelo número de pedidos finalizados com valor registrado. O ranking soma as unidades por produto.

Por isso, um período pode mostrar pedidos em andamento ou cancelados e ainda ter vendas zeradas. Pedidos antigos sem data de entrada ficam fora do filtro.

## API do dashboard

A página `/dashboard` consulta o endpoint **`GET /dashboard/dados`**. Uma única resposta alimenta os indicadores, o gráfico e as tabelas para que todos usem o mesmo período.

| Parâmetro | Formato | Comportamento |
| --- | --- | --- |
| `inicio` | `AAAA-MM-DD` | Data inicial opcional. Se omitida, usa a data final menos 29 dias. |
| `fim` | `AAAA-MM-DD` | Data final opcional. Se omitida, usa o dia atual no fuso do estabelecimento. |

### Dados da resposta

| Campo | Conteúdo |
| --- | --- |
| `inicio` e `fim` | Datas efetivamente usadas na consulta. |
| `totalPedidos` | Quantidade total de pedidos no período. |
| `indicadores` | Quantidade de finalizados, valor líquido e ticket médio. |
| `vendasPorDia` | Lista com a data e o valor líquido de cada dia. |
| `pedidosPorStatus` | Quantidade de pedidos em cada situação. |
| `produtosMaisVendidos` | Lista com ID, nome e quantidade de unidades de cada produto no ranking. |

**Exemplo de resposta para um dia sem pedidos**

```json
{
  "inicio": "2026-11-10",
  "fim": "2026-11-10",
  "totalPedidos": 0,
  "indicadores": {
    "pedidosFinalizados": 0,
    "valorLiquido": 0.00,
    "ticketMedio": 0.00
  },
  "vendasPorDia": [
    { "data": "2026-11-10", "valorLiquido": 0.00 }
  ],
  "pedidosPorStatus": {
    "RECEBIDO": 0,
    "EM_PREPARACAO": 0,
    "PRONTO": 0,
    "SAIU_PARA_ENTREGA": 0,
    "RETIRADO": 0,
    "FINALIZADO": 0,
    "CANCELADO": 0,
    "OUTROS": 0
  },
  "produtosMaisVendidos": []
}
```

### Respostas HTTP

| Código | Quando ocorre |
| --- | --- |
| `200` | Consulta realizada, inclusive quando o período não tem pedidos. |
| `302` | Usuário sem sessão autenticada é redirecionado para o login. |
| `400` | Data inválida, período invertido ou intervalo acima de 366 dias. |
| `403` | Usuário autenticado sem permissão para acessar o recurso. |
| `500` | Falha inesperada durante a consulta. |

## Testes realizados

Os testes foram feitos no navegador, com a aplicação rodando localmente e conectada ao MongoDB.

| Cenário | Resultado observado |
| --- | --- |
| Abertura do painel | A página carregou os dados da API e exibiu os indicadores, o gráfico e as tabelas. |
| Consulta de períodos diferentes | O botão **Atualizar** aplicou as datas selecionadas. Nos períodos testados sem pedidos finalizados, os indicadores de vendas ficaram zerados. |
| Período sem pedidos | O painel apresentou os valores zerados e informou que não havia pedidos no intervalo. |
| Datas invertidas | A API rejeitou a consulta com resposta `400`. |
| Controle de acesso | `ADMIN` e `GERENTE` tiveram acesso. Para `ATENDENTE`, o link ficou oculto e o acesso direto retornou `403`. |

### Evidência do filtro por período

Na consulta de **12/10/2026 a 01/11/2026**, o painel mostrou **8 pedidos** no total e **nenhum finalizado**. O valor líquido e o ticket médio ficaram em **R$ 0,00**, e o ranking ficou vazio.