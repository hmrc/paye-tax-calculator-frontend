

var printLink = document.querySelector('.print-page');

if(printLink){
    printLink.addEventListener('click', function(e){
        window.print();
        return false;
    });
}


// functions to open details summary for print
function beforePrintCall(){
          if($('.no-details').length > 0){
              // store current focussed element to return focus to later
              var fe = document.activeElement;
              // store scroll position
              var scrollPos = window.pageYOffset;
              $('details').not('.open').each(function(){
                  $(this).addClass('print--open');
                  $(this).find('summary').trigger('click');
              });
              // blur focus off current element in case original cannot take focus back
              $(document.activeElement).blur();
              // return focus if possible
              $(fe).focus();
              // return to scroll pos
              window.scrollTo(0,scrollPos);
          } else {
              $('details').attr("open","open").addClass('print--open');
          }
          $('details.print--open').find('summary').addClass('heading-medium');
      }




  function afterPrintCall(){
          $('details.print--open').find('summary').removeClass('heading-medium');
          if($('.no-details').length > 0){
              // store current focussed element to return focus to later
              var fe = document.activeElement;
              // store scroll position
              var scrollPos = window.pageYOffset;
              $('details.print--open').each(function(){
                  $(this).removeClass('print--open');
                  $(this).find('summary').trigger('click');
              });
              // blur focus off current element in case original cannot take focus back
              $(document.activeElement).blur();
              // return focus if possible
              $(fe).focus();
              // return to scroll pos
              window.scrollTo(0,scrollPos);
          } else {
              $('details.print--open').removeAttr("open").removeClass('print--open');
          }
      }


 //Chrome
      if(typeof window.matchMedia != 'undefined'){
          mediaQueryList = window.matchMedia('print');
          mediaQueryList.addListener(function(mql) {
              if (mql.matches) {
                  beforePrintCall();
              };
              if (!mql.matches) {
                  afterPrintCall();
              };
          });
      }


//Firefox and IE (above does not work)
      window.onbeforeprint = function(){
          beforePrintCall();
      }
      window.onafterprint = function(){
          afterPrintCall();
      }
