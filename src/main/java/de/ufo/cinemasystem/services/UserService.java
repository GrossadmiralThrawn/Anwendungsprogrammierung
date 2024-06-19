package de.ufo.cinemasystem.services;


import de.ufo.cinemasystem.additionalfiles.RegistrationForm;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import de.ufo.cinemasystem.models.UserEntry;
import de.ufo.cinemasystem.repository.UserRepository;

import java.util.Arrays;
import java.util.List;


@Service
@Transactional
public class UserService {
	public static final  Role                  USER_ROLE = Role.of("USER"); //Im Original Customer
	private final        UserRepository        userRepository;
	private final        UserAccountManagement userAccounts;
	private static final List<String>          KNOWN_EMAIL_PROVIDERS = Arrays.asList(
											   "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "aol.com",
											   "icloud.com", "mail.com", "gmx.de", "yandex.com", "protonmail.com");


	UserService(UserRepository userRepository, UserAccountManagement userAccounts) {

		Assert.notNull(userRepository, "CustomerRepository must not be null!");
		Assert.notNull(userAccounts, "UserAccountManagement must not be null!");

		this.userRepository = userRepository;
		this.userAccounts   = userAccounts;
	}

	private boolean isKnownEmailProvider(String email) {
		if (email != null && email.contains("@") && !email.endsWith("@"))
		{
			if (KNOWN_EMAIL_PROVIDERS.stream().anyMatch(email::endsWith)) {
				return true;
			}

			String host = email.substring(email.lastIndexOf("@") + 1);

			try {
				java.net.InetAddress.getByName(host);
				return true;
			}
			catch (java.net.UnknownHostException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Creates a new {@link UserEntry} using the information given in the {@link RegistrationForm}.
	 *
	 * @param form must not be {@literal null}.
	 * @return the new {@link UserEntry} instance.
	 * return 0 for success 1 for existing mail and 2 for existing username
	 */
	public short createUser(RegistrationForm form) {

		Assert.notNull(form, "Registration form must not be null!");


		if (userRepository.findByeMail(form.getEMail()) != null || userRepository.findByUserAccountEmail(form.getEMail()) != null)
		{
			return 1;
		}
		if (userRepository.findByUserAccountUsername(form.getUsername()) != null) {
			return 2;
		}
		if (!isKnownEmailProvider(form.getEMail())) {
			return 3;  // Return a new code for unknown email providers
		}

		long postalCode= Long.parseLong(form.getPostalCode());

		if ((postalCode < 1067 && !form.getPostalCode().startsWith("0")) || postalCode > 99998)
		{
			return 4;
		}


		var password = Password.UnencryptedPassword.of(form.getPassword());
		var userAccount = userAccounts.create(form.getUsername(), password, USER_ROLE);


		userRepository.save(new UserEntry(userAccount, form.getFirstName(), form.getLastName(), form.getEMail(), form.getStreetName(), form.getStreetNumber(), form.getCity(), form.getPostalCode(), form.getState(), form.getCountry()));


		return 0;
	}

	public UserEntry createUser(UserAccount userAccount, String firstName, String lastName, String eMail, String streetName, String streetNumber, String city, String postalCode, String state, String country) {
		return userRepository.save(new UserEntry(userAccount, firstName, lastName, eMail,  streetName, streetNumber, city, postalCode, state, country));
	}


	/**
	 * Returns all {@link UserEntry}s currently available in the system.
	 *
	 * @return all {@link UserEntry} entities.
	 */
	public Streamable<UserEntry> findAll() {
		return userRepository.findAll();
	}

	public void deleteEmployee(UserEntry user) {
		UserAccount userAccount = user.getUserAccount();
		userRepository.delete(user);
		userAccounts.disable(userAccount.getId()); 		// before delete user account, disable it - if not springs crashes
		userAccounts.delete(userAccount);
	}

	public UserEntry getEmployeeById(UserEntry.UserIdentifier id) {
		return userRepository.findById(id).orElse(null);
	}

	public void removeAllRoles(UserAccount userAccount) {
		for (Role role : userAccount.getRoles()) {
			userAccount.remove(role);
		}
		userAccounts.save(userAccount);
	}
}