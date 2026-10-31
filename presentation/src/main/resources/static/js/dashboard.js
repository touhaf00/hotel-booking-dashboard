(() => {
    "use strict";

    const bouton = document.getElementById("actualiser-dashboard");
    const libelle = document.getElementById("libelle-actualiser");
    const message = document.getElementById("message-actualisation");

    if (!bouton || !libelle || !message) return;

    let enCours = false;

    bouton.addEventListener("click", async (event) => {
        if (event.ctrlKey || event.metaKey ||
            event.shiftKey || event.altKey) return;

        const contenu = document.getElementById("dashboard-contenu");
        if (!contenu) return;

        event.preventDefault();
        if (enCours) return;

        enCours = true;
        bouton.setAttribute("aria-disabled", "true");
        bouton.classList.add("opacity-75");
        contenu.setAttribute("aria-busy", "true");

        libelle.textContent = "Actualisation…";
        message.classList.remove("text-danger");
        message.classList.add("text-secondary");
        message.textContent = "Mise à jour en cours…";

        const controleur = new AbortController();
        const delai = setTimeout(() => controleur.abort(), 15000);

        try {
            const reponse = await fetch(bouton.dataset.url, {
                headers: { "Accept": "text/html" },
                cache: "no-store",
                signal: controleur.signal
            });

            if (!reponse.ok) {
                throw new Error(`HTTP ${reponse.status}`);
            }

            const html = await reponse.text();
            const page = new DOMParser().parseFromString(
                html,
                "text/html"
            );

            const nouveauContenu = page.getElementById(
                "dashboard-contenu"
            );

            if (!nouveauContenu) {
                throw new Error("Fragment absent");
            }

            contenu.replaceWith(nouveauContenu);

            const heure = new Date().toLocaleTimeString("fr-FR");
            message.textContent = `Données mises à jour à ${heure}.`;

        } catch (erreur) {
            message.classList.remove("text-secondary");
            message.classList.add("text-danger");
            message.textContent =
                "Impossible d’actualiser. Les données affichées sont conservées. Réessayez.";

        } finally {
            clearTimeout(delai);
            enCours = false;

            bouton.removeAttribute("aria-disabled");
            bouton.classList.remove("opacity-75");
            libelle.textContent = "Actualiser";

            document.getElementById("dashboard-contenu")
                .removeAttribute("aria-busy");
        }
    });
})();