package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Min;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Reserva extends Model {

	@Required
	public String nomeCliente;

	@Min(value = 1, message = "A quantidade deve ser maior que zero")
	public int quantidade;

	@Lob
	public String observacao;

	@Temporal(TemporalType.DATE)
	public Date data;

	@Required
	@ManyToOne
	public Produto produto;

	@Enumerated(EnumType.STRING)
	public Status status;

	public Reserva() {
		this.status = Status.ATIVO;
		this.data = new Date();
	}

	public double getValorTotal() {
		if (produto == null) {
			return 0;
		}
		return quantidade * produto.preco;
	}
}