package org.keycloak.models.map.common;

public interface ExpirableEntity {

  String getId();

  void setId(String id);

  /**
   * Returns a point in the time (timestamp in milliseconds since The Epoch) when
   * this entity expires.
   *
   * @return a timestamp in milliseconds since The Epoch or {@code null} if this
   *         entity never expires
   *         or expiration is not known.
   */
  Long getExpiration();

  /**
   * Sets a point in the time (timestamp in milliseconds since The Epoch) when
   * this entity expires.
   *
   * @param expiration a timestamp in milliseconds since The Epoch or {@code null}
   *                   if this entity never expires.
   */
  void setExpiration(Long expiration);
}
