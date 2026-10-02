package models;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

@Entity
public class Users extends Model{

	public String nome;
	public String login;
	public String senha;

	@Enumerated(EnumType.STRING)
	public Perfil perfil;

	public static Users autenticar(String login, String senha) {
		Users user = Users.find("login = ?1 and senha = ?2", login, senha).first();
		return user;
	}
}
