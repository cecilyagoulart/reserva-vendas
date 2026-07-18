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
	        // Busca todas as reservas ATIVAS (Requisito 4a)
	        reservas = Reserva.find("status != ?1", Status.INATIVO).fetch();
	    } else {
	        // Busca por nome do cliente OU nome do produto (Requisito 5 e 6)
	        reservas = Reserva.find("status != ?1 and (lower(nomeCliente) like ?2 or lower(produto.nomeProduto) like ?2)", 
	                                Status.INATIVO, "%" + termo.toLowerCase() + "%").fetch();
	    }
	    // Envia a lista e o termo de busca para o HTML
	    render(reservas, termo);
	}
	
	public static void salvar(Reserva r) {
	
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
			flash.success("Reserva removida!");
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
}
