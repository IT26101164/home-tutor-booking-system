// MEMBER 3: Time-slot list, add and edit forms.
function slotModal(tid, s) {
    showModal(
        `<h2 class="dialog-title">${s ? "Edit lesson time" : "Add a lesson time"}</h2><p class="muted">Enter Sri Lanka local time.</p><form id="slot-form" data-tutor="${tid}" data-id="${s?.id || ""}">${field("Start", "start", s?.start?.slice(0, 16) || "", "datetime-local", "required")}${field("End", "end", s?.end?.slice(0, 16) || "", "datetime-local", "required")}<div class="form-error"></div><button class="btn">Save time slot</button></form>`,
    );
}
function availableSlotsPanel(slots) {
    return (
        slots
            .map(
                (s) =>
                    `<div class="slot"><div><strong>${e(date(s.start))}</strong><div class="muted tiny-text">Until ${e(date(s.end))}</div></div><button class="btn tiny" data-action="book" data-id="${s.id}">Book</button></div>`,
            )
            .join("") || blank("No available slots. Please check again later.")
    );
}
async function availabilityPage() {
    if (!requireUser()) return "";
    if (user.role !== "TUTOR") return blank("This page is for tutors.");
    const t = await api("/tutors/mine");
    if (!t)
        return (
            title(
                "Availability",
                "Set up your profile first.",
                "Your time slots belong to your tutor profile.",
            ) + '<a class="btn" href="#teach">Create tutor profile</a>'
        );
    const slots = await api("/tutors/" + t.id + "/slots?available=false");
    return (
        title(
            "Availability",
            "Make time for great lessons.",
            "Create, edit or delete your lesson slots.",
        ) +
        `<section class="card"><button class="btn" data-action="slot-new" data-tutor="${t.id}">+ Add a time slot</button><div class="notice">Slots must be 15 minutes–8 hours. Slots with booking history cannot be edited or deleted.</div>${slots.map((s) => `<div class="slot"><div><strong>${e(date(s.start))}</strong><div class="tiny-text muted">Until ${e(date(s.end))} · ${s.available ? "Available" : "Past / reserved"}</div></div><div><button class="link" data-action="slot-edit" data-id="${s.id}" data-tutor="${t.id}">Edit</button><button class="link" data-action="slot-delete" data-id="${s.id}">Delete</button></div></div>`).join("") || blank("Add your first lesson time.")}</section>`
    );
}
onForm("slot-form", async (form, data) => {
    await api(
        "/tutors/" +
        form.dataset.tutor +
        "/slots" +
        (form.dataset.id ? "/" + form.dataset.id : ""),
        form.dataset.id ? "PUT" : "POST",
        data,
    );
    closeModal();
    toast("Availability saved.");
    await render();
});
onAction("slot-new", (button) => slotModal(button.dataset.tutor));
onAction("slot-edit", async (button) => {
    const slots = await api(
        "/tutors/" + button.dataset.tutor + "/slots?available=false",
    );
    slotModal(
        button.dataset.tutor,
        slots.find((s) => s.id === Number(button.dataset.id)),
    );
});
onAction("slot-delete", (button) =>
    confirmAction("Delete this time slot?", "slot-delete", button.dataset.id),
);
onConfirm("slot-delete", (button) =>
    api("/slots/" + button.dataset.id, "DELETE"),
);
