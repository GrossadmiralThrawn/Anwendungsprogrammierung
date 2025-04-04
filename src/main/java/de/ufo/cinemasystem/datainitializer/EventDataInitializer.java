package de.ufo.cinemasystem.datainitializer;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import de.ufo.cinemasystem.models.CinemaHall;
import de.ufo.cinemasystem.models.Event;
import de.ufo.cinemasystem.repository.CinemaHallRepository;
import de.ufo.cinemasystem.repository.EventRepository;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DataInitialiser für Events
 * @author Tobias Knoll
 */
@Component
@Order(5)
public class EventDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(EventDataInitializer.class);

	private final EventRepository eventRepository;
	private final CinemaHallRepository cinemaHallRepository;

        /**
         * Erstelle einen neuen Initialiser mit den angegebenen Abhängigkeiten.
         * @param eventRepository Implementierung Event-Repository
         * @param cinemaHallRepository Implementierung CinemaHall-Repository
         */
	EventDataInitializer(EventRepository eventRepository, CinemaHallRepository cinemaHallRepository) {
		Assert.notNull(eventRepository, "EventRepository must not be null!");
		Assert.notNull(cinemaHallRepository, "cinemaHallRepository must not be null!");

		this.eventRepository = eventRepository;
		this.cinemaHallRepository = cinemaHallRepository;

	}

	@Override
	public void initialize() {

		if (eventRepository.findAll().iterator().hasNext()) {
			return;
		}

		LOG.info("Creating default event entries.");

		Event event1;
		Event event2;
		Event event3;
		List<CinemaHall> allCinemaHalls = cinemaHallRepository.findAll().toList();
		CinemaHall hall1 = allCinemaHalls.get(2);
		CinemaHall hall2 = allCinemaHalls.get(3);
		CinemaHall hall3 = allCinemaHalls.get(4);

		event1 = new Event("Jubiläumsfeier: 10 Jahre Zusammenarbeit",
			LocalDateTime.of(2024, 7, 20, 9, 0, 0),
			180);
		event2 = new Event("Firmenpräsentation: Neue Produkteinführung",
			LocalDateTime.of(2024,9,12,19,0,0),
			240);
		event3 = new Event("Filmabend mit Freunden",
			LocalDateTime.of(2024, 5, 10, 18, 0, 0),
			300);

		hall1.addEvent(event1);
		hall2.addEvent(event2);
		hall3.addEvent(event3);

		cinemaHallRepository.save(hall1);
		eventRepository.save(event1);
		cinemaHallRepository.save(hall2);
		eventRepository.save(event2);
		cinemaHallRepository.save(hall2);
		eventRepository.save(event3);


		eventRepository.findAll().forEach(e -> {
			System.out.println("ID: " + e.getId());
			System.out.println("Event: " + e.getName());
			System.out.println("Start: " + e.getStartDateTime().toString());
			System.out.println("Dauer: " + e.getDuration());
			System.out.println("Kinosaal: " + e.getCinemaHall().getName());
			System.out.println("=======================================");
		});

	}
}
