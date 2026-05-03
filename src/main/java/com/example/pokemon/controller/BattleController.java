package com.example.pokemon.controller;

import com.example.pokemon.entity.Battle;
import com.example.pokemon.entity.BattleStatus;
import com.example.pokemon.entity.Pokemon;
import com.example.pokemon.entity.TurnPhase;
import com.example.pokemon.repository.BattleRepository;
import com.example.pokemon.repository.PokemonRepository;
import com.example.pokemon.service.BattleService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleRepository battleRepository;
    private final PokemonRepository pokemonRepository;
    private final BattleService battleService;

    public BattleController(BattleRepository battleRepository, PokemonRepository pokemonRepository,
            BattleService battleService) {
        this.battleRepository = battleRepository;
        this.pokemonRepository = pokemonRepository;
        this.battleService = battleService;
    }

    @PostMapping("/start")
    public Battle startBattle(@RequestParam Long p1Id, @RequestParam Long p2Id) {
        Pokemon p1 = pokemonRepository.findById(p1Id).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(p2Id).orElseThrow();

        Battle battle = new Battle();
        battle.setPlayer1PokemonId(p1.getId());
        battle.setPlayer2PokemonId(p2.getId());

        battle.setPlayer1CurrentHp(p1.getHp());
        battle.setPlayer2CurrentHp(p2.getHp());

        battle.setStatus(BattleStatus.IN_PROGRESS);
        battle.setPhase(TurnPhase.START);
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

        if (battle.getPhase() != TurnPhase.PLAYER_ACTION &&
                battle.getPhase() != TurnPhase.START &&
                battle.getPhase() != TurnPhase.RESOLUTION &&
                battle.getPhase() != TurnPhase.END) {
            throw new IllegalStateException("Invalid phase");
        }

        if (battle.getStatus() == BattleStatus.FINISHED) {
            throw new IllegalStateException("Battle is already over!");
        }

        // Fetch the base stats of the Pokémon to calculate damage
        Pokemon p1 = pokemonRepository.findById(battle.getPlayer1PokemonId()).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(battle.getPlayer2PokemonId()).orElseThrow();

        switch (battle.getPhase()) {
            case START:
                battle.setPhase(TurnPhase.PLAYER_ACTION);
                break;

            case PLAYER_ACTION:
                // Very simple MVP damage calculation
                if (battle.isPlayer1Turn()) {
                    int damage = battleService.calculateDamage(p1, p2); // Arbitrary simple math
                    battle.setPlayer2CurrentHp(battle.getPlayer2CurrentHp() - damage);
                } else {
                    int damage = battleService.calculateDamage(p2, p1);
                    battle.setPlayer1CurrentHp(battle.getPlayer1CurrentHp() - damage);
                }

                battle.setPhase(TurnPhase.RESOLUTION);
                break;

            case RESOLUTION:
                // Check for faint (HP drops below 0)
                if (battle.getPlayer1CurrentHp() <= 0 || battle.getPlayer2CurrentHp() <= 0) {
                    battle.setStatus(BattleStatus.FINISHED);
                    return battleRepository.save(battle);
                }

                battle.setPhase(TurnPhase.END);
                break;

            case END:
                battle.setPlayer1Turn(!battle.isPlayer1Turn());
                battle.setTurnNumber(battle.getTurnNumber() + 1);
                battle.setPhase(TurnPhase.START);
                break;

        }

        return battleRepository.save(battle); // Saves the new state back to Postgres
    }

    @PostMapping("/{id}/heal")
    public Battle heal(@PathVariable Long id) {
        Battle battle = battleRepository.findById(id).orElseThrow();
        Pokemon p1 = pokemonRepository.findById(battle.getPlayer1PokemonId()).orElseThrow();

        if (battle.isPlayer1Turn() && battle.getStatus() == BattleStatus.IN_PROGRESS) {
            int amount = battleService.calculateHeal(p1);
            battle.setPlayer1CurrentHp(Math.min(battle.getPlayer1CurrentHp() + amount, 100)); // Cap at 100 or maxHP

            // Advance turn and phase
            battle.setTurnNumber(battle.getTurnNumber() + 1);
            battle.setPlayer1Turn(false); // End player turn
        }

        return battleRepository.save(battle);
    }
}
