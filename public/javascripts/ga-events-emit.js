$(function() {
// TODO conditional helpSend
// TODO welsh switch after the page was loaded
// FIXME remove the logging
    $("a.welshSwitch").on("mouseup", function () {
        ga("send", "event", "welsh-switch-clicked", this.getAttribute("href"));
    });

    $("#get-help-action").on("mouseup", function () {
        console.log(this.getAttribute("href"));
        ga("send", "event", "help-switch-clicked", window.location.pathname);
    });

    $("#report-submit").on("mouseup", function () {
        console.log(["send", "event", "help-send-clicked", window.location.pathname]);
        ga("send", "event", "help-send-clicked", window.location.pathname);
    });
});
