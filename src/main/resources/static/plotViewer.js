window.PlotViewer = (() => {

  let images = [];
  let current = 0;

  let viewer, main, track, prevBtn, nextBtn;
  let isActive = false;
  let initialized = false;

  const modal = document.createElement("div");
  modal.className = "plot-modal";
  modal.innerHTML = `<img />`;
  document.body.appendChild(modal);

  // ==========================
  // INIT
  // ==========================
  function initDOM() {

    if (initialized) return true;

    viewer = document.getElementById("plot-viewer");
    main = document.getElementById("plot-main");
    track = document.querySelector(".thumb-track");
    prevBtn = document.getElementById("plot-prev");
    nextBtn = document.getElementById("plot-next");

    if (!viewer || !main || !track) return false;

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

    prevBtn?.addEventListener("click", prev);
    nextBtn?.addEventListener("click", next);

    initialized = true;
    return true;
  }

  // ==========================
  // RENDER
  // ==========================
  function render() {
    if (!initDOM() || !images.length) return;

    renderMain();
    renderThumbnails();
  }

  function renderMain() {
    main.innerHTML = `
      <img src="${images[current]}" />
    `;
  }

  function renderThumbnails() {
    track.innerHTML = "";

    images.forEach((src, i) => {
      const img = document.createElement("img");
      img.src = src;

      img.className = "plot-thumb" + (i === current ? " active" : "");

      img.onclick = () => {
        current = i;
        render();
      };

      track.appendChild(img);
    });

    centerActiveThumb();
  }

  // ==========================
  // CENTER ACTIVE THUMB
  // ==========================
  function centerActiveThumb() {
    const active = track.querySelector(".plot-thumb.active");
    if (!active) return;

    const trackRect = track.getBoundingClientRect();
    const activeRect = active.getBoundingClientRect();

    const offset =
      (activeRect.left + activeRect.width / 2) -
      (trackRect.left + trackRect.width / 2);

    track.scrollBy({
      left: offset,
      behavior: "smooth"
    });
  }

  // ==========================
  // NAVIGATION (LOOPED)
  // ==========================
  function next() {
    if (!images.length) return;
    current = (current + 1) % images.length;
    render();
  }

  function prev() {
    if (!images.length) return;
    current = (current - 1 + images.length) % images.length;
    render();
  }

  function setIndex(i) {
    if (!images.length) return;
    current = ((i % images.length) + images.length) % images.length;
    render();
  }

  function setImages(imgs) {
    images = (imgs || []).filter(Boolean);
    current = 0;
    render();
  }

  // ==========================
  // MODAL
  // ==========================
  main?.addEventListener("click", () => {
    if (!images.length) return;

    modal.classList.add("show");
    modal.querySelector("img").src = images[current];
  });

  modal.addEventListener("click", () => {
    modal.classList.remove("show");
  });

  // ==========================
  // EXPORT
  // ==========================
  return {
    setImages,
    setIndex,
    next,
    prev
  };

})();