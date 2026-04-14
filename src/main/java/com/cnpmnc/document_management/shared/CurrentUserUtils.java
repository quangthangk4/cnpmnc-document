package com.cnpmnc.document_management.shared;

import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserUtils {
  private CurrentUserUtils() {}

  public static String getUserId() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

}
