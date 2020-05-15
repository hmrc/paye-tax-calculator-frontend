// GA Data to be pushed into GTM dataLayer managed by SES Team
window.dataLayer = window.dataLayer || [];

window.dataLayer.push({
    'Session ID': new Date().getTime() + '.' + Math.random().toString(36).substring(5),
    'Hit TimeStamp': new Date().toUTCString()

})
