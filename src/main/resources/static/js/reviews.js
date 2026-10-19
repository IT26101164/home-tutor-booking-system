// MEMBER 5: Review submission, display, edit and deletion.
function reviewModal(bookingId, r) {
  showModal(
    `<h2 class="dialog-title">${r ? "Edit your review" : "How was your lesson?"}</h2><form id="review-form" data-id="${r?.id || ""}" data-booking="${bookingId}"><div class="field"><label for="rating">Rating</label><select id="rating" name="rating">${[5, 4, 3, 2, 1].map((n) => `<option value="${n}" ${r?.rating === n ? "selected" : ""}>${n} stars</option>`).join("")}</select></div><div class="field"><label for="comment">Your experience</label><textarea name="comment" id="comment" required maxlength="1000">${e(r?.comment || "")}</textarea></div><div class="form-error"></div><button class="btn">Save review</button></form>`,
  );
}
function reviewPanel(reviews, tid) {
  return `<article class="card"><h2>Student voices</h2>${reviews.map((r) => `<div class="review"><div class="review-head"><strong>${e(r.studentName)}</strong><span class="rating">${"★".repeat(r.rating)}</span></div><p>${e(r.comment)}</p>${user && (user.id === r.studentId || user.role === "ADMIN") ? `<div class="actions">${user.id === r.studentId ? `<button class="link" data-action="review-edit" data-id="${r.id}" data-tutor="${tid}">Edit review</button>` : ""}<button class="link" data-action="review-delete" data-id="${r.id}">Delete review</button></div>` : ""}</div>`).join("") || '<p class="muted">No reviews yet.</p>'}</article>`;
}
async function reviewsPage() {
  if (!requireUser()) return "";
  if (user.role !== "STUDENT")
    return blank("Student reviews are available through tutor profiles.");
  const bookings = await api("/bookings");
  const completed = bookings.filter((b) => b.status === "COMPLETED");
  return (
    title(
      "Your reviews",
      "Share what helped you learn.",
      "Write or edit one review for each completed lesson.",
    ) +
    `<div class="stack">${completed.map((b) => `<article class="card"><h3>${e(b.tutorName)}</h3><p class="muted">${e(date(b.start))} · Booking #${b.id}</p><button class="btn secondary" data-action="review-new" data-id="${b.id}" data-tutor="${b.tutorId}">Write / edit review</button> <a class="link" href="#tutor/${b.tutorId}">View reviews</a></article>`).join("") || blank("Complete a lesson to leave a review.")}</div>`
  );
}
onForm("review-form", async (form, data) => {
  await api(
    "/reviews" + (form.dataset.id ? "/" + form.dataset.id : ""),
    form.dataset.id ? "PUT" : "POST",
    {
      bookingId: Number(form.dataset.booking),
      rating: Number(data.rating),
      comment: data.comment,
    },
  );
  closeModal();
  toast("Review saved.");
  await render();
});
for (const action of ["review-new", "review-edit"])
  onAction(action, async (button) => {
    const reviews = await api("/tutors/" + button.dataset.tutor + "/reviews");
    const review = reviews.find((r) =>
      action === "review-edit"
        ? r.id === Number(button.dataset.id)
        : r.bookingId === Number(button.dataset.id),
    );
    reviewModal(review?.bookingId || Number(button.dataset.id), review);
  });
onAction("review-delete", (button) =>
  confirmAction("Delete this review?", "review-delete", button.dataset.id),
);
onConfirm("review-delete", (button) =>
  api("/reviews/" + button.dataset.id, "DELETE"),
);
