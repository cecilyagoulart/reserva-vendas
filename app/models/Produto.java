package models;

import javax.persistence.Entity;

import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Produto extends Model {

	@Required
	public String nomeProduto;

	@Min(value = 0, message = "O preço não pode ser negativo")
	public double preco;

	@Min(value = 0, message = "O estoque não pode ser negativo")
	public int estoque;

	public String toString() {
		return nomeProduto;
	}
}