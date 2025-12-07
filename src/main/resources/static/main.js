// DOM elements
const button = document.querySelector(".transfer-btn");
const input = document.getElementById("timeeditUrl");
const output = document.getElementById("json-output");   // for testing/dev. purposes

// Event listener for the button
button.addEventListener("click", async () => {
    const url = input.value.trim();

    if (!url) {
        alert("Fyll i en TimeEdit-URL.");
        return;
    }

    try {
        // Call the backend endpoint
        const response = await fetch(`/kalender/timeEditURL?url=${encodeURIComponent(url)}`);

        if (!response.ok) {
            throw new Error("Det funkade inte.");
        }

        // Parse the JSON
        const json = await response.json();

        // Display the JSON nicely formatted
        output.textContent = JSON.stringify(json, null, 2);

    } catch (err) {
        console.error(err);
        alert("Kunde inte hämta data.");
    }
});
