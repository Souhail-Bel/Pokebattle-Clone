async function fetchCharacters(){
    try{
        const response=await fetch('http://localhost:8080/api/pokemons');
        const data=await response.json();
        renderCards(data);
    }catch(error){
        console.warn("Backend non trouvé, chargement des données de test...");
        const mockData = [
            { id: 1, nom: "MANDO", type: "fire", atk: 50, def: 30 },
            { id: 2, nom: "UNICCO", type: "water", atk: 45, def: 40 }
        ];
        renderCards(mockData);
    }  
}

function renderCards(list) {
    const grid = document.getElementById('character-grid');
    
    grid.innerHTML = list.map(p => {
        // On détermine la classe CSS selon le type (par défaut 'fire' si non trouvé)
        const typeClass = `card-${(p.type || 'fire').toLowerCase()}`;
        
        return `
            <div class="col-6 col-md-3">
                <div class="card pokemon-card h-100 ${typeClass}" onclick="window.selectPokemon(${p.id})">
                    <img src="https://picsum.photos/seed/${p.nom}/200" class="card-img-top" alt="${p.nom}">
                    <div class="card-body text-center p-2">
                        <p class="pokemon-name" style="font-size: 0.7rem;">${p.nom}</p>
                        <div class="stats-mini" style="font-size: 0.5rem; color: #ffd700;">
                            ATK: ${p.atk} | DEF: ${p.def}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}
function selectCharacter(pokemonId){
    console.log("Selected Pokemon ID:", pokemonId);
    localStorage.setItem('selectedPokemon', pokemonId);
    window.location.href = "../game/game.html";
}

fetchCharacters();