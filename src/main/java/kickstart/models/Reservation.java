/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kickstart.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single reservation in the system. Calling any of the methods of this class with a null argument will result in a NullPointerException.
 * @author Jannik
 */
@Entity
@Table(name= "RESERVATIONS")
public class Reservation {
    
    private @Id @GeneratedValue Long id;
    //private 
    @ManyToOne
    private @NotNull UserEntry reservingAccount;
    
    @ManyToOne
    private @NotNull CinemaShow cinemaShow;
    
    /**
     * TODO: linking
     */
    @OneToMany
    private List<DummyEntity> tickets = new ArrayList<>();

    /**
     * Hibernate-only constructor. Do not use, you will break things.
     */
    public Reservation() {
    }

    
    
    /**
     * Create a new reservation, initially containing 0 tickets.
     * @param reservingAccount
     * @param cinemaShow 
     */
    public Reservation(UserEntry reservingAccount, CinemaShow cinemaShow) {
        this.reservingAccount = Objects.requireNonNull(reservingAccount);
        this.cinemaShow = Objects.requireNonNull(cinemaShow);
    }

    
    /**
     * Get the id of this reservation
     * @return 
     */
    public Long getId() {
        return id;
    }

    public UserEntry getReservingAccount() {
        return reservingAccount;
    }

    public void setReservingAccount(UserEntry reservingAccount) {
        this.reservingAccount = Objects.requireNonNull(reservingAccount);
    }

    public CinemaShow getCinemaShow() {
        return cinemaShow;
    }

    public void setCinemaShow(CinemaShow cinemaShow) {
        this.cinemaShow = Objects.requireNonNull(cinemaShow);
    }

    /**
     * Allocates a new array containing all the tickets in this reservation. The returned array has exactly as many elements
     * as there are tickets in this reservation. Changes to the returned array will not affect this class or any other already-obtained return value from
     * this class, but modifying the individual tickets in the array will.
     * @return 
     */
    public DummyEntity[] getTickets(){
        return this.tickets.toArray(DummyEntity[]::new);
    }
    
    /**
     * Add a ticket to this reservation.
     * @param ticket 
     */
    public void addTicket(DummyEntity ticket){
        if(this.tickets.contains(ticket)){
            return;
        }
        this.tickets.add(ticket);
    }
    
    /**
     * Remove a ticket from this reservation.
     * @param ticket 
     */
    public void removeTicket(DummyEntity ticket){
        this.tickets.remove(ticket);
    }

    
    /**
     * Generate a hash code for this film. Due to the equals contract, hashcode is calculated from the id only.
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    /**
     * Checks wether {@code this} and the passed object are identical. 
     * Two reservations are considered identical when they have the same id.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Reservation other = (Reservation) obj;
        return Objects.equals(this.id, other.id);
    }
    
    
}
