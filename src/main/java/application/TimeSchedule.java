package application;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class TimeSchedule {
 private LocalDateTime scheduledDateTime;
 private LocalDateTime currentDateTime;

 public Long scheduledOneTime(String primaryKey, String dateTime) {
  scheduledDateTime = LocalDateTime.parse(dateTime);
  currentDateTime = LocalDateTime.now();
  if (scheduledDateTime.isAfter(currentDateTime)) {
   return calculateDuration(primaryKey, dateTime);
  }
  return null;
 }

 public Long scheduleWeekly(String primaryKey, String day, String time) {
  currentDateTime = LocalDateTime.now();
  DayOfWeek dayOfWeek = convertStringToDayOfWeek(day);
  LocalDateTime now = LocalDateTime.now();
  scheduledDateTime = now.with(TemporalAdjusters.nextOrSame(dayOfWeek)).with(LocalTime.parse(time));
  // 指定の時刻がすでに過ぎている場合、次の週の同じ曜日にスケジュール
  if (now.isAfter(scheduledDateTime)) {
   scheduledDateTime = scheduledDateTime.plusWeeks(1);
  }
  return calculateDuration(primaryKey, scheduledDateTime);
 }

 DayOfWeek convertStringToDayOfWeek(String day) {
  switch (day) {
  case "日":
   return DayOfWeek.SUNDAY;
  case "月":
   return DayOfWeek.MONDAY;
  case "火":
   return DayOfWeek.TUESDAY;
  case "水":
   return DayOfWeek.WEDNESDAY;
  case "木":
   return DayOfWeek.THURSDAY;
  case "金":
   return DayOfWeek.FRIDAY;
  case "土":
   return DayOfWeek.SATURDAY;
  default:
   throw new IllegalArgumentException("Invalid day:" + day);
  }
 }

 private long calculateDuration(String primaryKey, String dateTime) {
  Duration duration = Duration.between(currentDateTime, scheduledDateTime);
  return duration.toMillis();
 }

 private long calculateDuration(String primaryKey, LocalDateTime dateTime) {
  Duration duration = Duration.between(currentDateTime, scheduledDateTime);
  return duration.toMillis();
 }

}
