package de.ufo.cinemasystem.models;

import jakarta.persistence.*;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;

import java.util.*;
import java.util.stream.Stream;

// https://www.baeldung.com/hibernate-one-to-many
/**
 * Modellklasse eines Kinosaals
 * @author Yannick Harnisch
 */
@Entity
@Table(name = "CINEMA_HALLS")
public class CinemaHall {

	@Id @GeneratedValue
	private Long id;
	private String name;
	private int numberOfPlaces = 0;
	@ElementCollection(fetch = FetchType.EAGER)
	private final Map<Seat, Seat.PlaceGroup> seats = new TreeMap<>();

	@OneToMany(mappedBy = "cinemaHall", cascade =  CascadeType.ALL)
	private final SortedSet<CinemaShow> cinemaShows = new TreeSet<>();

	@OneToMany(mappedBy = "cinemaHall", cascade = CascadeType.ALL)
	private final SortedSet<Event> events = new TreeSet<>();

	/**
	 * Do not use!
	 * Use CinemaHallService to Update!
     * @param name Name
     * @param seats Sitzplätze
	 */
	public CinemaHall(String name, final Map<Seat, Seat.PlaceGroup> seats) {
		this.name = name;
		this.numberOfPlaces = 0;/*seats.size();*/
		this.seats.putAll(seats);
		//this.seats = new TreeMap<>(seats);
	}

        /**
         * Hibernate-Konstruktor. Bitte nicht benutzen, da die Instanzvariablen nicht gesetzt werden.
         */
	public CinemaHall() {}

        /**
         * Erhalte die ID dieses Kinosaals.
         * @return ID dieses Kinosaals.
         */
	public Long getId() {
		return id;
	}

        /**
         * Erhalte den Namen dieses Kinosaals
         * @return Namen dieses Kinosaals
         */
	public String getName() {
		return this.name;
	}

        /**
         * Erhalte die Anzahl an Sitzplätzen im Kinosaal.
         * @return Anzahl an Sitzplätzen
         */
	public int getNumberOfPlaces() { return this.numberOfPlaces; }

	/**
	 * @return alle Sitzplätze und die dazugehörigen Platzgruppen des Kinosaals
	 */
	public Streamable<Map.Entry<Seat, Seat.PlaceGroup>> getSeatsAndPlaceGroups() {
		return Streamable.of(this.seats.entrySet());
	}

	/**
	 * @param row Reihe des Platzes beginnend bei index 0
	 * @param pos Position des Platzes in jeder Reihe beginnend bei index 0, max. 99
	 * @return Sitz und Platzgruppe, wenn der Platz an der Stelle im Kinosaal vorhanden ist, sonst empty
	 */
	public Optional<Map.Entry<Seat, Seat.PlaceGroup>> getSeatAndPlaceGroup(int row, int pos) {
		Optional<Seat> optSeat = getSeat(row, pos);
		if(optSeat.isEmpty()) return Optional.empty();
		Seat seat = optSeat.get();

		return Optional.of(new AbstractMap.SimpleEntry<>(seat, seats.get(seat)));
	}

	/**
	 * @return alle Sitzplätze des Kinosaals
	 */
	public Streamable<Seat> getSeats() {
		return Streamable.of(this.seats.keySet());
	}

	/**
	 * @param row Reihe des Platzes beginnend bei index 0
	 * @param pos Position des Platzes in jeder Reihe beginnend bei index 0, max. 99
	 * @return Sitz, wenn der Platz an der Stelle im Kinosaal vorhanden ist, sonst empty
	 */
	public Optional<Seat> getSeat(int row, int pos) {
		return this.seats.keySet().stream().filter(s -> s.getRow() == row && s.getPosition() == pos).findAny();
	}

	/**
	 * @param row Reihe des Platzes beginnend bei index 0
	 * @param pos Position des Platzes in jeder Reihe beginnend bei index 0, max. 99
	 * @return Platzgruppe, wenn der Platz an der Stelle im Kinosaal vorhanden ist, sonst empty
	 */
	public Optional<Seat.PlaceGroup> getPlaceGroup(int row, int pos) {
		return getSeat(row, pos).map(seats::get);
	}

	/**
	 * @param seat Sitzplatz an einer Stelle
	 * @return Platzgruppe, wenn der Platz an der Stelle im Kinosaal vorhanden ist, sonst empty
	 */
	public Optional<Seat.PlaceGroup> getPlaceGroup(Seat seat) {
		return Optional.ofNullable(seats.get(seat));
	}

	/**
         * Erhalte eine Liste aller Vorführungen in diesem Saal.
         * 
         *  siehe: https://www.tabnine.com/code/java/methods/org.springframework.data.util.Streamable/of
         * @return Liste
         */
	public Streamable<CinemaShow> getCinemaShows() {
		return Streamable.of(this.cinemaShows);
	}

        /**
         * Erhalte eine Liste aller Events in diesem Saal.
         * 
         * @return Liste
         */
	public Streamable<Event> getEvents() {
		return Streamable.of(this.events);
	}


        /**
         * Füge eine neue Vorführung hinzu.
         * @param newCinemaShow die neue Vorführung
         */
	public void addCinemaShow(CinemaShow newCinemaShow) {
		if(cinemaShows.contains(newCinemaShow)) return;

		this.cinemaShows.add(newCinemaShow);
		newCinemaShow.setCinemaHall(this);
	}

        /**
         * Entferne eine Vorführung
         * @param cinemaShow zu entfernende Vorführung
         */
	public void removeCinemaShow(CinemaShow cinemaShow) {
		if(!cinemaShows.contains(cinemaShow)) return;

		this.cinemaShows.remove(cinemaShow);
		cinemaShow.setCinemaHall(null);
	}

        /**
         * Füge ein Event hinzu
         * @param newEvent hinzuzufügendes Event
         */
	public void addEvent(Event newEvent) {
		if(events.contains(newEvent)) return;

		this.events.add(newEvent);
		newEvent.setCinemaHall(this);
	}


	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName(), getNumberOfPlaces(),
			this.seats, this.cinemaShows/*, this.events*/);
	}

	@Override
	public boolean equals(Object object) {
		if(this == object) return true;

		if(!(object instanceof CinemaHall cinemaHall))
			return false;

		return Objects.equals(getId(), cinemaHall.getId()) &&
			Objects.equals(getName(), cinemaHall.getName()) &&
			Objects.equals(getNumberOfPlaces(), cinemaHall.getNumberOfPlaces()) &&
			Objects.equals(this.seats, cinemaHall.seats) &&
			Objects.equals(this.cinemaShows, cinemaHall.cinemaShows) /*&&
			Objects.equals(this.events, cinemaHall.events)*/;
	}
}
