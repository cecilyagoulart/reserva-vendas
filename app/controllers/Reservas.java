package controllers;
import java.util.List;
import models.Produto;
import models.Reserva;
import models.Status;
import play.mvc.Controller;

public class Reservas extends Controller {

	public static void forms() {
		Reserva r = new Reserva();
		List<Produto> produtos = Produto.findAll();
        render(r, produtos);
	}
	
	public static void listar(String termo) {
	    List<Reserva> reservas;
	    if (termo == null || termo.isEmpty()) {
	        reservas = Reserva.find("status != ?1", Status.INATIVO).fetch();
	    } else {
	        reservas = Reserva.find("status != ?1 and (lower(nomeCliente) like ?2 or lower(produto.nomeProduto) like ?2)", 
	                                Status.INATIVO, "%" + termo.toLowerCase() + "%").fetch();
	    }
	    render(reservas, termo);
	}
	
	public static void salvar(Reserva r) {
		if (r.produto != null) {
			Produto produtoNovo = Produto.findById(r.produto.id);

			if (r.id == null) {
				
				if (produtoNovo == null || produtoNovo.estoque < r.quantidade) {
					flash.error("Estoque insuficiente! Disponível: " + (produtoNovo != null ? produtoNovo.estoque : 0));
					List<Produto> produtos = Produto.findAll();
					render("Reservas/forms.html", r, produtos);
					return;
				}

				
				produtoNovo.estoque -= r.quantidade;
				produtoNovo.save();

			} else {
				
				Reserva original = Reserva.findById(r.id);

				if (original != null) {
					Produto produtoAntigo = original.produto;
					boolean trocouProduto = produtoAntigo == null
							|| produtoNovo == null
							|| !produtoAntigo.id.equals(produtoNovo.id);

					if (!trocouProduto) {
						
						int diferenca = r.quantidade - original.quantidade;
						if (diferenca > 0 && produtoNovo.estoque < diferenca) {
							flash.error("Estoque insuficiente para aumentar a quantidade! Disponível: " + produtoNovo.estoque);
							List<Produto> produtos = Produto.findAll();
							render("Reservas/forms.html", r, produtos);
							return;
						}
						produtoNovo.estoque -= diferenca;
						produtoNovo.save();

					} else {
						
						if (produtoNovo == null || produtoNovo.estoque < r.quantidade) {
							flash.error("Estoque insuficiente! Disponível: " + (produtoNovo != null ? produtoNovo.estoque : 0));
							List<Produto> produtos = Produto.findAll();
							render("Reservas/forms.html", r, produtos);
							return;
						}
						
						if (produtoAntigo != null) {
							produtoAntigo.estoque += original.quantidade;
							produtoAntigo.save();
						}
						
						produtoNovo.estoque -= r.quantidade;
						produtoNovo.save();
					}
				}
			}
		}

		r.save();
		flash.success("Produto reservado com sucesso!");
		listar(null);
	}
	
	public static void editar(Long id) {
		Reserva r = Reserva.findById(id);
		List<Produto>produtos = Produto.findAll();
		renderTemplate("Reservas/forms.html", r, produtos);
	}
	
	public static void remover(Long id) {
		Reserva r = Reserva.findById(id);
		if(r!= null) {
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
	
	public static void formProduto() {
		Produto p = new Produto();
		renderTemplate("Reservas/formProduto.html", p);
	}
	
	public static void salvarProduto(Produto p) {
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
	
	public static void adicionarEstoque(Long id, int quantidade) {
		Produto p = Produto.findById(id);
		if (p != null && quantidade > 0) {
			p.estoque += quantidade;
			p.save();
			flash.success("Estoque de " + p.nomeProduto + " atualizado!");
		}
		listarProdutos();
	}

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
	

	public static void editarProduto(Long id) {
		Produto p = Produto.findById(id);
		renderTemplate("Reservas/formProduto.html", p);
	}

	public static void removerProduto(Long id) {
		Produto p = Produto.findById(id);
		if (p != null) {
			long qtdAtivas = Reserva.count("produto.id = ?1 and status != ?2", id, Status.INATIVO);
			if (qtdAtivas > 0) {
				flash.error("Não é possível excluir '" + p.nomeProduto + "': existem reservas ativas vinculadas a ele.");
			} else {
				
				List<Reserva> reservasInativas = Reserva.find("produto.id = ?1", id).fetch();
				for (Reserva r : reservasInativas) {
					r.delete();
				}
				p.delete();
				flash.success("Produto excluído com sucesso!");
			}
		}
		listarProdutos();
	}
}