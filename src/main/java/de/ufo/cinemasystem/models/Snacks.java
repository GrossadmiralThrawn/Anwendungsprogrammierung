package de.ufo.cinemasystem.models;

import javax.money.MonetaryAmount;

import org.salespointframework.catalog.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "SNACKS")
public class Snacks extends Product {

    public static enum SnackType {
        Getränk,
        Essen
    }

    // private @EmbeddedId ProductIdentifier id =
    // ProductIdentifier.of(UUID.randomUUID().toString());
    // private Metric metric;
    private SnackType type;
    private int count;

    @SuppressWarnings({ "unused", "deprecation" })
    private Snacks() {
    }

    public Snacks(String name, MonetaryAmount price) {
        super(name, price);
        this.count = 0;
    }

    public Snacks(String name, MonetaryAmount price, int count, SnackType type) {
        super(name, price);
        this.type = type;
        this.count = count;
    }

    public String getSnackType() {
        return this.type.toString();
    }

    public int getCount() {
        return count;
    }

    public int addStock(int count) {
        this.count += count;
        return this.count;
        // ToDO Logging einrichten
    }

    public int removeStock(int count) {
        if (this.count >= count) {
            this.count -= count;
        }
        return this.count;
        // ToDO Logging einrichten
    }

}
