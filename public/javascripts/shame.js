function getPeriodTab() {
  var getYearlyTabFromHtml = document.getElementById("tabContentannual").id;
  var getMonthlyTabFromHtml = document.getElementById("tabContentmonthly").id;
  var getWeeklyTabFromHtml = document.getElementById("tabContentweekly").id;

  var getYearlyTabAriaFromHtml = document.getElementById("tabContentannual").getAttribute('aria-hidden');
  var getMonthlyTabAriaFromHtml = document.getElementById("tabContentmonthly").getAttribute('aria-hidden');
  var getWeeklyTabAriaFromHtml = document.getElementById("tabContentweekly").getAttribute('aria-hidden');

  if (getYearlyTabAriaFromHtml === "false")
  {
    document.getElementById("user-tab").value = getYearlyTabFromHtml;
  }
  else if (getMonthlyTabAriaFromHtml === "false")
  {
    document.getElementById("user-tab").value = getMonthlyTabFromHtml;
  }
  else if (getWeeklyTabAriaFromHtml === "false")
  {
      document.getElementById("user-tab").value = getWeeklyTabFromHtml;
  }
  return document.getElementById("user-tab").value.toString ;
}