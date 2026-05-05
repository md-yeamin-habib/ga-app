// ==========================
// STATE
// ==========================
const state = {
  pendingFilterPath: [],
  pendingLeaf: null,

  appliedFilterPath: [],
  appliedLeaf: null,

  customFormula: "",

  tables: {
    "0": [] //input table
  }
};

let defaultConfig = {};
let savedConfig = {};

let selectedRows = new Set();

// ==========================
// ELEMENTS
// ==========================
const filterBtn = document.getElementById("filter-btn");
const filterPopup = document.getElementById("filter-popup");
const filterDisplay = document.getElementById("filter-display");

const modal = document.getElementById("param-modal");
const paramBtn = document.getElementById("param-btn");

const customPopup = document.getElementById("custom-popup");
const customInput = document.getElementById("custom-input");
const customApply = document.getElementById("custom-apply");
const customCancel = document.getElementById("custom-cancel");

const genSelect = document.getElementById("generation-select");
const nextBtn = document.getElementById("to-next-gen");
const lastBtn = document.getElementById("to-last-gen");

const tabsContainer = document.getElementById("tabs");
const tableHead = document.querySelector("#main-table thead");
const tableBody = document.querySelector("#main-table tbody");

const applyBtn = document.getElementById("apply-btn");

const importBtn = document.getElementById("import-btn");
const exportBtn = document.getElementById("export-btn");
const clearBtn = document.getElementById("clear-btn");

const runBtn = document.getElementById("run-btn");

// ==========================
// GLOBAL CLICK HANDLER
// ==========================
window.onclick = (e) => {
  if (e.target === filterPopup) filterPopup.style.display = "none";
  if (e.target === customPopup) customPopup.style.display = "none";
  if (e.target === modal) {
    applyConfig(savedConfig);
    modal.style.display = "none";
  }
};


document.addEventListener("DOMContentLoaded", () => {

  const generateBtn = document.getElementById("generate-btn");
  const generatePopup = document.getElementById("generate-popup");

  const genRows = document.getElementById("gen-rows");
  const genSeed = document.getElementById("gen-seed");

  const genOk = document.getElementById("gen-ok");
  const genCancel = document.getElementById("gen-cancel");

  const seedBtn = document.getElementById("seed-randomize");

  seedBtn.onclick = () => {
    // generate a long random number
    const randomSeed = Math.floor(Math.random() * 1e12);

    genSeed.value = randomSeed;

    // trigger spin animation
    seedBtn.classList.add("spin");

    setTimeout(() => {
      seedBtn.classList.remove("spin");
    }, 400);
  };

  function openGeneratePopup() {
    generatePopup.classList.add("show");
  }

  function closeGeneratePopup() {
    generatePopup.classList.remove("show");
  }

  generateBtn.onclick = () => {
    openGeneratePopup();
  };

  genCancel.onclick = () => {
    closeGeneratePopup();
  };

  generatePopup.onclick = (e) => {
    if (e.target === generatePopup) {
      closeGeneratePopup();
    }
  };

  genOk.onclick = async () => {

    closeGeneratePopup();

    const type = getProblemType().toUpperCase();

    if (type === "NONE") {
      alert("Please select a problem type before generating.");
      return;
    }

    const req = buildGARequest();

    try {
      const res = await fetch("/api/ga/generate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(req)
      });

      const data = await res.json();

      state.tables["0"] = data.problem || data.population || [];

      activeTab = "0";
      renderTable();
      updateRunButtonState();
      updateSummary();

    } catch (err) {
      console.error(err);
      alert("Generate failed.");
    }
  };

});

// ==========================
// FILTER POPUP
// ==========================
filterBtn.onclick = () => {
  filterPopup.style.display = "block";
};

// ==========================
// TREE
// ==========================
document.querySelectorAll(".node").forEach(node => {
  node.addEventListener("click", function (e) {
    e.stopPropagation();
    this.classList.toggle("open");
  });
});

document.querySelectorAll(".leaf").forEach(leaf => {
  leaf.onclick = function (e) {
    e.stopPropagation();

    document.querySelectorAll(".leaf").forEach(l => l.classList.remove("selected"));
    this.classList.add("selected");

    let path = [];
    let current = this;

    while (current.closest("li")) {
      current = current.parentElement.closest("li");
      if (!current) break;

      let txt = current.firstChild.textContent.trim();
      if (txt !== "Filters") path.unshift(txt);
    }

    const selected = this.textContent.trim();
    path.push(selected);

    state.pendingFilterPath = path;
    state.pendingLeaf = selected;

    filterDisplay.value = path.join(" - ");
  };
});

// ==========================
// APPLY FILTER
// ==========================
applyBtn.onclick = () => {

  if (!state.pendingFilterPath.length) return;

  state.appliedFilterPath = [...state.pendingFilterPath];
  state.appliedLeaf = state.pendingLeaf;

  updateProblemTypeFromFilter();

  filterPopup.style.display = "none";

  activeTab = "0";
  renderTable();
  updateRunButtonState();
};

// ==========================
// CUSTOM FORMULA
// ==========================
filterDisplay.onclick = () => {
  if (state.pendingLeaf === "Custom") {
    customPopup.style.display = "block";
    customInput.value = state.customFormula || "";
  }
};

customApply.onclick = () => {
  const formula = customInput.value.trim();
  if (!formula) return;

  state.customFormula = formula;

  const basePath = state.appliedFilterPath.slice(0, -1);
  filterDisplay.value =
    `${basePath.join(" - ")} - Custom (Fitness Function = ${formula})`;

  customPopup.style.display = "none";
};

customCancel.onclick = () => {
  customPopup.style.display = "none";
};

// ==========================
// PARAM MODAL
// ==========================
paramBtn.onclick = () => {
  if (!state.appliedLeaf) return;

  updateBasicTab();
  updateNumberTypes();
  updateGeneInputs();
  setupAdvancedToggles();
  setupOperatorLogic();
  modal.style.display = "block";
};

// ==========================
// MODAL TABS
// ==========================
const modalTabs = document.querySelectorAll("#param-modal .tab");
const modalPanes = document.querySelectorAll("#param-modal .tab-pane");

const tabMap = {
  0: "basic-tab",
  1: "operation-tab",
  2: "advanced-tab"
};

modalTabs.forEach((tab, index) => {
  tab.addEventListener("click", () => {

    modalTabs.forEach(t => t.classList.remove("active"));
    modalPanes.forEach(p => p.classList.remove("active"));

    tab.classList.add("active");

    const pane = document.getElementById(tabMap[index]);
    if (pane) pane.classList.add("active");
  });
});

// ==========================
// FILTER HELPERS
// ==========================
function getProblemType() {
  const path = state.appliedFilterPath.join(" ");

  if (path.includes("Knapsack")) return "KNAPSACK";
  if (path.includes("TSP")) return "TSP";
  if (path.includes("Generic")) return "GENERIC";
  return "NONE";
}

function getGeneType() {
  const path = state.appliedFilterPath.join(" ");

  if (path.includes("Real")) return "REAL";
  if (path.includes("Integer") || path.includes("TSP")) return "INTEGER";
  if (path.includes("Binary") || path.includes("Knapsack")) return "BINARY";
  return "NONE";
}

function getFitnessType() {
  const path = state.appliedFilterPath.join(" ");

  if (path.includes("Sum")) return "SUM";
  if (path.includes("Custom")) return "CUSTOM";
  if (path.includes("Ones")) return "ONES";
  if (path.includes("BinaryEvaluation")) return "BINARY";
  if (path.includes("Knapsack")) return "KNAPSACK";
  if (path.includes("TSP")) return "TSP";
  return "NONE";
}

// ==========================
// BASIC TAB LOGIC
// ==========================
function updateBasicTab() {
  const type = getProblemType();

  const lengthLabel = document.getElementById("length-label");
  const geneGroup = document.getElementById("gene-range-group");
  const coordGroup = document.getElementById("coord-group");
  const knapsackGroup = document.getElementById("knapsack-group");

  geneGroup.classList.add("hidden");
  coordGroup.classList.add("hidden");
  knapsackGroup.classList.add("hidden");

  if (type === "GENERIC") {
    lengthLabel.textContent = "Chromosome Length";
    geneGroup.classList.remove("hidden");
  } else if (type === "TSP") {
    lengthLabel.textContent = "Number of Cities";
    coordGroup.classList.remove("hidden");
  } else if (type === "KNAPSACK") {
    lengthLabel.textContent = "Number of Items";
    knapsackGroup.classList.remove("hidden");
  }
}

// ==========================
// INPUT VALIDATION
// ==========================
document.getElementById("pop-size").oninput = e => {
  if (e.target.value < 2) e.target.value = 2;
};

document.getElementById("length-input").oninput = e => {
  if (e.target.value < 1) e.target.value = 1;
};

// ==========================
// NUMBER TYPE LOGIC
// ==========================
function updateNumberTypes() {
  const path = state.appliedFilterPath.join(" ");
  const isInt = path.includes("Integer");

  ["min-gene", "max-gene"].forEach(id => {
    const el = document.getElementById(id);
    el.step = isInt ? "1" : "any";
  });
}

// ==========================
// GENE INPUT LOGIC
// ==========================
function updateGeneInputs() {
  const minGene = document.getElementById("min-gene");
  const maxGene = document.getElementById("max-gene");

  const path = state.appliedFilterPath;

  if (path.includes("Binary")) {
    minGene.value = 0;
    maxGene.value = 1;
    minGene.disabled = true;
    maxGene.disabled = true;
  } else {
    minGene.value = 0;
    maxGene.value = 9;
    minGene.disabled = false;
    maxGene.disabled = false;
  }
}

// ==========================
// ADVANCED TOGGLES
// ==========================
function setupAdvancedToggles() {

  const targetToggle = document.getElementById("target-toggle");
  const targetValue = document.getElementById("target-value");

  const convToggle = document.getElementById("conv-toggle");
  const convPatience = document.getElementById("conv-patience");
  const convTolerance = document.getElementById("conv-tolerance");

  const divToggle = document.getElementById("div-toggle");
  const divThreshold = document.getElementById("div-threshold");
  const divPatience = document.getElementById("div-patience");

  if (!targetToggle || !convToggle || !divToggle) return;

  targetToggle.onchange = () => {
    targetValue.disabled = targetToggle.value !== "true";
  };

  convToggle.onchange = () => {
    const enabled = convToggle.value === "true";
    convPatience.disabled = !enabled;
    convTolerance.disabled = !enabled;
  };

  divToggle.onchange = () => {
    const enabled = divToggle.value === "true";
    divThreshold.disabled = !enabled;
    divPatience.disabled = !enabled;
  };

  targetToggle.onchange();
  convToggle.onchange();
  divToggle.onchange();
}

// ==========================
// OPERATOR LOGIC
// ==========================
function setupOperatorLogic() {

  const selection = document.getElementById("select-mode");
  const tournamentSize = document.getElementById("tournament-size");

  const crossover = document.getElementById("crossover-mode");
  const mutation = document.getElementById("mutation-mode");

  const replacement = document.getElementById("replacement-mode");
  const eliteCount = document.getElementById("elite-count");

  if (!selection || !crossover || !mutation || !replacement) return;

  function updateCrossover() {
    const problem = getProblemType();
    const onePoint = [...crossover.options].find(o => o.value === "One_Point");
    const twoPoint = [...crossover.options].find(o => o.value === "Two_Point");
    const order = [...crossover.options].find(o => o.value === "Order");

    [...crossover.options].forEach(o => o.disabled = false);

    if (problem === "TSP") {
      if (onePoint) onePoint.disabled = true;
      if (twoPoint) twoPoint.disabled = true;
      if (crossover.value === "One_Point" || crossover.value === "Two_Point") {
        crossover.value = "Order";
      }
    } 
    
    else {
      if (order) order.disabled = true;
      if (crossover.value === "Order") {
        crossover.value = "One_Point";
      }
    }

  }

  function updateMutation() {
    const problem = getProblemType();
    const gene = getGeneType();

    [...mutation.options].forEach(o => o.disabled = false);

    for (const opt of mutation.options) {
      const v = opt.value;

      if (problem === "TSP") {
        if (v === "Bit_Flip" || v === "Random_Reset") opt.disabled = true;
      }

      if (problem === "KNAPSACK") {
        if (v === "Random_Reset") opt.disabled = true;
      }

      if (problem === "GENERIC" && gene === "BINARY") {
        if (v === "Random_Reset") opt.disabled = true;
      }

      if (problem === "GENERIC" && (gene === "INTEGER" || gene === "REAL")) {
        if (v === "Bit_Flip") opt.disabled = true;
      }
    }

    if (mutation.selectedOptions[0]?.disabled) {
      mutation.value = "Swap";
    }
  }

  selection.onchange = () => {
    tournamentSize.disabled = selection.value !== "Tournament";
  };

  replacement.onchange = () => {
    eliteCount.disabled = replacement.value === "Full";
  };

  crossover.onchange = updateCrossover;
  mutation.onchange = updateMutation;

  selection.onchange();
  replacement.onchange();
  updateCrossover();
  updateMutation();
}

// ==========================
// CONFIG
// ==========================
function collectConfig() {
  const inputs = document.querySelectorAll("#param-modal input, #param-modal select");
  let data = {};
  inputs.forEach(el => data[el.id] = el.value);
  return data;
}

function applyConfig(configData) {
  Object.keys(configData).forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = configData[id];
  });

  updateBasicTab();
  updateNumberTypes();
  updateGeneInputs();
  setupAdvancedToggles();
  setupOperatorLogic();
}

// ==========================
// TABLE SYSTEM
// ==========================
const config = {
  get maxGenerations() {
    return parseInt(document.getElementById("max-gen")?.value || 1);
  },

  get selectionMode() {
    return document.getElementById("select-mode")?.value?.toLowerCase() || "none";
  },

  problemType: "none"
};

let activeTab = "0";
let currentGen = 1;
let totalGenerations = 1;
const dropdown = document.getElementById("generation-dropdown");

function setGeneration(val) {

  const max = totalGenerations;

  currentGen = Math.max(1, Math.min(val, max));

  genSelect.value = currentGen;

  syncGenUI();
  updateGenButtons();
  renderTable();
  updateSummary();
  updatePlots();
}

function syncGenUI() {

  dropdown.style.display = "none";

  buildTabs(currentGen);
  updateProblemTypeFromFilter?.();
  updateSummary?.();
  updatePlots?.();
  renderTable?.();
}

function rebuildGenerationDropdown(totalGens = 1) {

  totalGenerations = totalGens;
  currentGen = 1;

  genSelect.value = 1;
  dropdown.style.display = "none";
}

function renderDropdown(pos) {

  const start = Math.max(pos - 5, 1);

  const end = Math.min(pos + 4, totalGenerations);

  dropdown.innerHTML = "";
  dropdown.style.display = "block";

  const frag = document.createDocumentFragment();

  for (let i = start; i <= end; i++) {

    const item = document.createElement("div");
    item.className = "dropdown-item";
    item.textContent = `Generation ${i}`;

    if (i === currentGen) item.classList.add("active");

    item.onclick = () => setGeneration(i);

    frag.appendChild(item);
  }

  dropdown.appendChild(frag);
}

genSelect.addEventListener("input", () => {

  let val = parseInt(genSelect.value);

  if (isNaN(val)) return;

  setGeneration(val);
  renderTable();
  updateSummary();
  updatePlots();
  updateGenButtons();
  renderDropdown(val);
});

genSelect.addEventListener("focus", () => {
  renderDropdown(currentGen);
});

document.addEventListener("click", (e) => {
  if (!e.target.closest(".gen-control")) {
    dropdown.style.display = "none";
  }
});

nextBtn.onclick = () => setGeneration(currentGen + 1);

lastBtn.onclick = () => setGeneration(totalGenerations);

function updateGenButtons() {
  const max = totalGenerations;

  nextBtn.disabled = currentGen >= max;
  lastBtn.disabled = currentGen >= max;
}

// ==========================
// TABS
// ==========================
function buildTabs(gen) {
  tabsContainer.innerHTML = "";

  const tabs = [
    { id: "0", label: "Table 0 (Input)" },
    { id: `${gen}-1`, label: `${gen}.1 Population` },
    { id: `${gen}-2`, label: `${gen}.2 Crossover` },
    { id: `${gen}-3`, label: `${gen}.3 Mutation` },
    {
      id: `${gen}-4`,
      label: gen === config.maxGenerations
        ? `${gen}.4 Final Population`
        : `${gen}.4 New Population`
    }
  ];

  tabs.forEach(tab => {
    const el = document.createElement("div");
    el.className = "tab";
    if (tab.id === activeTab) el.classList.add("active");

    el.textContent = tab.label;
    el.onclick = () => {
      activeTab = tab.id;
      buildTabs(gen);
      renderTable();
    };

    tabsContainer.appendChild(el);
  });
}

// ==========================
// TABLE RENDER
// ==========================
function getColumns(tabId) {

  if (tabId === "0") {
    const base = config.problemType === "tsp" ? ["City", "X-Coordinate", "Y-Coordinate"]
      : config.problemType === "knapsack" ? ["Item", "Value", "Weight"]
      : ["Individual", "Chromosome"];

    return base;
  }

  const type = tabId.split("-")[1];

  if (type === "1") {
    let cols = ["Individual", "Chromosome", "Fitness Score"];

    const mode = config.selectionMode;

    if (mode === "roulette") cols.push("Adjusted Fitness", "Probability", "Expected Count");
    if (mode === "rank") cols.push("Fitness Rank", "Weight", "Rank Probability");

    return cols;
  }

  if (type === "2") {
    return ["Parent 1", "Parent 2", "Action", "Offspring", "Offspring Fitness"];
  }

  if (type === "3") {
    return ["Offspring", "Chromosome", "Action", "Mutated Offspring", "Fitness Score"];
  }

  if (type === "4") {
    return ["Individual", "Chromosome", "Fitness Score"];
  }

  return [];
}

function renderTable() {
  const cols = getColumns(activeTab);

  tableHead.innerHTML = "";
  tableBody.innerHTML = "";

  if (!cols.length) return;

  /* ==========================
     HEADER
     ========================== */
  const tr = document.createElement("tr");

  cols.forEach(c => {
    const th = document.createElement("th");
    th.textContent = c;
    tr.appendChild(th);
  });

  tableHead.appendChild(tr);

  /* ==========================
     GET REAL DATA (IMPORTANT FIX)
     ========================== */

  const data = getTableData(activeTab) || [];

  const container = document.querySelector(".table-container");
  const rowHeight = 26;

  const visibleRows = Math.ceil(container.clientHeight / rowHeight);
  const rowCount = Math.max(visibleRows, data.length, 20);

  /* ==========================
     BODY
     ========================== */
  for (let i = 0; i < rowCount; i++) {
    const row = document.createElement("tr");

    const rowData = data[i] || [];

    cols.forEach((_, colIndex) => {

      const td = document.createElement("td");

      td.textContent = rowData[colIndex] ?? "";

      row.appendChild(td);
    });

    tableBody.appendChild(row);
  }
}

function getTableData(tab) {
  return state?.tables?.[tab] || [];
}

// ==========================
// INIT
// ==========================
document.addEventListener("DOMContentLoaded", () => {
  defaultConfig = collectConfig();
  savedConfig = { ...defaultConfig };

  rebuildGenerationDropdown();

  genSelect.value = 1;
  buildTabs(1);
  updateProblemTypeFromFilter();
  renderTable();
  updateRunButtonState();
  updateSummary();
  updatePlots();
});


// ==========================
// MODAL BUTTONS
// ==========================
document.getElementById("ok-btn").onclick = () => {
  savedConfig = collectConfig();

  rebuildGenerationDropdown();
  buildTabs(1);
  renderTable();
  updateRunButtonState();

  modal.style.display = "none";
};

document.getElementById("cancel-btn").onclick = () => {
  applyConfig(savedConfig);
  modal.style.display = "none";
};

// ==========================
// FILTER → CONFIG SYNC
// ==========================
function updateProblemTypeFromFilter() {
  const path = state.appliedFilterPath.join(" ").toLowerCase();

  if (path.includes("tsp")) config.problemType = "tsp";
  else if (path.includes("knapsack")) config.problemType = "knapsack";
  else config.problemType = "generic";
}

function parseCSV(text) {
  const rawLines = text.trim().split("\n");

  const headers = rawLines[0].split(",").map(h => h.trim().toLowerCase());

  const expectedSchemas = {
    generic: ["individual", "chromosome"],
    tsp: ["city", "x-coordinate", "y-coordinate"],
    knapsack: ["item", "value", "weight"]
  };

  const type = detectType(headers, expectedSchemas);
  const currentType = getProblemType().toLowerCase();

  // invalid CSV
  if (!type) {
    alert("CSV does not match any supported problem type.");
    return;
  }

  // no problem selected
  if (!currentType || "none" == currentType) {
    alert(`Please select a problem type ${currentType} first.`);
    return;
  }

  // mismatch
  if (type !== currentType) {
    alert(`CSV type (${type}) does not match selected problem (${currentType}).`);
    return;
  }

  const { popSize, geneLength } = inferPopulationAndGeneLength(rawLines, type);

  document.getElementById("pop-size").value = popSize;
  document.getElementById("length-input").value = geneLength;

  const rows = [];

  for (let i = 1; i < rawLines.length; i++) {
    let parts = rawLines[i].split(",").map(p => p.trim());
    rows.push(parts);
  }

  loadIntoTable0(headers, rows, type);
}

function inferPopulationAndGeneLength(rawLines, type) {

  const validRows = rawLines
    .map(r => r.trim())
    .filter(r => r.length > 0);

  // remove header if needed (safe check)
  const dataRows = isHeader(validRows[0]) ? validRows.slice(1) : validRows;

  // population size
  const popSize = dataRows.length;

  let geneLength = 0;

  if (type === "generic") {

    // take first valid row
    const firstRow = dataRows[0];

    const genes = firstRow
      .split(",")[1]       // chromosome column
      .split("-")
      .map(g => g.trim());

    geneLength = genes.length;
  }

  if (type === "tsp") {
    // for TSP, gene length usually = number of cities
    geneLength = dataRows.length;
  }

  if (type === "knapsack") {
    // usually gene length = number of items
    geneLength = dataRows.length;
  }

  return { popSize, geneLength };
}

function isHeader(row) {
  return row.toLowerCase().includes("chromosome") ||
         row.toLowerCase().includes("city") ||
         row.toLowerCase().includes("item");
}

function detectType(headers, schemas) {
  for (const [type, cols] of Object.entries(schemas)) {
    const match =
      cols.length === headers.length &&
      cols.every(c => headers.includes(c));

    if (match) return type;
  }
  return null;
}

function toTitleCase(word) {
  if (!word) return ""; // handle empty input
  return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
}

function loadIntoTable0(headers, rows, type) {
  activeTab = "0";
  state.tables["0"] = rows;
  config.problemType = type;
  renderTable();
  updateRunButtonState();
}

// ==========================
// EXPORT CSV (FIXED)
// ==========================
function exportCSV() {
  let csv = "";

  const tables = state.tables || {};

  // ==========================
  // TABLE 0 (INPUT)
  // ==========================
  if (tables["0"] && tables["0"].length) {
    csv += "Table 0 (Input)\n";

    const headers = getColumns("0");
    csv += headers.join(",") + "\n";

    tables["0"].forEach(row => {
      csv += row.join(",") + "\n";
    });

    csv += "\n";
  }

  // ==========================
  // GENERATIONS
  // ==========================
  for (let gen = 1; gen <= config.maxGenerations; gen++) {

    let hasData = false;

    const ids = [`${gen}-1`, `${gen}-2`, `${gen}-3`, `${gen}-4`];

    // check if any table exists
    for (let id of ids) {
      if (tables[id] && tables[id].length) {
        hasData = true;
        break;
      }
    }

    if (!hasData) continue;

    csv += `Generation ${gen}\n`;

    ids.forEach(id => {
      const table = tables[id];
      if (!table || !table.length) return;

      const type = id.split("-")[1];

      let label =
        type === "1" ? `${gen}.1 Population` :
        type === "2" ? `${gen}.2 Crossover` :
        type === "3" ? `${gen}.3 Mutation` :
        `${gen}.4 New Population`;

      csv += label + "\n";

      const headers = getColumns(id);
      csv += headers.join(",") + "\n";

      table.forEach(row => {
        csv += row.join(",") + "\n";
      });

      csv += "\n";
    });
  }

  if (!csv) {
    alert("No data to export.");
    return;
  }

  const blob = new Blob([csv], { type: "text/csv" });
  const url = URL.createObjectURL(blob);

  const a = document.createElement("a");
  a.href = url;
  a.download = "ga_export.csv";
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  URL.revokeObjectURL(url);
}

// ==========================
// CLEAR ALL (FIXED)
// ==========================
function clearAll() {
  selectedRows.clear();

  state.tables = { "0": [] };

  activeTab = "0";
  currentGen = 1;

  // Clear UI
  tableHead.innerHTML = "";
  tableBody.innerHTML = "";

  renderTable();
  updateSummary();
  updatePlots();
  updateRunButtonState();
}

// ==========================
// IMPORT 
// ==========================
importBtn.onclick = () => {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = ".csv";

  input.onchange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();

    reader.onload = (event) => {
      const text = event.target.result;
      parseCSV(text);
    };

    reader.readAsText(file);
  };

  input.click();
};

// ==========================
// BUTTON HOOKS (FIXED)
// ==========================
exportBtn.onclick = () => {
  exportCSV(); 
};

clearBtn.onclick = () => {
  clearAll();
};

function buildGARequest() {
  updateGeneInputs();
  setupOperatorLogic(); 
  return {
    problemType: getProblemType(),
    geneType: getGeneType(),
    fitnessType: getFitnessType(),

    populationSize: parseInt(document.getElementById("pop-size").value) || 10,
    geneLength: parseInt(document.getElementById("length-input").value) || 5,

    seed: parseInt(document.getElementById("gen-seed").value) || null,

    minGene: parseFloat(document.getElementById("min-gene")?.value || 0),
    maxGene: parseFloat(document.getElementById("max-gene")?.value || 1),

    minValue: parseFloat(document.getElementById("min-value")?.value || 0),
    maxValue: parseFloat(document.getElementById("max-value")?.value || 0),
    minWeight: parseFloat(document.getElementById("min-weight")?.value || 0),
    maxWeight: parseFloat(document.getElementById("max-weight")?.value || 0),
    capacity: parseFloat(document.getElementById("capacity")?.value || 0),

    minCoord: parseFloat(document.getElementById("min-coord")?.value || 0),
    maxCoord: parseFloat(document.getElementById("max-coord")?.value || 0),

    maxGenerations: parseInt(document.getElementById("max-gen").value) || 1,

    targetFitness: parseFloat(document.getElementById("target-value").value) || 0,
    applyTarget: document.getElementById("target-toggle").value === "true",

    applyConvergence: document.getElementById("conv-toggle").value === "true",
    convergencePatience: parseInt(document.getElementById("conv-patience").value) || 10,
    convergenceTolerance: parseFloat(document.getElementById("conv-tolerance").value) || 0.000001,

    applyDiversity: document.getElementById("div-toggle").value === "true",
    diversityThreshold: parseFloat(document.getElementById("div-threshold").value) || 0.3,
    diversityPatience: parseInt(document.getElementById("div-patience").value) || 3,

    crossoverRate: parseFloat(document.getElementById("crossover-rate").value) || 0.8,
    crossoverIndices: document.getElementById("crossover-indices").value || "random",
    mutationRate: parseFloat(document.getElementById("mutation-rate").value) || 0.1,
    mutationIndices: document.getElementById("mutation-indices").value || "random",

    selectionType: document.getElementById("select-mode").value.toUpperCase(),
    crossoverType: document.getElementById("crossover-mode").value.toUpperCase(),
    mutationType: document.getElementById("mutation-mode").value.toUpperCase(),
    replacementType: document.getElementById("replacement-mode").value.toUpperCase(),

    tournamentSize: parseInt(document.getElementById("tournament-size").value || 3),
    eliteCount: parseInt(document.getElementById("elite-count").value || 2),

    customFormula: state.customFormula,

    tables: state.tables
  };
}

function updateRunButtonState() {
  const hasData =
    state.tables &&
    state.tables["0"] &&
    state.tables["0"].length > 0 &&
    state.tables["0"].some(row => row && row.length > 0);

  runBtn.disabled = !hasData;
}

runBtn.onclick = async () => {

  const payload = buildGARequest(); 
  payload.tables = state.tables || { "0": [] };

  // safety cleanup (prevents Spring 400)
  if (!payload.populationSize || payload.populationSize < 2) {
    payload.populationSize = 10;
  }

  if (!payload.geneLength || payload.geneLength < 1) {
    payload.geneLength = 5;
  }

  try {
    const res = await fetch("/api/ga/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const errText = await res.text();
      console.error("Backend Error:", errText);
      alert("Run failed: check console for backend error");
      return;
    }

    const data = await res.json();

    state.generations = data.generations;
    const lastGen = data.generations[data.generations.length - 1];

    rebuildGenerationDropdown(lastGen.generation);
    loadRunResults(data); 

    currentGen = 1;
    activeTab = "0";
    buildTabs(1);
    renderTable();
    updateSummary();
    updatePlots();

  } catch (err) {
    console.error(err);
    alert("Network error while running GA");
  }
};

function loadRunResults(data) {

  // reset all tables except 0
  const inputTable = state.tables["0"];
  state.tables = { "0": inputTable };

  data.generations.forEach(gen => {

    const g = gen.generation;

    state.tables[`${g}-1`] = gen.populationBefore?.rows || [];
    state.tables[`${g}-2`] = gen.afterCrossover?.rows || [];
    state.tables[`${g}-3`] = gen.afterMutation?.rows || [];
    state.tables[`${g}-4`] = gen.nextGeneration?.rows || [];
  });

  activeTab = "0";
  currentGen = 1;
  buildTabs(1);
  renderTable();
  updateSummary();
  updatePlots();
}

function extractKnapsackData(table) {

  if (!table || table.length <= 1) {
    return { labels: [], weights: [], values: [] };
  }

  const rows = table; // remove header

  return {
    labels: rows.map(r => "I" + r[0]),
    weights: rows.map(r => Number(r[1])),
    values: rows.map(r => Number(r[2]))
  };
}

function extractTSPData(table0) {

  if (!Array.isArray(table0)) {
    return { labels: [], x: [], y: [] };
  }

  const labels = [];
  const x = [];
  const y = [];

  for (let i = 0; i < table0.length; i++) {
    const row = table0[i];

    labels.push("C" + row[0]);
    x.push(Number(row[1]));
    y.push(Number(row[2]));
  }

  return { labels, x, y };
}

function extractPlotData(generationsRaw) {

  const generations = Array.isArray(generationsRaw)
    ? generationsRaw
    : generationsRaw?.generations || [];

  return {
    labels: generations.map(g => g.generation),

    bestFitness: generations.map(g => g.bestFitness),
    avgFitness: generations.map(g => g.avgFitness)
  };
}

function extractRouletteData(gen) {

  const table = gen?.populationBefore?.rows || [];

  const labels = [];
  const values = [];

  for (let row of table) {

    const name = row[0];              // Individual name
    const prob = parseFloat(row[row.length - 2]); // rouletteProbability

    if (!isNaN(prob)) {
      labels.push(name);
      values.push(prob);
    }
  }

  return { labels, values };
}

function extractRankData(gen) {

  const table = gen?.populationBefore?.rows || [];

  const labels = [];
  const values = [];

  for (let row of table) {

    const name = row[0];              // Individual name
    const prob = parseFloat(row[row.length - 1]); // rouletteProbability

    if (!isNaN(prob)) {
      labels.push(name);
      values.push(prob);
    }
  }

  return { labels, values };
}

function extractPopulationComparison(before, after) {

  const b = Array.isArray(before) ? before : [];
  const a = Array.isArray(after) ? after : [];

  const labels = [];
  const values = [];
  const colors = [];
  const groupIndex = []; // 0 = before, 1 = after

  const colorMap = {
    offspring: "rgba(244, 163, 0, 0.7)",   // orange
    mutated: "rgba(44, 160, 44, 0.7)",     // green
    elite: "rgba(31, 119, 180, 0.9)",      // deeper blue
    default: "rgba(31, 119, 180, 0.5)"     // initial population
  };

  function pushGroup(arr, typeDefault, groupId) {
    for (const ind of arr) {

      labels.push(ind.name ?? "X");
      values.push(ind.fitness ?? 0);

      let type = null;

      if (groupId === 1) {
        type = ind.type ?? typeDefault;
      }
      else {
        type = typeDefault;
      }
      colors.push(colorMap[type] || colorMap.default);
      groupIndex.push(groupId);
    }
  }

  pushGroup(b, "default", 0);
  pushGroup(a, "offspring", 1);

  return { labels, values, colors, groupIndex };
}

function drawOverlay(ctx, chart, nodes, bestRoute, area) {

  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

  const xScale = chart.scales.x;
  const yScale = chart.scales.y;

  const toX = (x) => xScale.getPixelForValue(x);
  const toY = (y) => yScale.getPixelForValue(y);

  // ==========================
  // 1. COMPLETE GRAPH (GRAY EDGES)
  // ==========================
  ctx.strokeStyle = "#ccc";
  ctx.lineWidth = 1;

  for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {

      ctx.beginPath();
      ctx.moveTo(toX(nodes[i].x), toY(nodes[i].y));
      ctx.lineTo(toX(nodes[j].x), toY(nodes[j].y));
      ctx.stroke();
    }
  }

  // ==========================
  // 2. BEST ROUTE (RED + ARROWS)
  // ==========================
  if (bestRoute && bestRoute.length > 1) {

    ctx.strokeStyle = "red";
    ctx.lineWidth = 2;

    for (let i = 0; i < bestRoute.length - 1; i++) {

      const a = nodes[bestRoute[i]];
      const b = nodes[bestRoute[i + 1]];

      const x1 = toX(a.x);
      const y1 = toY(a.y);
      const x2 = toX(b.x);
      const y2 = toY(b.y);

      // line
      ctx.beginPath();
      ctx.moveTo(x1, y1);
      ctx.lineTo(x2, y2);
      ctx.stroke();

      drawArrow(ctx, x1, y1, x2, y2);
    }
  }

  if (bestRoute?.length > 2) {

    const a = nodes[bestRoute[bestRoute.length - 1]];
    const b = nodes[bestRoute[0]];

    ctx.beginPath();
    ctx.moveTo(toX(a.x), toY(a.y));
    ctx.lineTo(toX(b.x), toY(b.y));
    ctx.stroke();

    drawArrow(ctx,
      toX(a.x), toY(a.y),
      toX(b.x), toY(b.y)
    );
  }
}

function drawArrow(ctx, x1, y1, x2, y2) {

  const headlen = 8;
  const angle = Math.atan2(y2 - y1, x2 - x1);

  ctx.fillStyle = "red";

  ctx.beginPath();
  ctx.moveTo(x2, y2);
  ctx.lineTo(
    x2 - headlen * Math.cos(angle - Math.PI / 6),
    y2 - headlen * Math.sin(angle - Math.PI / 6)
  );
  ctx.lineTo(
    x2 - headlen * Math.cos(angle + Math.PI / 6),
    y2 - headlen * Math.sin(angle + Math.PI / 6)
  );
  ctx.closePath();
  ctx.fill();
}

function parseRoute(route) {
  if (!route) return null;

  if (Array.isArray(route)) return route;

  if (typeof route === "string") {
    return route
      .replace(/\s+/g, "")
      .split("-")
      .map(Number)
      .filter(Number.isFinite);
  }

  return null;
}

let _chartInstance = null;
Chart.defaults.font.family = "Arial";
Chart.defaults.font.size = 12;

function renderKnapsackChart(container, data) {

  container.innerHTML = `
    <div style="width:100%; height:100%; min-height:320px;">
      <canvas id="knapsack-chart"></canvas>
    </div>
  `;

  const canvas = container.querySelector("#knapsack-chart");

  if (canvas._chartInstance) {
    canvas._chartInstance.destroy();
  }

  const chart = new Chart(canvas, {
    data: {
      labels: data.labels,

      datasets: [

        // WEIGHTS (bar)
        {
          type: "bar",
          label: "Weight",
          data: data.weights,
          backgroundColor: "rgba(100, 149, 237, 0.5)", // soft blue
          borderWidth: 0
        },

        // VALUES (bar)
        {
          type: "bar",
          label: "Value",
          data: data.values,
          backgroundColor: "rgba(244, 163, 0, 0.5)", // orange
          borderWidth: 0
        }
      ]
    },

    options: {
      layout: {padding : 10},
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        legend: {
          position: "top"
        },

        title: {
          display: true,
          text: "Knapsack: Weights / Values"
        }
      },

      scales: {
        x: {
          title: {
            display: true,
            text: "Items",
            padding: {top: 10}
          },
          grid: { display: false }
        },

        y: {
          beginAtZero: true,
          title: {
            display: true,
            text: "Weights & Values",
            padding: {right: 10}
          },
          grid: { color: "#eee" }
        }
      }
    }
  });

  canvas._chartInstance = chart;
}

function renderTSPChart(container, data, bestRoute = null, minCoord, maxCoord, index = 1) {

  container.innerHTML = `
    <div style="position:relative; width:100%; height:100%; min-height:320px;">
      <canvas id="tsp-base"></canvas>
      <canvas id="tsp-overlay"
        style="position:absolute; top:0; left:0; pointer-events:none;"></canvas>
    </div>
  `;

  const baseCanvas = container.querySelector("#tsp-base");
  const overlayCanvas = container.querySelector("#tsp-overlay");

  const startIndex = bestRoute?.[0] ?? 0;

  const ctxOverlay = overlayCanvas.getContext("2d");

  // destroy old chart if exists
  if (baseCanvas._chartInstance) {
    baseCanvas._chartInstance.destroy();
  }

  const nodes = data.labels.map((label, i) => ({
    name: label,
    x: data.x[i],
    y: data.y[i]
  }));

  const cityData = nodes
  .map((n, i) => ({
    name: n.name,
    x: n.x,
    y: n.y
  }))
  .filter((_, i) => i !== startIndex);

  // ==========================
  // CHART.JS NODE LAYER
  // ==========================
  const chart = new Chart(baseCanvas, {
    type: "scatter",
    data: {
      datasets: [{
        label: "Cities",
        data: cityData,
        backgroundColor: "#1f77b4",
        pointRadius: 5,
        pointStyle: "circle"
      },
      {
      label: "Start City",
      data: [nodes[startIndex]],

      backgroundColor: "#000",
      pointRadius: 8,
      pointStyle: "triangle"
    }]},

    options: {
      layout: {padding : 10},
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        legend: { display: true },
        title: {
          display: true,
          text: `TSP: Best Route (Generation ${index})`
        },

        tooltip: {
          callbacks: {
            label: (ctx) => {
              const d = ctx.raw;
              return `${d.name} (${d.x}, ${d.y})`;
            }
          }
        }
      },

      scales: {
        x: {
          type: "linear",
          min: minCoord,
          max: maxCoord,
          title: {
            display: true,
            text: "X-Coordinates",
            padding: {top: 10}
          },
          grid: { color: "#eee" }
        },
        y: {
          type: "linear",
          min: minCoord,
          max: maxCoord,
          title: {
            display: true,
            text: "Y-Coordinates",
            padding: {right: 10}
          },
          grid: { color: "#eee" }
        }
      },

      animation: false
    }
  });

  baseCanvas._chartInstance = chart;

  // ==========================
  // WAIT FOR LAYOUT READY
  // ==========================
  requestAnimationFrame(() => {

    const chartArea = chart.chartArea;

    overlayCanvas.width = baseCanvas.width;
    overlayCanvas.height = baseCanvas.height;

    drawOverlay(
      ctxOverlay,
      chart,
      nodes,
      bestRoute,
      chartArea
    );
  });
}

function renderFitnessChart(container, data, targetFitness = null) {

  container.innerHTML = `
    <div style="width:100%; height:100%; min-height:320px;">
      <canvas id="fitness-chart"></canvas>
    </div>
  `;

  const canvas = container.querySelector("#fitness-chart");

  const labels = data.labels || [];
  const best = data.bestFitness || [];
  const avg = data.avgFitness || [];

  if (canvas._chartInstance) {
    canvas._chartInstance.destroy();
  }

  const datasets = [

    // ==========================
    // BEST FITNESS (bars)
    // ==========================
    {
      type: "bar",
      label: "Best Fitness",
      data: best,
      backgroundColor: "rgba(31, 119, 180, 0.35)", // lighter blue (less dominant)
      borderWidth: 0,
      barPercentage: 0.6,
      categoryPercentage: 0.7
    },

    // ==========================
    // AVG FITNESS (line ABOVE bars)
    // ==========================
    {
      type: "line",
      label: "Avg Fitness",
      data: avg,
      borderColor: "rgba(244, 163, 0, 1)", // strong orange
      backgroundColor: "transparent",
      tension: 0.35,

      pointRadius: 4,
      pointHoverRadius: 6,

      pointBackgroundColor: "rgba(244, 163, 0, 0.9)",
      pointBorderColor: "#fff",
      pointBorderWidth: 1.5,

      borderWidth: 2
    }
  ];

  // ==========================
  // TARGET FITNESS (TRUE FLAT LINE)
  // ==========================
  if (targetFitness != null) {
    datasets.push({
      type: "line",
      label: "Target Fitness",
      data: labels.map(() => targetFitness),

      borderColor: "red",
      borderWidth: 2,
      borderDash: [6, 6],

      pointRadius: 0,          // NO nodes
      pointHoverRadius: 0,

      fill: false
    });
  }

  const chart = new Chart(canvas, {
    data: { labels, datasets },

    options: {
      responsive: true,
      maintainAspectRatio: false,

      layout: {
        padding: 20
      },

      plugins: {
        legend: {
          position: "top",
          labels: {
            usePointStyle: true,
            boxWidth: 10
          }
        },

        title: {
          display: true,
          text: "Fitness Over Generations"
        }
      },

      interaction: {
        mode: "index",
        intersect: false
      },

      scales: {
        x: {
          title: {
            display: true,
            text: "Generations"
          },
          grid: { display: false }
        },

        y: {
          beginAtZero: false,
          title: {
            display: true,
            text: "Fitness Score"
          },
          grid: {
            color: "#eee"
          }
        }
      }
    }
  });

  canvas._chartInstance = chart;
}

function renderRouletteChart(container, data) {

  container.innerHTML = `
    <div style="width:100%; height:100%; min-height:320px;">
      <canvas id="roulette-chart"></canvas>
    </div>
  `;

  const canvas = container.querySelector("#roulette-chart");

  if (canvas._chartInstance) {
    canvas._chartInstance.destroy();
  }

  const chart = new Chart(canvas, {
    type: "pie",

    data: {
      labels: data.labels,
      datasets: [{
        data: data.values,

        backgroundColor: data.labels.map((_, i) =>
          `hsl(${(i * 360) / data.labels.length}, 45%, 65%)`
        ),

        borderWidth: 0,
        hoverOffset: 6
      }]
    },

    options: {
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        title: {
          display: true,
          text: "Roulette Selection Probability"
        },

        legend: {
          position: "right"
        },

        tooltip: {
          callbacks: {
            label: (ctx) => {
              return `${ctx.label}: ${(ctx.raw * 100).toFixed(2)}%`;
            }
          }
        }
      }
    }
  });

  canvas._chartInstance = chart;
}

function renderRankChart(container, data) {

  container.innerHTML = `
    <div style="width:100%; height:100%; min-height:320px;">
      <canvas id="rank-chart"></canvas>
    </div>
  `;

  const canvas = container.querySelector("#rank-chart");

  if (canvas._chartInstance) {
    canvas._chartInstance.destroy();
  }

  const chart = new Chart(canvas, {
    type: "pie",

    data: {
      labels: data.labels,
      datasets: [{
        data: data.values,

        backgroundColor: data.labels.map((_, i) =>
          `hsl(${(i * 360) / data.labels.length}, 45%, 65%)`
        ),

        borderWidth: 0,
        hoverOffset: 6
      }]
    },

    options: {
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        title: {
          display: true,
          text: "Rank Selection Probability"
        },

        legend: {
          position: "right"
        },

        tooltip: {
          callbacks: {
            label: (ctx) => {
              return `${ctx.label}: ${(ctx.raw * 100).toFixed(2)}%`;
            }
          }
        }
      }
    }
  });

  canvas._chartInstance = chart;
}

function renderPopulationComparisonChart(container, data, targetFitness = null) {

  container.innerHTML = `
    <div style="width:100%; height:100%; min-height:320px;">
      <canvas id="pop-comparison"></canvas>
    </div>
  `;

  const canvas = container.querySelector("#pop-comparison");

  if (canvas._chartInstance) {
    canvas._chartInstance.destroy();
  }

  // ==========================
  // GAP LOGIC (visual separation)
  // ==========================
  const gapSize = 2; // visual spacing between groups

  const labels = data.labels;
  const values = data.values;

  const adjustedLabels = [];
  const adjustedValues = [];
  const adjustedColors = [];

  for (let i = 0; i < labels.length; i++) {

    adjustedLabels.push(labels[i]);
    adjustedValues.push(values[i]);
    adjustedColors.push(data.colors[i]);

    // insert empty spacer after populationBefore
    if (data.groupIndex[i] === 0 &&
        (i === data.groupIndex.lastIndexOf(0))) {

      for (let g = 0; g < gapSize; g++) {
        adjustedLabels.push("");
        adjustedValues.push(null);
        adjustedColors.push("transparent");
      }
    }
  }

  const datasets = [
    {
      type: "bar",
      label: "Fitness",
      data: adjustedValues,

      backgroundColor: adjustedColors,

      borderWidth: 0,

      barPercentage: 0.9,
      categoryPercentage: 0.9
    }
  ];

  // ==========================
  // TARGET FITNESS LINE (true horizontal line)
  // ==========================
  if (targetFitness != null) {

    datasets.push({
      type: "line",
      label: "Target Fitness",
      data: adjustedLabels.map(() => targetFitness),

      borderColor: "red",
      borderWidth: 2,
      borderDash: [6, 6],

      pointRadius: 0,
      fill: false
    });
  }

  const typeColors = {
    offspring: "rgba(244, 163, 0, 0.7)",
    mutated: "rgba(44, 160, 44, 0.7)",
    elite: "rgba(31, 119, 180, 0.9)",
    default: "rgba(31, 119, 180, 0.5)"
  };

  const presentTypes = new Set();

  for (let i = 0; i < data.colors.length; i++) {
    const color = data.colors[i];

    if (color === typeColors.offspring) presentTypes.add("offspring");
    if (color === typeColors.mutated) presentTypes.add("mutated");
    if (color === typeColors.elite) presentTypes.add("elite");
    if (color === typeColors.default) presentTypes.add("default");
  }

  const chart = new Chart(canvas, {
    data: {
      labels: adjustedLabels,
      datasets
    },

    options: {
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        legend: {
          position: "top",

          labels: {
            generateLabels: (chart) => {

              const labels = [];

              // TYPE LEGENDS (manual)
              if (presentTypes.has("elite")) {
                labels.push({
                  text: "Elite",
                  fillStyle: typeColors.elite
                });
              }

              if (presentTypes.has("mutated")) {
                labels.push({
                  text: "Mutated",
                  fillStyle: typeColors.mutated
                });
              }

              if (presentTypes.has("offspring")) {
                labels.push({
                  text: "Offspring",
                  fillStyle: typeColors.offspring
                });
              }

              if (presentTypes.has("default")) {
                labels.push({
                  text: "Initial Population",
                  fillStyle: typeColors.default
                });
              }

              // TARGET FITNESS
              if (chart.data.datasets.some(d => d.label === "Target Fitness")) {
                labels.push({
                  text: "Target Fitness",
                  strokeStyle: "red",
                  lineWidth: 2,
                  fillStyle: "transparent"
               });
              }

              return labels;
            }
          }
        },

        title: {
          display: true,
          text: "Population Comparison"
        },

        tooltip: {
          callbacks: {
            label: (ctx) => {
              if (ctx.raw == null) return null;
              return `${ctx.label}: ${ctx.raw}`;
            }
          }
        }
      },

      scales: {
        x: {
          title: {
            display: true,
            text: "Individuals"
          },
          grid: { display: false }
        },

        y: {
          beginAtZero: false,
          title: {
            display: true,
            text: "Fitness Score"
          },
          grid: { color: "#eee" }
        }
      }
    }
  });

  canvas._chartInstance = chart;
}

function updatePlots() {

  const PlotViewer = window.PlotViewer;

  if (!PlotViewer || !PlotViewer.setPlots) {
    console.error("PlotViewer not ready");
    return;
  }

  const plots = [];
  const problemType = getProblemType();
  const selectionType = document.getElementById("select-mode")?.value?.toUpperCase() || "";
  const gens = state.generations || [];
  const hasGenerations = gens.length > 0;

  const index = hasGenerations
    ? Math.max(0, currentGen - 1)
    : 0;

  const gen = hasGenerations ? gens[index] : null;

  state._plotGenerations = gens;
  
  let targetFitness = null;
  const applyTarget = document.getElementById("target-toggle")?.value === "true";

  if (applyTarget) {
    targetFitness = parseFloat(document.getElementById("target-value")?.value) || null;
  }

  // ==========================
  // KNAPSACK PLOT (ONLY IF KNAPSACK)
  // ==========================
  if (problemType === "KNAPSACK") {

    const table0 = state.tables?.["0"] || [];

    const knapsackData = extractKnapsackData(table0);

    plots.push({
      title: "Knapsack: Weights / Values",
      render: (container) =>
        renderKnapsackChart(container, knapsackData)
    });
  }

  if (problemType === "TSP" && hasGenerations && gen) {

    const table0 = state.tables["0"] || [];
    const tspData = extractTSPData(table0);

    const minCoord = parseFloat(document.getElementById("min-coord")?.value || 0);
    const maxCoord = parseFloat(document.getElementById("max-coord")?.value || 9);

    plots.push({
      title: `TSP: Best Route (Generation ${index+1})`,

      render: (container) => {

        const bestPath = parseRoute(gen?.bestIndividual);

        renderTSPChart(container, tspData, bestPath, minCoord, maxCoord, index + 1);
      }
    });
  }

  if (selectionType === "ROULETTE" && hasGenerations && gen) {

    const rouletteData = extractRouletteData(gen);

    plots.push({
      title: `Roulette Wheel (Generation ${index+1})`,
      render: (container) =>
        renderRouletteChart(container, rouletteData)
    });
  }

  if (selectionType === "RANK" && hasGenerations && gen) {

    const rouletteData = extractRankData(gen);

    plots.push({
      title: `Rank Wheel (Generation ${index+1})`,
      render: (container) =>
        renderRankChart(container, rouletteData)
    });
  }

  // ==========================
  // FITNESS PLOT (always)
  // ==========================

  if (gen?.populationBefore && gen?.nextGeneration) {

    const before = gen.before?.rows || gen.before || [];
    const after  = gen.after?.rows || gen.after || [];

    const popData = extractPopulationComparison(before, after);

    plots.push({
      title: "Population Evolution",
      render: (container) =>
        renderPopulationComparisonChart(container, popData, targetFitness)
    });
  }

  const fitnessData = extractPlotData(state.generations);

  plots.push({
    title: "Fitness Over Generations",
    render: (container) =>
      renderFitnessChart(container, fitnessData, targetFitness)
  });

  PlotViewer.setPlots(plots);
}

function clearSummary() {
  document.getElementById("sum-gen").innerHTML = "-";
  document.getElementById("sum-best").textContent = "-";
  document.getElementById("sum-avg").textContent = "-";
  document.getElementById("sum-ind").textContent = "-";
  document.getElementById("sum-trend").textContent = "-";

  const stopRow = document.getElementById("stop-row");
  stopRow.style.display = "none";
}


  // ==========================
  // Stop Criteria
  // ==========================
function formatStopReason(reason, gen) {
  switch (reason) {
    case "target":
      return `Target fitness reached at generation ${gen}`;
    case "convergence":
       return "No significant improvement (converged)";
    case "diversity":
      return "Population lost diversity";
     case "maxgen":
      return "Reached maximum generations";
    default:
      return "-";
  }
}

function updateSummary() {
  const gens = state.generations;

  // HARD RESET FIRST
  clearSummary();

  if (!Array.isArray(gens) || gens.length === 0) return;

  const index = Math.max(0, currentGen - 1);
  const genData = gens[index];

  if (!genData) return;

  // ==========================
  // BASIC
  // ==========================
  const sumGen = document.getElementById("sum-gen");

  sumGen.innerHTML = `
    <span class="gen-current">${genData.generation}</span>
    <span class="gen-separator"> / ${gens.length}</span>
  `;

  document.getElementById("sum-best").textContent =
    genData.bestFitness?.toFixed?.(5) ?? "-";

  document.getElementById("sum-avg").textContent =
    genData.avgFitness?.toFixed?.(5) ?? "-";

  document.getElementById("sum-ind").textContent =
    genData.bestIndividual ?? "-";

  // ==========================
  // TREND
  // ==========================
  const trendEl = document.getElementById("sum-trend");

  let trendText = "N/A";

  if (currentGen > 1 && gens[currentGen - 2]) {
    const prev = gens[currentGen - 2];

    const diff = genData.avgFitness - prev.avgFitness;

    if (Math.abs(diff) < 1e-9) {
      trendText = "Stable (No change)";
      trendEl.style.color = "gray";
    } else if (diff > 0) {
      trendText = `Improved ↑ (+${diff.toFixed(4)})`;
      trendEl.style.color = "green";
    } else {
      trendText = `Degraded ↓ (${diff.toFixed(4)})`;
      trendEl.style.color = "red";
    }
  } else {
    trendEl.style.color = "gray";
  }

  trendEl.textContent = trendText;

  // ==========================
  // STOP REASON
  // ==========================
  const stopRow = document.getElementById("stop-row");

  const isLast = currentGen === gens.length;

  if (isLast) {
    stopRow.style.display = "flex";
    document.getElementById("sum-stop").textContent =
      formatStopReason(genData.stopCriteria, currentGen);
  } else {
    stopRow.style.display = "none";
  }
}
