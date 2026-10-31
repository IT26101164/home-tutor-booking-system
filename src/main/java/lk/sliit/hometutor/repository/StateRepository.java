package lk.sliit.hometutor.repository;

import lk.sliit.hometutor.model.State;

/** Abstraction: business services do not depend on file parsing details. */
public interface StateRepository {
    State load();

    void save(State state);
}
