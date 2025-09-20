let deferredPrompt;

window.addEventListener('beforeinstallprompt', (e) => {
    deferredPrompt = e;

    deferredPrompt.prompt();
// Wait for the user to respond to the prompt
    deferredPrompt.userChoice
        .then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('User accepted the A2HS prompt');
            } else {
                console.log('User dismissed the A2HS prompt');
            }
            deferredPrompt = null;
        })
});