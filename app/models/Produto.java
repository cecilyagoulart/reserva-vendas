package models;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import play.db.jpa.Model;
@Entity
public class Produto extends Model {
	
    public String nomeProduto;
    public double preco;
    public int estoque;
    
   
    public String toString() {
        return nomeProduto;
    }
}
	

