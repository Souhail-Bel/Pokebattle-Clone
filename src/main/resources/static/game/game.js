let currentBattleId = null;
let p1MaxHp = 100;
let p2MaxHp = 100;

const log = document.getElementById('battle-log');
const attackBtn = document.getElementById('attack-btn');
const healBtn = document.getElementById('heal-btn'); // Ensure your HTML has id="heal-btn"

async function heal() {
    attackBtn.disabled = true;
    healBtn.disabled = true;
    log.innerHTML = `<p>Healing...</p>`;

    try {
        const response = await fetch(`/api/battle/${currentBattleId}/heal`, { method: 'POST' });
        const battle = await response.json();
        
        updateUI(battle);
        log.innerHTML = `<p>You felt better!</p>`;

        // After healing, it's the enemy's turn
        setTimeout(enemyTurn, 1000);
    } catch (error) {
        console.error("Heal failed:", error);
    }
}

healBtn.addEventListener('click', heal);

// 1. Initialize Battle on Load
async function initBattle() {
    try {
        // 1. Fetch all available pokemon first
        const pokeResponse = await fetch('/api/pokemon');
        const allPokemon = await pokeResponse.json();

        if (allPokemon.length < 2) {
            log.innerHTML = "NOT ENOUGH POKEMON IN DATABASE";
            return;
        }

        // 2. Get Player 1 ID from selection (fallback to first in list)
        const p1Id = localStorage.getItem('selectedPokemon') || allPokemon[0].id;
        
        // 3. Pick a random opponent (p2) that isn't the player
        const opponents = allPokemon.filter(p => p.id != p1Id);
        const randomOpponent = opponents[Math.floor(Math.random() * opponents.length)] || allPokemon[0];
        const p2Id = randomOpponent.id;

        // 4. Start the battle on the backend
        const battleResponse = await fetch(`/api/battle/start?p1Id=${p1Id}&p2Id=${p2Id}`, { method: 'POST' });
        
        if (!battleResponse.ok) {
            throw new Error("Backend failed to start battle. Check if IDs exist.");
        }
        
        const battle = await battleResponse.json();

        // 5. Update UI State
        currentBattleId = battle.id;
        p1MaxHp = battle.player1CurrentHp;
        p2MaxHp = battle.player2CurrentHp;

        // Find the full pokemon objects for names and images
        const p1 = allPokemon.find(p => p.id == p1Id);
        const p2 = allPokemon.find(p => p.id == p2Id);

        document.getElementById('player-name-tag').innerText = p1.name;
        document.getElementById('enemy-name-tag').innerText = p2.name;
        document.getElementById('player-img').src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${p1.id}.png`;
        document.getElementById('enemy-img').src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${p2.id}.png`;

        log.innerHTML = `A wild ${p2.name} appeared!`;
        updateUI(battle);

    } catch (error) {
        console.error("Battle Init Error:", error);
        log.innerHTML = "SYSTEM ERROR: Check Backend Logs";
    }
}

// 2. Helper function to call the backend /attack endpoint
async function executePhase() {
    const response = await fetch(`/api/battle/${currentBattleId}/attack`, { method: 'POST' });
    return await response.json();
}

// 3. Player Attack Action
async function attack() {
    attackBtn.disabled = true;
    log.innerHTML = `<p>Attacking...</p>`;
    let battle;

    try {
        // Loop through START -> PLAYER_ACTION -> RESOLUTION -> END
        do {
            battle = await executePhase();
            updateUI(battle);
        } while (battle.phase !== 'END' && battle.status !== 'FINISHED');

        animateImpact('enemy-container');

        // If the battle isn't over, trigger the enemy's turn
        if (battle.status !== 'FINISHED') {
            setTimeout(enemyTurn, 1000);
        } else {
            endGame(battle);
        }
    } catch (error) {
        console.error("Attack failed:", error);
    }
}

// 4. Enemy Turn Action
async function enemyTurn() {
    log.innerHTML = `<p>Enemy is attacking...</p>`;
    let battle;

    try {
        do {
            battle = await executePhase();
            updateUI(battle);
        } while (battle.phase !== 'END' && battle.status !== 'FINISHED');

        animateImpact('player-container');

        if (battle.status !== 'FINISHED') {
            log.innerHTML = `<p>Your turn!</p>`;
            attackBtn.disabled = false;
        } else {
            endGame(battle);
        }
    } catch (error) {
        console.error("Enemy attack failed:", error);
    }
}

// 5. Update UI with Backend Data
function updateUI(battle) {
    const p1bar = document.getElementById("p1-health");
    const p2bar = document.getElementById("p2-health");

    const p1Percentage = Math.max((battle.player1CurrentHp / p1MaxHp) * 100, 0);
    const p2Percentage = Math.max((battle.player2CurrentHp / p2MaxHp) * 100, 0);

    p1bar.style.width = p1Percentage + '%';
    p2bar.style.width = p2Percentage + '%';


    if (p1Percentage < 30) p1bar.classList.replace('bg-success', 'bg-danger');
    if (p2Percentage < 30) p2bar.classList.replace('bg-success', 'bg-danger');
}

// 6. Animations and Cleanup
function animateImpact(containerId) {
    const container = document.getElementById(containerId);
    container.classList.add("shake");
    setTimeout(() => container.classList.remove("shake"), 500);
}

function endGame(battle) {
    const won = battle.player1CurrentHp > 0;
    log.innerHTML = won ? "<p>YOU WIN!</p>" : "<p>GAME OVER...</p>";
    setTimeout(() => window.location.href = "../menu/menu.html", 3000);
}

// Event Listeners
attackBtn.addEventListener('click', attack);

// Kick off the battle
initBattle();