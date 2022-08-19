package com.depsdoctor.github.action.utils;

/**
 * String utilities
 */
public class Strings {

  public static boolean isBlank(String text) {
    if(text == null) return true;
    return text.trim().length() == 0;
  }

}
