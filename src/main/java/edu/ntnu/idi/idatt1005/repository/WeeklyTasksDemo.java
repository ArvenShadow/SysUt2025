package edu.ntnu.idi.idatt1005.repository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Demo class showing how to use the weekly task completion functions
 * Updated to properly handle Norwegian timezone and week calculations
 */
public class WeeklyTasksDemo {
  public static void main(String[] args) {
    try {
      // Assume we have a user with ID 1
      int userId = 1;

      // Create repository
      StatisticsRepository repository = new StatisticsRepository();

      // Get the current date in Norwegian timezone
      LocalDate now = LocalDate.now(ZoneId.of("Europe/Oslo"));
      System.out.println("Current date in Norway: " + now);

      // Get tasks completed this week
      int tasksThisWeek = repository.getTasksCompletedThisWeek(userId);

      // Use Norwegian locale for week calculations
      Locale norwegianLocale = new Locale("nb", "NO");
      WeekFields weekFields = WeekFields.of(norwegianLocale);
      int currentWeek = now.get(weekFields.weekOfYear());

      System.out.println("Current week: " + currentWeek);
      System.out.println("Tasks completed this week: " + tasksThisWeek);

      // Get tasks completed by week for the last 4 weeks INCLUDING current week
      int[][] weeklyStats = repository.getWeeklyTaskCompletions(userId, 4);

      // Debug the array contents
      System.out.println("\nWeekly stats array contents:");
      for (int i = 0; i < weeklyStats.length; i++) {
        System.out.println("Index " + i + ": Week " + weeklyStats[i][0] + " - Count " + weeklyStats[i][1]);
      }

      // Prepare formatter for dates
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d").withLocale(norwegianLocale);

      System.out.println("\nWeekly task completions for last 4 weeks:");
      System.out.println("Week | Week Start Date | Tasks Completed");
      System.out.println("--------------------------------------");

      // Calculate correct start date for each week number directly
      int year = now.getYear();

      for (int i = 0; i < weeklyStats.length; i++) {
        int weekNumber = weeklyStats[i][0];
        int count = weeklyStats[i][1];

        // Create a date for this week number using the locale's week definition
        // This ensures the week number and date match exactly
        LocalDate firstDayOfWeek = LocalDate.now()
          .withYear(year)
          .with(weekFields.weekOfYear(), weekNumber)
          .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // If the week number is larger than current week, it's from previous year
        if (weekNumber > currentWeek && i < weeklyStats.length / 2) {
          firstDayOfWeek = firstDayOfWeek.withYear(year - 1);
        }

        System.out.printf("%4d | %s | %d\n",
          weekNumber,
          firstDayOfWeek.format(formatter),
          count);
      }

    } catch (SQLException e) {
      System.err.println("Database error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}