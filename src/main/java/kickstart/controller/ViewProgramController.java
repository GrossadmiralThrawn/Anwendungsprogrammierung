/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kickstart.controller;

import kickstart.models.CinemaShow;
import kickstart.repository.CinemaShowRepository;
import org.springframework.data.util.Streamable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Jannik
 */
@Controller
public class ViewProgramController {

	private CinemaShowRepository cinemaShowRepository;

	public ViewProgramController(CinemaShowRepository cinemaShowRepository) {
		this.cinemaShowRepository = cinemaShowRepository;
	}

    /**
     * todo: where rights check?
     * @param week
     * @param m 
     */
    @GetMapping("/current-films/{year}/{week}")
    public String getCurrentProgram(@PathVariable int year, @PathVariable int week , Model m) {

		LocalDateTime startDateTime = getStartWeekDateTime(year, week);
		LocalDate dayDate;
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		List<CinemaShowDayEntry> oneWeekCinemaShows = new ArrayList<>();

		m.addAttribute("weekRangeFormat", getWeekRangeFormat(year, week));
		// TODO: effizienter umsetzen:
		// Alle Wochentage einzeln behandeln
		for(int i = 1; i <= 7; i++) {
			dayDate = startDateTime.with(weekFields.dayOfWeek(), i).toLocalDate();
			oneWeekCinemaShows.add(
				new CinemaShowDayEntry(dayDate, this.cinemaShowRepository.findCinemaShowsOnDay(dayDate))
			);
		}
		m.addAttribute("oneWeekCinemaShows", oneWeekCinemaShows);

		//System.out.println("Start der Woche: " + getStartWeekDateTime(year, week));
		//System.out.println("Ende der Woche: " + getEndWeekDateTime(year, week));

		return "current-films-renderer";
	}

	public static class CinemaShowDayEntry {
		private Streamable<CinemaShow> cinemaShows;
		private String dayDateHeadline;

		/**
		 *
		 * @param dayDate Datum, für alle Veranstaltungen welche an dem Tag laufen (Startzeit).
		 * @param cinemaShows alle Veranstaltungen an einem Tag
		 */
		public CinemaShowDayEntry(LocalDate dayDate, Streamable<CinemaShow> cinemaShows) {
			// Verhindert Fehler bei nicht vorhandenen Kino-Veranstaltungen an dem Tag
			if(cinemaShows == null) cinemaShows = Streamable.empty();
			this.cinemaShows = cinemaShows;
			this.dayDateHeadline = getDayFormat(dayDate);
		}

		public Streamable<CinemaShow> getCinemaShows() {
			return this.cinemaShows;
		}

		/**
		 * @return Erhalte Datums-Überschrift-Eintrag
		 */
		public String getDayDateHeadline() {
			return this.dayDateHeadline;
		}
	}

	// Helper Methoden
	// TODO: Später in einen Service verschieben

	/**
	 * @return max. Anzahl an Kalenderwochen, welches ein Jahr hat.
	 * Es zählen auch die erste und die letzte Woche, auch wenn diese nicht vollständig in dem Jahr liegen.
	 */
	public static int getMaxYearWeeks(int year) {
		return getWeekOfYear(LocalDateTime.of(year, 12, 31, 0, 0));
	}

	/**
	 * @return aktuelle Kalenderwoche eines Jahres
	 */
	public static int getWeekOfYear(LocalDateTime dateTime) {
		return dateTime
			.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
	}

	// Chat GPT 3.5
	// Promt: Wie bekomme ich das Start-Datum zu einer bestimmten Woche?

	/**
	 *
	 * @param year das Jahr
	 * @param week die Kalenderwoche zum angegebenen Jahr
	 * @return Startdatum der Woche (Montag um 00:00)
	 */
	public static LocalDateTime getStartWeekDateTime(int year, int week) {
		WeekFields weekFields = WeekFields.of(Locale.getDefault());

		return LocalDateTime.of(year, 1, 1, 0, 0)
			.with(weekFields.weekOfYear(), week)
			.with(weekFields.dayOfWeek(), 1);
	}

	/**
	 * @param dateTime beliebiges Datum (+ Zeitpunk)
	 * siehe {@link #getStartWeekDateTime(int, int)}
	 */
	public static LocalDateTime getStartWeekDateTime(LocalDateTime dateTime) {
		return getStartWeekDateTime(
			dateTime.getYear(),
			getWeekOfYear(dateTime)
		);
	}

	/**
	 *
	 * @param year das Jahr
	 * @param week die Kalenderwoche zum angegebenen Jahr
	 * @return Enddatum der Woche (Sonntag um 23:59)
	 */
	public static LocalDateTime getEndWeekDateTime(int year, int week) {
		WeekFields weekFields = WeekFields.of(Locale.getDefault());

		return LocalDateTime.of(year, 1, 1, 23, 59)
			.with(weekFields.weekOfYear(), week)
			.with(weekFields.dayOfWeek(), 7);
	}

	/**
	 * @param dateTime beliebiges Datum (+ Zeitpunk)
	 * siehe {@link #getEndWeekDateTime(int, int)}
	 */
	public static LocalDateTime getEndWeekDateTime(LocalDateTime dateTime) {
		return getEndWeekDateTime(
			dateTime.getYear(),
			getWeekOfYear(dateTime)
		);
	}

	/**
	 * @return Ausgabe String: "Woche-Von-Datum (dd.MM.yyyy) - Woche-Bis-Datum (dd.MM.yyyy)"
	 */
	public static String getWeekRangeFormat(int year, int week) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());

		String formattedStartDate = getStartWeekDateTime(year, week).format(formatter);
		String formattedEndDate = getEndWeekDateTime(year, week).format(formatter);

		return formattedStartDate + " - " + formattedEndDate;
	}
	/**
	 * @param dateTime beliebiges Datum (+ Zeitpunk)
	 * siehe {@link #getWeekRangeFormat(int, int)}
	 */
	public static String getWeekRangeFormat(LocalDateTime dateTime) {
		return getWeekRangeFormat(dateTime.getYear(), getWeekOfYear(dateTime));
	}

	/**
	 * @return Ausgabe String: "Wochentag, Datum (dd.MM.yyyy)"
	 */
	public static String getDayFormat(LocalDate date) {
		// EEEE: Wochentag, Sprache hängt von Locale.getDefault() ab
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.getDefault());
		return date.format(formatter);
	}
}
