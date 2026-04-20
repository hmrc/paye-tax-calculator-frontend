window.addEventListener('hashchange', function() {
    var allowanceSpan = document.getElementById('js-personal-allowance');
    if (!allowanceSpan) return;
    var hash = window.location.hash.replace('#', '');
    if (!hash) return;
    var period = hash.toLowerCase().replace(/_/g, '-');
    var value = allowanceSpan.getAttribute('data-' + period);
    if (value) allowanceSpan.textContent = value;
});
