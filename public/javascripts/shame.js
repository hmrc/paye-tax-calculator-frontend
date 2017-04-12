function getPeriodTab() {
  var getYearlyTabFromHtml = document.getElementById("tab-content-annual").id;
  var getMonthlyTabFromHtml = document.getElementById("tab-content-monthly").id;
  var getWeeklyTabFromHtml = document.getElementById("tab-content-weekly").id;

  var getYearlyTabAriaFromHtml = document.getElementById("tab-content-annual").getAttribute('aria-hidden');
  var getMonthlyTabAriaFromHtml = document.getElementById("tab-content-monthly").getAttribute('aria-hidden');
  var getWeeklyTabAriaFromHtml = document.getElementById("tab-content-weekly").getAttribute('aria-hidden');

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