# SplitWiseAssignment
Welcome to the Expense Sharing Application repository! This application allows users to track and share expenses among a group of participants. It provides features such as adding expenses, splitting expenses, viewing balances, and simplifying balances between users.

## Table of Contents

* Features
* Technologies Used
* Usage
* API Endpoints

## Features
* Add expenses with different types (EQUAL, EXACT, PERCENTAGE)
* Split expenses among participants
* View balances for each user
* Simplify balances between users
* Add participants to an expense
* Fetch transaction history for users

## Technologies Used

* Java
* Spring Boot
* Spring Data JPA
* Spring Web
* Hibernate
* MySQL (or any other supported database)
* Postman (for testing APIs)
* ModelMapper (for DTO mapping)
* Maven (for dependency management)

## Usage
* Use Postman or any other API testing tool to interact with the application's endpoints.
* Create users using the ***'/users/add'*** endpoint.
* Add expenses using the ***'/expenses/add'*** endpoint, specifying the type of expense and participants.
* View balances using the ***'/expenses/balances'*** endpoint.
* Simplify balances using the ***'/expenses/simplify'*** endpoint.

## API Endpoints

* **'/users/add'** - Add a new user
* **'/expenses/add'** - Add a new expense
* **'/expenses/balances'** - View balances for all users
* **'/expenses/simplify'** - Simplify balances between users
* **'/expenses/passbook/user/{userId}'** - Transaction detail for a user

