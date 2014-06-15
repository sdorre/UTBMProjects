package com.bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * interface with the Database. It gives the a Connection with the database,
 * with getting a driver for PostGreSQL and connecting with informations : user
 * : lo53 password : lo53
 */

public class BDDInterface {

	private Connection connection = null;
	private Statement statement = null;

	public BDDInterface() {
		try {
			System.out.println("BDD - Chargement du driver....");
			Class.forName("org.postgresql.Driver");
			System.out.println("BDD - Driver Chargé !");
		} catch (ClassNotFoundException e) {
			System.out
					.println("BDD - Erreur lors du chargement : le driver n'a été trouvé! <br/>"
							+ e.getMessage());
		}

		String url = "jdbc:postgresql://localhost:5432/lo53";
		String utilisateur = "lo53";
		String password = "lo53";

		try {
			System.out.println("BDD - Connexion à la BD");
			connection = DriverManager
					.getConnection(url, utilisateur, password);
			System.out.println("BDD - Connexion reussie");

			statement = connection.createStatement();
			System.out.println("BDD - Objet requète créé");
		} catch (SQLException e) {
			System.out.println("BDD - Erreur lors de la connexion ; "
					+ e.toString());
		}
	}

	public Statement getStatement() {
		return statement;
	}

	public Connection getConnection() {
		return connection;
	}

	/**
	 * function to close correctly all objects opened.
	 */
	public void closeDataBase() {

		if (statement != null) {
			try {
				System.out.println("BDD - Fermeture de l'objet Statement.");
				statement.close();
			} catch (SQLException ignore) {
			}
		}

		if (connection != null) {
			try {
				System.out.println("BDD - Fermeture de l'objet Connection.");
				connection.close();
			} catch (SQLException ignore) {
			}
		}
	}

}
