package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.db.jpa.Model;

@Entity
public class Reserva extends Model{

	public String nomeCliente;
	public int quantidade;

	@Temporal(TemporalType.DATE)
	public Date data;
	
	@ManyToOne
	public Produto produto;

	@Enumerated(EnumType.STRING)
	public Status status;
	
	public Reserva() {
		this.status = Status.ATIVO;
		this.data = new Date();
		}
		
	
	
	
}
