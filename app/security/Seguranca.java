package security;

import controllers.Logins;
import controllers.Reservas;
import models.Perfil;
import play.mvc.Before;
import play.mvc.Controller;

public class Seguranca extends Controller {

	@Before(priority = 1)
	static void autenticar() {
		if (!session.contains("usuarioLogado")) {
			flash.error("Restrito para usuários autenticados!");
			Logins.form();
		}
	}

	@Before(priority = 2)
	static void verificarAdministrador() {
		Administrador anotacao = getActionAnnotation(Administrador.class);
		String perfil = session.get("perfilUsuario");

		if (anotacao != null && !Perfil.ADMIN.name().equals(perfil)) {
			flash.error("Acesso restrito aos administradores do sistema.");
			Reservas.listar(null);
		}
	}
}
