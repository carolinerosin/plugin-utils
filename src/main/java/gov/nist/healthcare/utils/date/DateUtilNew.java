package gov.nist.healthcare.utils.date;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class DateUtilNew {

  // private enum Precision {
  // YEAR, MONTH, DAY, HOUR, MINUTES, SECONDS, MILLIS_1, MILLIS_2, MILLIS_3, MILLIS_4, UNDEFINED
  // };

  private final static String YEAR = "(\\d{4})";
  private final static String MONTH = "(\\d{2})";
  private final static String DAY = "(\\d{2})";
  private final static String HOUR = "(\\d{2})";
  private final static String MINUTES = "(\\d{2})";
  private final static String SECONDS = "(\\d{2})";
  private final static String MILLISECONDS = "(?:\\.(\\d{1,4}))"; // separate
                                                                  // the
                                                                  // last
                                                                  // one
  private final static String TIMEZONE_OFFSET = "((?:\\+|\\-)\\d{4})";

  // YYYY[MM[DD[HH[MM[SS[.S[S[S[S]]]]]]]]][+/-ZZZZ]
  private final static String DTM =
      YEAR + "(?:" + MONTH + "(?:" + DAY + "(?:" + HOUR + "(?:" + MINUTES + "(?:" + SECONDS + "(?:"
          + MILLISECONDS + ")?)?)?)?)?)?" + "(?:" + TIMEZONE_OFFSET + ")?";


  public static boolean isValid(String dtm, String defaultTz) {
    try {
      parseDTM(dtm, defaultTz);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Checks if two DTM can be compared.
   * 
   * @param dtm1 first dtm to compare
   * @param dtm2 second dtm to compare
   * @return true when the two DTMs have the same granularity
   */
  public static boolean canCompare(String dtm1, String dtm2, String defaultTz) {
    // check for format
    if (!isValid(dtm1, defaultTz) || !isValid(dtm2, defaultTz)) {
      return false;
    }
    String dt1 = RegExUtils.removePattern(dtm1, "(\\+|\\-).*");
    String tz1 = StringUtils.remove(dtm1, dt1);

    String dt2 = RegExUtils.removePattern(dtm2, "(\\+|\\-).*");
    String tz2 = StringUtils.remove(dtm2, dt2);

    // we can only compare DTMs if both have a timezone offset or if both do
    // not have a timezone offset
    boolean timezoneCheck = tz1.length() == tz2.length();

    // granularity check : both DTMs must represent the same "concept"
    boolean granularityCheck = dt1.length() == dt2.length();

    // if granularity is YYYY or YYYYMM or YYYYMMDD and the time zones are
    // different, we don't compare
    boolean coherenceCheck = true;
    if (!tz1.equals(tz2) && (dt1.length() == 4 || dt1.length() == 6 || dt1.length() == 8)) {
      coherenceCheck = false;
    }

    return timezoneCheck && granularityCheck && coherenceCheck;
  }

  public static OffsetDateTime parseDTM(String dtm, String defaultTz) {
    Pattern p = Pattern.compile(DTM);
    Matcher m = p.matcher(dtm);
    if (m.matches()) {
      String year = m.group(1) != null ? m.group(1) : "0000";
      String month = m.group(2) != null ? m.group(2) : "01";
      String day = m.group(3) != null ? m.group(3) : "01";
      String hours = m.group(4) != null ? m.group(4) : "00";
      String minutes = m.group(5) != null ? m.group(5) : "00";
      String seconds = m.group(6) != null ? m.group(6) : "00";
      String millis = m.group(7) != null ? m.group(7) : "0000";
      String tz = m.group(8) != null ? m.group(8) : defaultTz;

      // timezone in OffsetDateTime is represented as "+/-ZZ:ZZ"
      // timezone in HL7 is represented as +/-ZZZZ

      tz = StringUtils.join(StringUtils.left(tz, 3), ":", StringUtils.right(tz, 2));

      String d = StringUtils.join(Arrays.asList(year, month, day), "-");
      String sm = StringUtils.join(Arrays.asList(seconds, millis), ".");
      String t = StringUtils.join(Arrays.asList(hours, minutes, sm), ":");
      String date = StringUtils.join(Arrays.asList(d, t), "T");

      OffsetDateTime result = OffsetDateTime.parse(StringUtils.join(date, tz));

      return result;
    }
    throw new IllegalArgumentException(
        String.format("The value '%s' is not a valid date/time format", dtm));
  }

  public static void main(String[] args) {

    // parse year only - timezone is set to default (-05:00)
    System.out.println(parseDTM("2014", "-05:00"));

    // default timezone is ignored
    System.out.println(parseDTM("2014+0100", ""));
    System.out.println(parseDTM("201412+0100", ""));
    System.out.println(parseDTM("20141209+0100", ""));

    System.out.println(parseDTM("2014120915+0100", ""));

    // both are valid
    System.out.println(isValid("20141209033852.123", "-05:00"));
    System.out.println(isValid("20141209033852.1234", "-05:00"));
    System.out.println(isValid("20141209033852.1234", ""));

    // the parser will ignore the last digit in the second case. Result from
    // conversion : 2014-12-09T03:38:52.123-05:00
    System.out.println(parseDTM("20141209033852.123", "-05:00"));
    System.out.println(parseDTM("20141209033852.1234", "-05:00"));

    System.out.println(canCompare("2015", "201512", "-05:00")); // false
    System.out.println(canCompare("2015", "2015", "-05:00"));// true
    System.out.println(canCompare("2015+0500", "2015", "-05:00"));// false
    System.out.println(canCompare("2015+0500", "2015+0400", "-05:00"));// false
    System.out.println(canCompare("2015+0500", "2015+0500", "-05:00"));// true



    System.out.println(parseDTM("2019121714-0600", "-05:00"));
    System.out.println(parseDTM("2019121713-0500", "-05:00"));

    // joda can parse that
    // DateTime.parse("20141209033852.123");
    // joda cannot parse that (java.lang.IllegalArgumentException: Invalid
    // format)
    // DateTime.parse("20141209033852.1234");

    // ExtendedDate dtm1 = toExtendedDate("201812120400-0400");
    // ExtendedDate dtm2 = toExtendedDate("201812120300-0500");
    //
    // System.out.println(dtm1.getJodaDate());
    // System.out.println(dtm2.getJodaDate());
    //
    // System.out.println(dtm1.isEqual(dtm2));
    // System.out.println(dtm1.isBefore(dtm2));
    // System.out.println(dtm1.isAfter(dtm2));

  }
}
