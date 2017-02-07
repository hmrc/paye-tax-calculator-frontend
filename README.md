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

#### View Index Page
```
GET   	/paye-tax-calculator/
```

#### Redirect to first page of the form aka Tax Code Page
```
GET   	/paye-tax-calculator/quick-calculation/
```
If user decides to manually change the url to the above.

#### View Tax Code Page
```
GET       /quick-calculation/tax-code
```


#### View Is Over 65 Page
```
GET       /quick-calculation/age
```

#### View Salary Page
```
GET       /quick-calculation/age
```

### The following endpoints CANNOT BE CALLED DIRECTLY. PLEASE DO NOT USE!

#### Submit Tax Code
```
POST      /quick-calculation/tax-code
```

#### Submit Is Over 65
```
POST      /quick-calculation/age
```

#### Submit Salary
```
POST      /quick-calculation/salary
```

#### View Results
```
GET       /quick-calculation/summary-result    
```
Can only be viewed when all questions from the pages mentioned are submitted.

#### Change Language
```
GET      /language/:lang  
```
Currently the app supports English and Welsh

#### Responses From Requests
| Responses    | Status    | Description |
| --------|---------|-------|
| Ok  | 200   | Successfully view page and along with any valid user input. |
| Bad Request | 400   |  Invalid Form Input |
| Redirect  | 303   |  Redirect to next page and along with any valid user input. |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")