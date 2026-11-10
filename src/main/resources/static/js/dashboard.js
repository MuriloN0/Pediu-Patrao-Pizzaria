const dashboard = document.getElementById("dashboard");
const formulario = document.getElementById("filtros");
const inicio = document.getElementById("inicio");
const fim = document.getElementById("fim");
const botao = document.getElementById("atualizar");
const mensagem = document.getElementById("mensagem");
const resultados = document.getElementById("resultados");

const moeda = new Intl.NumberFormat("pt-BR", {

    style: "currency",
    currency: "BRL"
});

const nomesStatus = {

    RECEBIDO: "Recebido",
    EM_PREPARACAO: "Em preparação",
    PRONTO: "Pronto",
    SAIU_PARA_ENTREGA: "Saiu para entrega",
    RETIRADO: "Retirado",
    FINALIZADO: "Finalizado",
    CANCELADO: "Cancelado",
    OUTROS: "Outros"
};

let graficoVendas;

function mostrarMensagem(texto, tipo = "info") {

    mensagem.className = `alert alert-${tipo}`;
    mensagem.textContent = texto;
    mensagem.hidden = !texto;
}

function preencherTabela(id, linhas) {

    const corpo = document.getElementById(id);
    corpo.replaceChildren();

    for (const valores of linhas) {
        const linha = corpo.insertRow();

        for (const valor of valores) {
            linha.insertCell().textContent = valor;
        }
    }
}

function atualizarGrafico(vendas) {

    if (graficoVendas) {
        graficoVendas.destroy();
    }

    graficoVendas = new Chart(document.getElementById("grafico-vendas"), {

        type: "bar",
        data: {
            labels: vendas.map(venda => venda.data.split("-").reverse().join("/")),
            datasets: [{
                label: "Valor líquido",
                data: vendas.map(venda => venda.valorLiquido),
                backgroundColor: "#28a745"
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero: true,
                        callback: valor => moeda.format(valor)
                    }
                }]
            },
            tooltips: {
                callbacks: {
                    label: item => moeda.format(item.yLabel)
                }
            }
        }
    });
}

function mostrarDados(dados) {

    const indicadores = dados.indicadores;
    document.getElementById("valor-liquido").textContent = moeda.format(indicadores.valorLiquido);
    document.getElementById("finalizados").textContent = indicadores.pedidosFinalizados;
    document.getElementById("ticket-medio").textContent = moeda.format(indicadores.ticketMedio);
    document.getElementById("total-pedidos").textContent = dados.totalPedidos;

    const situacoes = Object.entries(dados.pedidosPorStatus)
        .map(([status, quantidade]) => [nomesStatus[status] ?? status, quantidade]);
    preencherTabela("situacoes", situacoes);

    const produtos = dados.produtosMaisVendidos
        .map(produto => [produto.nome, produto.unidades]);
    preencherTabela("produtos", produtos);
    document.getElementById("ranking").hidden = produtos.length === 0;
    document.getElementById("ranking-vazio").hidden = produtos.length > 0;

    resultados.hidden = false;
    atualizarGrafico(dados.vendasPorDia);
}

async function consultarDashboard() {

    resultados.hidden = true;

    if (inicio.value && fim.value && inicio.value > fim.value) {
        mostrarMensagem("A data inicial deve ser anterior ou igual à data final.", "warning");
        return;
    }

    const url = new URL(dashboard.dataset.url, window.location.origin);

    if (inicio.value) {
        url.searchParams.set("inicio", inicio.value);
    }
    if (fim.value) {
        url.searchParams.set("fim", fim.value);
    }

    botao.disabled = true;
    mostrarMensagem("Carregando dados do período…");

    try {
        const resposta = await fetch(url, {
            headers: { Accept: "application/json" },
            cache: "no-store"
        });

        if (resposta.redirected || resposta.status === 401) {
            window.location.assign(dashboard.dataset.login);
            return;
        }
        if (resposta.status === 400) {
            throw new Error("Informe um período válido de até 366 dias.");
        }
        if (resposta.status === 403) {
            throw new Error("Seu perfil não tem acesso a este painel.");
        }
        if (!resposta.ok) {
            throw new Error("Não foi possível consultar o dashboard. Tente novamente.");
        }

        const dados = await resposta.json();
        inicio.value = dados.inicio;
        fim.value = dados.fim;
        mostrarDados(dados);
        mostrarMensagem(dados.totalPedidos === 0 ? "Nenhum pedido no período selecionado." : "");
    } catch (erro) {

        resultados.hidden = true;
        mostrarMensagem(erro instanceof TypeError
            ? "Não foi possível conectar ao servidor. Tente novamente."
            : erro.message, "danger");
    } finally {
        botao.disabled = false;
    }
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    consultarDashboard();
});

consultarDashboard();