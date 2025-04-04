
package de.ufo.cinemasystem.controller;

import de.ufo.cinemasystem.additionalfiles.AdditionalDateTimeWorker;
import de.ufo.cinemasystem.models.*;
import de.ufo.cinemasystem.repository.ReservationRepository;
import de.ufo.cinemasystem.services.CinemaShowService;
import de.ufo.cinemasystem.services.ScheduledActivityService;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.ufo.cinemasystem.repository.CinemaHallRepository;
import de.ufo.cinemasystem.repository.CinemaShowRepository;
import de.ufo.cinemasystem.repository.FilmRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.salespointframework.core.Currencies.EURO;

/**
 *Spring MVC Controller for viewing and altering the current program.
 * @author Jannik Schwaß
 * @author Yannick Harnisch
 */
@Controller
public class ViewProgramController {

	private CinemaShowRepository cinemaShowRepository;

	private CinemaShowService cinemaShowService;

	private CinemaHallRepository cinemaHallRepository;

	private FilmRepository filmRepository;

	private ScheduledActivityService scheduledActivityService;

	@Autowired
	private DeleteReservationController deleteReservationController;
	@Autowired
	private ReservationRepository reservationRepository;

        /**
         * Construct a new ViewProgramController with the specified autowired dependencies.
         * @param cinemaShowRepository Implementation of the CinemaShow-Repository
         * @param cinemaShowService CinemaShowService
         * @param cinemaHallRepository Implementation of the CinemaHall-Repository
         * @param filmRepository Implementation of the Film-Repository
         * @param scheduledActivityService ScheduledActivityService
         */
	public ViewProgramController(CinemaShowRepository cinemaShowRepository, CinemaShowService cinemaShowService,
								 CinemaHallRepository cinemaHallRepository, FilmRepository filmRepository,
								 ScheduledActivityService scheduledActivityService) {
		this.cinemaShowRepository = cinemaShowRepository;
		this.cinemaShowService = cinemaShowService;
		this.cinemaHallRepository = cinemaHallRepository;
		this.filmRepository = filmRepository;
		this.scheduledActivityService = scheduledActivityService;
	}
        
        /**
         * GET-Mapping redirecting to the current week.
         * @param m model
         * @return a string describing the redirect.
         */
        @GetMapping("/current-films")
        public String getCurrentWeekProgram(Model m){
            LocalDateTime now = LocalDateTime.now();
			return "redirect:/current-films/" + now.getYear() + "/" + AdditionalDateTimeWorker.getWeekOfYear(now);
        }

    /**
     * GET-Endpoint for viewing the program in the specified year and week.
     * todo: where rights check?
     * @param year anzuzeigendes Jahr
     * @param week anzuzeigende Jahreswoche
     * @param m Modell
     * @return "current-films"
     */
    @GetMapping("/current-films/{year}/{week}")
    public String getCurrentProgram(@PathVariable int year, @PathVariable int week , Model m) {

		LocalDateTime startDateTime = AdditionalDateTimeWorker.getStartWeekDateTime(year, week);
		LocalDate dayDate;
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		List<CinemaShowDayEntry> oneWeekCinemaShows = new ArrayList<>();

		int[] nextYearWeek = AdditionalDateTimeWorker.nextWeek(year, week);
		int[] lastYearWeek = AdditionalDateTimeWorker.lastWeek(year, week);

		m.addAttribute("lastWeekRangeFormat", AdditionalDateTimeWorker.getWeekRangeFormat(lastYearWeek[0], lastYearWeek[1]));
		m.addAttribute("lastYear", lastYearWeek[0]);
		m.addAttribute("lastWeek", lastYearWeek[1]);

		m.addAttribute("currentWeekRangeFormat", AdditionalDateTimeWorker.getWeekRangeFormat(year, week));

		m.addAttribute("nextWeekRangeFormat", AdditionalDateTimeWorker.getWeekRangeFormat(nextYearWeek[0], nextYearWeek[1]));
		m.addAttribute("nextYear", nextYearWeek[0]);
		m.addAttribute("nextWeek", nextYearWeek[1]);

		// TODO: Sortierung nach Zeit an einem Tag korrekt implementieren
		// TODO: effizienter umsetzen:
		// Alle Wochentage einzeln behandeln
		for(int i = 1; i <= 7; i++) {
			dayDate = startDateTime.with(weekFields.dayOfWeek(), i).toLocalDate();
			oneWeekCinemaShows.add(
				new CinemaShowDayEntry(dayDate, this.cinemaShowRepository.findCinemaShowsOnDay(dayDate, Sort.sort(CinemaShow.class).by(CinemaShow::getStartDateTime).ascending()))
			);
		}
		m.addAttribute("oneWeekCinemaShows", oneWeekCinemaShows);
		m.addAttribute("allCinemaHalls", cinemaHallRepository.findAll());
		m.addAttribute("allFilms", filmRepository.findAll());
                m.addAttribute("title","Filmplan");

		//System.out.println("Start der Woche: " + getStartWeekDateTime(year, week));
		//System.out.println("Ende der Woche: " + getEndWeekDateTime(year, week));

		return "current-films";
	}

    /**
     * POST-Endpunkt: neue Vorführung
     * @param redirectAttributes Redirect-Modell
     * @param year Jahr
     * @param week Jahreswoche
     * @param film Kinofilm
     * @param room Saal
     * @param addTime Startzeit
     * @return redirect-View
     */
	@PreAuthorize("hasAnyRole('BOSS', 'AUTHORIZED_EMPLOYEE')")
	@PostMapping("/current-films/{year}/{week}")
	public String postNewProgram(RedirectAttributes redirectAttributes,
								 @PathVariable int year, @PathVariable int week,
								 @RequestParam("film") Long film, @RequestParam("room") Long room,
								 //ChatGPT 3.5
								 // Promt: Wie kann ich aus einem input Feld vom type="datetime-local ein LocalDateTime Objekt machen?
								 @RequestParam("addTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime addTime) {

		Optional<Film> optFilmInst = filmRepository.findById(film);
		Optional<CinemaHall> optRoomInst = cinemaHallRepository.findById(room);
		if(optFilmInst.isEmpty()) {
			// Dieser Fehler sollte im Regelfall nicht auftreten!
			redirectAttributes.addFlashAttribute("errorMessage", "Der Film existiert nicht!");
			return "redirect:/current-films/{year}/{week}";
		}
		if(optRoomInst.isEmpty()) {
			// Dieser Fehler sollte im Regelfall nicht auftreten!
			redirectAttributes.addFlashAttribute("errorMessage", "Der Kinosaal existiert nicht!");
			return "redirect:/current-films/{year}/{week}";
		}

		Film filmInst = optFilmInst.get();
		CinemaHall cinemaHallInst = optRoomInst.get();

		if(addTime.isBefore(LocalDateTime.now().plusHours(1))) {
			redirectAttributes.addFlashAttribute("errorMessage",
				"Die Vorführung muss mind. 1 Stunde in der Zukunft liegen!");
			return "redirect:/current-films/{year}/{week}";
		}

		if(!filmInst.isAvailableAt(addTime)) {
			redirectAttributes.addFlashAttribute("errorMessage",
				"Dieser Film wurde in der Zeit nicht ausgeliehen oder der Ticket-Preis wurde nicht gesetzt!");
			return "redirect:/current-films/{year}/{week}";
		}

		// Prüfung, ob sich Events oder Vorführungen in dem Vorführungszeitraum und Kinosaal überlappen,
		// wenn ja Abbruch und Fehler
		if(!scheduledActivityService.isTimeSlotAvailable(
			addTime, addTime.plusMinutes(filmInst.getTimePlaying()), cinemaHallInst.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage",
				"In dem Kinosaal findet von Vorführungs-Beginn, bis Ende " +
					" bereits eine oder mehrere andere Vorführungen oder Events statt!");
			redirectAttributes.addFlashAttribute("infoMessage",
				"Bis 20 min vor und nach Veranstaltungen/Events können keine Vorführungen gebucht werden!");
			return "redirect:/current-films/{year}/{week}";
		}

		// Erstelle neue Vorführung
		redirectAttributes.addFlashAttribute("successMessage",
			"Neue Vorführung wurde erfolgreich erstellt!");
		CinemaShow cinemaShow = cinemaShowService.createCinemaShow(addTime, Money.of(9.99, EURO), filmInst, cinemaHallInst);

		return "redirect:/current-films/"
			+ cinemaShow.getStartDateTime().getYear() + "/"
			+ AdditionalDateTimeWorker.getWeekOfYear(cinemaShow.getStartDateTime());
	}

        /**
         * GET-Endpunkt: Veranstaltungsdetails
         * @param id Vorführungs-ID
         * @param m Modell
         * @return "cinema-show-detail"
         */
	@GetMapping("/cinema-shows/{id}")
	public String detailCinemaShow(@PathVariable Long id, Model m) {
		Optional<CinemaShow> optionalCinemaShow = cinemaShowRepository.findById(id);
		if(optionalCinemaShow.isEmpty()) {
			// TODO Fehlerbehandlung
			return "redirect:/current-films";
		}
		CinemaShow cinemaShow = optionalCinemaShow.get();

		m.addAttribute("cinemaShow", cinemaShow);
		m.addAttribute("allFilms", filmRepository.findAll());
                m.addAttribute("title", cinemaShow.getFilm().getTitle());
                
		return "cinema-show-detail";
	}

        /**
         * POST-Endpunkt: Veranstaltung ändern
         * @param redirectAttributes Redirect-Modell
         * @param id Vorführungs-ID
         * @param film Film
         * @param editTime neue Startzeit
         * @return redirect-View
         */
	@PreAuthorize("hasAnyRole('BOSS', 'AUTHORIZED_EMPLOYEE')")
	@PostMapping("/cinema-shows/{id}/edit")
	public String editCinemaShow(RedirectAttributes redirectAttributes,
								 @PathVariable Long id,
								 @RequestParam("film") Long film,
								 @RequestParam("editTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime editTime) {
		redirectAttributes.addFlashAttribute("editMode", true);
		Optional<Film> optFilmInst = filmRepository.findById(film);

		Optional<CinemaShow> optCinemaShow = cinemaShowRepository.findById(id);
		if(optCinemaShow.isEmpty()) {
			// Dieser Fehler sollte im Regelfall nicht auftreten!
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"Die aktuell angezeigte Vorführung konnte in der Datenbank nicht gefunden werden!");
			return "redirect:/cinema-shows/{id}";
		}

		CinemaShow cinemaShow = optCinemaShow.get();

		if(optFilmInst.isEmpty()) {
			// Dieser Fehler sollte im Regelfall nicht auftreten!
			redirectAttributes.addFlashAttribute("errorMessageEdit", "Der Film existiert nicht!");
			return "redirect:/cinema-shows/{id}";
		}

		Film filmInst = optFilmInst.get();

		if(cinemaShow.getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"Die Vorführung liegt in der Vergangenheit oder beginnt in einer Stunde und kann daher nicht mehr geändert werden!");
			return "redirect:/cinema-shows/{id}";
		}

		if(editTime.isBefore(LocalDateTime.now().plusHours(1))) {
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"Die Vorführung muss mind. 1 Stunde in der Zukunft liegen!");
			return "redirect:/cinema-shows/{id}";
		}

		if(cinemaShow.hasBoughtSeats()) {
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"Die Vorführung kann nicht mehr geändert werden, da bereits verkaufte Tickets existieren!");
			return "redirect:/cinema-shows/{id}";
		}

		if(!filmInst.isAvailableAt(editTime)) {
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"Dieser Film wurde in der Zeit nicht ausgeliehen oder der Ticket-Preis wurde nicht gesetzt!");
			return "redirect:/cinema-shows/{id}";
		}

		// Prüfung, ob sich Events oder Vorführungen in dem Vorführungszeitraum und Kinosaal überlappen,
		// wenn ja Abbruch und Fehler
		if(!scheduledActivityService.isTimeSlotAvailable(
			editTime, editTime.plusMinutes(filmInst.getTimePlaying()), cinemaShow.getCinemaHall().getId(), cinemaShow)) {
			redirectAttributes.addFlashAttribute("errorMessageEdit",
				"In dem Kinosaal findet von Vorführungs-Beginn, bis Ende " +
					" bereits eine oder mehrere andere Vorführungen oder Events statt!");
			redirectAttributes.addFlashAttribute("infoMessageEdit",
				"Bis 20 min vor und nach Veranstaltungen/Events können keine Vorführungen gebucht werden!");
			return "redirect:/cinema-shows/{id}";
		}

		redirectAttributes.addFlashAttribute("successMessageEdit",
			"Vorführung wurde erfolgreich geändert!");
		cinemaShowService.update(id)
			.setStartDateTime(editTime)
			.setFilm(optFilmInst.get())
			.save();

		return "redirect:/cinema-shows/{id}";
	}

        /**
         * POST-Endpunkt: Veranstaltung löschen
         * @param redirectAttributes Redirect-Moddell
         * @param id Vorführungs-ID
         * @return Redirect-View
         */
	@PreAuthorize("hasAnyRole('BOSS', 'AUTHORIZED_EMPLOYEE')")
	@PostMapping("/cinema-shows/{id}/delete")
	public String deleteCinemaShow(RedirectAttributes redirectAttributes, @PathVariable Long id) {
		redirectAttributes.addFlashAttribute("editMode", false);
		Optional<CinemaShow> optionalCinemaShow = cinemaShowRepository.findById(id);
		if(optionalCinemaShow.isEmpty()) {
			// TODO Fehlerbehandlung
			return "redirect:/current-films";
		}
		CinemaShow cinemaShow = optionalCinemaShow.get();

		if(cinemaShow.hasBoughtSeats()) {
			redirectAttributes.addFlashAttribute("errorMessageDelete",
				"Die Vorführung kann nicht mehr gelöscht werden, da bereits verkaufte Tickets existieren!");
			return "redirect:/cinema-shows/{id}";
		}

		if(cinemaShow.getStartDateTime().isBefore(LocalDateTime.now().plusHours(1))) {
			redirectAttributes.addFlashAttribute("errorMessageDelete",
				"Die Vorführung liegt in der Vergangenheit oder beginnt in einer Stunde und kann daher nicht mehr gelöscht werden!");
			return "redirect:/cinema-shows/{id}";
		}

		for(Reservation reservation : reservationRepository.findAllByCinemaShow(cinemaShow)) {
			deleteReservationController.deleteTickets(reservation);
			reservationRepository.delete(reservation);
		}

		String redirectUrl = "redirect:/current-films/"
			+ cinemaShow.getStartDateTime().getYear() + "/"
			+ AdditionalDateTimeWorker.getWeekOfYear(cinemaShow.getStartDateTime());

		cinemaShowService.deleteCinemaShow(optionalCinemaShow.get());

		return redirectUrl;
	}

        /**
         * Interne View-Klasse der Programmansicht zur Speicherung von Programm je Tag
         */
	public static class CinemaShowDayEntry {
		private Streamable<CinemaShow> cinemaShows;
		private String dayDateHeadline;

		/**
		 *Erstelle einen neuen Tageseintrag
		 * @param dayDate Datum, für alle Veranstaltungen welche an dem Tag laufen (Startzeit).
		 * @param cinemaShows alle Veranstaltungen an einem Tag
		 */
		public CinemaShowDayEntry(LocalDate dayDate, Streamable<CinemaShow> cinemaShows) {
			// Verhindert Fehler bei nicht vorhandenen Kino-Veranstaltungen an dem Tag
			if(cinemaShows == null) cinemaShows = Streamable.empty();
			this.cinemaShows = cinemaShows;
			this.dayDateHeadline = AdditionalDateTimeWorker.getDayFormat(dayDate);
		}

                /**
                 * Erhalte ein Streamable aller bekannten Einträge
                 * @return Streamable
                 */
		public Streamable<CinemaShow> getCinemaShows() {
			return this.cinemaShows;
		}

		/**
                 * Erhalte Datums-Überschrift-Eintrag
		 * @return Erhalte Datums-Überschrift-Eintrag
		 */
		public String getDayDateHeadline() {
			return this.dayDateHeadline;
		}
	}
}
