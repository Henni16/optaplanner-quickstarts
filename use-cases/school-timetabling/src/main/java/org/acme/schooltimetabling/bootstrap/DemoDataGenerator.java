package org.acme.schooltimetabling.bootstrap;

import io.quarkus.runtime.StartupEvent;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    DemoData demoData;

    @Inject
    RoomRepository roomRepository;

    @Inject
    TimeslotRepository timeslotRepository;
    @Inject
    LessonRepository lessonRepository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        timeslotRepository.persist(generateTimeslots());

        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room("Raum"));
        roomRepository.persist(rooms);


        lessonRepository.persist(generateLessons());
    }

    public static List<Timeslot> generateTimeslots() {
        var startDate = LocalDate.of(2023, 2, 13);
        var endDate = LocalDate.of(2023, 4, 6);

        var blockedDays = new HashSet<LocalDate>();
        blockedDays.add(LocalDate.of(2023, 3, 8));
        blockedDays.add(LocalDate.of(2023, 3, 24));
        blockedDays.add(LocalDate.of(2023, 3, 17));
        blockedDays.add(LocalDate.of(2023, 3, 27));
        blockedDays.add(LocalDate.of(2023, 4, 6));
        var blockedSlots = new HashSet<Timeslot>();
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 15), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 15), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 23), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 23), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 28), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 28), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 29), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 29), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 3), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 31), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 31), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 4, 5), LocalTime.of(12, 15), LocalTime.of(13, 45)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 4, 5), LocalTime.of(14, 0), LocalTime.of(15, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 17), LocalTime.of(8, 0), LocalTime.of(9, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 3, 17), LocalTime.of(10, 0), LocalTime.of(11, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 4, 3), LocalTime.of(8, 0), LocalTime.of(9, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 2, 13), LocalTime.of(8, 0), LocalTime.of(9, 30)));
        blockedSlots.add(new Timeslot(LocalDate.of(2023, 4, 3), LocalTime.of(10, 0), LocalTime.of(11, 30)));

        List<Timeslot> timeslotList = new ArrayList<>();
        timeslotList.add(new Timeslot(LocalDate.of(2023, 3, 8), LocalTime.of(8, 0), LocalTime.of(9, 30)));
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            if (blockedDays.contains(date) || date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            timeslotList.add(new Timeslot(date, LocalTime.of(8, 0), LocalTime.of(9, 30)));
            timeslotList.add(new Timeslot(date, LocalTime.of(10, 0), LocalTime.of(11, 30)));
            timeslotList.add(new Timeslot(date, LocalTime.of(12, 15), LocalTime.of(13, 45)));
            if (date.getDayOfWeek() != DayOfWeek.FRIDAY || date.get(ChronoField.ALIGNED_WEEK_OF_YEAR) % 2 == 1) {
                timeslotList.add(new Timeslot(date, LocalTime.of(14, 0), LocalTime.of(15, 30)));
            }
        }
        timeslotList.removeAll(blockedSlots);
        return timeslotList;
    }

    public static List<Lesson> generateLessons() {
        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(new Lesson("2.5 Klausur", "Norbert", "Chaosclub"));
        lessonList.add(new Lesson("3.5 Klausur", "Norbert", "Chaosclub"));
        addLessons(lessonList, 13, "2.6 a", "Sonja");
        addLessons(lessonList, 6, "2.6 b", "Isabelle");
        addLessons(lessonList, 15, "3.6", "Martin");
        addLessons(lessonList, 8, "3.7", "Barbel");
        addLessons(lessonList, 14, "4.3", "Julia");
        addLessons(lessonList, 9, "9.4", "Norbert");
        addLessons(lessonList, 7, "10.2", "Susanne");
        addLessons(lessonList, 2, "13.4", "Susanne");
        addLessons(lessonList, 2, "11.5", "Elena");
        addLessons(lessonList, 12, "11.7", "Isabelle");
        addLessons(lessonList, 10, "12.1 b", "Michael");
        addLessons(lessonList, 11, "13.3", "Andrea");
        return lessonList;
    }

    private static void addLessons(List<Lesson> lessonList, int num, String subject, String teacher) {
        for (int i = 0; i < num; i++) {
            lessonList.add(new Lesson(subject, teacher, "Chaosclub"));
        }
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
