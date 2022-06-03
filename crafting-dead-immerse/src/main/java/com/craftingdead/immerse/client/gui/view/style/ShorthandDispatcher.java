/*
 * Crafting Dead
 * Copyright (C) 2022  NexusNode LTD
 *
 * This Non-Commercial Software License Agreement (the "Agreement") is made between
 * you (the "Licensee") and NEXUSNODE (BRAD HUNTER). (the "Licensor").
 * By installing or otherwise using Crafting Dead (the "Software"), you agree to be
 * bound by the terms and conditions of this Agreement as may be revised from time
 * to time at Licensor's sole discretion.
 *
 * If you do not agree to the terms and conditions of this Agreement do not download,
 * copy, reproduce or otherwise use any of the source code available online at any time.
 *
 * https://github.com/nexusnode/crafting-dead/blob/1.18.x/LICENSE.txt
 *
 * https://craftingdead.net/terms.php
 */

package com.craftingdead.immerse.client.gui.view.style;

import java.util.ArrayList;
import java.util.Set;
import com.craftingdead.immerse.client.gui.view.property.Transition;
import com.craftingdead.immerse.client.gui.view.style.parser.value.ValueParser;
import com.craftingdead.immerse.client.gui.view.style.parser.value.ValueParserRegistry;
import com.craftingdead.immerse.client.gui.view.style.selector.StyleNodeState;

public class ShorthandDispatcher<T> implements PropertyDispatcher<T> {

  private final String name;
  private final ValueParser<T> parser;
  private final ShorthandArgMapper argMapper;
  private final PropertyDispatcher<T>[] dispatchers;

  @SafeVarargs
  public ShorthandDispatcher(String name, ValueParser<T> parser, ShorthandArgMapper argMapper,
      PropertyDispatcher<T>... dispatchers) {
    this.name = name;
    this.parser = parser;
    this.argMapper = argMapper;
    this.dispatchers = dispatchers;
  }

  @Override
  public void defineState(String value, int specificity, Set<StyleNodeState> nodeStates) {
    var values = new ArrayList<String>();
    var current = value;
    while (!current.isEmpty()) {
      var validated = this.parser.validate(current);
      if (validated == 0) {
        break;
      }

      values.add(current.substring(0, validated));
      current = current.substring(validated).trim();
    }

    for (int i = 0; i < values.size(); i++) {
      for (int childIndex : this.argMapper.map(i, values.size())) {
        this.dispatchers[childIndex].defineState(values.get(i), specificity, nodeStates);
      }
    }
  }

  @Override
  public void refreshState() {}

  @Override
  public void reset() {}

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setTransition(Transition transition) {
    for (var dispatcher : this.dispatchers) {
      dispatcher.setTransition(transition);
    }
  }

  @SafeVarargs
  public static <T> ShorthandDispatcher<T> create(String name, Class<T> type,
      ShorthandArgMapper argMapper, PropertyDispatcher<T>... properties) {
    return new ShorthandDispatcher<>(name, ValueParserRegistry.getInstance().getParser(type),
        argMapper, properties);
  }
}
