var beforePrint = function() {
  $("details").attr('open', '');
};
var afterPrint = function() {
  $("details").removeAttr('open');
};

// Webkit
if (window.matchMedia) {
  var mediaQueryList = window.matchMedia('print');
  mediaQueryList.addListener(function(mql) {
    if (mql.matches) {
      beforePrint();
    } else {
      afterPrint();
    }
  });
}

// IE, Firefox
window.onbeforeprint = beforePrint;
window.onafterprint = afterPrint;