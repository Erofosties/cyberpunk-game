package com.cyberpunk.domain.defensa;

import jakarta.persistence.*;

@Entity
@Table(name = "defensas")
public class Defensas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int escudos;
    private int torretasNeocromo;
    private int canonesHexalium;
    private int integridadNave;

    // 🔹 Constructor obligatorio para JPA
    public Defensas() {
        this.escudos = 1;
        this.torretasNeocromo = 2;
        this.canonesHexalium = 1;
        this.integridadNave = 100;
    }

    public Long getId() {
        return id;
    }

    // 🔹 Poder defensivo total
    public int calcularPoderDefensivo() {
        int poderEscudos = escudos * 80;
        int poderTorretas = torretasNeocromo * 40;
        int poderCanones = canonesHexalium * 70;
        return poderEscudos + poderTorretas + poderCanones;
    }

    public void recibirAtaque(int fuerzaEnemiga) {
        int poderDefensa = calcularPoderDefensivo();

        if (poderDefensa >= fuerzaEnemiga) {
            dañarEstructuras(fuerzaEnemiga / 10);
        } else {
            int dañoRestante = fuerzaEnemiga - poderDefensa;
            integridadNave -= dañoRestante / 5;
            if (integridadNave < 0) integridadNave = 0;
            dañarEstructuras(fuerzaEnemiga / 5);
        }
    }

    private void dañarEstructuras(int impacto) {
        torretasNeocromo -= impacto / 20;
        canonesHexalium -= impacto / 25;

        if (torretasNeocromo < 0) torretasNeocromo = 0;
        if (canonesHexalium < 0) canonesHexalium = 0;
    }

    public void reparar(int cantidad) {
        integridadNave += cantidad;
        if (integridadNave > 100) integridadNave = 100;
    }

    public void construirEscudo() { escudos++; }
    public void construirTorreta() { torretasNeocromo++; }
    public void construirCanon() { canonesHexalium++; }
}