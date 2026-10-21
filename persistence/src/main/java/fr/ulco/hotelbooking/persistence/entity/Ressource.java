package fr.ulco.hotelbooking.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "ressources")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer etage;

    @Column(nullable = false)
    private Integer capacite;

    @Column(length = 2000)
    private String description;

    protected Ressource() {
    }

    public Long getId() { return id; }

    public Integer getEtage() { return etage; }
    public void setEtage(Integer etage) { this.etage = etage; }

    public Integer getCapacite() { return capacite; }
    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }
}