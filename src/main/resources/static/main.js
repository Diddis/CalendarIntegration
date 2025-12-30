// DOM elements
const button1 = document.getElementById("fetch-cal-btn");   // I Uppgift-3 var denna id "transfer-btn". 
const button2 = document.getElementById("transfer-cal-btn");
const clearButton = document.getElementById("empty-cal-btn");
const input = document.getElementById("timeeditUrl");
const output = document.getElementById("json-output");   // for testing/dev. purposes

const API_BASE = "";    // Kunde ändras från "" till "/kalender", men behövs det eller blir det då bara krångligare?
const tableBody = document.querySelector(".data-table tbody");
// const selectedCountEl = document.querySelector(".selected-count strong");  KS: Element & klass för räknaren behövs till HTML
// const errorEl = document.querySelector(".error-message");     KS: Element & klass för felmeddelande behövs till HTML
let events= [];  // I Uppgift-3 var denna variabel "students".
let calTable = [];  // I Uppgift-3 var denna variabel "courseTabell". 
let errorTimer = null;


// Event listener for the button
clearButton.addEventListener("click", () => {
  document.getElementById("timeeditUrl").value = "";
  document.getElementById("course-code-value").innerText = ``; 
  document.getElementById("course-name-value").innerText = ``; 

  output.textContent = "";    // Clear the output area from previous JSON data displayed (for testing purposes)
  tableBody.innerHTML = "";   // Clear the table body
  events = []; 
  console.log("Alla fält är nu tömda."); 
});



async function fetchJSON(path) {  
  console.log("Hämtar kalenderhändelser från: ", path);  
  const res = await fetch(`${API_BASE}${path}`);
  if (!res.ok) {
    throw new Error(`Kunde inte läsa från: ${path} : ${res.status}`);
  }
  return res.json();
}

// Event listener for the button
button1.addEventListener("click", async () => {
    const url = input.value.trim();

    if (!url) {
        alert("Fyll i en TimeEdit-URL.");
        return;
    }

    try {
        // First, call the backend endpoint to fetch events from TimeEdit 
        // Then, parse the JSON response and assign it to JS-variable 'events'
        // ( Observe that the declaration-word "const" of 'events' was removed to make the variable global (shadowing issue) )
        const calEvents = await fetchJSON(`/kalender/timeEditURL?url=${encodeURIComponent(url)}`);
        
        // Debugging line for fetching events
        console.log("Following events were fetched:", calEvents);  

        // Display the JSON nicely formatted for testing
        output.textContent = JSON.stringify(calEvents, null, 2);       
        
        // Store the events in a global variable or pass them to the next function if needed
        // window.eventsToPost = calEvents;   KS: Deactivated this statement as to use the global 'events' variable instead.

        // KS: Assign fetched events to global variable for table rendering
        events = calEvents;   
        
        // KS: Render the table with the fetched events 
        renderTable(); 

        // KS: Get course code and name (variables 'courseCode' & 'courseName') from the first event fetched   
        const info = getCourseCodeName();
        console.log("Visar kursdata för:", info.courseCode + ": " + info.courseName);
        // Om du har ett element med id "course-header":
        document.getElementById("course-code-value").innerText = `${info.courseCode}`; 
        document.getElementById("course-name-value").innerText = `${info.courseName}`;        
        // TESTING: document.getElementById("course-header").innerText = `${info.courseCode} & ${info.courseName}`;      
        

    } catch (err) {
        console.error(err);
        alert("Kunde inte hämta data.");
    }
});


// Event listener for the button
button2.addEventListener("click", async () => {
    // const events = window.eventsToPost;  KS: Deactivated this statement as to use the global 'events' variable instead.

    // Here and onwards in the function using the global 'events' variable
    if (!events || events.length === 0) {
        alert("Inga händelser att överföra.");
        return;
    }

    try {
        console.log("Posting events to Canvas");
        const canvasData = JSON.stringify(events);

        // Send the events to the backend Canvas endpoint via POST
        const postResponse = await fetch('/kalender/postToCanvas', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: canvasData // Send events as JSON in the body
        });

        clearButton.click();  // KS addition: Clear all fields after posting data to Canvas

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

/* KS: Under arbete..... */
function renderTable() {
  // Bygger tabellrader från aktuell sorteringsordning
  const data = [...events]

  tableBody.innerHTML = data
    .map((event) => {
      const checkboxCell = event.editable
        ? `<input aria-label="Markera ${event.startAt}" type="checkbox" data-id="${event.startAt}" 
        ${event.marked ? "checked" : ""} />`
        : "";

      /*  Alternativ till koden ovan för att alltid visa checkboxar (oavsett om event.editable är true eller false)
        const checkboxCell = `<input type="checkbox" data-id="${event.startAt}" ${event.marked ? "checked" : ""} />`;
      */

      const dateCell = event.startAt
        ? event.startAt.split('T')[0] : "";

      const startTimeCell = event.startAt
        ? event.startAt.slice(11, 16) : "";
       
      const endTimeCell = event.startAt
        ? event.endAt.slice(11, 16) : "";

      const activityCell = event.activity
        ? event.activity : "";  
      
      const placeCell = event.locationName
        ? event.locationName : "";
        // ? event.locationName.replace("Zoom, ", "") : "";  Tar bort "Zoom, " från platsnamnet om det finns där.

      const personCell = event.person
        ? event.person : ""; 

      const zoomCell = event.zoomLink
        ? `<a href="${event.zoomLink}">https://ltu-se.zoom.us</a>` : "";

      const commentCell = event.comments
        ? event.comments : "";    

      return `<tr>
        <td class="cell-check">${checkboxCell}</td>
        <td class="cell-date">${dateCell}</td>
        <td class="cell-time">
            <span>${startTimeCell} - ${endTimeCell}</span>
        </td>
        <td class="cell-activity">
            <span>${activityCell}: ${placeCell}</span>
        </td>
        <td class="cell-person">${personCell}</td> 
        <td class="cell-zoom">${zoomCell}</td>
        <td class="cell-comments">${commentCell}</td>    
      </tr>`;
    })
    .join("");

  // updateSelectedCount();     KS: Uppdatera räknaren för markerade rader (Kan kompletteras med detta senare)
  // attachCheckboxHandlers();  KS: Aktivera checkbox-hantering (Kan kompletteras med detta senare)
  // updateSortIndicators();    KS: Uppdatera sorteringsindikatorer (Behöver tilläggas bara om sortering implementeras)
}
/* ......... KS arbete slutar här */

<<<<<<< HEAD
=======
function getCourseCodeName() {
  const urlParams = new URLSearchParams(window.location.search);
  const courseCode = urlParams.get("courseCode");
  const courseName = urlParams.get("courseName");
  return { courseCode, courseName };
}


function getCourseCodeName() {
    // Check for fetched events
    if (events && events.length > 0) {
        // Extract data from the first event in the list
        const firstEvent = events[0];

        return {
            // Use field names from backend (CalendarEvent.java)
            courseCode: firstEvent.kurskod || "Kursod saknas",
            courseName: firstEvent.kurs || "Kursnamn saknas"
        };
    }
    return { courseCode: null, courseName: null };
}

>>>>>>> ddb54ef (Added dynamics for: Course code+name fields & Clear-fields button)
