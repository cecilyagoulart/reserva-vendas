package controllers;
import java.util.Date;
import java.util.List;

import models.Produto;
import models.Reserva;
import models.Status;
import play.data.validation.Valid;
import play.mvc.Controller;
import play.mvc.With;
import security.Seguranca;
import security.Administrador;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import play.db.jpa.Blob;
import play.libs.MimeTypes;

@With(Seguranca.class)
public class Reservas extends Controller {

	public static void forms() {
		Reserva r = new Reserva();
		List<Produto> produtos = Produto.findAll();
		render(r, produtos);
	}

	public static void listar(String termo) {
		List<Reserva> reservas;
		if (termo == null || termo.isEmpty()) {
			reservas = Reserva.find("status = ?1", Status.ATIVO).fetch();
		} else {
			reservas = Reserva.find("status = ?1 and (lower(nomeCliente) like ?2 or lower(produto.nomeProduto) like ?2)",
					Status.ATIVO, "%" + termo.toLowerCase() + "%").fetch();
		}
		render(reservas, termo);
	}

	public static void salvar(@Valid Reserva r, Long idReserva) {

		// Se veio um id, é edição: busca a reserva como ela está no banco
		Reserva original = null;
		if (idReserva != null) {
			original = Reserva.findById(idReserva);
			if (original == null || original.status != Status.ATIVO) {
				flash.error("Reserva não encontrada.");
				listar(null);
			}
		}

		// Regra de negócio: a quantidade não pode passar do estoque disponível
		if (!validation.hasErrors()) {
			Produto produto = Produto.findById(r.produto.id);
			int disponivel = produto.estoque;

			// Na edição do mesmo produto, o que já estava reservado volta a contar
			if (original != null && original.produto != null && original.produto.id.equals(produto.id)) {
				disponivel += original.quantidade;
			}

			if (r.quantidade > disponivel) {
				validation.addError("r.quantidade", "Estoque insuficiente. Disponível: " + disponivel + " un.");
			}
		}

		if (validation.hasErrors()) {
			List<Produto> produtos = Produto.findAll();
			renderTemplate("Reservas/forms.html", r, produtos, idReserva);
		}

		Produto produto = Produto.findById(r.produto.id);

		if (original == null) {
			// Nova reserva: dá baixa no estoque
			produto.estoque -= r.quantidade;
			produto.save();
			r.save();
			flash.success("Reserva registrada com sucesso!");
		} else {
			// Edição: devolve o estoque antigo e depois debita o novo
			if (original.produto != null) {
				original.produto.estoque += original.quantidade;
				original.produto.save();
			}
			produto.estoque -= r.quantidade;
			produto.save();

			original.nomeCliente = r.nomeCliente;
			original.produto = produto;
			original.quantidade = r.quantidade;
			original.observacao = r.observacao;
			original.save();
			flash.success("Reserva atualizada com sucesso!");
		}
		listar(null);
	}

	public static void editar(Long id) {
		Reserva r = Reserva.findById(id);
		if (r == null) {
			notFound("Reserva não encontrada");
		}
		if (r.status != Status.ATIVO) {
			flash.error("Só é possível editar reservas que ainda não foram retiradas.");
			listar(null);
		}
		List<Produto> produtos = Produto.findAll();
		renderTemplate("Reservas/forms.html", r, produtos);
	}

	// Confirma que o cliente já retirou o produto: a reserva sai da lista,
	// a venda continua no histórico e o estoque NÃO volta (o produto saiu).
	public static void retirar(Long id) {
		Reserva r = Reserva.findById(id);
		if (r == null) {
			notFound("Reserva não encontrada");
		}
		if (r.status != Status.ATIVO) {
			flash.error("Esta reserva já foi retirada ou removida.");
			listar(null);
		}
		r.status = Status.ENTREGUE;
		r.save();
		flash.success("Retirada confirmada! A venda continua no histórico.");
		listar(null);
	}

	@Administrador
	public static void remover(Long id) {
		Reserva r = Reserva.findById(id);
		if (r != null && r.status == Status.ATIVO) {
			r.status = Status.INATIVO;
			r.save();

			if (r.produto != null) {
				Produto p = Produto.findById(r.produto.id);
				if (p != null) {
					p.estoque += r.quantidade;
					p.save();
				}
			}

			flash.success("Reserva removida! Estoque devolvido.");
		}
		listar(null);
	}

	@Administrador
	public static void formProduto() {
		Produto p = new Produto();
		renderTemplate("Reservas/formProduto.html", p);
	}

	@Administrador
	public static void salvarProduto(@Valid Produto p, File foto) throws FileNotFoundException {
		boolean enviouFoto = foto != null && foto.length() > 0;

		if (enviouFoto) {
			String tipo = MimeTypes.getContentType(foto.getName());
			if (!tipo.startsWith("image/")) {
				validation.addError("foto", "Envie um arquivo de imagem (png, jpg, gif)");
			}
		}

		if (validation.hasErrors()) {
			renderTemplate("Reservas/formProduto.html", p);
		}

		if (enviouFoto) {
			p.foto = new Blob();
			p.foto.set(new FileInputStream(foto), MimeTypes.getContentType(foto.getName()));
		}

		if (p.id != null) {

			Produto original = Produto.findById(p.id);
			if (original != null) {
				p.estoque = original.estoque;
			}
			p.save();
			flash.success("Produto atualizado!");
		} else {
			p.save();
			flash.success("Produto cadastrado!");
		}
		forms();
	}

	public static void listarProdutos() {
		List<Produto> produtos = Produto.findAll();
		render(produtos);
	}

	@Administrador
	public static void adicionarEstoque(Long id, int quantidade) {
		Produto p = Produto.findById(id);
		if (p != null && quantidade > 0) {
			p.estoque += quantidade;
			p.save();
			flash.success("Estoque de " + p.nomeProduto + " atualizado!");
		}
		listarProdutos();
	}

	@Administrador
	public static void removerEstoque(Long id, int quantidade) {
		Produto p = Produto.findById(id);
		if (p != null && quantidade > 0) {
			if (p.estoque < quantidade) {
				flash.error("Estoque insuficiente para dar baixa em '" + p.nomeProduto + "'. Disponível: " + p.estoque);
			} else {
				p.estoque -= quantidade;
				p.save();
				flash.success("Baixa de " + quantidade + " un. em '" + p.nomeProduto + "' registrada!");
			}
		}
		listarProdutos();
	}

	@Administrador
	public static void editarProduto(Long id) {
		Produto p = Produto.findById(id);
		if (p == null) {
			notFound("Produto não encontrado");
		}
		renderTemplate("Reservas/formProduto.html", p);
	}

	@Administrador
	public static void removerProduto(Long id) {
		Produto p = Produto.findById(id);
		if (p != null) {
			long qtdAtivas = Reserva.count("produto.id = ?1 and status != ?2", id, Status.INATIVO);
			if (qtdAtivas > 0) {
				flash.error("Não é possível excluir '" + p.nomeProduto + "': existem reservas ou vendas vinculadas a ele.");
			} else {
				try {
					List<Reserva> reservasInativas = Reserva.find("produto.id = ?1", id).fetch();
					for (Reserva r : reservasInativas) {
						r.delete();
					}
					p.delete();
					flash.success("Produto excluído com sucesso!");
				} catch (Exception e) {
					flash.error("Não foi possível excluir o produto. Tente novamente.");
				}
			}
		}
		listarProdutos();
	}

	// ===== Vendas por período =====
	public static void vendas(Date inicio, Date fim) {
		if (inicio != null && fim != null && inicio.after(fim)) {
			flash.error("A data inicial não pode ser depois da data final.");
			vendas(null, null);
		}

		List<Reserva> vendas;
		if (inicio != null && fim != null) {
			vendas = Reserva.find("status != ?1 and data >= ?2 and data <= ?3 order by data desc",
					Status.INATIVO, inicio, fim).fetch();
		} else if (inicio != null) {
			vendas = Reserva.find("status != ?1 and data >= ?2 order by data desc",
					Status.INATIVO, inicio).fetch();
		} else if (fim != null) {
			vendas = Reserva.find("status != ?1 and data <= ?2 order by data desc",
					Status.INATIVO, fim).fetch();
		} else {
			vendas = Reserva.find("status != ?1 order by data desc", Status.INATIVO).fetch();
		}

		int totalUnidades = 0;
		double valorTotal = 0;
		for (Reserva v : vendas) {
			totalUnidades += v.quantidade;
			valorTotal += v.getValorTotal();
		}

		render(vendas, inicio, fim, totalUnidades, valorTotal);
	}
	public static void verFoto(Long id) {
		Produto produto = Produto.findById(id);
		if (produto == null || produto.foto == null || !produto.foto.exists()) {
			notFound("Imagem não encontrada");
		}
		response.setContentTypeIfNotSet(produto.foto.type());
		renderBinary(produto.foto.get());
	}
}