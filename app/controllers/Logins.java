package controllers;

import models.Users;
import play.mvc.Controller;

public class Logins extends Controller{
	

	public static void form() {
		render();
	}

	public static void logar(String login, String senha) {
		Users usuario = Users.autenticar(login, senha);

		if (usuario == null) {
			flash.error("Usuário ou senha inválidos. Tente novamente!");
			form();
		}

		session.put("usuarioLogado", usuario.login);
		session.put("nomeUsuario", usuario.nome);
		session.put("perfilUsuario", usuario.perfil.name());

		flash.success("Bem-vindo(a), " + usuario.nome + "!");
		Reservas.listar(null);
	}

	public static void sair() {
		session.clear();
		form();
	}
}
