# paye-tax-calculator-frontend

[![Build Status](https://travis-ci.org/hmrc/paye-tax-calculator-frontend.svg)](https://travis-ci.org/hmrc/paye-tax-calculator-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/paye-tax-calculator-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/paye-tax-calculator-frontend/_latestVersion)


## Software Requirements
*   MongoDB 3.2 or later version
*   KEYSTORE and ASSETS_FRONTEND get the latest version via the service-manager

## Run the application locally

To run the application execute

```
sbt run
```
## Run Tests
- Run Unit Tests:  `sbt test`
- Run Integration Tests: `sbt it:test`
- Run Unit and Integration Tests: `sbt test it:test`
- Run Unit and Integration Tests with coverage report: `sbt clean compile coverage test it:test coverageReport dependencyUpdates`


## Endpoints <a name="endpoints"></a>

#### Get to the first form page by a web browser
```
GET /estimate-paye-take-home-pay/
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
