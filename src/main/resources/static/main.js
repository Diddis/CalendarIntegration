// DOM elements
const button1 = document.getElementById("fetch-cal-btn");   // I Uppgift-3 var denna id "transfer-btn". 
const button2 = document.getElementById("transfer-cal-btn");
const clearButton = document.getElementById("empty-cal-btn");
const input = document.getElementById("timeeditUrl");
const output = document.getElementById("json-output");   // for testing/dev. purposes

// KS tillägg 
const API_BASE = "";    // Kunde ändras från "" till "/kalender", men behövs det eller blir det då bara krångligare?
const idInput = document.getElementById("cal-id"); // KS tillägg 2026-01-01 
const tableBody = document.querySelector(".data-table tbody");
const markedCount = document.getElementById("count");  
// const errorEl = document.querySelector(".error-message");     KS: Element & klass för felmeddelande behövs till HTML
const markAllBtn = document.getElementById("mark-all-btn");  // KS tillägg 2026-01-04
const markNoneBtn = document.getElementById("mark-none-btn");  // KS tillägg 2026-01-04

let events= [];  // I Uppgift-3 var denna variabel "students".
let calTable = [];  // I Uppgift-3 var denna variabel "courseTabell". 
let errorTimer = null;


// Event listener for the button
clearButton.addEventListener("click", () => {
  document.getElementById("timeeditUrl").value = "";
  document.getElementById("course-code-value").innerText = ``; 
  document.getElementById("course-name-value").innerText = ``;
  document.getElementById("cal-id").value = "";  // KS tillägg 2026-01-01

  output.textContent = "";    // Clear the output area from previous JSON data displayed (for testing purposes)
  tableBody.innerHTML = "";   // Clear the table body
  events = [];
  updateMarkedCount();      // Update the marked count to "0" 
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
        
        // Store the events in a global variable or pass them to the next function if needed
        // window.eventsToPost = calEvents;   KS: Deactivated this statement as to use the global 'events' variable instead.

        // KS: Assign fetched events to global variable 'events' for table rendering
        events = calEvents;  

        // KS: Call function to add boolean property 'marked' to events, also update global 'events' variable 
        addMarkedToEvents();

        // Display the JSON nicely formatted for testing
        output.textContent = JSON.stringify(events, null, 2);  
        
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
        alert("FEL: Kunde inte hämta data.");
    }
});


// Event listener for the button
button2.addEventListener("click", async () => {
  // const events = window.eventsToPost;  KS: Deactivated this statement as to use the global 'events' variable instead.

  // KS tillägg 2026-01-01 börjar här ...         
  const currentId = idInput.value.trim();  // Hämta det aktuella värdet vid knapptryckning ("Överför kalender")
  if (!currentId) {
      alert("Fyll i ett ID i fältet för att kunna överföra händelser.");
      return;
  }
  // .... KS tillägg 2026-01-01 slutar här 


  // KS 205-01-03 modifications start:
  // Create new filtered list and include only marked events
  const eventsToTransfer = events.filter(e => e.marked === true);  

  if (eventsToTransfer.length === 0) {
      alert("FEL: Inga händelser att överföra.");
      return;
  }

  /* KS: Kontroller att antalet markerade events stämmer med antal events i listan eventsToTransfer
  alert("Antalet events i listan eventsToTransfer: " + eventsToTransfer.length); 
  alert("Antalet events i variabeln markedCount: " + Number(markedCount.textContent)); 
  */
  
  if (eventsToTransfer.length !== Number(markedCount.textContent)) {
      alert("FEL: Antalet markerade händelser motsvarar inte antalet som ska överföras. Överföringen avbruten.");
      return;
  }
  
  // Använd sedan den nya variabeln i JSON.stringify
  //  const canvasData = JSON.stringify(eventsToTransfer);  KS: Kopierade denna rad ner till "try" -blocket nedan. 
  // .... KS 205-01-03 modifications end here

  /*
  events = events.filter(e => e.marked);  // KS 205-01-03: Filter and include only marked events to be posted to Canvas
  // Here and onwards in the function using the global 'events' variable
  if (!events || events.length === 0) {
      alert("Inga händelser att överföra.");
      return;
  }
  */

  try {
      console.log("Överför events till Canvas. Antal: ", eventsToTransfer.length);
      const canvasData = JSON.stringify(eventsToTransfer);

      // Send the events to the backend Canvas endpoint via POST
      //  const postResponse = await fetch('/kalender/postToCanvas', {   // KS ändring 2026-01-01
      // KS ändring 2026-01-01 i raden nedan (const postResponse = ...)  
      const postResponse = await fetch(`/kalender/postToCanvas?idInputCode=${encodeURIComponent(currentId)}`, {  
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: canvasData // Send events as JSON in the body
      });

      // KS tillägg 2026-01-01 börjar här ... 
      // Kontrollera om statuskoden är OK, d.v.s. mellan 200-299
      if (postResponse.ok) {
          output.textContent = `Status-kod: ` + postResponse.status; // Visa statuskoden i output-området
          const successText = await postResponse.text();
          output.textContent = `Success: ${successText}`;
          output.textContent = `Response body: ` + successText; // Visa hela svaret 
          alert("Skickat! Överföringen lyckades."); // Alert vid framgång

          clearButton.click();  // KS addition: Clear all fields after posting data to Canvas 
      } else {
          // Om statuskoden är t.ex. 400 eller 500
          const errorText = await postResponse.text();
          output.textContent = `Failure: ${errorText}`;
          output.textContent = `Response body: ` + errorText; // Visa hela svaret 
          alert("FEL: " + errorText); // Alert-ruta med felmeddelandet från Java
      }        
      // .... KS tillägg 2026-01-01 slutar här 
      
  } catch (err) {
      console.error(err);
      alert("FEL: Kunde inte överföra data till Canvas: " + err.message);
  }
});


function renderTable() {
  // Bygger tabellrader från aktuell sorteringsordning
  const data = [...events]

  tableBody.innerHTML = data
    .map((event, index) => {    // KS 2026-01-03: Lägg till index för checkbox-data-attribute 
      const checkboxCell = event  // Innan 2025-01-02: event.editable (om checkboxen skulle visas => alltid false)
        ? `<input aria-label="Markera ${event.startAt}" type="checkbox" data-index="${index}" 
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

  // När tabellen uppdateras, uppdatera även räknaren för (tidigare) markerade/avmarkerade events 
  updateMarkedCount();   
}


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


async function addMarkedToEvents() {
  // Uppdatera events direkt genom att mappa om varje event och lägga till 'marked'-egenskapen
  events = events.map(event => ({
    ...event,
    marked: false  // Alla event börjar som omarkerade, d.v.s. 'marked' = false
  }));
}

/*
async function addMarkedToEvents() {
const modifiedEvents = JSON.parse(JSON.stringify(events));
const eventsMarked = modifiedEvents.map(event => ({
  ...event,
  marked: false  // Defaultvärde för 'marked' är false
}));
events = eventsMarked; // Update the objects of the global variable 'events' with the property 'marked'
}
*/

/*
function activateCheckboxes() {
  // Koppla lyssnaren EN gång till tabellkroppen
  tableBody.addEventListener("change", (e) => {
    // Säkerställ att det var en checkbox-typ som klickades
    if (e.target.matches('input[type="checkbox"]')) {
            
      // Låt ID vara en sträng för att matcha event.startAt exakt
      const boxId = e.target.dataset.startAt; 
      
      // Hitta eventet i 'events' baserat på 'startAt'
      const currentEvent = events.find((evt) => evt.startAt === boxId);
      
      if (currentEvent) {   // Kontrollera att eventet finns och hittades         
        currentEvent.marked = e.target.checked;   // Uppdatera variabeln 'marked' baserat på checkboxens aktuella status
        console.log("Event uppdaterat:", currentEvent);  // Testutskrift 
      }
    }
  });
}
*/


function activateCheckboxes() {
  console.log("DEBUG: activateCheckboxes har körts. tableBody är:", tableBody);
  
  tableBody.addEventListener("change", (e) => {
    console.log("En 'change'-händelse fångades i tabellen.");
    console.log("Element som klickades: ", e.target);
    
    if (e.target.matches('input[type="checkbox"]')) {
      const index = Number(e.target.dataset.index);
      
      console.log("Index hittades vid indexnr: ", index);
      console.log("Hela events-listan har längden: ", events.length);
      console.log("Objektet vid detta index är: ", events[index]);

      const currentEvent = events[index];
      
      if (currentEvent) {
        currentEvent.marked = e.target.checked;
        updateMarkedCount();  // Uppdatera räknaren separat för varje markerad/avmarkerad event
        console.log("Eventet är nu markerat!", currentEvent);
      } else {
        console.log("FEL: currentEvent är undefined! (Indexet finns inte i listan?)");
      }
    } else {
      console.log("FEL: Klicket var INTE på en checkbox, kanske på en <label> eller text?");
    }
  });
}

markAllBtn?.addEventListener("click", () => {  
  // Uppdatera alla events för att sätta 'marked' = true
  events = events.map((e) => ({ ...e, marked: true }));

  // Ritar om tabellen med den nya datan och därmed med alla checkboxar markerade
  renderTable();
});

markNoneBtn?.addEventListener("click", () => {  
  // Uppdatera alla events för att sätta 'marked' = false
  events = events.map((e) => ({ ...e, marked: false }));

  // Ritar om tabellen med den nya datan och därmed med alla checkboxar omarkerade
  renderTable();
});

function updateMarkedCount() {
  const markedEventCount = events.filter(e => e.marked).length;
  markedCount.textContent = markedEventCount;
}

// Anropa funktionen 1) utanför och innan "renderTable" och 2) bara en gång när scriptet laddas
activateCheckboxes();   
