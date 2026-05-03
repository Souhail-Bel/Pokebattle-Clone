package com.example.pokemon.controller;

import com.example.pokemon.entity.Battle;
import com.example.pokemon.entity.Pokemon;
import com.example.pokemon.repository.BattleRepository;
import com.example.pokemon.repository.PokemonRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleRepository battleRepository;
    private final PokemonRepository pokemonRepository;

    public BattleController(BattleRepository battleRepository, PokemonRepository pokemonRepository) {
        this.battleRepository = battleRepository;
        this.pokemonRepository = pokemonRepository;
    }

    @PostMapping("/start")
    public Battle startBattle(@RequestParam Long p1Id, @RequestParam Long p2Id) {
        Pokemon p1 = pokemonRepository.findById(p1Id).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(p2Id).orElseThrow();

        Battle battle = new Battle();
        battle.setPlayer1PokemonId(p1.getId());
        battle.setPlayer2PokemonId(p2.getId());
        // For MVP, we will use Defense as HP just to keep the entity simple
        battle.setPlayer1CurrentHp(p1.getDefense() * 2); 
        battle.setPlayer2CurrentHp(p2.getDefense() * 2);
        
        battle.setStatus("IN_PROGRESS");
        battle.setPlayer1Turn(true);
        battle.setTurnNumber(1);

        return battleRepository.save(battle);
    }

    @GetMapping("/{id}")
    public Battle getBattleStatus(@PathVariable Long id) {
        return battleRepository.findById(id).orElseThrow();
    }

    @PostMapping("/{id}/attack")
    public Battle executeAttack(@PathVariable Long id) {
        Battle battle = battleRepository.findById(id).orElseThrow();
        
        if (battle.getStatus().equals("FINISHED")) {
            throw new IllegalStateException("Battle is already over!");
        }

        // Fetch the base stats of the Pokémon to calculate damage
        Pokemon p1 = pokemonRepository.findById(battle.getPlayer1PokemonId()).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(battle.getPlayer2PokemonId()).orElseThrow();

        // Very simple MVP damage calculation
        if (battle.isPlayer1Turn()) {
            int damage = p1.getAttack() / 2; // Arbitrary simple math
            battle.setPlayer2CurrentHp(battle.getPlayer2CurrentHp() - damage);
            battle.setPlayer1Turn(false); // Switch turns
        } else {
            int damage = p2.getAttack() / 2;
            battle.setPlayer1CurrentHp(battle.getPlayer1CurrentHp() - damage);
            battle.setPlayer1Turn(true);
        }

        battle.setTurnNumber(battle.getTurnNumber() + 1);

        // Check for faint (HP drops below 0)
        if (battle.getPlayer1CurrentHp() <= 0 || battle.getPlayer2CurrentHp() <= 0) {
            battle.setStatus("FINISHED");
        }

        return battleRepository.save(battle); // Saves the new state back to Postgres
    }
}
