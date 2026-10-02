package jobs;

import models.Perfil;
import models.Produto;
import models.Users;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Inicializador extends Job {

	@Override
	public void doJob() throws Exception {

		if (Users.count() != 0) {
			return;
		}

		Users admin = new Users();
		admin.nome = "Administrador";
		admin.login = "admin";
		admin.senha = "admin123";
		admin.perfil = Perfil.ADMIN;
		admin.save();

		Users operador = new Users();
		operador.nome = "Operador de Vendas";
		operador.login = "operador";
		operador.senha = "op123";
		operador.perfil = Perfil.OPERADOR;
		operador.save();

		Produto p1 = new Produto();
		p1.nomeProduto = "Dindin de Pudim";
		p1.preco = 5.50;
		p1.estoque = 20;
		p1.save();

		Produto p2 = new Produto();
		p2.nomeProduto = "Brownie";
		p2.preco = 5.00;
		p2.estoque = 15;
		p2.save();

		System.out.println("Inicializador executado com sucesso!");
	}
}