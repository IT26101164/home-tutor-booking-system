// MEMBER 2: Tutor profiles, search and subject ordering.
function tutorCard(t, i = 0) {
  return `<article class="card"><div class="card-head"><div class="avatar ${["", "blue", "peach"][i % 3]}">${e(initials(t.name))}</div><div><h3>${e(t.name)}</h3><span class="tiny-text muted">${e(t.location)} · ${t.experienceYears} years experience</span></div></div><span class="pill">${e(t.subject)}</span><p class="bio">${e(t.bio)}</p><div class="card-meta"><span class="muted">${e(t.qualification)}</span></div><div class="card-meta"><span class="rating">★ ${t.reviewCount ? Number(t.rating).toFixed(1) : "New"} <span class="muted">(${t.reviewCount} reviews)</span></span></div><div class="card-bottom"><span class="price">${money(t.hourlyFee)} <small>/ hr</small></span><a class="btn secondary tiny" href="#tutor/${t.id}">View tutor ↗</a></div></article>`;
}
async function home() {
  const list = await api("/tutors");
  return `<section class="hero"><div><span class="pill">A little guidance. A world of possibilities.</span><h1>The right tutor.<br>Your next<br><em>breakthrough.</em></h1><p class="lead">Find friendly, experienced tutors near you. Learn at your pace, build confidence, and make progress that lasts.</p><div class="hero-actions"><a class="btn" href="#search">Find my tutor ↗</a><a class="btn secondary" href="#register/tutor">Become a tutor</a></div><div class="trust"><div class="dots"><span>AF</span><span>NP</span><span>SF</span></div><span>Personal lessons. Real connections.</span></div></div><div class="hero-art" aria-label="Illustration of a mathematics lesson"><div class="art-label">SMALL STEPS. BIG UNDERSTANDING.</div><div class="lesson-card"><span class="pill">Your learning moment</span><h3>It finally<br>makes sense.</h3><div class="formula">x² + 2x + 1<br><small>= (x + 1)²</small></div></div><div class="check-bubble"><strong>✓ One-to-one attention</strong><br>A lesson built around you</div><div class="art-foot">Mathematics · Science · Languages · ICT</div></div></section><section><div class="section-head"><div><span class="eyebrow">A good place to start</span><h2>Meet your next tutor</h2><p>Explore teachers by subject, location and availability.</p></div><a class="link" href="#search">Explore all tutors →</a></div><div class="subjects">${["Mathematics", "Physics", "English", "Chemistry", "ICT"].map((s) => `<a class="chip" href="#search/${encodeURIComponent(s)}">${s} ↗</a>`).join("")}</div><div class="grid">${list.slice(0, 3).map(tutorCard).join("") || blank("No tutors yet. Register as a tutor to get started.")}</div></section><section class="banner"><div><h2>Turn what you know into someone’s “aha!”</h2><p>Create your tutor profile and share your available lesson times.</p></div><a class="btn light" href="#register/tutor">Start teaching ↗</a></section>`;
}
async function searchPage(subject = "") {
  return (
    title(
      "Find your fit",
      "Good learning starts here.",
      "Search by subject, location or tutor ID. Results are ordered by subject.",
    ) +
    `<form id="search-form" class="searchbar">${field("Subject", "subject", subject, "text", 'placeholder="e.g. Mathematics" maxlength="80"')}${field("Location", "location", "", "text", 'placeholder="e.g. Colombo" maxlength="100"')}${field("Tutor ID (optional)", "id", "", "number", 'min="1" placeholder="Any tutor"')}<button class="btn">Search tutors ↗</button></form><div class="section-head"><p id="result-count">Finding tutors…</p><span class="tiny-text muted">Subject A–Z</span></div><div id="results" class="grid"></div>`
  );
}
async function searchResults(form) {
  const data = new FormData(form),
    q = new URLSearchParams();
  for (const [k, v] of data) if (v.trim()) q.set(k, v);
  const list = await api("/tutors?" + q);
  if (!document.querySelector("#results")) return;
  document.querySelector("#result-count").textContent =
    `${list.length} tutor${list.length === 1 ? "" : "s"} found`;
  document.querySelector("#results").innerHTML =
    list.map(tutorCard).join("") ||
    blank("No matches. Try another subject or location.");
}
function tutorForm(t) {
  return `<form id="tutor-form" data-id="${t?.id || ""}"><div class="row">${field("Primary subject", "subject", t?.subject || "", "text", 'required maxlength="80"')}${field("Location", "location", t?.location || "", "text", 'required maxlength="100"')}</div>${field("Qualification", "qualification", t?.qualification || "", "text", 'required maxlength="180"')}<div class="row">${field("Hourly fee (LKR)", "hourlyFee", t?.hourlyFee || "", "number", 'required min="0.01" max="100000" step="0.01"')}${field("Years of experience", "experienceYears", t?.experienceYears ?? 0, "number", 'required min="0" max="60"')}</div><div class="field"><label for="bio">About your teaching</label><textarea id="bio" name="bio" required maxlength="1200">${e(t?.bio || "")}</textarea></div><div class="form-error"></div><button class="btn">Save tutor profile</button></form>`;
}
async function teachPage() {
  if (!requireUser()) return "";
  if (user.role !== "TUTOR") return blank("This page is for tutors.");
  const t = await api("/tutors/mine");
  return (
    title(
      "Tutor studio",
      "Your teaching, in focus.",
      "Create and maintain your public tutor profile.",
    ) +
    `<section class="card"><h2>Your tutor profile</h2>${tutorForm(t)}${t ? `<div class="actions"><a class="link" href="#tutor/${t.id}">View public profile</a><a class="btn secondary" href="#availability">Manage availability</a><button class="link" data-action="tutor-delete" data-id="${t.id}">Delete profile</button></div>` : ""}</section>`
  );
}
async function tutorPage(id) {
  const [t, slots, reviews] = await Promise.all([
    api("/tutors/" + id),
    api("/tutors/" + id + "/slots"),
    api("/tutors/" + id + "/reviews"),
  ]);
  return `<a class="back" href="#search">← Back to tutors</a><div class="cols"><section class="stack"><article class="card"><div class="card-head"><div class="avatar detail-avatar">${e(initials(t.name))}</div><div><span class="pill">${e(t.subject)}</span><h1 class="profile-name">${e(t.name)}</h1><span class="muted">${e(t.location)} · ${t.experienceYears} years experience</span></div></div><h3>About your tutor</h3><p>${e(t.bio)}</p><h3>Qualifications</h3><p class="muted">${e(t.qualification)}</p><span class="rating">★ ${Number(t.rating).toFixed(1)} · ${t.reviewCount} reviews</span></article>${reviewPanel(reviews, id)}</section><aside class="card"><span class="eyebrow">Make time for progress</span><h2>${money(t.hourlyFee)} <small>/ hour</small></h2><p class="muted">Choose a time. Your tutor will confirm the request.</p><div class="notice">Sri Lanka time. No online payment is collected.</div><h3>Available lessons</h3>${availableSlotsPanel(slots)}</aside></div>`;
}
onForm("search-form", async (form) => searchResults(form));
onForm("tutor-form", async (form, data) => {
  await api(
    "/tutors" + (form.dataset.id ? "/" + form.dataset.id : ""),
    form.dataset.id ? "PUT" : "POST",
    {
      ...data,
      hourlyFee: Number(data.hourlyFee),
      experienceYears: Number(data.experienceYears),
    },
  );
  toast("Tutor profile saved.");
  await render();
});
onAction("tutor-delete", (button) =>
  confirmAction(
    "Delete this tutor profile? Profiles with booking history must instead be deactivated.",
    "tutor-delete",
    button.dataset.id,
  ),
);
onConfirm("tutor-delete", (button) =>
  api("/tutors/" + button.dataset.id, "DELETE"),
);
