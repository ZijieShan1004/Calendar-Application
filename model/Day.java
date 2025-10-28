package model;

/**
 * Day-of-week abbreviations for recurring events.
 */
public enum Day {
  M("Monday"),
  T("Tuesday"),
  W("Wednesday"),
  R("Thursday"),
  F("Friday"),
  S("Saturday"),
  U("Sunday");

  private final String fullName;

  Day(String fullName) {
    this.fullName = fullName;
  }

  public String getFullName() {
    return fullName;
  }

  /**
   * Convert a single-character abbreviation to the Day enum.
   *
   * @param ch one of: M, T, W, R, F, S, U
   * @return corresponding Day
   * @throws IllegalArgumentException if invalid char
   */
  public static Day fromAbbreviation(char ch) {
    switch (ch) {
      case 'M':
        return M;
      case 'T':
        return T;
      case 'W':
        return W;
      case 'R':
        return R;
      case 'F':
        return F;
      case 'S':
        return S;
      case 'U':
        return U;
      default:
        throw new IllegalArgumentException("Invalid day abbreviation: " + ch);
    }
  }
}
