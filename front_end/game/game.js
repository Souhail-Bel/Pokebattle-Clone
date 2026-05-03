let p1hp=100;
let p2hp=100;
const log=document.getElementById('battle-log');

const selectedid=localStorage.getItem('selectedPokemon');
function attack(){
    document.getElementById('attack-btn').disabled=true;
    const damage=Math.floor(Math.random()*20)+10;
    p2hp=Math.max(p2hp-damage,0);
    updateUI();
    animateImpact('enemy-container');
    log.innerHTML+=`<p>You dealt ${damage} damage!</p>`;
    if(p2hp>0){
        setTimeout(enemyturn,1000);
    }
    else{
        log.innerHTML+=`<p>You win!</p>`;
        setTimeout(()=>alert ("You win!"),1000);

    }
}
function enemyTurn() {
    const damage = Math.floor(Math.random() * 12) + 8;
    p1HP = Math.max(0, p1HP - damage);
    
    updateUI();
    animateImpact('player-container');
    log.innerText = `ENEMY HITS YOU FOR ${damage} HP!`;
    
    document.getElementById('attack-btn').disabled = false;

    if (p1HP <= 0) {
        log.innerText = "GAME OVER...";
        setTimeout(() => window.location.href = "../menu/menu.html", 2000);
    }
}
function updateUI(){
    const p1bar=document.getElementById("p1-health");
    const p2bar=document.getElementById("p2-health");
    if(p1hp<30){
        p1bar.classList.replace('bg-success','bg-danger');
    }
    if(p2hp<30){
        p2bar.classList.replace('bg-success','bg-danger');
    }
}
function animateImpact(containerId){
    const container=document.getElementById(containerId);
    container.classList.add("shake");
    setTimeout(()=>{
        container.classList.remove("shake");
    },500);
}

document.getElementById('attack-btn').addEventListener('click', attack);