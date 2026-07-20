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
		// Verifica estoque antes de gravar
		 if (r.id == null) {
		        if (r.produto != null) {
		            Produto produtoAtual = Produto.findById(r.produto.id);
		            
		            // Validação de estoque para o novo pedido
		            if (produtoAtual == null || produtoAtual.estoque < r.quantidade) {
		                flash.error("Estoque insuficiente! Disponível: " + (produtoAtual != null ? produtoAtual.estoque : 0));
		                List<Produto> produtos = Produto.findAll();
		                render("Reservas/forms.html", r, produtos);
		                return;
		            }
		            
		            // Subtração: Só acontece aqui, na primeira vez que salva
		            produtoAtual.estoque -= r.quantidade;
		            produtoAtual.save();
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
			
			// Devolve o estoque ao remover a reserva
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
		p.save();
		flash.success("Produto cadastrado!");
		forms();
	}
	
	// ===== NOVO: controle de estoque =====
	
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
	// ===== NOVO: editar e excluir produto =====
	
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
					// Remove definitivamente as reservas inativas (histórico) vinculadas a este produto,
					// para liberar a exclusão sem violar a integridade do banco
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