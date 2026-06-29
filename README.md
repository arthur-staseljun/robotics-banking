# robotics-banking
Robotics Consulting Banking System

Part 1
Description
Create a REST API for bank account handling in Java, Spring Boot:
• Add money to account
• Debit money from account
• Get account balance
• Perform currency exchange (for simplicity with fixed exchange rates for different currencies)
• Retrieve transaction history for a given account Requirements
• A user can have multiple accounts with separate balances
• Every account has exactly one currency (potential options are EUR, USD, SEK, GBP, VND)
• Debiting orders can only use one currency (no automatic currency exchange)
• Before debiting, simulate a call to external system (i.e. external logging) by calling some web page (like https://httpstat.us/)
• Microservice (self-contained)
• Saved in an SQL DB (pick your own, H2 is ok)

Part 2
Description
Create a frontend single-page application (SPA) that uses the REST API written in Part 1 using Angular.
Requirements
• Home Page
    o Shows all accounts for the current user
    o Balance & currency for each account
    o Ability to click on any account to go to Account Overview Page
• Account Overview Page
    o Shows information about one specific account
    o Balance & currency for the specific account
    o Transaction history for the specific account
▪ By default, only a limited number of historical transactions are loaded
▪ More transactions are loaded dynamically as needed (without page reload; "infinite scroll")
▪ Ability to click on any transaction to go to Transaction Overview Page
    o A line chart diagram showing the time-series data of the historic account balance (time on the horizontal axis, balance on the vertical axis)
• Transaction Overview Page
    o Shows information about one specific transaction
    o Button to export & download transaction summary as a *.pdf report

Nice to have
Make use of NgRx state management.

Note
Don't focus on making the design pretty - make it functional.