// slightly modified https://github.com/hmrc/assets-frontend/blob/master/assets/javascripts/modules/toggle.js
// to be deleted once PR: https://github.com/hmrc/assets-frontend/pull/747/files is merged

function updateTag() {
  getPeriodTab();
  alert(document.getElementById("getPeriodTab").innerHTML);
}
function getPeriodTab() {
  var getYearlyTabFromHtml = document.getElementById("tabContentannual").id;
  var getMonthlyTabFromHtml = document.getElementById("tabContentmonthly").id;
  var getWeeklyTabFromHtml = document.getElementById("tabContentweekly").id;

  var getYearlyTabAriaFromHtml = document.getElementById("tabContentannual").getAttribute('aria-hidden');
  var getMonthlyTabAriaFromHtml = document.getElementById("tabContentmonthly").getAttribute('aria-hidden');
  var getWeeklyTabAriaFromHtml = document.getElementById("tabContentweekly").getAttribute('aria-hidden');

  if (getYearlyTabAriaFromHtml == "false")
  {
    document.getElementById("getPeriodTab").innerHTML = getYearlyTabFromHtml;
  }
  else if (getMonthlyTabAriaFromHtml == "false")
  {
    document.getElementById("getPeriodTab").innerHTML = getMonthlyTabFromHtml;
  }
  else if (getWeeklyTabAriaFromHtml == "false")
  {
      document.getElementById("getPeriodTab").innerHTML = getWeeklyTabFromHtml;
  }
  return document.getElementById("getPeriodTab").innerHTML;
}



