function setSalaryType() {

  var yearly = document.getElementById('salary-type-yearly');
  var monthly = document.getElementById('salary-type-monthly');
  var weekly = document.getElementById('salary-type-weekly');
  var daily = document.getElementById('salary-type-daily');
  var hourly = document.getElementById('salary-type-hourly');

  if (yearly.checked) {
    document.getElementById('amount-salary').name = "amount-yearly"
  }
  else if (monthly.checked) {
    document.getElementById("amount-salary").name = "amount-monthly"
  }
  else if (weekly.checked) {
    document.getElementById('amount-salary').name = "amount-weekly"
  }
  else if (daily.checked) {
    document.getElementById('amount-salary').name = "amount-daily"

  }
  else if (hourly.checked) {
    document.getElementById('amount-salary').name = "amount-hourly"
  }
}