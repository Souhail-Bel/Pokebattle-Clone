package com.example.pokemon.entity;

import jakarta.persistence.*;

@Entity
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long player1PokemonId;
    private Long player2PokemonId;
    
    private int player1CurrentHp;
    private int player2CurrentHp;
    
    private String status; // WAITING, IN_PROGRESS, FINISHED
    private int turnNumber;
    private boolean isPlayer1Turn;

    public Battle() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayer1PokemonId() { return player1PokemonId; }
    public void setPlayer1PokemonId(Long player1PokemonId) { this.player1PokemonId = player1PokemonId; }

    public Long getPlayer2PokemonId() { return player2PokemonId; }
    public void setPlayer2PokemonId(Long player2PokemonId) { this.player2PokemonId = player2PokemonId; }

    public int getPlayer1CurrentHp() { return player1CurrentHp; }
    public void setPlayer1CurrentHp(int player1CurrentHp) { this.player1CurrentHp = player1CurrentHp; }

    public int getPlayer2CurrentHp() { return player2CurrentHp; }
    public void setPlayer2CurrentHp(int player2CurrentHp) { this.player2CurrentHp = player2CurrentHp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public boolean isPlayer1Turn() { return isPlayer1Turn; }
    public void setPlayer1Turn(boolean player1Turn) { isPlayer1Turn = player1Turn; }
}
