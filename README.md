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

## Endpoints <a name="endpoints"></a>

#### View landing Page
```
GET   	/paye-tax-calculator/
```

#### Redirect to first page of the form of quick-calculation Tax Code Page
```
GET   	/paye-tax-calculator/quick-calculation/
```

#### Change Language
```
GET      /paye-tax-calculator/language/:lang  
```
Currently the app supports English(:lang = english) and Welsh(:lang = welsh)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")