package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import javax.json.JsonObject;

public abstract class Module implements AutoCloseable {
  protected static final JsonObject DEFAULT_STATE = JsonObject.EMPTY_JSON_OBJECT;

  protected Config config;
  private JsonObject state;
  private JsonObject previousState;

  public Module(Config config) {
    this.config = config;
    this.state = DEFAULT_STATE;
  }

  public final JsonObject getState() {
    return this.state;
  }

  public final JsonObject getPreviousState() {
    return this.previousState;
  }

  public final void setState(JsonObject state) {
    this.state = state;
  }

  public final void setPreviousState(JsonObject previousState) {
    this.previousState = previousState;
  }
}
