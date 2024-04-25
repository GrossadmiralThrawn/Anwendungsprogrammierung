package kickstart.models;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "CINEMA_HALLS")
public class CinemaHall {

	@Id @GeneratedValue
	private Long id;
	private String name;
	private int numberOfPlaces;
	@OneToMany(cascade = CascadeType.ALL)
	private final SortedSet<Seat> places;

	@OneToMany(cascade = CascadeType.ALL)
	private final SortedSet<CinemaShow> cinemaShows;

	public CinemaHall(String name, final Collection<Seat> places) {
		this.name = name;
		this.numberOfPlaces = places.size();
		this.places = new TreeSet<>(places);
		this.cinemaShows = new TreeSet<>();
	}

	public CinemaHall() {
		this.places = new TreeSet<>();
		this.cinemaShows = new TreeSet<>();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return this.name;
	}

	public int getNumberOfPlaces() {
		return this.numberOfPlaces;
	}

	public Iterable<Seat> getPlaces() {
		return this.places;
	}

	public Iterable<CinemaShow> getCinemaShows() {
		return this.cinemaShows;
	}

	void addCinemaShow(CinemaShow newCinemaShow) {
		if(cinemaShows.contains(newCinemaShow)) return;

		this.cinemaShows.add(newCinemaShow);
	}
}
