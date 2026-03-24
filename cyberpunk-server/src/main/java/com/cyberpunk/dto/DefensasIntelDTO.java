package com.cyberpunk.dto;

public class DefensasIntelDTO {

    private final Integer escudos;
    private final Integer torretasNeocromo;
    private final Integer canonesHexalium;

    public DefensasIntelDTO(Integer escudos, Integer torretasNeocromo, Integer canonesHexalium) {
        this.escudos = escudos;
        this.torretasNeocromo = torretasNeocromo;
        this.canonesHexalium = canonesHexalium;
    }

    public Integer getEscudos() {
        return escudos;
    }

    public Integer getTorretasNeocromo() {
        return torretasNeocromo;
    }

    public Integer getCanonesHexalium() {
        return canonesHexalium;
    }
}
