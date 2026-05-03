package com.example.pokemon.controller;

import com.example.pokemon.entity.Pokemon;
import com.example.pokemon.repository.PokemonRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonRepository pokemonRepository;

    public PokemonController(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    @GetMapping
    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    @PostMapping
    public Pokemon createPokemon(@RequestBody Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }
}
