package com.cyberpunk.dto;

public class ConstruirEdificioRequest {
	
	private Long coloniaId;
	private String tipoEdificio;
	
	public Long getColoniaId() {
		return coloniaId;
	}
	
	public void setColoniaId(Long coloniaId) {
		this.coloniaId = coloniaId;
	}
	
	public String getTipoEdificio() {
		return tipoEdificio;
	}
	public void setTipoEdificio(String tipoEdificio) {
		this.tipoEdificio = tipoEdificio;
	}

}
