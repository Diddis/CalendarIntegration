// Grunddata och DOM-referenser
const API_BASE = "";
const kursSelect = document.getElementById("kurskod");
const uppgiftSelect = document.getElementById("uppgift");
const modulSelect = document.getElementById("modul");
const dependentSelects = [uppgiftSelect, modulSelect];
const tableBody = document.querySelector(".data-table tbody");
const sortableHeaders = document.querySelectorAll(".data-table thead th[data-sort-key]");
const selectedCountEl = document.querySelector(".selected-count strong");
const markAllBtn = document.querySelector('[data-mark="all"]');
const markGradedBtn = document.querySelector('[data-mark="graded"]');
const markNoneBtn = document.querySelector('[data-mark="none"]');
const bulkDateInput = document.getElementById("bulk-date");
const bulkDateBtn = document.querySelector(".date-controls button");
const transferBtn = document.getElementById("transfer-btn");
const errorEl = document.querySelector(".error-message");
const transferModeSelect = document.getElementById("transfer-mode");
const modalBackdrop = document.getElementById("transfer-modal");
const modalCount = document.querySelector(".modal-count strong");
const modalCloseBtn = document.querySelector(".modal-close");
const modalCancelBtn = document.querySelector(".modal-cancel");
const modalConfirmBtn = document.querySelector(".modal-confirm");
const modalExaminerChecks = document.querySelectorAll(".modal-examiner");
// State för sortering, studenter och valda filter
let sortState = { key: "name", dir: "asc" };
let students = [];
let courseTabell = [];
let selectedCourse = "-";
let selectedAssignment = "-";
let selectedModule = "-";
let errorTimer = null;

async function fetchJSON(path) {
  console.log("fetch", path);
  const res = await fetch(`${API_BASE}${path}`);
  if (!res.ok) {
    throw new Error(`Kunde inte läsa ${path}: ${res.status}`);
  }
  return res.json();
}

function setSelectOptions(selectEl, options) {
  if (!selectEl) return;
  const unique = Array.from(new Set(options || []));
  selectEl.innerHTML =
    `<option value="-">-</option>` + unique.map((opt) => `<option value="${opt}">${opt}</option>`).join("");
  selectEl.value = "-";
}

function toggleDependentSelects() {
  // Stänger av/aktiverar beroende dropdowns utifrån vald kurskod
  const disabled = kursSelect.value === "-";
  dependentSelects.forEach((el) => {
    el.disabled = disabled;
    if (disabled) {
      el.value = "-";
    }
  });
}

function compareValues(a, b, key) {
  // Grundläggande jämförelse för sortering på valfritt fält
  const valA = (a[key] ?? "").toString().toLowerCase();
  const valB = (b[key] ?? "").toString().toLowerCase();
  if (valA < valB) return -1;
  if (valA > valB) return 1;
  return 0;
}

function renderTable() {
  // Bygger tabellrader från aktuell sorteringsordning
  const data = [...students].sort((a, b) => {
    const result = compareValues(a, b, sortState.key);
    return sortState.dir === "asc" ? result : -result;
  });

  tableBody.innerHTML = data
    .map((student) => {
      const checkboxCell = student.editable
        ? `<input aria-label="Markera ${student.name}" type="checkbox" data-id="${student.id}" ${
            student.marked ? "checked" : ""
          } />`
        : "";

      const gradeCell = student.editable
        ? `<select class="table-select" aria-label="Betyg för ${student.name}" data-id="${student.id}">
            <option ${student.ladokGrade === "-" ? "selected" : ""}>-</option>
            <option ${student.ladokGrade === "U" ? "selected" : ""}>U</option>
            <option ${student.ladokGrade === "G" ? "selected" : ""}>G</option>
            <option ${student.ladokGrade === "VG" ? "selected" : ""}>VG</option>
          </select>`
        : `<span>${student.ladokGrade}</span>`;

      const examValue = student.examDate && student.examDate !== "-" ? student.examDate : "";
      const dateCell = student.editable
        ? `<input class="table-input date-field" type="date" data-id="${student.id}" value="${examValue}" />`
        : `<span class="${student.examDate === "-" ? "muted" : ""}">${student.examDate}</span>`;

      return `<tr>
        <td class="col-check">${checkboxCell}</td>
        <td class="col-name">
          <span class="name-link">${student.name}</span>
          <span class="personnummer">${student.personnummer ?? ""}</span>
        </td>
        <td class="canvas-grade">${student.canvasGrade}</td>
        <td>${gradeCell}</td>
        <td>${dateCell}</td>
        <td class="status ${student.status ? "status-muted" : ""}">${student.status}</td>
      </tr>`;
    })
    .join("");

  updateSelectedCount();
  attachCheckboxHandlers();
  attachDateHandlers();
  attachGradeHandlers();
  updateSortIndicators();
}

function updateSelectedCount() {
  // Uppdaterar räknare för markerade rader och tar bort fel vid behov
  const selected = students.filter((s) => s.marked).length;
  selectedCountEl.textContent = selected;
  if (selected > 0) {
    hideError();
  }
}

function attachCheckboxHandlers() {
  // Binder change-händelser till checkboxar för att lagra markeringar
  tableBody.querySelectorAll('input[type="checkbox"]').forEach((box) => {
    box.addEventListener("change", () => {
      const id = Number(box.dataset.id);
      const student = students.find((s) => s.id === id);
      if (student) student.marked = box.checked;
      updateSelectedCount();
    });
  });
}

function attachGradeHandlers() {
  // Uppdaterar ladokGrade i state när dropdown ändras
  tableBody.querySelectorAll(".table-select").forEach((select) => {
    select.addEventListener("change", () => {
      const id = Number(select.dataset.id);
      const student = students.find((s) => s.id === id);
      if (student) {
        student.ladokGrade = select.value;
      }
      select.classList.remove("input-error");
    });
  });
}

function attachDateHandlers() {
  // Binder change-händelser till datumfält för att lagra och rensa fel
  tableBody.querySelectorAll('.table-input[type="date"]').forEach((input) => {
    input.addEventListener("change", () => {
      const id = Number(input.dataset.id);
      const student = students.find((s) => s.id === id);
      if (student) {
        student.examDate = input.value;
        input.classList.remove("input-error");
      }
    });
  });
}

function updateSortIndicators() {
  // Hanterar visuella sorteringspilar och aria-sort attribut
  sortableHeaders.forEach((th) => {
    const key = th.dataset.sortKey;
    const indicator = th.querySelector(".sort");
    th.removeAttribute("aria-sort");
    th.classList.remove("is-sorted");
    indicator.classList.remove("sort-asc", "sort-desc");

    if (key === sortState.key) {
      th.classList.add("is-sorted");
      th.setAttribute("aria-sort", sortState.dir === "asc" ? "ascending" : "descending");
      indicator.classList.add(sortState.dir === "asc" ? "sort-asc" : "sort-desc");
    }
  });
}

function openModal(selected) {
  // Öppnar modal för överföring med antal markerade
  if (!modalBackdrop) return;
  modalCount.textContent = selected;
  modalExaminerChecks.forEach((c) => {
    c.checked = false;
  });
  updateModalConfirmState();
  modalBackdrop.hidden = false;
  document.body.classList.add("modal-open");
}

function closeModal() {
  // Stänger modal och återställer ev. state
  if (!modalBackdrop) return;
  modalBackdrop.hidden = true;
  document.body.classList.remove("modal-open");
}

function updateModalConfirmState() {
  // Aktiverar överför-knappen i modal när en examinator är vald
  const anyChecked = [...modalExaminerChecks].some((c) => c.checked);
  if (modalConfirmBtn) {
    modalConfirmBtn.disabled = !anyChecked;
  }
}

function showError(message) {
  // Visar felmeddelande och auto-gömmer det efter 3 sekunder
  if (!errorEl) return;
  if (errorTimer) {
    clearTimeout(errorTimer);
    errorTimer = null;
  }
  errorEl.textContent = message;
  errorEl.classList.add("is-visible");
  errorTimer = setTimeout(() => {
    hideError();
  }, 3000);
}

function hideError() {
  // Döljer felmeddelande och rensar timer
  if (!errorEl) return;
  errorEl.textContent = "";
  errorEl.classList.remove("is-visible");
  if (errorTimer) {
    clearTimeout(errorTimer);
    errorTimer = null;
  }
}

function flagMissingDates(ids) {
  // Markerar fält som saknar datum för markerade studenter
  tableBody.querySelectorAll('.table-input[type="date"]').forEach((input) => {
    const id = Number(input.dataset.id);
    if (ids.includes(id)) {
      input.classList.add("input-error");
    } else {
      input.classList.remove("input-error");
    }
  });
}

function flagMissingGrades(ids) {
  // Markerar betygs-dropdowns som saknar värde för markerade studenter
  tableBody.querySelectorAll(".table-select").forEach((select) => {
    const id = Number(select.dataset.id);
    if (ids.includes(id)) {
      select.classList.add("input-error");
    } else {
      select.classList.remove("input-error");
    }
  });
}

function rebuildStudentsState() {
  // Bygger students-array från tabellsvaret från backend
  const previous = new Map(students.map((s) => [s.name, s]));
  students = courseTabell.map((row, index) => {
    const prev = previous.get(row.namn) || {};
    return {
      id: index + 1,
      username: row.användarnamn,
      name: row.namn || "Okänd student",
      canvasGrade: row.canvasOmdöme || "-",
      ladokGrade: prev.ladokGrade ?? "-",
      examDate: prev.examDate ?? "",
      status: prev.status ?? "",
      marked: prev.marked ?? false,
      editable: true
    };
  });
  renderTable();
}

async function loadCourses() {
  try {
    const kurskoder = await fetchJSON("/registrering/kurskod");
    setSelectOptions(kursSelect, kurskoder);
  } catch (e) {
    console.error("Fel vid hämtning av kurskoder", e);
    showError("Kunde inte läsa kurskoder från backend.");
  } finally {
    toggleDependentSelects();
  }
}

async function loadCourseData(kurskod) {
  console.log("loadCourseData", kurskod);
  selectedCourse = kurskod;
  selectedAssignment = "-";
  selectedModule = "-";
  setSelectOptions(uppgiftSelect, []);
  setSelectOptions(modulSelect, []);
  courseTabell = [];
  students = [];
  renderTable();

  if (kurskod === "-") {
    toggleDependentSelects();
    return;
  }

  try {
    const [uppgifter, moduler, tabellData] = await Promise.all([
      fetchJSON(`/registrering/uppgifter?kurskod=${encodeURIComponent(kurskod)}`),
      fetchJSON(`/registrering/modulkoder?kurskod=${encodeURIComponent(kurskod)}`)
      //fetchJSON(`/registrering/tabell?kurskod=${encodeURIComponent(kurskod)}`)
    ]);

    console.log("Fetched uppgifter:", uppgifter);
    console.log("Fetched moduler:", moduler);
    //console.log("Fetched tabell:", tabellData);

    courseTabell = [];    //Array.isArray(tabellData) ? tabellData : [];
    students = [];
    renderTable();

    setSelectOptions(uppgiftSelect, uppgifter);
    setSelectOptions(modulSelect, moduler);
    dependentSelects.forEach((el) => (el.disabled = false));
    rebuildStudentsState();
  } catch (e) {
    console.error("Fel vid hämtning av kursdata", e);
    showError("Kunde inte hämta data för vald kurs från backend.");
  } finally {
    toggleDependentSelects();
  }
}

// Therese tillägg (ladda tabell efter uppgift vald)

async function handleAssignmentChange() {
  selectedAssignment = uppgiftSelect?.value ?? "-";

  if (selectedAssignment === "-" || selectedCourse === "-") {
    courseTabell = [];
    students = [];
    renderTable();
    return;
  }

  try {
    const tabellData = await fetchJSON(
      `/registrering/tabell?kurskod=${encodeURIComponent(selectedCourse)}&uppgift=${encodeURIComponent(selectedAssignment)}`
    );

    courseTabell = Array.isArray(tabellData) ? tabellData : [];
    rebuildStudentsState();

  } catch (e) {
    console.error("Fel vid hämtning av tabell för uppgift", e);
    showError("Kunde inte läsa tabell för vald uppgift.");
  }
}




/*function handleAssignmentChange() {
  selectedAssignment = uppgiftSelect?.value ?? "-";
  rebuildStudentsState();
}*/

function handleModuleChange() {
  selectedModule = modulSelect?.value ?? "-";
  rebuildStudentsState();
}

sortableHeaders.forEach((th) => {
  th.addEventListener("click", () => {
    const key = th.dataset.sortKey;
    if (sortState.key === key) {
      sortState.dir = sortState.dir === "asc" ? "desc" : "asc";
    } else {
      sortState = { key, dir: "asc" };
    }
    renderTable();
  });
});

markAllBtn?.addEventListener("click", () => {
  students = students.map((s) => (s.editable ? { ...s, marked: true } : s));
  renderTable();
});

markGradedBtn?.addEventListener("click", () => {
  students = students.map((s) => {
    if (!s.editable) return s;
    const hasGrade = (s.ladokGrade ?? "-") !== "-";
    return { ...s, marked: hasGrade };
  });
  renderTable();
});

markNoneBtn?.addEventListener("click", () => {
  students = students.map((s) => (s.editable ? { ...s, marked: false } : s));
  renderTable();
});

bulkDateBtn?.addEventListener("click", () => {
  if (!bulkDateInput || !bulkDateInput.value) {
    bulkDateInput?.classList.add("input-error");
    showError("Välj ett datum att sätta för markerade studenter.");
    return;
  }
  const selected = students.filter((s) => s.marked && s.editable);
  if (selected.length === 0) {
    showError("Markera minst en student för att sätta datum.");
    return;
  }
  const value = bulkDateInput.value;
  bulkDateInput.classList.remove("input-error");
  students = students.map((s) => (s.marked ? { ...s, examDate: value } : s));
  // Uppdatera existerande inputs direkt för snabb feedback
  tableBody.querySelectorAll('.table-input[type="date"]').forEach((input) => {
    const id = Number(input.dataset.id);
    const student = students.find((s) => s.id === id);
    if (student?.marked) {
      input.value = value;
      input.classList.remove("input-error");
    }
  });
  hideError();
  renderTable();
});

bulkDateInput?.addEventListener("change", () => {
  if (bulkDateInput.value) {
    bulkDateInput.classList.remove("input-error");
  }
});

modalExaminerChecks.forEach((checkbox) => {
  checkbox.addEventListener("change", updateModalConfirmState);
});

modalCloseBtn?.addEventListener("click", closeModal);
modalCancelBtn?.addEventListener("click", closeModal);
modalBackdrop?.addEventListener("click", (e) => {
  if (e.target === modalBackdrop) {
    closeModal();
  }
});

transferBtn?.addEventListener("click", () => {
  if (!modulSelect || modulSelect.value === "-" || modulSelect.disabled) {
    showError("Välj en modul i Ladok innan du överför.");
    return;
  }

  const markedIds = students.filter((s) => s.marked).map((s) => s.id);
  const bulkDateValue = bulkDateInput?.value;
  if (!bulkDateValue) {
    flagMissingDates(markedIds);
    bulkDateInput?.classList.add("input-error");
    showError("Välj ett datum för markerade resultat innan du överför.");
    return;
  }
  bulkDateInput?.classList.remove("input-error");

  const selected = students.filter((s) => s.marked).length;
  if (selected === 0) {
    showError("Markera minst ett betyg för att överföra till Ladok.");
    return;
  }
  hideError();

  let updated = false;
  students = students.map((s) => {
    if (s.marked && (!s.examDate || s.examDate === "-")) {
      updated = true;
      return { ...s, examDate: bulkDateValue };
    }
    return s;
  });
  if (updated) {
    renderTable();
  }

  const missingGrades = students
    .filter((s) => s.marked)
    .filter((s) => !s.ladokGrade || s.ladokGrade === "-")
    .map((s) => s.id);
  flagMissingGrades(missingGrades);
  if (missingGrades.length > 0) {
    showError("Sätt betyg i Ladok för markerade studenter.");
    return;
  }
  const missingDates = students
    .filter((s) => s.marked)
    .filter((s) => !s.examDate || s.examDate === "-")
    .map((s) => s.id);
  flagMissingDates(missingDates);
  if (missingDates.length > 0) {
    showError("Ange examinationsdatum för markerade studenter.");
    return;
  }
  openModal(selected);
});

modalConfirmBtn?.addEventListener("click", async () => {
  if (modalConfirmBtn.disabled) return;
  closeModal();
  // Här skulle överföring triggas mot backend med notifiering till vald examinator.

// Therese tillägg för överföring

  // Förbered underlag data
  const selectedStudents = students.filter(s => s.marked);
  //const ladokBetyg = selectedStudents[0].ladokGrade; 
  //const datum = selectedStudents[0].examDate;       

  const resultatLista = selectedStudents.map(s => ({
    användarnamn: s.username,
    kurskod: selectedCourse,
    modulkod: selectedModule,
    datum: s.examDate,
    betyg: s.ladokGrade,
    status: s.status
  }));

  try {
    const response = await fetch(`${API_BASE}/registrering/skicka`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(resultatLista)
    });

    if (!response.ok) throw new Error("Fel vid skickande av betyg");

    const status = await response.text(); // backend returnerar "klarmarkerad" för alla skickade studenter

    // Uppdatera tabell med status
    students = students.map(s => s.marked ? { ...s, status } : s);

    renderTable();

    alert("Betyg skickade till Ladok!");

  } catch (e) {
    console.error("Fel vid skickande av betyg", e);
    showError("Kunde inte skicka betyg till Ladok.");
  }

});

kursSelect.addEventListener("change", (e) => {
  console.log("kurskod change", e.target.value);
  loadCourseData(e.target.value);
});
uppgiftSelect?.addEventListener("change", handleAssignmentChange);
modulSelect?.addEventListener("change", handleModuleChange);

toggleDependentSelects();
renderTable();
loadCourses();
