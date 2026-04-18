package com.cnpmnc.document_management.shared;

import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserUtils {
  private CurrentUserUtils() {}

  public static String getUserId() {
    return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
  }

}
