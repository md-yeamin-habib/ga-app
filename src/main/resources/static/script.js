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
}

function syncGenUI() {

  dropdown.style.display = "none";

  buildTabs(currentGen);
  updateProblemTypeFromFilter?.();
  updateSummary?.();
  renderTable?.();
}

function rebuildGenerationDropdown(totalGens = 1) {

  totalGenerations = totalGens;
  currentGen = 1;

  genSelect.value = 1;
  dropdown.style.display = "none";
}

function renderDropdown(pos) {

  const start = Math.max(pos - 3, 1);

  const end = Math.min(pos + 3, totalGenerations);

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

window.addEventListener("DOMContentLoaded", () => {
  PlotViewer.setImages([
  "img1.png", //optional graph for TSP all routes/Knapsack weight vs value bar graph
  "img2.png", //bar graph showing fitness of initial population
  "img3.png", //optional pie graph for Roulette/Rank selection
  "img4.png", //fitness bar graph of parents and offsprings
  "img5.png", //pie graph for mutated vs non mutated
  "img6.png", //fitness bar graph for parents and mutated offsprings
  "img7.png", //pie graph for offsprings vs elites
  "img8.png", //fitness bar graph between old and new population, and target fitness
  "img9.png", //optional graph to show best route in TSP
  "img10.png" //bar graph showing best (and line graph for average) fitness over all generations
]);
});

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

  // IMPORTANT: clear actual data, not just UI
  state.tables = { "0": [] };

  activeTab = "0";
  currentGen = 1;

  // Clear UI
  tableHead.innerHTML = "";
  tableBody.innerHTML = "";

  renderTable();
  updateSummary();
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
  setupOperatorLogic(); // ensure operator states are correct before reading
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
  // ensure tables exist in correct format
  console.log("Payload before run:", payload);
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

    console.log("Stopped at generation ", lastGen.generation, " due to:", lastGen.stopCriteria);
    rebuildGenerationDropdown(lastGen.generation);
    loadRunResults(data); 

    currentGen = 1;
    activeTab = "0";
    buildTabs(1);
    renderTable();
    updateSummary();

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