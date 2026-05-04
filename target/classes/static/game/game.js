let currentBattleId = null;
let myPokemonId = localStorage.getItem('selectedPokemon');
let myRole = null;
let p1MaxHp = 100;
let p2MaxHp = 100;
let uiInitialized = false;
let timerInterval = null;
let timeLeft = 30;

let isMyTurnActive = false;

const log = document.getElementById('battle-log');
const attackBtn = document.getElementById('attack-btn');
const healBtn = document.getElementById('heal-btn');

// 1. Initialize Battle on Load
async function initBattle() {
    const response = await fetch(`/api/battle/join?pokemonId=${myPokemonId}`, { method: 'POST' });
    const battle = await response.json();
    currentBattleId = battle.id;
    // P1 just created the room → player2 slot is still empty
    // P2 just filled the room → both slots are set
    myRole = (battle.player2PokemonId == null) ? 'P1' : 'P2';
    setInterval(pollUpdate, 1500);
}

// 2. Action: Attack
async function attack() {
    disableControls(true);
    // Note: We append ?playerId= to prove to the backend who is attacking
    const response = await fetch(`/api/battle/${currentBattleId}/attack?playerId=${myPokemonId}`, { method: 'POST' });
    const battle = await response.json();

    updateUI(battle);
    animateImpact(myRole === 'P1' ? 'enemy-container' : 'player-container');
    // We do NOT call enemyTurn() here. pollUpdate() handles the turn swap.
}

// 3. Action: Heal
async function heal() {
    disableControls(true);
    log.innerHTML = `<p>Healing...</p>`;

    try {
        // Note: We append ?playerId= to prove to the backend who is healing
        const response = await fetch(`/api/battle/${currentBattleId}/heal?playerId=${myPokemonId}`, { method: 'POST' });
        const battle = await response.json();

        updateUI(battle);
        log.innerHTML = `<p>You felt better!</p>`;
        // We do NOT call enemyTurn() here. pollUpdate() handles the turn swap.
    } catch (error) {
        console.error("Heal failed:", error);
    }
}

// 4. Polling Logic (The heartbeat of multiplayer)
async function pollUpdate() {
    if (!currentBattleId) return;

    const response = await fetch(`/api/battle/${currentBattleId}`);
    const battle = await response.json();

    // Setup UI the moment Player 2 joins
    if (!uiInitialized && battle.player2PokemonId) {
        await setupStaticUI(battle);
    }

    updateUI(battle);

    // ENABLE buttons only if it is YOUR turn
    const isMyTurn = (myRole === 'P1' && battle.player1Turn) ||
        (myRole === 'P2' && !battle.player1Turn);

    const isGameOver = battle.status === 'FINISHED';

    if (battle.status === 'WAITING') {
        log.innerHTML = "Waiting for Player 2...";
        disableControls(true);
    } else if (isGameOver) {
        endGame(battle);
    } else {
        disableControls(!isMyTurn);
        if (isMyTurn) {
            log.innerHTML = "YOUR TURN!";
            if (!isMyTurnActive) {   // only start timer once per turn
                isMyTurnActive = true;
                startTurnTimer();
            }
        } else {
            isMyTurnActive = false;
            clearInterval(timerInterval);
            document.querySelector('.timer-style').textContent = '–';
            log.innerHTML = "Opponent is thinking...";
        }
    }
}

// 5. One-time Setup for Sprites and Names
async function setupStaticUI(battle) {
    const p1 = await (await fetch(`/api/pokemon`)).json().then(list => list.find(p => p.id == battle.player1PokemonId));
    const p2 = await (await fetch(`/api/pokemon`)).json().then(list => list.find(p => p.id == battle.player2PokemonId));

    myRole = (battle.player1PokemonId == myPokemonId) ? 'P1' : 'P2';
    p1MaxHp = p1.hp;
    p2MaxHp = p2.hp;

    const myPokemon = myRole === 'P1' ? p1 : p2;
    const enemyPokemon = myRole === 'P1' ? p2 : p1;
    const enemyRole = myRole === 'P1' ? 'P2' : 'P1';
    document.getElementById('player-name-tag').innerText = `[YOU - ${myRole}] ${myPokemon.name}`;
    document.getElementById('enemy-name-tag').innerText = `[${enemyRole}] ${enemyPokemon.name}`;
    document.getElementById('player-img').src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${myPokemon.id}.png`;
    document.getElementById('enemy-img').src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${enemyPokemon.id}.png`;

    document.getElementById('player-container').style.visibility = 'visible';
    document.getElementById('enemy-container').style.visibility = 'visible';
    uiInitialized = true;
}

// 6. Helpers & UI Updates
function disableControls(disabled) {
    attackBtn.disabled = disabled;
    healBtn.disabled = disabled;
}

function updateUI(battle) {
    const p1bar = document.getElementById("p1-health");
    const p2bar = document.getElementById("p2-health");

    const p1Percentage = Math.max((battle.player1CurrentHp / p1MaxHp) * 100, 0);
    const p2Percentage = Math.max((battle.player2CurrentHp / p2MaxHp) * 100, 0);

    p1bar.style.width = p1Percentage + '%';
    p2bar.style.width = p2Percentage + '%';

    if (p1Percentage < 30) p1bar.classList.replace('bg-success', 'bg-danger');
    else p1bar.classList.replace('bg-danger', 'bg-success'); // Added to fix color if healed above 30%

    if (p2Percentage < 30) p2bar.classList.replace('bg-success', 'bg-danger');
    else p2bar.classList.replace('bg-danger', 'bg-success'); // Added to fix color if healed above 30%
}

function animateImpact(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.classList.add("shake");
        setTimeout(() => container.classList.remove("shake"), 500);
    }
}

function endGame(battle) {
    disableControls(true);
    const p1Won = battle.player2CurrentHp <= 0;
    const opponentQuit = battle.player1CurrentHp > 0 && battle.player2CurrentHp > 0;

    let message;
    if (opponentQuit) {
        message = "<p>OPPONENT QUIT. YOU WIN!</p>";
    } else {
        const iWon = (myRole === 'P1' && p1Won) || (myRole === 'P2' && !p1Won);
        message = iWon ? "<p>YOU WIN!</p>" : "<p>GAME OVER...</p>";
    }
    log.innerHTML = message;
    setTimeout(() => window.location.href = "../menu/menu.html", 3000);
}

function startTurnTimer() {
    clearInterval(timerInterval);
    timeLeft = 30;
    document.querySelector('.timer-style').textContent = timeLeft;

    timerInterval = setInterval(() => {
        timeLeft--;
        document.querySelector('.timer-style').textContent = timeLeft;
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            // Auto-attack when time runs out
            attack();
        }
    }, 1000);
}

// Event Listeners
attackBtn.addEventListener('click', attack);
healBtn.addEventListener('click', heal);

window.addEventListener('beforeunload', () => {
    if (currentBattleId) {
        navigator.sendBeacon(`/api/battle/${currentBattleId}/abandon`);
    }
});

// Kick off the battle
initBattle();