// DOM elements
const button1 = document.getElementById("fetch-cal-btn");   
const button2 = document.getElementById("transfer-cal-btn");
const clearButton = document.getElementById("empty-cal-btn");
const input = document.getElementById("timeeditUrl");
const output = document.getElementById("json-output");   // for testing/dev. purposes

const API_BASE = "";    // Kunde ändras från "" till "/kalender", men behövs det?
const idInput = document.getElementById("cal-id"); 
const tableBody = document.querySelector(".data-table tbody");
const markedCount = document.getElementById("count");  
const markAllBtn = document.getElementById("mark-all-btn");  
const markNoneBtn = document.getElementById("mark-none-btn"); 

let events= [];  
let calTable = [];  


// Event listener for the button
clearButton.addEventListener("click", () => {
  document.getElementById("timeeditUrl").value = "";
  document.getElementById("course-code-value").innerText = ``; 
  document.getElementById("course-name-value").innerText = ``;
  document.getElementById("cal-id").value = "";  // KS tillägg 2026-01-01

  output.textContent = "";    // Clear output area from previous JSON data displayed (for testing purposes)
  tableBody.innerHTML = "";   // Clear table body
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
        const calEvents = await fetchJSON(`/kalender/timeEditURL?url=${encodeURIComponent(url)}`);
        
        // Debugging line for fetching events
        console.log("Following events were fetched:", calEvents);       
        
        // Assign fetched events to global variable 'events' for table rendering
        events = calEvents;  

        // Call function to add boolean property 'marked' to events, also update global 'events' variable 
        addMarkedToEvents();

        // Display the JSON nicely formatted for testing
        output.textContent = JSON.stringify(events, null, 2);  
        
        // Render the table with the fetched events
        renderTable(); 

         
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
  const currentId = idInput.value.trim();  // Hämta det aktuella värdet vid knapptryckning ("Överför kalender")
  if (!currentId) {
      alert("Fyll i ett ID i fältet för att kunna överföra händelser.");
      return;
  }

  // Create new filtered list and include only marked events
  const eventsToTransfer = events.filter(e => e.marked === true);  

  if (eventsToTransfer.length === 0) {
      alert("FEL: Inga händelser att överföra.");
      return;
  }
  
  if (eventsToTransfer.length !== Number(markedCount.textContent)) {
      alert("FEL: Antalet markerade händelser motsvarar inte antalet som ska överföras. Överföringen avbruten.");
      return;
  }
  
  try {
      console.log("Överför events till Canvas. Antal: ", eventsToTransfer.length);
      const canvasData = JSON.stringify(eventsToTransfer);

      // Send the events to the backend Canvas endpoint via POST 
      const postResponse = await fetch(`/kalender/postToCanvas?idInputCode=${encodeURIComponent(currentId)}`, {  
          method: 'POST',
          headers: {
              'Content-Type': 'application/json'
          },
          body: canvasData // Send events as JSON in the body
      });

      // Kontrollera om statuskoden är OK, d.v.s. mellan 200-299
      if (postResponse.ok) {
          output.textContent = `Status-kod: ` + postResponse.status; // Visa statuskoden i output-området
          const successText = await postResponse.text();
          output.textContent = `Success: ${successText}`;
          output.textContent = `Response body: ` + successText; // Visa hela svaret 
          alert("Skickat! Överföringen lyckades."); // Alert vid framgång

          clearButton.click();  // Clear all fields after posting data to Canvas 
      } else {          
          const errorText = await postResponse.text();
          output.textContent = `Failure: ${errorText}`;
          output.textContent = `Response body: ` + errorText; // Visa hela svaret 
          alert("FEL: " + errorText); // Alert-ruta med felmeddelandet från Java
      }        
            
  } catch (err) {
      console.error(err);
      alert("FEL: Kunde inte överföra data till Canvas: " + err.message);
  }
});


function renderTable() {
  // Bygger tabellrader från aktuell sorteringsordning
  const data = [...events]

  tableBody.innerHTML = data
    .map((event, index) => {    // Lägg till index för checkbox-data-attribute 
      const checkboxCell = event  
        ? `<input aria-label="Markera ${event.startAt}" type="checkbox" data-index="${index}" 
        ${event.marked ? "checked" : ""} />`
        : "";

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

      /* Använd elementet "textarea" för person, zoom och kommentar för bättre radbrytning i placeholders */      
      const personCell = `<textarea class="edit-person" data-index="${index}" 
        placeholder="[Personer]">${event.person || ''}</textarea>`;
     
      const zoomCell = `<textarea class="edit-zoom" data-index="${index}" 
        placeholder="[Zoom-länk]">${event.zoomLink || ''}</textarea>`;

      const commentCell = `<textarea class="edit-comment" data-index="${index}" 
        placeholder="[Kommentar]">${event.comments || ''}</textarea>`;

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

function activateTableEditing() {
  // Här används typen "input" för att fånga ändringar då användare skriver i fälten
  tableBody.addEventListener("input", (e) => {
    const ind = e.target.dataset.index;  // Hämta index från attributet "data-index" (= eventens index i events-listan)
    if (!ind) return;

    const thisEvent = events[Number(ind)];  // Hämta själva event-objektet baserat på dess index
    if (!thisEvent) return;

    if (e.target.classList.contains("edit-person")) {
      // Uppdatera person-variabeln i event-objektet med det nya/ändrade värdet i input-fältet
      thisEvent.person = e.target.value; 
    } else if (e.target.classList.contains("edit-comment")) {
      // Uppdatera comments-variabeln i event-objektet med det nya/ändrade värdet i input-fältet
      thisEvent.comments = e.target.value;
    } else if (e.target.classList.contains("edit-zoom")) {
      // Uppdatera zoomLink-variabeln i event-objektet med det nya/ändrade värdet i input-fältet
      thisEvent.zoomLink = e.target.value;
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

// Anropa "activateCheckboxes" 1) utanför och innan "renderTable" och 2) bara en gång när scriptet laddas
activateCheckboxes();   
activateTableEditing()
