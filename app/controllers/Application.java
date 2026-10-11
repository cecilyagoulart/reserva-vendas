package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Produto;
import models.Reserva;
import models.Status;
import play.mvc.Controller;
import play.mvc.With;
import security.Seguranca;

@With(Seguranca.class)
public class Application extends Controller {

	public static void index() {
		// Hoje à meia-noite (a data da reserva é gravada só como dia)
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date hoje = c.getTime();

		// Vendas de hoje
		List<Reserva> reservasHoje = Reserva.find("status != ?1 and data = ?2", Status.INATIVO, hoje).fetch();
		int vendasHoje = reservasHoje.size();
		double valorHoje = 0;
		for (Reserva r : reservasHoje) {
			valorHoje += r.getValorTotal();
		}

		// Reservas ativas (ainda não retiradas)
		int totalReservas = (int) Reserva.count("status = ?1", Status.ATIVO);

		// Produto mais vendido: reservas ativas e já retiradas
		List<Reserva> vendidas = Reserva.find("status != ?1", Status.INATIVO).fetch();

		Map<String, Integer> porProduto = new HashMap<String, Integer>();
		for (Reserva r : vendidas) {
			if (r.produto != null) {
				String nome = r.produto.nomeProduto;
				Integer atual = porProduto.get(nome);
				porProduto.put(nome, (atual == null ? 0 : atual) + r.quantidade);
			}
		}

		String maisVendido = "-";
		int qtdMaisVendido = 0;
		for (Map.Entry<String, Integer> e : porProduto.entrySet()) {
			if (e.getValue() > qtdMaisVendido) {
				qtdMaisVendido = e.getValue();
				maisVendido = e.getKey();
			}
		}

		// Produtos com estoque baixo ou esgotado (5 ou menos)
		List<Produto> estoqueBaixo = Produto.find("estoque <= ?1 order by estoque", 5).fetch();

		render(vendasHoje, valorHoje, totalReservas, maisVendido, qtdMaisVendido, estoqueBaixo);
	}

}