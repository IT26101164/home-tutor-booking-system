<<<<<<< Updated upstream
"use strict";
const app = document.querySelector("#app"),
  nav = document.querySelector("#nav"),
  modal = document.querySelector("#modal");
let user = null,
  csrf = "",
  csrfHeader = "X-CSRF-TOKEN",
  routeVersion = 0;
const escapeHtml = (v) =>
  String(v ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const e = escapeHtml;
const money = (n) =>
  "LKR " + Number(n).toLocaleString("en-LK", { maximumFractionDigits: 2 });
const date = (v) =>
  new Date(v).toLocaleString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
const initials = (name) =>
  name
    .split(" ")
    .slice(0, 2)
    .map((x) => x[0])
    .join("");
const blank = (t) => `<div class="empty">${e(t)}</div>`;
const field = (label, name, value = "", type = "text", extra = "") =>
  `<div class="field"><label for="${e(name)}">${e(label)}</label><input id="${e(name)}" name="${e(name)}" type="${type}" value="${e(value)}" ${extra}></div>`;
const status = (s) => `<span class="status ${e(s)}">${e(s)}</span>`;
async function api(url, method = "GET", data) {
  let response = await fetch("/api" + url, {
    method,
    credentials: "same-origin",
    headers: {
      ...(data !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(method !== "GET" ? { [csrfHeader]: csrf } : {}),
    },
    body: data === undefined ? undefined : JSON.stringify(data),
  });
  let raw = await response.text(),
    result;
  try {
    result = raw ? JSON.parse(raw) : null;
  } catch {
    result = null;
  }
  if (!response.ok)
    throw new Error(
      result?.message ||
        (response.status === 403
          ? "Your session may have expired. Refresh the page and try again."
          : "Request failed. Please try again."),
    );
  return result;
}
async function session() {
  const s = await api("/session");
  user = s.user;
  csrf = s.csrfToken;
  csrfHeader = s.csrfHeader;
  drawNav();
}
function drawNav() {
  nav.innerHTML = `<a href="#search">Find a tutor</a>${user ? `<a href="#bookings">${user.role === "TUTOR" ? "Requests" : "Bookings"}</a>${user.role === "TUTOR" ? '<a href="#teach">Tutor studio</a><a href="#availability">Availability</a>' : ""}${user.role === "ADMIN" ? '<a href="#admin">Admin</a>' : ""}${user.role === "STUDENT" ? '<a href="#reviews">Reviews</a>' : ""}<a href="#account">${e(user.name.split(" ")[0])}</a><button class="btn secondary tiny" data-action="logout">Sign out</button>` : '<a href="#login">Sign in</a><a class="btn tiny" href="#register">Get started ↗</a>'}`;
}
let toastTimer;
function toast(message) {
  const t = document.querySelector("#toast");
  t.textContent = message;
  t.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove("show"), 5000);
}
function showModal(html) {
  document.querySelector("#modal-content").innerHTML = html;
  modal.showModal();
}
function closeModal() {
  modal.close();
}
function requireUser() {
  if (!user) {
    location.hash = "login";
    return false;
  }
  return true;
}
function title(tag, heading, sub) {
  return `<div class="page-title"><div class="eyebrow">${e(tag)}</div><h1>${e(heading)}</h1><p class="muted">${e(sub)}</p></div>`;
}
// Each member registers handlers for their own forms and buttons.
const formHandlers = new Map(),
  actionHandlers = new Map(),
  confirmHandlers = new Map();
const onForm = (name, handler) => formHandlers.set(name, handler);
const onAction = (name, handler) => actionHandlers.set(name, handler);
const onConfirm = (name, handler) => confirmHandlers.set(name, handler);
=======
"use strict";
const app = document.querySelector("#app"),
  nav = document.querySelector("#nav"),
  modal = document.querySelector("#modal");
let user = null,
  csrf = "",
  csrfHeader = "X-CSRF-TOKEN",
  routeVersion = 0;
const escapeHtml = (v) =>
  String(v ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
const e = escapeHtml;
const money = (n) =>
  "LKR " + Number(n).toLocaleString("en-LK", { maximumFractionDigits: 2 });
const date = (v) =>
  new Date(v).toLocaleString("en-GB", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
const initials = (name) =>
  name
    .split(" ")
    .slice(0, 2)
    .map((x) => x[0])
    .join("");
const blank = (t) => `<div class="empty">${e(t)}</div>`;
const field = (label, name, value = "", type = "text", extra = "") =>
  `<div class="field"><label for="${e(name)}">${e(label)}</label><input id="${e(name)}" name="${e(name)}" type="${type}" value="${e(value)}" ${extra}></div>`;
const status = (s) => `<span class="status ${e(s)}">${e(s)}</span>`;
async function api(url, method = "GET", data) {
  let response = await fetch("/api" + url, {
    method,
    credentials: "same-origin",
    headers: {
      ...(data !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(method !== "GET" ? { [csrfHeader]: csrf } : {}),
    },
    body: data === undefined ? undefined : JSON.stringify(data),
  });
  let raw = await response.text(),
    result;
  try {
    result = raw ? JSON.parse(raw) : null;
  } catch {
    result = null;
  }
  if (!response.ok)
    throw new Error(
      result?.message ||
        (response.status === 403
          ? "Your session may have expired. Refresh the page and try again."
          : "Request failed. Please try again."),
    );
  return result;
}
async function session() {
  const s = await api("/session");
  user = s.user;
  csrf = s.csrfToken;
  csrfHeader = s.csrfHeader;
  drawNav();
}
function drawNav() {
  nav.innerHTML = `<a href="#search">Find a tutor</a>${user ? `<a href="#bookings">${user.role === "TUTOR" ? "Requests" : "Bookings"}</a>${user.role === "TUTOR" ? '<a href="#teach">Tutor studio</a><a href="#availability">Availability</a>' : ""}${user.role === "ADMIN" ? '<a href="#admin">Admin</a>' : ""}${user.role === "STUDENT" ? '<a href="#reviews">Reviews</a>' : ""}<a href="#account">${e(user.name.split(" ")[0])}</a><button class="btn secondary tiny" data-action="logout">Sign out</button>` : '<a href="#login">Sign in</a><a class="btn tiny" href="#register">Get started ↗</a>'}`;
}
let toastTimer;
function toast(message) {
  const t = document.querySelector("#toast");
  t.textContent = message;
  t.classList.add("show");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove("show"), 5000);
}
function showModal(html) {
  document.querySelector("#modal-content").innerHTML = html;
  modal.showModal();
}
function closeModal() {
  modal.close();
}
function requireUser() {
  if (!user) {
    location.hash = "login";
    return false;
  }
  return true;
}
function title(tag, heading, sub) {
  return `<div class="page-title"><div class="eyebrow">${e(tag)}</div><h1>${e(heading)}</h1><p class="muted">${e(sub)}</p></div>`;
}
// Each member registers handlers for their own forms and buttons.
const formHandlers = new Map(),
  actionHandlers = new Map(),
  confirmHandlers = new Map();
const onForm = (name, handler) => formHandlers.set(name, handler);
const onAction = (name, handler) => actionHandlers.set(name, handler);
const onConfirm = (name, handler) => confirmHandlers.set(name, handler);
>>>>>>> Stashed changes
