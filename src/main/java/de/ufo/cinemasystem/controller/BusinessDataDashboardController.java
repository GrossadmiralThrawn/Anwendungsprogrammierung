package de.ufo.cinemasystem.controller;

import de.ufo.cinemasystem.models.EmployeeEntry;
import de.ufo.cinemasystem.models.Film;
import de.ufo.cinemasystem.models.Orders;
import de.ufo.cinemasystem.repository.EmployeeRepository;
import de.ufo.cinemasystem.repository.FilmRepository;
import org.javamoney.moneta.Money;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


//benötigte Klassen: Bestellung, (Ausgaben für Personalkosten, Ausgaben für momentan geliehene Filme)
@Controller
public class BusinessDataDashboardController {

	@Autowired
	FilmRepository filmRepository;

	@Autowired
	EmployeeRepository employeeRepository;
	private final OrderManagement<Order> orderManagement;

    public BusinessDataDashboardController(OrderManagement<Order> orderManagement) {
		Assert.notNull(orderManagement, "Order darf nicht Null sein!");
        this.orderManagement = orderManagement;
    }


    //Anzeigen von Tageseinnahmen Diagramm, darunter Monatsumsatz Diagramm
	@GetMapping("/statistics")
	@PreAuthorize("hasRole('BOSS')")
	public String getDashboard(Model m) {

		List<FinancialTransaction> dailyIncomeData = List.of(
			new FinancialTransaction("Ticket: Bell im Märchenland", 250),
			new FinancialTransaction("Ticket: Parry Hotter und die Hochschule Merseburg", 200),
			new FinancialTransaction("Snacks: Popcorn", 75),
			new FinancialTransaction("Snacks: Softdrinks", 30),
			new FinancialTransaction("Ticket: Extra 4's Jannes Müller", 10)
		);
		m.addAttribute("dailyIncomeData", dailyIncomeData);


		List<RevenueData> revenueData = new ArrayList<>();
		for (LocalDate day = LocalDate.now().minusDays(30); !day.isAfter(LocalDate.now()); day = day.plusDays(1)) {

		// Ausgaben berechnen
		Money dailyExpenses = Money.of(0, "EUR");

		//Mitarbeiter Kosten
			for(EmployeeEntry employee : employeeRepository.findAll()){
				Money employeeCost = employee.getSalary().multiply( 7.0 / employee.getHoursPerWeek());
				dailyExpenses = dailyExpenses.add(employeeCost);

			}

			//FilmLeih Kosten
			for(Film film : filmRepository.findAll()){
				if(film.isRent(day.atStartOfDay())){
					Money filmRentCost = Money.of(film.getBasicRentFee() / 7.0,"EUR");
					dailyExpenses = dailyExpenses.add(filmRentCost);
				}
			}

			//Einnahmen
			Money dailyIncome = Money.of(0, "EUR");

			//Abhängigkeit Orders fehlt
			dailyIncome = dailyIncome.add(Money.of(1000, "EUR"));


			Money dailyRevenue = dailyIncome.subtract(dailyExpenses);

			revenueData.add(new RevenueData(dailyRevenue.getNumber().doubleValueExact() , day));
	}


		m.addAttribute("revenueData", revenueData);
                m.addAttribute("title", "Wirtschaftsstatistik");



		return "business-data-dashboard";
	}

	/**
	 * Repräsentiert eine finanzielle Transaktion im System
	 * und enthält Informationen über die Quelle der Transaktion und den Betrag.
	 * Der Betrag kann positiv für Einnahmen und negativ für Ausgaben sein.
	 */
	public static class FinancialTransaction {
		private String sourceOfTransaction;
		private double amount;

		/**
		 *
		 * @param sourceOfTransaction String, der die Quelle der Transaktion angibt.
		 * @param amount Der Betrag der Transaktion. (positiv für Einnahmen und negativ für Ausgaben)
		 */
		public FinancialTransaction(String sourceOfTransaction, double amount) {
			this.sourceOfTransaction = sourceOfTransaction;
			this.amount = amount;
		}

		/**
		 * Extrahiert die Finanztransaktionsdaten aus einer Order und transformiert sie
		 * in eine Liste von FinancialTransaction Objekten.
		 *
		 * @param orders Liste der Bestellungen, aus denen die Daten extrahiert werden.
		 * @return Eine Liste von FinancialTransaction Objekten.
		 */
		public List<FinancialTransaction> extractDataFromOrders(List<Orders> orders){
			//	TODO: Ticketeinnahmen und Snackeinnahmen aus Order in DailyIncomeData List transformieren
			return List.of(new FinancialTransaction("Popcorn", 100.0));
		}

		public String getSource() {
			return sourceOfTransaction;
		}

		public double getAmount() {
			return amount;
		}
	}

	/**
	 * Repräsentiert den  Umsatz des Systems an einem bestimmten Datum.
	 */
	public static class RevenueData {
		private double revenue;
		private LocalDate date;

		/**
		 *
		 * @param transactionsOnDate Liste der Finanztransaktionen an angegebenem Tag.
		 * @param date Das Datum des Umsatzes.
		 */
		public RevenueData(List<FinancialTransaction> transactionsOnDate, LocalDate date){
			this.revenue = 0;
			for(FinancialTransaction transaction : transactionsOnDate){
				this.revenue += transaction.getAmount();
			}
			this.date = date;
		}

		public RevenueData(double revenue, LocalDate date){
			this.revenue = revenue;
			this.date = date;
		}

		public double getRevenue() {
			return revenue;
		}

		public LocalDate getDate() {
			return date;
		}
	}
}



