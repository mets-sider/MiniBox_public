package application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class JsonTimeAndDays {
 private LocalDateTime dateTime;
 private LocalDate date;
 private LocalTime time;
 private Map<String, Boolean> days;

 public LocalDate getDate() {
  return date;
 }

 public void setDate(LocalDate date) {
  this.date = date;
 }

 public void setDate(LocalDateTime dateTime) {
  this.dateTime = dateTime;
 }

 public LocalTime getTime() {
  return time;
 }

 public void setTime(LocalTime time) {
  this.time = time;
 }

 public Map<String, Boolean> getDays() {
  return days;
 }

 public void setDays(Map<String, Boolean> days) {
  this.days = days;
 }

}
