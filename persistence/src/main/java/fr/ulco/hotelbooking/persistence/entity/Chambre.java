package fr.ulco.hotelbooking.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "chambres")
@PrimaryKeyJoinColumn(name = "id")
public class Chambre extends Ressource {

    @Column(nullable = false, unique = true)
    private String numero;

    public Chambre() {
    }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
}