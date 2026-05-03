document.addEventListener('DOMContentLoaded', () => {
    const startBtn = document.getElementById('start-btn');

    startBtn.addEventListener('click', () => {
        // Effet sonore ou visuel optionnel ici
        console.log("Loading Selection...");
        window.location.href = "../character_selection/charcater_selection.html";
    });
});