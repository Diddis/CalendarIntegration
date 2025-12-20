// DOM elements
const button1 = document.querySelector(".fetch-cal-btn");
const button2 = document.querySelector(".transfer-cal-btn");
const input = document.getElementById("timeeditUrl");
const output = document.getElementById("json-output");   // for testing/dev. purposes

// Event listener for the button
button1.addEventListener("click", async () => {
    const url = input.value.trim();

    if (!url) {
        alert("Fyll i en TimeEdit-URL.");
        return;
    }

    try {
        console.log("Fetching events...");  // Debugging line

        // Call the backend endpoint to fetch events
        const response = await fetch(`/kalender/timeEditURL?url=${encodeURIComponent(url)}`);

        if (!response.ok) {
            throw new Error("Det funkade inte.");
        }

        // Parse the JSON response
        const events = await response.json();
        console.log("Events fetched:", events);  // Debugging line

        // Display the JSON nicely formatted for testing
        output.textContent = JSON.stringify(events, null, 2);

        // Store the events in a global variable or pass them to the next function if needed
        window.eventsToPost = events;

    } catch (err) {
        console.error(err);
        alert("Kunde inte hämta data.");
    }
});

// Event listener for the button
button2.addEventListener("click", async () => {
    const events = window.eventsToPost;  // Retrieve events from the previous action

    if (!events) {
        alert("Inga händelser att överföra.");
        return;
    }

    try {
        console.log("Posting events to Canvas...");
        console.log("Sending events:", JSON.stringify(events));

        // Send the events to the backend Canvas endpoint via POST
        const postResponse = await fetch('/kalender/postToCanvas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(events)  // Send events as JSON in the body
        });

        // Log the response status and body
        console.log("Response status:", postResponse.status);
        const responseText = await postResponse.text();  // Get raw response text
        console.log("Response body:", responseText);

        // Handle plain text response
        if (responseText.includes("success")) {
            // Handle success
            output.textContent = `Success: ${responseText}`;
        } else {
            // Handle failure
            output.textContent = `Failure: ${responseText}`;
        }

    } catch (err) {
        console.error(err);
        alert("Kunde inte överföra data till Canvas: " + err.message);
    }
});