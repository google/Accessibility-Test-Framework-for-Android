package com.google.android.apps.common.testing.accessibility.framework.uielement;

/** Represents the type of element that a ViewHierarchyElement represents. */
public enum ViewHierarchyElementOrigin {
  UNKNOWN,
  /* From Composeable content */
  COMPOSE,
  /* From Flutter content */
  FLUTTER,
  /* From web content */
  WEB,
  /* From a View object that is not COMPOSE or WEB */
  VIEW;
}
