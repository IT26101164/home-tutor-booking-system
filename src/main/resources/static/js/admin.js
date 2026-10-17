// MEMBER 6: Admin account CRUD and restoration.
async function adminPage() {
    if (!requireUser()) return "";
    if (user.role !== "ADMIN") return blank("Administrator access required.");
    const [stats, users] = await Promise.all([
        api("/admin/stats"),
        api("/admin/users"),
    ]);
    return (
        title(
            "Administration",
            "Your community, at a glance.",
            "Create accounts, update details and manage access.",
        ) +
        `<div class="stats">${Object.entries(stats)
            .map(
                ([k, v]) =>
                    `<div class="stat"><strong>${v}</strong><span>${e(k)}</span></div>`,
            )
            .join(
                "",
            )}</div><div class="actions"><button class="btn" data-action="user-new">+ Add account</button><a class="btn secondary" href="#bookings">Manage bookings</a><a class="btn secondary" href="#search">Moderate reviews</a></div><div class="section-head"><h2>Accounts</h2></div><div class="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Actions</th></tr></thead><tbody>${users.map((u) => `<tr><td>${e(u.name)}</td><td>${e(u.email)}</td><td>${e(u.role)}</td><td>${u.active ? "Active" : "Deactivated"}</td><td><button class="btn secondary tiny" data-action="user-edit" data-id="${u.id}">Edit</button> ${u.id === user.id ? "" : u.active ? `<button class="btn danger tiny" data-action="user-delete" data-id="${u.id}">Deactivate</button>` : `<button class="btn tiny" data-action="user-activate" data-id="${u.id}">Restore</button>`}</td></tr>`).join("")}</tbody></table></div>`
    );
}
function adminUserModal(u) {
    showModal(
        `<h2 class="dialog-title">${u ? "Edit account" : "Create account"}</h2><form id="admin-user-form" data-id="${u?.id || ""}">${field("Full name", "name", u?.name || "", "text", 'required maxlength="80"')}${field("Email address", "email", u?.email || "", "email", 'required maxlength="150"')}${u ? "" : field("Initial password", "password", "", "password", 'required minlength="8" maxlength="128"') + '<div class="field"><label for="role">Role</label><select id="role" name="role"><option value="STUDENT">Student</option><option value="TUTOR">Tutor</option><option value="ADMIN">Admin</option></select></div>'}<div class="form-error"></div><button class="btn">Save account</button></form>`,
    );
}
onForm("admin-user-form", async (form, data) => {
    await api(
        "/admin/users" + (form.dataset.id ? "/" + form.dataset.id : ""),
        form.dataset.id ? "PUT" : "POST",
        data,
    );
    closeModal();
    await session();
    toast("Account saved.");
    await render();
});
onAction("user-new", () => adminUserModal());
onAction("user-edit", async (button) => {
    const users = await api("/admin/users");
    adminUserModal(users.find((u) => u.id === Number(button.dataset.id)));
});
onAction("user-delete", (button) =>
    confirmAction("Deactivate this account?", "user-delete", button.dataset.id),
);
onConfirm("user-delete", (button) =>
    api("/admin/users/" + button.dataset.id, "DELETE"),
);
onAction("user-activate", async (button) => {
    await api("/admin/users/" + button.dataset.id + "/activate", "PUT");
    toast("Account restored.");
    await render();
});
