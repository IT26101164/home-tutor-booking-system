<<<<<<< Updated upstream
package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** Shared state and ONE transaction monitor across all six modules. */
public final class ApplicationStore {
  private final StateRepository repository;
  private final Clock clock;
  State state;
  TutorBST tutors;

  public ApplicationStore(StateRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
    state = repository.load();
    tutors = TutorBST.from(state.tutors.values());
  }

  LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  <T> T commit(Function<State, T> action) {
    if (!Thread.holdsLock(this))
      throw new IllegalStateException("Mutation requires shared store lock");
    State next = state.copy();
    T result = action.apply(next);
    repository.save(next);
    for (long id : state.tutors.keySet()) if (!next.tutors.containsKey(id)) tutors.delete(id);
    for (Tutor t : next.tutors.values()) if (!t.equals(state.tutors.get(t.id()))) tutors.insert(t);
    state = next;
    return result;
  }
}
=======
package lk.sliit.hometutor.service;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import lk.sliit.hometutor.algorithm.*;
import lk.sliit.hometutor.model.*;
import lk.sliit.hometutor.repository.*;
import lk.sliit.hometutor.service.Views.*;

/** Shared state and ONE transaction monitor across all six modules. */
public final class ApplicationStore {
  private final StateRepository repository;
  private final Clock clock;
  State state;
  TutorBST tutors;

  public ApplicationStore(StateRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
    state = repository.load();
    tutors = TutorBST.from(state.tutors.values());
  }

  LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  <T> T commit(Function<State, T> action) {
    if (!Thread.holdsLock(this))
      throw new IllegalStateException("Mutation requires shared store lock");
    State next = state.copy();
    T result = action.apply(next);
    repository.save(next);
    for (long id : state.tutors.keySet()) if (!next.tutors.containsKey(id)) tutors.delete(id);
    for (Tutor t : next.tutors.values()) if (!t.equals(state.tutors.get(t.id()))) tutors.insert(t);
    state = next;
    return result;
  }
}
>>>>>>> Stashed changes
