async function fetchCharacters() {
  try {
    const response = await fetch("/api/pokemon");
    const data = await response.json();
    renderCards(data);
  } catch (error) {
    console.warn("Backend non trouvé, chargement des données de test...");
    const mockData = [
      { id: 1, nom: "MANDO", type: "fire", atk: 50, def: 30 },
      { id: 2, nom: "UNICCO", type: "water", atk: 45, def: 40 },
    ];
    renderCards(mockData);
  }
}

function renderCards(list) {
  const grid = document.getElementById("character-grid");
  grid.innerHTML = list.map((p) => {
    // On détermine la classe CSS selon le type (par défaut 'fire' si non trouvé)
    const typeClass = `card-${(p.type || "fire").toLowerCase()}`;
    return `
            <div class="col-6 col-md-3">
                <div class="card pokemon-card h-100 ${typeClass}" onclick="selectPokemon(${p.id})">
                <img src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${p.id}.png" class="card-img-top" alt="${p.name}">
                    <div class="card-body text-center p-2">
                        <p class="pokemon-name" style="font-size: 0.7rem;">${p.name}</p>
                        <div class="stats-mini" style="font-size: 0.5rem; color: #ffd700;">
                            ATK: ${p.attack} | DEF: ${p.defense}
                        </div>
                    </div>
                </div>
            </div>
        `;
  }).join("");
}

// Ensure the function name is exactly selectPokemon
function selectPokemon(pokemonId) {
  localStorage.setItem("selectedPokemon", pokemonId);
  window.location.href = "../game/game.html";
}

fetchCharacters();
