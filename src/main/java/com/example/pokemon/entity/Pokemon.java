package com.example.pokemon.entity;

import jakarta.persistence.*;

@Entity
public class Pokemon {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int attack;
    private int defense;
    private int hp;

    // Default constructor required by Spring JPA
    public Pokemon() {
    }

    // Constructor for creating new instances
    public Pokemon(Long id, String name, int attack, int defense, int hp) {
        this.id = id;
        this.name = name;
        this.attack = attack;
        this.defense = defense;
        this.hp = hp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}
