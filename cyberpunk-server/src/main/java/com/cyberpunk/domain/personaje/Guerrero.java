package com.cyberpunk.domain.personaje;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.cyberpunk.domain.map.MapSector;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "guerreros")
@DiscriminatorValue("GUERRERO")
public class Guerrero extends Personaje {

    private int fuerza;
    private int destreza;
    private int resistencia;
    private int hackeo;

    // NUEVO: rango de visión en el mapa
    private int vision = 1;

    private String misionPendiente;

    @ManyToOne
    @JoinColumn(name = "sector_mision_id")
    private MapSector sectorMision;

    private int ticksVueltaMision = 0;

    public Guerrero() {}

    public Guerrero(
            String nombre,
            int fuerza,
            int destreza,
            int resistencia,
            int hackeo) {

        super(nombre, 120);

        this.fuerza = fuerza;
        this.destreza = destreza;
        this.resistencia = resistencia;
        this.hackeo = hackeo;
    }

    // ================= PRODUCCIÓN =================

    @Override
    public int getProduccion() {
        return 0;
    }

    // ================= GETTERS =================

    public int getFuerza() {
        return fuerza;
    }

    public int getDestreza() {
        return destreza;
    }

    public int getResistencia() {
        return resistencia;
    }

    public int getHackeo() {
        return hackeo;
    }

    public int getVision() {
        return vision;
    }

    public String getMisionPendiente() {
        return misionPendiente;
    }

    public MapSector getSectorMision() {
        return sectorMision;
    }

    public int getTicksVueltaMision() {
        return ticksVueltaMision;
    }

    public void setVision(int vision) {
        this.vision = vision;
    }

    public void setMisionPendiente(String misionPendiente) {
        this.misionPendiente = misionPendiente;
    }

    public void setSectorMision(MapSector sectorMision) {
        this.sectorMision = sectorMision;
    }

    public void setTicksVueltaMision(int ticksVueltaMision) {
        this.ticksVueltaMision = Math.max(0, ticksVueltaMision);
    }

    @Override
    protected void perderAtributoAleatorio(Random random) {
        List<Runnable> reductores = new ArrayList<>();

        if (fuerza > 0) reductores.add(() -> fuerza--);
        if (destreza > 0) reductores.add(() -> destreza--);
        if (resistencia > 0) reductores.add(() -> resistencia--);
        if (hackeo > 0) reductores.add(() -> hackeo--);

        if (!reductores.isEmpty()) {
            reductores.get(random.nextInt(reductores.size())).run();
        }
    }

}