$(function() {
    $("a.welshSwitch").on("mouseup", function () {
        ga("send", "event", "welsh-switch-clicked", this.getAttribute("href"));
    });

    $("#get-help-action").on("mouseup", function () {
        ga("send", "event", "help-switch-clicked", window.location.pathname);
    });

    $("#report-submit").on("mouseup", function () {
        ga("send", "event", "help-send-clicked", window.location.pathname);
    });
    //might be not needed if the page navigation covers this
    $("#print-calculation").on("mouseup", function () {
        ga("send", "event", "print-calculation-clicked", this.getAttribute("href"));
    });
});
