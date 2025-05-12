# Crypto Wallet

A Spring Boot application for managing cryptocurrency wallets, assets, and evaluating portfolio performance.  
It supports scheduled price updates, asset management, and provides RESTful endpoints for wallet and evaluation operations.

---

## Features

- **User Wallet Management:** Create wallets, add/remove assets, and view wallet info.
- **Asset Tracking:** Track quantity, purchase price, and current value of each asset.
- **Price Fetching:** Scheduled background updates of asset prices from the CoinCap API.
- **Portfolio Evaluation:** Calculate total value, best/worst performing assets, and historical performance.
- **REST API:** Exposes endpoints for wallet and evaluation operations.
- **Exception Handling:** Consistent API error responses.
- **Unit Tests:** Comprehensive test coverage for services, controllers, and utilities.

---

## Requirements

- Java 17+
- Maven 3.8+
- (Optional) Docker (for running H2 database in a container)

---

## Getting Started

### 1. **Clone the Repository**

```sh
git clone https://github.com/yourusername/crypto-wallet.git
cd crypto-wallet/crypto-wallet
```

### 2. **Build the Project**

```sh
mvn clean install
```

### 3. **Run the Application**

```sh
mvn spring-boot:run
```
or
```sh
java -jar target/crypto-wallet-*.jar
```

The application will start on [http://localhost:8080](http://localhost:8080).

---

## Configuration

The main configuration is in [`src/main/resources/application.properties`](src/main/resources/application.properties):

- H2 database is used by default (data persists in `./data/crypto-wallet-db`).
- Price updates are scheduled every 5 minutes (see `crypto.update.interval-cron`).
- Thread pool size for price fetching is configurable.

---

## API Endpoints

### **Wallet Endpoints**

- **Create Wallet**
  - `POST /api/wallet/create?email={email}`
  - **Body:** None
  - **Response:** `User` object

- **Add Asset**
  - `POST /api/wallet/addAsset?email={email}&symbol={symbol}&quantity={quantity}&purchasePrice={purchasePrice}`
  - **Body:** None
  - **Response:** `WalletAssetInfoDTO`

- **Get Wallet Info**
  - `GET /api/wallet/info?email={email}`
  - **Response:** `WalletInfoDTO`

### **Evaluation Endpoints**

- **Get Wallet Evaluation**
  - `GET /api/evaluation/evolution?email={email}&date={dd-MM-yyyy}`
  - **date** is optional; if omitted, uses today.
  - **Response:** `WalletEvaluationDTO`

---

## Example Usage

### **Create a Wallet**

```sh
curl -X POST "http://localhost:8080/api/wallet/create?email=user@example.com"
```

### **Add an Asset**

```sh
curl -X POST "http://localhost:8080/api/wallet/addAsset?email=user@example.com&symbol=BTC&quantity=1.5&purchasePrice=30000"
```

### **Get Wallet Info**

```sh
curl "http://localhost:8080/api/wallet/info?email=user@example.com"
```

### **Get Wallet Evaluation**

```sh
curl "http://localhost:8080/api/evaluation/evolution?email=user@example.com&date=12-05-2025"
```

---

## Testing

### **Run All Unit Tests**

```sh
mvn test
```

Test classes are located in `src/test/java/com/crypto_wallet/` and cover:
- Services (WalletServiceImpl, EvaluationServiceImpl, PriceFetchServiceImpl)
- Controllers (WalletController, EvaluationController)
- Utilities (CoinCapClient, DataInitializer)

---

## H2 Database Console

Access the H2 console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- JDBC URL: `jdbc:h2:file:./data/crypto-wallet-db`
- User: `sa`
- Password: (leave blank)

---

## Project Structure

```
src/
  main/
    java/com/crypto_wallet/
      controller/
      dto/
      entity/
      exception/
      repository/
      service/
      util/
    resources/
      application.properties
  test/
    java/com/crypto_wallet/
      (unit tests)
```

---

## License

This project is licensed under the MIT License.

---