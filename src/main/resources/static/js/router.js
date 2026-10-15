// SHARED: Route and event dispatch. Keep component logic in its owner file.
async function render() {
  const version = ++routeVersion;
  const [page = "home", arg] = location.hash.slice(1).split("/");
  app.innerHTML = blank("Loading…");
  try {
    let html;
    switch (page) {
      case "home":
      case "":
        html = await home();
        break;
      case "search":
        html = await searchPage(decodeURIComponent(arg || ""));
        break;
      case "tutor":
        html = await tutorPage(Number(arg));
        break;
      case "login":
        html = authPage();
        break;
      case "register":
        html = authPage(true, arg === "tutor");
        break;
      case "bookings":
        html = await bookingsPage();
        break;
      case "teach":
        html = await teachPage();
        break;
      case "availability":
        html = await availabilityPage();
        break;
      case "reviews":
        html = await reviewsPage();
        break;
      case "account":
        html = accountPage();
        break;
      case "admin":
        html = await adminPage();
        break;
      default:
        html = blank("Page not found. Use the navigation to continue.");
    }
    if (version !== routeVersion) return;
    app.innerHTML = html;
    drawNav();
    if (page === "search")
      await searchResults(document.querySelector("#search-form"));
    window.scrollTo(0, 0);
  } catch (err) {
    if (version === routeVersion)
      app.innerHTML = `<div class="error">${e(err.message)}</div><a class="btn secondary" href="#home">Back to home</a>`;
  }
}
function confirmAction(message, action, id) {
  showModal(
    `<h2 class="dialog-title">Please confirm</h2><p>${e(message)}</p><div class="actions"><button class="btn danger" data-action="confirmed" data-kind="${action}" data-id="${id || ""}">Confirm</button><button class="btn secondary" data-action="close">Keep it</button></div>`,
  );
}
onAction("close", () => closeModal());
onAction("confirmed", async (button) => {
  const handler = confirmHandlers.get(button.dataset.kind);
  if (!handler) throw new Error("Unknown action.");
  await handler(button);
  closeModal();
  toast("Done.");
  await render();
});
document.addEventListener("submit", async (event) => {
  const form = event.target;
  if (!(form instanceof HTMLFormElement)) return;
  const handler = formHandlers.get(form.getAttribute("id"));
  if (!handler) return;
  event.preventDefault();
  const button = form.querySelector('button[type="submit"],button:not([type])');
  if (button) button.disabled = true;
  const error = form.querySelector(".form-error");
  if (error) error.innerHTML = "";
  try {
    await handler(form, Object.fromEntries(new FormData(form)));
  } catch (err) {
    if (error) error.innerHTML = `<div class="error">${e(err.message)}</div>`;
    else toast(err.message);
  } finally {
    if (button) button.disabled = false;
  }
});
document.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const handler = actionHandlers.get(button.dataset.action);
  if (!handler) return;
  button.disabled = true;
  try {
    await handler(button);
  } catch (err) {
    toast(err.message);
  } finally {
    button.disabled = false;
  }
});
window.addEventListener("hashchange", render);
session()
  .then(render)
  .catch(
    (err) =>
      (app.innerHTML = `<div class="error">${e(err.message)} Start Spring Boot, then refresh.</div>`),
  );
