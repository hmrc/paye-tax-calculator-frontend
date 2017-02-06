# paye-tax-calculator-frontend

[![Build Status](https://travis-ci.org/hmrc/paye-tax-calculator-frontend.svg)](https://travis-ci.org/hmrc/paye-tax-calculator-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/paye-tax-calculator-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/paye-tax-calculator-frontend/_latestVersion)

This is a placeholder README.md for a new repository


## Software Requirements
*   MongoDB 3.2 or later version
*   KEYSTORE and ASSETS_FRONTEND get the latest version via the service-manager

## Run the application locally

To run the application execute
```
sbt run
```

## Endpoints <a name="endpoints"></a>

#### Get calculator selection index page
```
GET   	/paye-tax-calculator/
```

#### Get the quick-calculation first page
```
GET   	/paye-tax-calculator/quick-calculation/
```

#### All the other endpoints, please do not call them directly
All the other endpoints, such as 
```
GET       /quick-calculation/tax-code
POST      /quick-calculation/tax-code

GET       /quick-calculation/age
POST      /quick-calculation/age

GET       /quick-calculation/salary
POST      /quick-calculation/salary

GET       /quick-calculation/summary-result    
```

| Responses    | Status    | Description |
| --------|---------|-------|
| Ok  | 200   | Successfully into a responding page, when the user use app started from the index page or first page. |
| Bad Request | 400   |  When the user try to jump pages. |  
| Redirect  | 303   | To the responding Ok or Bad Request page, follow the Ok and Bad Request rule. |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")