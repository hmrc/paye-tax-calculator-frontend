function getPeriodTab() {
  var getYearlyTabFromHtml = document.getElementById("tabContentannual").id;
  var getMonthlyTabFromHtml = document.getElementById("tabContentmonthly").id;
  var getWeeklyTabFromHtml = document.getElementById("tabContentweekly").id;

  var getYearlyTabAriaFromHtml = document.getElementById("tabContentannual").getAttribute('aria-hidden');
  var getMonthlyTabAriaFromHtml = document.getElementById("tabContentmonthly").getAttribute('aria-hidden');
  var getWeeklyTabAriaFromHtml = document.getElementById("tabContentweekly").getAttribute('aria-hidden');

  if (getYearlyTabAriaFromHtml === "false")
  {
    document.getElementById("getPeriodTab").setAttribute = ('value', getYearlyTabFromHtml);
    document.getElementById("getPeriodTab").innerHTML = "getPeriodFromHtml(" + getYearlyTabFromHtml +")";
  }
  else if (getMonthlyTabAriaFromHtml === "false")
  {
    document.getElementById("getPeriodTab").setAttribute = ('value', getMonthlyTabFromHtml);
    document.getElementById("getPeriodTab").innerHTML = "getPeriodFromHtml(" + getMonthlyTabFromHtml +")";
  }
  else if (getWeeklyTabAriaFromHtml === "false")
  {
      document.getElementById("getPeriodTab").setAttribute = ('value', getWeeklyTabFromHtml);
      document.getElementById("getPeriodTab").innerHTML = "getPeriodFromHtml(" + getWeeklyTabFromHtml +")";
  }
  return document.getElementById("getPeriodTab").innerHTML.toString ;
}