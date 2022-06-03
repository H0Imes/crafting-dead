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

package com.craftingdead.immerse.client.gui.view.property;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import com.craftingdead.immerse.client.gui.view.style.PropertyDispatcher;
import com.craftingdead.immerse.client.gui.view.style.StyleNode;
import com.craftingdead.immerse.client.gui.view.style.parser.value.ValueParser;
import com.craftingdead.immerse.client.gui.view.style.parser.value.ValueParserRegistry;
import com.craftingdead.immerse.client.gui.view.style.selector.StyleNodeState;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class StyleableProperty<T> extends BaseProperty<T> implements PropertyDispatcher<T> {

  private final StateDefinition<T> baseDefinition;

  private final StyleNode owner;

  private final Map<String, T> styleCache = new HashMap<>();

  private final ValueParser<T> parser;

  private final Set<StyleNode> trackedNodes = new ReferenceOpenHashSet<>();

  private final NavigableSet<StateDefinition<T>> definitions = new TreeSet<>();

  private Transition transition = Transition.INSTANT;

  @Nullable
  private Runnable transitionStopListener;

  private int order;

  @SafeVarargs
  public StyleableProperty(StyleNode owner, String name, Class<T> type, T defaultValue,
      Consumer<T>... listeners) {
    super(name, type, defaultValue, listeners);
    this.owner = owner;
    this.parser = ValueParserRegistry.getInstance().getParser(type);
    this.baseDefinition = new StateDefinition<>(0, 0, defaultValue, Set.of());
    this.addOrReplace(this.baseDefinition);
  }

  /**
   * Varargs version of {@link #defineState(Object, Set)}.
   */
  public final void defineState(T value, StyleNodeState... states) {
    this.defineState(value, Set.of(states));
  }

  /**
   * User-facing method to define a state.
   * 
   * @param value - the value to associate the state with
   * @param states - the state
   */
  public void defineState(T value, Set<StyleNodeState> states) {
    this.defineState(1000, value, states);
  }

  private void defineState(int specificity, T value, Set<StyleNodeState> nodeStates) {
    this.addOrReplace(new StateDefinition<>(this.order++, specificity, value, nodeStates));
  }

  private void addOrReplace(StateDefinition<T> definition) {
    this.definitions.remove(definition);
    this.definitions.add(definition);
  }

  @Override
  public final void setTransition(Transition transition) {
    this.transition = transition;
  }

  @Override
  public void defineState(String value, int specificity, Set<StyleNodeState> nodeStates) {
    nodeStates.stream()
        .map(StyleNodeState::node)
        .filter(this.trackedNodes::add)
        .forEach(node -> node.getStyleManager().addListener(this));
    this.defineState(specificity,
        this.styleCache.computeIfAbsent(value, this.parser::parse), nodeStates);
  }

  @Override
  public final void refreshState() {
    for (var definition : this.definitions) {
      if (!definition.nodeStates().stream().allMatch(StyleNodeState::check)) {
        continue;
      }

      if (this.transitionStopListener != null) {
        this.transitionStopListener.run();
      }

      var newValue = definition.value();
      if (!newValue.equals(this.getDirect())) {
        if (this.owner.isVisible()) {
          this.transitionStopListener = this.transition.transition(this, newValue);
        } else {
          this.set(newValue);
        }
      }
      return;
    }
  }

  @Override
  public final void reset() {
    this.trackedNodes.forEach(node -> node.getStyleManager().removeListener(this));
    this.trackedNodes.clear();
    this.definitions.clear();
    this.order = 0;
    this.addOrReplace(this.baseDefinition);
  }

  @Override
  public int validate(String style) {
    return this.parser.validate(style);
  }

  public record StateDefinition<T> (int order, int specificity, T value,
      Set<StyleNodeState> nodeStates) implements Comparable<StateDefinition<T>> {

    @Override
    public int compareTo(StateDefinition<T> o) {
      /*
       * Order of comparison: State count -> specificity -> node states -> order
       */

      var result = Integer.compare(o.nodeStates.size(), this.nodeStates.size());
      if (result != 0) {
        return result;
      }

      result = Integer.compare(o.specificity, this.specificity);
      if (result != 0) {
        return result;
      }

      if (!this.nodeStates.equals(o.nodeStates)) {
        return -1;
      }

      return Integer.compare(o.order, this.order);
    }
  }
}
