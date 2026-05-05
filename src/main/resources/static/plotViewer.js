window.PlotViewer = (() => {

  let plots = [];
  let current = 0;

  let viewer, main, prevBtn, nextBtn;
  let isActive = false;
  let initialized = false;

  const modal = document.createElement("div");
  modal.className = "plot-modal";
  modal.innerHTML = `<div class="plot-modal-content"></div>`;
  document.body.appendChild(modal);

  // ==========================
  // INIT
  // ==========================
  function initDOM() {

    if (initialized) return true;

    viewer = document.getElementById("plot-viewer");
    main = document.getElementById("plot-main");
    prevBtn = document.getElementById("plot-prev");
    nextBtn = document.getElementById("plot-next");

    // ❗ track removed
    if (!viewer || !main || !prevBtn || !nextBtn) return false;

    // focus handling
    viewer.addEventListener("click", () => isActive = true);

    document.addEventListener("click", (e) => {
      if (!viewer.contains(e.target)) isActive = false;
    });

    document.addEventListener("keydown", (e) => {
      if (!isActive) return;

      if (e.key === "ArrowRight") next();
      if (e.key === "ArrowLeft") prev();
    });

    // DOUBLE CLICK → OPEN MODAL
    main.addEventListener("dblclick", () => {
      if (!plots.length) return;

      modal.classList.add("show");

      const container = modal.querySelector(".plot-modal-content");
      container.innerHTML = "";

      plots[current].render(container);
    });

    prevBtn?.addEventListener("click", prev);
    nextBtn?.addEventListener("click", next);

    initialized = true;
    return true;
  }

  // ==========================
  // RENDER
  // ==========================
  function render() {
    if (!initDOM() || !plots.length) return;
    renderMain();
  }

  function renderMain() {
    main.innerHTML = "";

    const plot = plots[current];
    if (!plot) return;

    const container = document.createElement("div");
    container.style.width = "100%";
    container.style.height = "100%";

    main.appendChild(container);

    plot.render(container);
  }

  // ==========================
  // NAVIGATION
  // ==========================
  function next() {
    if (!plots.length) return;
    current = (current + 1) % plots.length;
    render();
  }

  function prev() {
    if (!plots.length) return;
    current = (current - 1 + plots.length) % plots.length;
    render();
  }

  function setIndex(i) {
    if (!plots.length) return;
    current = ((i % plots.length) + plots.length) % plots.length;
    render();
  }

  function setPlots(p) {
    plots = (p || []).filter(Boolean);
    current = 0;
    render();
  }

  // CLOSE MODAL
  modal.addEventListener("click", () => {
    modal.classList.remove("show");
  });

  // ==========================
  // EXPORT
  // ==========================
  return {
    setPlots,
    setIndex,
    next,
    prev
  };

})();