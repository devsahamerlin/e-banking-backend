# e-banking-backend

# Frontend https://github.com/devsahamerlin/e-banking-frontend

### Inheritance Mapping Strategies:
- Single Table: Allow to have only 1 table in Database (`bank-account` table with `type` called `Discrimator column`)
- Table Per Class: Allow to have 2 tables (`current-account` table + `over-draft` property and `saving-account` table + `interest-rate` property)
- Joined Table: Allow to have 3 tables (`account` table with common properties, `current-account` table with `over-draft` property and `saving-account` with `interest-rate` property )

### Tech Stack:
- Java Spring Boot
- Angular
- Spring Security

### Database
![service-layer.png](images/service-layer.png)

### Swagger: http://localhost:8085/swagger-ui/index.html
```shell
mvn spring-boot:run
```
![swagger.png](images/swagger.png)

#### Get customer Banking Accounts

![customer-account.png](images/customer-account.png)

#### Credit Account
![credit-account.png](images/credit-account.png)

#### Transfer
![transfer.png](images/transfer.png)

#### Liste account transfert operations
- transfert Debit
![transfer-debit.png](images/transfer-debit.png)

- transfert Credit
![transfer-credit.png](images/transfer-credit.png)
