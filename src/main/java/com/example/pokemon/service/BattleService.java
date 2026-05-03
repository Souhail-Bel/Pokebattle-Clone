package com.example.pokemon.service;

import com.example.pokemon.entity.Pokemon;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class BattleService {

    public int calculateDamage(Pokemon attacker, Pokemon defender) {
        int base = attacker.getAttack();
        int defense = defender.getDefense();

        int damage = base - (defense / 3);

        // randomness
        int variance = ThreadLocalRandom.current().nextInt(-5, 6);
        damage += variance;

        // critical hit (10%)
        boolean crit = ThreadLocalRandom.current().nextDouble() < 0.1;
        if (crit) {
            damage *= 2;
        }

        return Math.max(damage, 1);
    }

    public int calculateHeal(Pokemon pokemon) {
        // Basic heal logic: 25% of their attack power + some base value
        int healAmount = (pokemon.getAttack() / 2) + 10;
        return healAmount;
    }
}