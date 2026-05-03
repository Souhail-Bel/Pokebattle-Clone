package com.example.pokemon.component;

import com.example.pokemon.entity.Pokemon;
import com.example.pokemon.repository.PokemonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PokemonRepository pokemonRepository;

    public DataSeeder(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        pokemonRepository.deleteAll();

        pokemonRepository.save(new Pokemon(25L, "Pikachu", 55, 40, 100));
        pokemonRepository.save(new Pokemon(6L, "Charizard", 84, 78, 150));
        pokemonRepository.save(new Pokemon(1L, "Bulbasaur", 49, 49, 110));
        pokemonRepository.save(new Pokemon(7L, "Squirtle", 48, 65, 120));
        System.out.println("Starter Pokemon added to the database!");
    }
}
