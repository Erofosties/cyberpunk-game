package com.cyberpunk.domain.defensa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    // Constructor obligatorio JPA
    public Defensas() {
        inicializarDefensas();
    }

    private void inicializarDefensas() {
        this.escudos = 1;
        this.torretasNeocromo = 2;
        this.canonesHexalium = 0;
        this.integridadNave = 100;
    }

    // ================= COMBATE =================

    public int calcularPoderDefensivo() {

        int poderEscudos = escudos * 80;

        int poderTorretas = torretasNeocromo * 40;

        int poderCanones = canonesHexalium * 70;

        return poderEscudos + poderTorretas + poderCanones;
    }

    public int getPenalizacionExploracion() {
        return escudos * 2;
    }

    public int calcularDanioConstanteAtaque(boolean objetivoEsNave) {
        int base = (torretasNeocromo * 3) + (canonesHexalium * 5);
        return objetivoEsNave ? base : Math.max(1, base / 2);
    }

    public boolean defensasActivas() {
        return torretasNeocromo > 0 || canonesHexalium > 0;
    }

    public void recibirAtaqueEnSector(int fuerzaEnemiga) {
        dañarEstructuras(Math.max(1, fuerzaEnemiga / 2));
    }

    public void recibirAtaque(int fuerzaEnemiga) {

        int poderDefensa = calcularPoderDefensivo();

        if (poderDefensa >= fuerzaEnemiga) {

            dañarEstructuras(fuerzaEnemiga / 10);

        } else {

            int dañoRestante = fuerzaEnemiga - poderDefensa;

            integridadNave -= dañoRestante / 5;

            if (integridadNave < 0)
                integridadNave = 0;

            dañarEstructuras(fuerzaEnemiga / 5);
        }
    }

    private void dañarEstructuras(int impacto) {

        int dañoTorretas = Math.max(1, impacto / 20);

        int dañoCanones = Math.max(1, impacto / 25);

        torretasNeocromo -= dañoTorretas;

        canonesHexalium -= dañoCanones;

        if (torretasNeocromo < 0)
            torretasNeocromo = 0;

        if (canonesHexalium < 0)
            canonesHexalium = 0;
    }

    // ================= REPARACIÓN =================

    public void reparar(int cantidad) {

        integridadNave += cantidad;

        if (integridadNave > 100)
            integridadNave = 100;
    }

    // ================= CONSTRUCCIÓN =================

    public void construirEscudo() { escudos++; }

    public void construirTorreta() { torretasNeocromo++; }

    public void construirCanon() { canonesHexalium++; }

    // ================= GETTERS =================

    public Long getId() { return id; }

    public int getEscudos() { return escudos; }

    public int getTorretasNeocromo() { return torretasNeocromo; }

    public int getCanonesHexalium() { return canonesHexalium; }

    public int getIntegridadNave() { return integridadNave; }
}