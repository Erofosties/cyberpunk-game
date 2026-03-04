package com.cyberpunk.domain.personaje;

import java.util.Map;
import java.util.Random;
import jakarta.persistence.*;

@Entity
@Table(name="guerreros")


public class Guerrero extends Personaje{
	//Atributos
	public enum Tipo {
		CHOPPER,
		RUNNER,
		GUNNER,
		TANKER,
		GLITCHER,
		TROYWORM
	}
	
	//Mapeo con las stats base de cada tipo y el string de nombres para cada guerrero. Tira del enum tipo y de Stats
	private static final Map<Tipo, Stats> STATS_BASE = Map.of(
			Tipo.CHOPPER, new Stats(120, 3, 3, 3, 0, new String [] {"Hierroto", "Oxigeno", "Mordaz", "Rajas", "Navaja","Mandibula","Rompedor", "Filoafilado", 
					"Crack", "ZeroCut","Hemorragia", "BloodSync", "Carnicero","GodFist", "Vantablack"}),
			Tipo.RUNNER, new Stats(50, 1, 4, 1, 3, new String[] {"DustRunner","GravBlink","SolarSkim","SporeHop", "RockShard","PulseFoot", "HoloTrail", 
					"CraterDash", "WindSlip","EchoStep", "HuellaCero", "RastroEstelar", "SobreReloj", "BucleInfinito", "OmegaCorredor"}),
			Tipo.GUNNER, new Stats(100,6,3,3, 0, new String[] {"DisparoFulgor", "CañonNeón", "RayoLetal", "BalazoSombra", "FusilCrítico","PulsoCorto", 
					"ImpactoVeloz", "TiroCertero", "ExplosiónX", "BalaFantasma", "RifleRojo", "CiberFuego", "PlasmaStrike", "BlastCore", "ZeroShot"}),
			Tipo.TANKER, new Stats(250,4,3,5, 0, new String[] {"MuroDeAcero", "Blindado", "Coloso", "Fortix", "Titanio","CuerpoEskombro", "Bastión", 
					"Paredura", "EskudoLetal", "ArmaduraX", "IronShield", "HeavyCore", "SteelGuard", "Fatitán", "Wallz"}),
			Tipo.GLITCHER, new Stats(100,1,3,3,8, new String[] {"HackerMan", "SoyProgramador", "HacheTemele", "PublicVoid", "GuailTru","T-glicheoxd"}),
			Tipo.TROYWORM, new Stats(80,4,7,3,3, new String[] {"Gusano", "Backdoor", "Spaguetti", "Polñopo"})
			);
			
	private static class Stats{
		int vida, fuerza, destreza, resistencia, hackeo;
		String[] nombres;
		Stats(int vida, int fuerza, int destreza, int resistencia, int hackeo, String[] nombres){
			this.vida = vida;
			this.fuerza = fuerza;
			this.destreza = destreza;
			this.resistencia = resistencia;
			this.hackeo = hackeo;
			this.nombres = nombres;
		}
	}
		
	//Atributos
	private int fuerza;
	private int destreza;
	private int resistencia;
	private int hackeo;
    private boolean disponible;
	private Tipo tipo;
	
	//Solo para runners(explorar)ç
	private int unidadesExploradas;
	
	//Constructor
	public Guerrero (Tipo tipo) {
		super(selectNombre(tipo), STATS_BASE.get(tipo).vida);
		Stats s = STATS_BASE.get(tipo);
		this.tipo= tipo;
        this.fuerza = s.fuerza;
        this.resistencia = s.resistencia;
        this.destreza = s.destreza;
        this.hackeo = s.hackeo;
        this.disponible = true;
	}
	
	
	
	//Seleccion de nombre aleatorio segun cada tipo de guerrero
	private static String selectNombre(Tipo tipo) {
		String[] nombres = STATS_BASE.get(tipo).nombres;
		return nombres[new Random().nextInt(nombres.length)];		
		}
	

	//Getters y Setters
	@Override
    public int getProduccion() {return 0;}
	
	public int getFuerza() {return fuerza;} 
	public int getResistencia() {return resistencia;}
	public int getDestreza() {return destreza;}
	public int getHacke() {return hackeo;}
	public boolean getDisponible() {return disponible;}
	public void setFuerza(int modFuerza) {this.fuerza+=modFuerza;}
	public void setResistencia(int modResistencia) {this.resistencia+=modResistencia;}
	public void setDestreza(int modDestreza) {this.destreza+=modDestreza;}
	public void setHacke(int modHackeo) {this.hackeo+=modHackeo;}
	public void setDisponible(boolean disponible) {this.disponible = disponible;}
	
	
	public Tipo getTipo() {return tipo;}
	public boolean isDisponible() {return disponible;}
	
	public int explorar(int tiempo) {
		if (tipo != Tipo.RUNNER){
			throw new UnsupportedOperationException("Solo los Runners pueden explorar");
		}
		unidadesExploradas=tiempo*(destreza+(int)(Math.random()*2));
		return unidadesExploradas;
	}
	
}
