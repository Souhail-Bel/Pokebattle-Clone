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

    @PostMapping("/join")
    public Battle joinBattle(@RequestParam Long pokemonId) {
        // 1. Find a battle waiting for a second player
        Battle battle = battleRepository.findAll().stream()
                .filter(b -> b.getStatus() == BattleStatus.WAITING)
                .findFirst()
                .orElse(null);

        Pokemon p = pokemonRepository.findById(pokemonId).orElseThrow();

        if (battle == null) {
            // Create new room as Player 1
            battle = new Battle();
            battle.setPlayer1PokemonId(pokemonId);
            battle.setPlayer1CurrentHp(p.getHp());
            battle.setStatus(BattleStatus.WAITING);
            battle.setPhase(TurnPhase.START);
            battle.setPlayer1Turn(true);
            return battleRepository.save(battle);
        } else {
            // Join existing room as Player 2
            battle.setPlayer2PokemonId(pokemonId);
            battle.setPlayer2CurrentHp(p.getHp());
            battle.setStatus(BattleStatus.IN_PROGRESS);
            return battleRepository.save(battle);
        }
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
    public Battle executeAttack(@PathVariable Long id, @RequestParam Long playerId) {
        Battle battle = battleRepository.findById(id).orElseThrow();

        // SECURITY: Is it this player's turn?
        boolean isP1 = playerId.equals(battle.getPlayer1PokemonId());
        if (battle.getStatus() != BattleStatus.IN_PROGRESS)
            throw new IllegalStateException("Game not active");
        if (isP1 != battle.isPlayer1Turn())
            throw new IllegalStateException("Not your turn!");

        Pokemon p1 = pokemonRepository.findById(battle.getPlayer1PokemonId()).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(battle.getPlayer2PokemonId()).orElseThrow();

        // Perform Action
        if (battle.isPlayer1Turn()) {
            int dmg = battleService.calculateDamage(p1, p2);
            battle.setPlayer2CurrentHp(Math.max(0, battle.getPlayer2CurrentHp() - dmg));
        } else {
            int dmg = battleService.calculateDamage(p2, p1);
            battle.setPlayer1CurrentHp(Math.max(0, battle.getPlayer1CurrentHp() - dmg));
        }

        // Check Win Condition
        if (battle.getPlayer1CurrentHp() <= 0 || battle.getPlayer2CurrentHp() <= 0) {
            battle.setStatus(BattleStatus.FINISHED);
        } else {
            // Flip turn
            battle.setPlayer1Turn(!battle.isPlayer1Turn());
            battle.setTurnNumber(battle.getTurnNumber() + 1);
        }

        return battleRepository.save(battle);
    }

    @PostMapping("/{id}/heal")
    public Battle executeHeal(@PathVariable Long id, @RequestParam Long playerId) {
        Battle battle = battleRepository.findById(id).orElseThrow();

        // SECURITY: Is it this player's turn?
        boolean isP1 = playerId.equals(battle.getPlayer1PokemonId());
        if (battle.getStatus() != BattleStatus.IN_PROGRESS)
            throw new IllegalStateException("Game not active");
        if (isP1 != battle.isPlayer1Turn())
            throw new IllegalStateException("Not your turn!");

        Pokemon p1 = pokemonRepository.findById(battle.getPlayer1PokemonId()).orElseThrow();
        Pokemon p2 = pokemonRepository.findById(battle.getPlayer2PokemonId()).orElseThrow();

        // Perform Heal (Example: heal 20 HP, capped at max HP)
        int healAmount = 20;
        if (battle.isPlayer1Turn()) {
            battle.setPlayer1CurrentHp(Math.min(p1.getHp(), battle.getPlayer1CurrentHp() + healAmount));
        } else {
            battle.setPlayer2CurrentHp(Math.min(p2.getHp(), battle.getPlayer2CurrentHp() + healAmount));
        }

        // Flip turn
        battle.setPlayer1Turn(!battle.isPlayer1Turn());
        battle.setTurnNumber(battle.getTurnNumber() + 1);

        return battleRepository.save(battle);
    }

    @PostMapping("/{id}/abandon")
    public void abandonBattle(@PathVariable Long id) {
        Battle battle = battleRepository.findById(id).orElseThrow();
        if (battle.getStatus() != BattleStatus.FINISHED) {
            battle.setStatus(BattleStatus.FINISHED);
            battleRepository.save(battle);
        }
    }
}
