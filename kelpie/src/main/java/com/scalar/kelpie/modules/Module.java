package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import javax.json.Json;
import javax.json.JsonObject;

public abstract class Module {
  protected Config config;
  protected JsonObject state;

  public Module(Config config) {
    this.config = config;
    this.state = Json.createObjectBuilder().build();
  }

  public JsonObject getState() {
    return this.state;
  }

  public void setState(JsonObject state) {
    this.state = state;
  }
}
