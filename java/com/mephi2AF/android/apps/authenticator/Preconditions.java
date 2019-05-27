

package com.mephi2AF.android.apps.authenticator;


public final class Preconditions {

  /** Hidden to avoid instantiation. */
  private Preconditions() {}


  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @throws IllegalArgumentException if {@code expression} is {@code false}.
   */
  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }


  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }


  public static void checkState(
      boolean expression, Object errorMessage) {
    if (!expression) {
      throw new IllegalStateException(String.valueOf(errorMessage));
    }
  }
}
