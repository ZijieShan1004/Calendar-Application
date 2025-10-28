package model;

import java.time.LocalDateTime;

/**
 * Represents a single calendar event (possibly part of a recurring series).
 */
public class Event {
  private String subject;
  private String description;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private String location;
  private Status status;
  private Integer seriesId;       // null if not part of any series
  private int occurrenceIndex;    // index within its series (0-based)

  /**
   * Builder pattern for Event construction.
   */
  public static class Builder {
    private String subject;
    private String description = "";
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location = "";
    private Status status = Status.PUBLIC;
    private Integer seriesId = null;
    private int occurrenceIndex = 0;

    /**
     * Sets the subject of the event.
     *
     * @param s the subject text
     * @return this Builder instance
     */
    public Builder subject(String s) {
      this.subject = s;
      return this;
    }

    /**
     * Sets the description of the event.
     *
     * @param d the description text
     * @return this Builder instance
     */
    public Builder description(String d) {
      this.description = d;
      return this;
    }

    /**
     * Sets the start date and time for the event.
     *
     * @param sdt the LocalDateTime when the event starts
     * @return this Builder instance
     */
    public Builder startDateTime(LocalDateTime sdt) {
      this.startDateTime = sdt;
      return this;
    }

    /**
     * Sets the end date and time for the event.
     *
     * @param edt the LocalDateTime when the event ends
     * @return this Builder instance
     */
    public Builder endDateTime(LocalDateTime edt) {
      this.endDateTime = edt;
      return this;
    }

    /**
     * Sets the location for the event.
     *
     * @param loc the location string
     * @return this Builder instance
     */
    public Builder location(String loc) {
      this.location = loc;
      return this;
    }

    /**
     * Sets the visibility status of the event.
     *
     * @param st the Status enum value
     * @return this Builder instance
     */
    public Builder status(Status st) {
      this.status = st;
      return this;
    }

    /**
     * Associates this event with a recurring series ID.
     *
     * @param sid the series identifier, or null if none
     * @return this Builder instance
     */
    public Builder seriesId(Integer sid) {
      this.seriesId = sid;
      return this;
    }

    /**
     * Sets the occurrence index within a recurring series.
     *
     * @param idx zero-based index of this occurrence
     * @return this Builder instance
     */
    public Builder occurrenceIndex(int idx) {
      this.occurrenceIndex = idx;
      return this;
    }

    /**
     * Builds an Event instance from the configured builder fields.
     * Requires subject and startDateTime to be non-null.
     *
     * @return a new Event object
     * @throws IllegalStateException if required fields are missing
     */
    public Event build() {
      if (subject == null || startDateTime == null) {
        throw new IllegalStateException("subject, startDateTime");
      }
      return new Event(this);
    }
  }

  private Event(Builder b) {
    this.subject = b.subject;
    this.description = b.description;
    this.startDateTime = b.startDateTime;
    this.endDateTime = b.endDateTime;
    this.location = b.location;
    this.status = b.status;
    this.seriesId = b.seriesId;
    this.occurrenceIndex = b.occurrenceIndex;
  }

  /**
   * Gets the subject of this event.
   *
   * @return the event subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Updates the subject of this event.
   *
   * @param subject the new subject text
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Gets the description of this event.
   *
   * @return the event description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Updates the description of this event.
   *
   * @param description the new description text
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the starting LocalDateTime of this event.
   *
   * @return the start date-time
   */
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  /**
   * Updates the starting LocalDateTime of this event.
   *
   * @param startDateTime the new start date-time
   */
  public void setStartDateTime(LocalDateTime startDateTime) {
    this.startDateTime = startDateTime;
  }

  /**
   * Gets the ending LocalDateTime of this event.
   *
   * @return the end date-time
   */
  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  /**
   * Updates the ending LocalDateTime of this event.
   *
   * @param endDateTime the new end date-time
   */
  public void setEndDateTime(LocalDateTime endDateTime) {
    this.endDateTime = endDateTime;
  }

  /**
   * Gets the location of this event.
   *
   * @return the event location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Updates the location of this event.
   *
   * @param location the new location string
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Gets the visibility status of this event.
   *
   * @return the event Status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Updates the visibility status of this event.
   *
   * @param status the new Status
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Gets the series ID if this event is part of a recurring series.
   *
   * @return the series ID, or null if not part of a series
   */
  public Integer getSeriesId() {
    return seriesId;
  }

  /**
   * Updates the series ID for this event.
   *
   * @param seriesId the new series identifier
   */
  public void setSeriesId(Integer seriesId) {
    this.seriesId = seriesId;
  }

  /**
   * Gets the occurrence index within its recurring series.
   *
   * @return the zero-based occurrence index
   */
  public int getOccurrenceIndex() {
    return occurrenceIndex;
  }

  /**
   * Updates the occurrence index within its recurring series.
   *
   * @param occurrenceIndex the new zero-based index
   */
  public void setOccurrenceIndex(int occurrenceIndex) {
    this.occurrenceIndex = occurrenceIndex;
  }

  /**
   * Compares this event to another for equality based on key fields.
   *
   * @param o the object to compare with
   * @return true if both events have the same subject, times, and series info
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event)) {
      return false;
    }
    Event other = (Event) o;
    return subject.equals(other.subject)
            && startDateTime.equals(other.startDateTime)
            && endDateTime.equals(other.endDateTime)
            && ((seriesId == null && other.seriesId == null)
            || (seriesId != null && seriesId.equals(other.seriesId)
            && occurrenceIndex == other.occurrenceIndex));
  }

  /**
   * Computes a hash code based on subject, times, and series details.
   *
   * @return the computed hash code
   */
  @Override
  public int hashCode() {
    int result = subject.hashCode();
    result = 31 * result + startDateTime.hashCode();
    result = 31 * result + endDateTime.hashCode();
    if (seriesId != null) {
      result = 31 * result + seriesId.hashCode();
      result = 31 * result + occurrenceIndex;
    }
    return result;
  }
}
