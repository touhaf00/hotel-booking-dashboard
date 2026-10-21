package fr.ulco.hotelbooking.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "salles")
@PrimaryKeyJoinColumn(name = "id")
public class Salle extends Ressource {

    @Column(nullable = false)
    private String nom;

    public Salle() {
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}