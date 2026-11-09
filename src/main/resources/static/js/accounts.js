// MEMBER 1: Authentication and own-account screens.
function authPage(register = false, tutor = false) {
  return `<section class="auth card"><span class="eyebrow">${register ? "Your next chapter" : "Welcome back"}</span><h1>${register ? "Let’s get started." : "Good to see you."}</h1><p class="muted">${register ? "Create your account to learn or teach." : "Sign in to manage your learning journey."}</p><form id="${register ? "register" : "login"}-form">${register ? field("Full name", "name", "", "text", 'required maxlength="80" autocomplete="name"') : ""}${field("Email address", "email", "", "email", 'required maxlength="150" autocomplete="email"')}${field("Password", "password", "", "password", `required minlength="8" maxlength="128" autocomplete="${register ? "new-password" : "current-password"}"`)}${register ? `<div class="field"><label for="role">I want to</label><select name="role" id="role"><option value="STUDENT">Find a tutor (student / parent)</option><option value="TUTOR" ${tutor ? "selected" : ""}>Teach as a tutor</option></select></div>` : ""}<div class="form-error"></div><button class="btn">${register ? "Create account" : "Sign in"} ↗</button></form><p class="auth-switch">${register ? 'Already registered? <a class="link" href="#login">Sign in</a>' : 'New here? <a class="link" href="#register">Create an account</a>'}</p><div class="notice">Demo: student@hometutor.test / tutor@hometutor.test / admin@hometutor.test<br>Password: <strong>Demo123!</strong></div></section>`;
}
function accountPage() {
  if (!requireUser()) return "";
  return (
    title(
      "Your account",
      "A little about you.",
      "Keep your details up to date.",
    ) +
    `<section class="card auth"><form id="account-form">${field("Full name", "name", user.name, "text", 'required maxlength="80"')}${field("Email address", "email", user.email, "email", 'required maxlength="150"')}${field("New password (leave blank to keep current)", "password", "", "password", 'minlength="8" maxlength="128" autocomplete="new-password"')}<div class="form-error"></div><button class="btn">Save changes</button></form><div class="notice">Role: ${e(user.role)}. Deactivation removes access and hides your tutor profile. Resolve active bookings first.</div><button class="btn danger" data-action="account-delete">Deactivate account</button></section>`
  );
}
for (const name of ["login-form", "register-form"])
  onForm(name, async (form, data) => {
    await api(
      name === "login-form" ? "/auth/login" : "/auth/register",
      "POST",
      data,
    );
    await session();
    location.hash =
      user.role === "TUTOR"
        ? "teach"
        : user.role === "ADMIN"
          ? "admin"
          : "search";
  });
onForm("account-form", async (form, data) => {
  await api("/account", "PUT", data);
  await session();
  toast("Account updated.");
});
onAction("logout", async () => {
  await api("/auth/logout", "POST");
  await session();
  location.hash = "home";
  await render();
});
onAction("account-delete", () =>
  confirmAction(
    "Deactivate your account? Resolve active bookings first.",
    "account-delete",
  ),
);
onConfirm("account-delete", async () => {
  await api("/account", "DELETE");
  await session();
  location.hash = "home";
});
