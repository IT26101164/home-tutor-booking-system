// MEMBER 4: Reservation lifecycle and rescheduling.
async function bookingsPage() {
    if (!requireUser()) return "";
    const list = await api("/bookings");
    return (
        title(
            "Keep moving forward",
            user.role === "TUTOR" ? "Your lesson requests" : "Your bookings",
            "Manage requests, lesson times and your learning history.",
        ) +
        `<div class="stack">${list.map((b) => `<article class="card booking"><div>${status(b.status)}<h3>${e(user.role === "STUDENT" ? b.tutorName : b.studentName)}</h3><p class="muted">${e(date(b.start))} → ${e(date(b.end))}<br>${money(b.totalFee)} · Booking #${b.id}${user.role === "ADMIN" ? ` · Tutor: ${e(b.tutorName)}` : ""}</p></div><div class="actions">${["TUTOR", "ADMIN"].includes(user.role) && b.status === "PENDING" ? `<button class="btn tiny" data-action="status" data-id="${b.id}" data-status="CONFIRMED">Accept</button><button class="btn secondary tiny" data-action="status" data-id="${b.id}" data-status="REJECTED">Reject</button>` : ""}${["TUTOR", "ADMIN"].includes(user.role) && b.status === "CONFIRMED" ? `<button class="btn tiny" data-action="status" data-id="${b.id}" data-status="COMPLETED">Complete</button>` : ""}${["PENDING", "CONFIRMED"].includes(b.status) ? `${user.role === "STUDENT" ? `<button class="btn secondary tiny" data-action="reschedule" data-id="${b.id}" data-tutor="${b.tutorId}">Reschedule</button>` : ""}<button class="btn danger tiny" data-action="status" data-id="${b.id}" data-status="CANCELLED">Cancel</button>` : ""}${user.role === "STUDENT" && b.status === "COMPLETED" ? `<button class="btn secondary tiny" data-action="review-new" data-id="${b.id}" data-tutor="${b.tutorId}">Write / edit review</button>` : ""}${["STUDENT", "ADMIN"].includes(user.role) && ["REJECTED", "CANCELLED"].includes(b.status) ? `<button class="btn danger tiny" data-action="booking-delete" data-id="${b.id}">Delete</button>` : ""}<a class="link" href="#tutor/${b.tutorId}">Tutor profile</a></div></article>`).join("") || blank("No bookings yet. Find a tutor to book your first lesson.")}</div>`
    );
}
onForm("reschedule-form", async (form, data) => {
    await api("/bookings/" + form.dataset.id + "/reschedule", "PUT", {
        slotId: Number(data.slotId),
    });
    closeModal();
    toast("Rescheduled. Awaiting tutor confirmation.");
    await render();
});
onAction("book", async (button) => {
    if (!requireUser()) return;
    if (user.role !== "STUDENT") {
        toast("Use a student / parent account to book.");
        return;
    }
    await api("/bookings", "POST", { slotId: Number(button.dataset.id) });
    toast("Request sent.");
    location.hash = "bookings";
});
onAction("status", async (button) => {
    if (button.dataset.status === "CANCELLED") {
        confirmAction(
            "Cancel this booking and release its time slot?",
            "cancel",
            button.dataset.id,
        );
        return;
    }
    await api("/bookings/" + button.dataset.id + "/status", "PUT", {
        status: button.dataset.status,
    });
    toast("Booking updated.");
    await render();
});
onAction("reschedule", async (button) => {
    const slots = await api("/tutors/" + button.dataset.tutor + "/slots");
    showModal(
        `<h2 class="dialog-title">Choose another lesson time</h2>${slots.length ? `<form id="reschedule-form" data-id="${button.dataset.id}"><div class="field"><label for="slotId">Available slots</label><select id="slotId" name="slotId">${slots.map((s) => `<option value="${s.id}">${e(date(s.start))} – ${e(date(s.end))}</option>`).join("")}</select></div><p class="muted">Your tutor must confirm the new time.</p><div class="form-error"></div><button class="btn">Reschedule</button></form>` : blank("No alternative slots are available.")}`,
    );
});
onAction("booking-delete", (button) =>
    confirmAction(
        "Delete this cancelled or rejected booking?",
        "booking-delete",
        button.dataset.id,
    ),
);
onConfirm("booking-delete", (button) =>
    api("/bookings/" + button.dataset.id, "DELETE"),
);
onConfirm("cancel", (button) =>
    api("/bookings/" + button.dataset.id + "/status", "PUT", {
        status: "CANCELLED",
    }),
);
